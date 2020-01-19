package de.wfb.rail.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.facade.RoutingFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.DrivingService;
import de.wfb.rail.events.BlockEnteredEvent;
import de.wfb.rail.events.BlockExitedEvent;
import de.wfb.rail.events.FeedbackBlockEvent;
import de.wfb.rail.events.RouteAddedEvent;
import de.wfb.rail.events.RouteFinishedEvent;

public class DefaultDrivingService implements DrivingService, ApplicationListener<ApplicationEvent> {

	private static final double DRIVING_SPEED = 50.0d;

	private static final Logger logger = LogManager.getLogger(DefaultDrivingService.class);

	@Autowired
	private RoutingFacade routingFacade;

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private BlockService blockService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		if (event instanceof RouteAddedEvent) {

			processRouteAddedEvent((RouteAddedEvent) event);

		} else if (event instanceof BlockEnteredEvent) {

			processBlockEnteredEvent((BlockEnteredEvent) event);

		} else if (event instanceof BlockExitedEvent) {

			processBlockExitedEvent((BlockExitedEvent) event);

		} else if (event instanceof RouteFinishedEvent) {

			processRouteFinishedEvent((RouteFinishedEvent) event);

		} else if (event instanceof FeedbackBlockEvent) {

			processFeedbackBlockEvent((FeedbackBlockEvent) event);

		}
	}

	/**
	 * After a XEvtSen command was executed, system learns about the states of
	 * blocks from the response of the command.
	 *
	 * For every block signaled as free or blocked, the system will publish a
	 * separate FeedbackBlockEvent.
	 *
	 * This method will react to the events. When a block is free or used, it will
	 * determine the locomotive that is on the block using a heuristic:
	 *
	 * The heuristic is: A block is reserved by at most one route. The route belongs
	 * to at most one locomotive. So when a block is used or freed the corresponding
	 * locomotive must be the locomotive that owns the route.
	 *
	 * If someone manually moves a locomotive onto a block, this heuristic does not
	 * hold any more and the software fails. That is why manual and automatic
	 * operation do not go together well.
	 */
	private void processFeedbackBlockEvent(final FeedbackBlockEvent event) {

		logger.trace("processFeedbackBlockEvent() BlockNumber: " + event.getFeedbackBlockNumber() + " BlockState: "
				+ event.getFeedbackBlockState());

		switch (event.getFeedbackBlockState()) {

		case BLOCKED:
			processFeedbackBlockBlocked(event);
			break;

		case FREE:
			processFeedbackBlockFree(event);
			break;

		default:
			break;
		}
	}

	private void processFeedbackBlockFree(final FeedbackBlockEvent event) {

		logger.info("processFeedbackBlockFree()");

		final int feedbackBlockNumber = event.getFeedbackBlockNumber();

		logger.trace("feedbackBlockNumber: " + feedbackBlockNumber);

		final Block block = blockService.getBlockById(feedbackBlockNumber + 1);
		if (block == null) {
			return;
		}

		logger.trace("block: " + block);

		final Route route = routingFacade.getRouteByBlock(block);
		if (route == null) {
			return;
		}

		logger.trace("route: " + route);

		final DefaultLocomotive locomotive = route.getLocomotive();
		if (locomotive == null) {
			return;
		}

		logger.trace("locomotive: " + locomotive);

		// It will determine which locomotive has exited that block. Because
		// only a single locomotive can reserve a route to this block.
		//
		// based on that knowledge it will send the BlockExitedEvent
		final BlockExitedEvent blockExitedEvent = new BlockExitedEvent(this, block, locomotive);
		applicationEventPublisher.publishEvent(blockExitedEvent);
	}

	/**
	 * determines which locomotive has entered that block. Because only a single
	 * locomotive can reserve a route to this block.
	 *
	 * based on that knowledge it will send the BlockEnteredEvent
	 *
	 * @param event
	 */
	private void processFeedbackBlockBlocked(final FeedbackBlockEvent event) {

		final int feedbackBlockNumber = event.getFeedbackBlockNumber() + 1;

		logger.info("processFeedbackBlockBlocked() feedbackBlockNumber: " + feedbackBlockNumber);

		// get block
		final Block block = blockService.getBlockById(feedbackBlockNumber);
		if (block == null) {

			logger.info("No Block! feedbackBlockNumber: " + feedbackBlockNumber);
			return;
		}

		// get route that currently owns the block
		final Route route = routingFacade.getRouteByBlock(block);
		if (route == null) {

			logger.info("No Route! feedbackBlockNumber: " + feedbackBlockNumber);
			return;
		}

		// get locomotive that executes the route
		final DefaultLocomotive locomotive = route.getLocomotive();
		if (locomotive == null) {
			logger.info("locomotive is null");
		}

		// send the BlockEnteredEvent
		final BlockEnteredEvent blockEnteredEvent = new BlockEnteredEvent(this, block, locomotive);
		applicationEventPublisher.publishEvent(blockEnteredEvent);
	}

	private void processRouteFinishedEvent(final RouteFinishedEvent event) {

		logger.info("processRouteFinishedEvent()");

		final DefaultLocomotive locomotive = event.getDefaultLocomotive();

		logger.info("Removing Route " + locomotive.getRoute());

		final Route route = locomotive.getRoute();
		route.setLocomotive(null);
		locomotive.setRoute(null);

		final RailNode railNode = locomotive.getRailNode();
		final GraphNode graphNode = locomotive.getGraphNode();

		logger.info("The locomotive is now on the RailNode ID: " + railNode.getId() + " and on GraphNode ID: "
				+ graphNode.getId() + ". Its orientation is: " + locomotive.getOrientation().name());

		// removing graphNode
		logger.info("Removing graphNode from locomotive!");
		locomotive.setGraphNode(null);

		locomotiveStop(locomotive);
	}

	/**
	 * Internal Block Exited Event. (Not caused by P50X XEvtSen command).
	 *
	 * After a XEvtSen returns, the system will publish FeedbackBlockEventS. Also
	 * the simulator (TimedDrivingThread) will publish fake FeedbackBlockEventS so
	 * that the system can be tested without sending P50X XEvtSen commands.
	 *
	 * Those FeedbackBlockEventS are handled by the DefaultDrivingService (this
	 * class). It will determine which locomotive did cause the FeedbackBlockEvent
	 * and it will publish a BlockExitedEvent.
	 *
	 * The BlockExitedEvent is handled by the DefaultDrivingService (this class) It
	 * will compute new routes.
	 *
	 * @param event
	 */
	private void processBlockExitedEvent(final BlockExitedEvent event) {

		logger.info("processBlockExitedEvent()");

		// the route subsection is freed when the train arrives at the next block
		// (processBlockEnteredEvent)
		// freeRouteExceptUpToBlock(event.getBlock(), event.getLocomotive());

//		logger.info(event.getLocomotive().getRoute());

//		// tell all other locomotives to recompute their routes
//		for (final DefaultLocomotive locomotive : modelFacade.getLocomotives()) {
//
//			if (locomotive == event.getLocomotive()) {
//				continue;
//			}
//
//			if (reserveUpToIncludingNextBlock(locomotive)) {
//				locomotiveGo(locomotive);
//			}
//		}
	}

	private void processBlockEnteredEvent(final BlockEnteredEvent event) {

		logger.info("processBlockEnteredEvent()");

		final DefaultLocomotive locomotive = event.getLocomotive();
		final Block enteredBlock = event.getBlock();
		final Route route = locomotive.getRoute();

		logger.info("Locomotive: " + locomotive + " EnteredBlock: " + enteredBlock + " Route: " + route);

		if (locomotive != null && enteredBlock != null) {

			final RailNode blockRailNode = enteredBlock.getNodes().get(0);

			// put the locomotive onto that block
			logger.info("Putting locomotive onto RailNode: " + blockRailNode);
			locomotive.setRailNode(blockRailNode);

			if (route != null) {

				// if route did finish, stop the locomotive
				if (route.endsWith(enteredBlock)) {

					logger.info("Locomotive is on the last block of it's route!");

					// free the last section
					freeRouteExceptUpToBlock(enteredBlock, locomotive);

					final RouteFinishedEvent routeFinishedEvent = new RouteFinishedEvent(this, route, locomotive);
					applicationEventPublisher.publishEvent(routeFinishedEvent);

					return;
				}
			}
		}

		// free the last section
		freeRouteExceptUpToBlock(enteredBlock, locomotive);

		// try to reserve the next section
		final boolean nextSectionIsReserved = reserveUpToIncludingNextBlock(locomotive);
		if (nextSectionIsReserved) {

			logger.info("Could reserve up to next block");

		} else {

			logger.info("Could NOT reserve up to next block");

		}
	}

	private void processRouteAddedEvent(final RouteAddedEvent event) {

		logger.info("processRouteAddedEvent()");

		final Route route = event.getRoute();
		final DefaultLocomotive locomotive = event.getDefaultLocomotive();

		logger.info(route);

		if (!reserveUpToIncludingNextBlock(locomotive)) {

			logger.info("Could not reserve up to next block");

			locomotiveStop(locomotive);

			return;
		}

		logger.info(route);

		locomotiveGo(locomotive);
	}

	private void locomotiveGo(final DefaultLocomotive locomotive) {

		logger.info(">>>>>>>>>> GO Locomotive GO! locomotive ID: " + locomotive.getId());

		final Direction locomotiveOrientation = locomotive.getOrientation();
		final GraphNode graphNode = locomotive.getGraphNode();

		if (graphNode == null) {

			logger.info("no graph node! Cannot make locomotive go!");
			return;
		}

		final Direction graphNodeExitDirection = graphNode.getExitDirection();

		final boolean forward = locomotiveOrientation == graphNodeExitDirection;
		final short address = locomotive.getAddress();

		logger.info("Locomotive GO forward: " + forward);
		logger.info("Locomotive GO Address: " + address);

		locomotive.setDirection(forward);
		locomotive.start(DRIVING_SPEED);
	}

	private void locomotiveStop(final DefaultLocomotive locomotive) {

		logger.info("<<<<<<<<<<< STOP Locomotive STOP!");

		final short address = locomotive.getAddress();

		logger.info("Locomotive STOP Address: " + address);

		locomotive.stop();
	}

	/**
	 * Free all nodes on the route up to block
	 *
	 * @param block      free all nodes up to this block
	 * @param locomotive
	 */
	private void freeRouteExceptUpToBlock(final Block block, final DefaultLocomotive locomotive) {

		logger.info("freeRouteExceptUpToBlock()");

//		final Block nextBlock = findNextBlock(locomotive);
//
//		logger.info("freeRouteExceptBlock() block = " + block + " nextBlock = " + nextBlock);
//
//		if (block == null || nextBlock == null) {
//
//			logger.info("oldBlock or nextBlock is null. Aborting! block = " + block + " nextBlock = " + nextBlock);
//			return;
//		}

		// go through the entire route.
		// If a RailNode and/or a Block is reserved for this locomotive, free it
		for (final GraphNode graphNode : locomotive.getRoute().getGraphNodes()) {

			final RailNode railNode = graphNode.getRailNode();

			logger.trace("Freeing GraphNode: " + graphNode + " RailNode: ID: " + railNode.getId() + " Reserved: "
					+ railNode.isReserved() + " ReservedLocomotiveID: " + railNode.getReservedLocomotiveId());

			final Block nodeBlock = railNode.getBlock();

			// arrived at the block, stop the for loop because the relevant subset of the
			// route was processed
			if (nodeBlock != null && nodeBlock.equals(block)) {

				// free nodeBlock

				logger.info("Done!");

			} else {

				if (railNode.isReserved()) {

					if (railNode.getReservedLocomotiveId() == locomotive.getId()) {

						// either free a single node or if the node is part of a block, free the entire
						// block
						final Block railNodeBlock = railNode.getBlock();

						logger.info("RailNode ID: " + railNode.getId() + " Block: " + railNodeBlock);

						if (railNodeBlock == null) {

							logger.info("FREE RAILNODE ID: " + railNode.getId() + " ReservedLocomotiveID: "
									+ railNode.getReservedLocomotiveId());

							railNode.setReserved(false);
							railNode.setReservedLocomotiveId(-1);

						} else {

							logger.info("block.getNodes().size: " + railNodeBlock.getNodes().size());

							railNodeBlock.getNodes().stream().filter(node -> node.isReserved()).forEach(node -> {

								logger.info("Resetting block!");

								node.setReserved(false);
								node.setReservedLocomotiveId(-1);
							});

						}
					}
				}
			}
		}

		// tell all other locomotives to recompute their routes
		for (final DefaultLocomotive tempLocomotive : modelFacade.getLocomotives()) {

			// skip the current locomotive
			if (tempLocomotive == locomotive) {
				continue;
			}

			// make this locomotive reserve it's path and start it
			if (reserveUpToIncludingNextBlock(tempLocomotive)) {

				locomotiveGo(tempLocomotive);
			}
		}
	}

	private boolean reserveUpToIncludingNextBlock(final DefaultLocomotive locomotive) {

		if (locomotive.getRoute() == null) {
			return false;
		}

		// find the next block on the route
		final Block nextBlock = findNextBlock(locomotive);

		if (nextBlock == null) {

			logger.info("No next node!");
			return false;
		}

		logger.info("Next Block: " + nextBlock);

		// check if all nodes from the current block to and including the next block are
		// free
		if (nextBlock.isReserved()) {
			return false;
		}

		for (final GraphNode graphNode : locomotive.getRoute().getSubList(locomotive.getRailNode())) {

			final Block nodeBlock = graphNode.getRailNode().getBlock();

			// walk until the next block is reached
			if (nodeBlock != null && nodeBlock.equals(nextBlock)) {
				break;
			}

			// is this rail node reserved for another locomotive
			if (graphNode.getRailNode().isReservedExcluding(locomotive.getId())) {
				return false;
			}

			// reserve this node for the locomotive
			graphNode.getRailNode().setReserved(true);
			graphNode.getRailNode().setReservedLocomotiveId(locomotive.getId());
		}

		// reserve the nextBlock for the locomotive
		nextBlock.reserveForLocomotive(locomotive);

		return true;
	}

	private Block findCurrentBlock(final Route route, final DefaultLocomotive locomotive) {

		if (locomotive.getRailNode() == null) {
			return null;
		}

		return locomotive.getRailNode().getBlock();
	}

	private Block findNextBlock(final DefaultLocomotive locomotive) {

		logger.info("Find next Block for locomotive ID: " + locomotive.getId());

		final Route route = locomotive.getRoute();
		final Block currentBlock = findCurrentBlock(route, locomotive);

		logger.info("currentBlock = " + currentBlock);

		if (locomotive.getRailNode() == null) {
			return null;
		}

		// start from the RailNode the locomotive currently is on
		for (final GraphNode graphNode : route.getSubList(locomotive.getRailNode())) {

			final Block nodeBlock = graphNode.getRailNode().getBlock();

			// if the RailNode has a block, and the block is not the current block
			// the next block was found
			if (nodeBlock != null && (currentBlock == null || nodeBlock.getId() != currentBlock.getId())) {

				return nodeBlock;
			}
		}

		return null;
	}

}
