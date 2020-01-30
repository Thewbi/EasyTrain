package de.wfb.rail.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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
import de.wfb.rail.facade.ProtocolFacade;

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

	@Autowired
	private ProtocolFacade protocolFacade;

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
	 * blocks from the response of the command.<br />
	 * <br />
	 *
	 * For every block signaled as free or blocked, the system will publish a
	 * separate FeedbackBlockEvent.<br />
	 * <br />
	 *
	 * This method will react to the events. When a block is free or used, it will
	 * determine the locomotive that is on the block using a heuristic:<br />
	 * <br />
	 *
	 * The heuristic is: A block is reserved by at most one route. The route belongs
	 * to at most one locomotive. So when a block is used or freed the corresponding
	 * locomotive must be the locomotive that owns the route.<br />
	 * <br />
	 *
	 * If someone manually moves a locomotive onto a block, this heuristic does not
	 * hold any more and the software fails. That is why manual and automatic
	 * operation do not go together well.<br />
	 * <br />
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

	/**
	 * Handler for when a block is freed.
	 *
	 * Converts a FeedbackBlockEvent into a BlockExitedEvent.
	 *
	 * @param event the event which has a feedback block state of type free.
	 */
	private void processFeedbackBlockFree(final FeedbackBlockEvent event) {

		logger.trace("processFeedbackBlockFree()");

		// determine the block
		final int feedbackBlockNumber = event.getFeedbackBlockNumber();
		logger.trace("feedbackBlockNumber: " + feedbackBlockNumber);
		final Block block = blockService.getBlockById(feedbackBlockNumber);
		if (block == null) {
			return;
		}

		// determine the route
		logger.trace("block: " + block);
		final Route route = routingFacade.getRouteByBlock(block);
		if (route == null) {
			return;
		}
		logger.trace("route: " + route);

		// determine the locomotive
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
	 * Determines which locomotive has entered that block. Because only a single
	 * locomotive can reserve a route to this block.
	 *
	 * based on that knowledge it will send the BlockEnteredEvent
	 *
	 * @param event
	 */
	private void processFeedbackBlockBlocked(final FeedbackBlockEvent event) {

		final int feedbackBlockNumber = event.getFeedbackBlockNumber();

		logger.trace("processFeedbackBlockBlocked() feedbackBlockNumber: " + feedbackBlockNumber);

		// get block
		final Block block = blockService.getBlockById(feedbackBlockNumber);
		if (block == null) {

			logger.info("No Block! feedbackBlockNumber: " + feedbackBlockNumber);
			return;
		}

		// get route that currently owns the block
		final Route route = routingFacade.getRouteByBlock(block);
		if (route == null) {

			logger.trace("No Route! feedbackBlockNumber: " + feedbackBlockNumber);
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

		// nop
		logger.trace("processBlockExitedEvent()");
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
		final Block nextBlock = reserveUpToIncludingNextBlock(locomotive);
		final boolean nextSectionIsReserved = (nextBlock != null);
		if (nextSectionIsReserved) {

			logger.info("Could reserve up to next block");

			// Slow down the locomotive to 20% of is driving speed when the locomotive is
			// approaching the last block of it's route.
			//
			// This is important because some blocks on the real layout are designed very,
			// very short.
			//
			// If decoder speed curves are very flat, the locomotive will take some time to
			// actually come to a halt. If the block is short and the locomotive
			// takes a long time to stop, the locomotive exits the block before it even
			// stops.
			//
			// This is not a problem per se because UNLESS THE LOCOMOTIVE RUNS OVER A
			// TURNOUT, the current block still remains reserved for the locomotive and the
			// system still correctly keeps track of the locomotive. It cannot be anywhere
			// else but in close vincinity to the block because there was no turnout nearby.
			//
			// The real problem occurs if THERE IS A TURNOUT directly after the LAST BLOCK
			// OF A ROUTE! The locomotive might run past that turnout and stop. Now the
			// system has lost track of where the locomotive actually is! It has lost track
			// because it does not take the slow down into account. For the system, the
			// locomotive now is located on the block still and has not run past a switch!
			// The routing algorithm will start computing routes from the last block of the
			// last route and not from the actual location of the locomotive on the real
			// layout which makes a difference in this situation!
			//
			// If the user starts a new route in that inconsistent situation, the locomotive
			// is already passed a turnout.
			// If the new route needs that exact turnout to change direction (thrown or
			// closed) then the locomotive is literaly on the wrong track (No pun intended!)
			// The system has no way of telling what happened, it assumes the routing is
			// correct ant it will make the locomotive go and wait for it to arrive
			// at the next block which might never happen because the locomotive run over
			// a incorrectly setup turnout.
			//
			// By slowing down the locomotive, the hopes are that the locomotive stops
			// on the last block of a route before going over the next turnout. That way,
			// the system assumes a location that actually matches the real situation.
			// Routing still works correctly.
			if (route.endsWith(nextBlock)) {

				locomotiveGo(locomotive, DRIVING_SPEED / 100.0d * 20.0d);
			}

		} else {

			logger.info("Could NOT reserve up to next block");

		}
	}

	private void processRouteAddedEvent(final RouteAddedEvent event) {

		logger.info("processRouteAddedEvent()");

		final Route route = event.getRoute();
		final DefaultLocomotive locomotive = event.getDefaultLocomotive();

		logger.info(route);

		final Block nextBlock = reserveUpToIncludingNextBlock(locomotive);
		if (nextBlock == null) {

			logger.info("Could not reserve up to next block");

			locomotiveStop(locomotive);

			return;
		}

		logger.info(route);

		locomotiveGo(locomotive, DRIVING_SPEED);
	}

	private void locomotiveGo(final DefaultLocomotive locomotive, final double speed) {

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
		locomotive.start(speed);
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

		// DEBUG
		logger.info("freeRouteExceptUpToBlock() blockID: " + block.getId());

		final Route route = locomotive.getRoute();

		final List<GraphNode> deleteList = new ArrayList<>();

		// go through the entire route.
		// If a RailNode and/or a Block is reserved for this locomotive, free it
		for (final GraphNode graphNode : route.getGraphNodes()) {

			final RailNode railNode = graphNode.getRailNode();

			// DEBUG
			logger.trace("Freeing GraphNode: " + graphNode + " RailNode: ID: " + railNode.getId() + " Reserved: "
					+ railNode.isReserved() + " ReservedLocomotiveID: " + railNode.getReservedLocomotiveId());

			final Block nodeBlock = railNode.getBlock();

			// arrived at the block, stop the for loop because the relevant subset of the
			// route was processed
			if (nodeBlock != null && nodeBlock.equals(block)) {

				// remove the highlight
				nodeBlock.getNodes().stream().filter(node -> node.isReserved()).forEach(node -> {

					railNode.setHighlighted(false);

					// send model changed event
					modelFacade.sendModelChangedEvent(railNode);

				});

				logger.trace("Done!");

				break;

			} else {

				// remove this node from the route
				deleteList.add(graphNode);

				if (railNode.isReserved()) {

					if (railNode.getReservedLocomotiveId() == locomotive.getId()) {

						// either free a single node or if the node is part of a block, free the entire
						// block
						final Block railNodeBlock = railNode.getBlock();

						logger.trace("RailNode ID: " + railNode.getId() + " Block: " + railNodeBlock);

						// if the node is not part of a block, only free the node
						if (railNodeBlock == null) {

							logger.trace("FREE RAILNODE ID: " + railNode.getId() + " ReservedLocomotiveID: "
									+ railNode.getReservedLocomotiveId());

							freeNode(railNode);

						} else {

							// if the node is attached to a block, free the entire block

							logger.trace("block.getNodes().size: " + railNodeBlock.getNodes().size());

							railNodeBlock.getNodes().stream().filter(node -> node.isReserved()).forEach(node -> {

								logger.trace("Resetting node ID = " + node.getId() + " in block!");

								freeNode(node);
							});

						}
					}
				}
			}
		}

		// from the start of the route remove all nodes that the train did pass by
		if (CollectionUtils.isNotEmpty(deleteList)) {

			for (final GraphNode graphNode : deleteList) {

				final GraphNode firstGraphNode = route.getGraphNodes().get(0);

				if (firstGraphNode.getId() != graphNode.getId()) {
					throw new IllegalArgumentException("Removing wrong node!");
				}

				logger.trace("Removing node GN.ID: " + firstGraphNode.getId() + " RN.ID: "
						+ firstGraphNode.getRailNode().getId() + " from the route!");

				route.getGraphNodes().remove(0);
			}
		}

		// tell all other locomotives to recompute their routes
		for (final DefaultLocomotive tempLocomotive : modelFacade.getLocomotives()) {

			// skip the current locomotive
			if (tempLocomotive == locomotive) {
				continue;
			}

			// make this locomotive reserve it's path and start it
			if (reserveUpToIncludingNextBlock(tempLocomotive) != null) {

				locomotiveGo(tempLocomotive, DRIVING_SPEED);
			}
		}
	}

	private void freeNode(final RailNode railNode) {

		railNode.setHighlighted(false);
		railNode.setReserved(false);
		railNode.setReservedLocomotiveId(-1);

		// send model changed event
		modelFacade.sendModelChangedEvent(railNode);
	}

	/**
	 * Processes a subsection of a route.<br />
	 * <br />
	 * <ul>
	 * <li />Reserve nodes on the subsection of the route.
	 * <li />Switch turnouts on the subsection of the route.
	 * </ul>
	 *
	 * @param locomotive
	 * @return
	 */
	private Block reserveUpToIncludingNextBlock(final DefaultLocomotive locomotive) {

		logger.info("reserveUpToIncludingNextBlock()");

		final Route route = locomotive.getRoute();
		if (route == null) {
			return null;
		}

		// find the next block on the route
		final Block nextBlock = findNextBlock(locomotive);
		if (nextBlock == null) {

			logger.info("No next node!");
			return null;
		}
		logger.info("Next Block.ID: " + nextBlock.getId());

		final Block currentBlock = findCurrentBlock(route, locomotive);

		// check if the nodes are free
		if (!checkNodes(locomotive, nextBlock)) {
			return null;
		}

		// reserve the nodes
		reserveNodes(locomotive, nextBlock);

		// switch the turnouts on this section of the route
		switchTurnouts(locomotive, currentBlock, nextBlock);

		return nextBlock;
	}

	private void switchTurnouts(final DefaultLocomotive locomotive, final Block currentBlock, final Block nextBlock) {

		logger.info("Switch turnouts up to Block.ID: " + nextBlock.getId());

		final Route route = locomotive.getRoute();
		final RailNode railNode = nextBlock.getNodes().get(0);

		final List<GraphNode> subList = route.getSubListUpToRailNode(railNode);

		// DEBUG: output the sublist
		for (final GraphNode graphNode : subList) {

			logger.trace("SubList RailNode.ID: " + graphNode.getRailNode().getId());
		}

		Route.switchTurnouts(subList, applicationEventPublisher, protocolFacade);
	}

	private void reserveNodes(final DefaultLocomotive locomotive, final Block nextBlock) {

		// once, the function knows, all blocks are free, reserve all blocks
		for (final GraphNode graphNode : locomotive.getRoute()
				.getSubListStartingFromRailNode(locomotive.getRailNode())) {

			final RailNode railNode = graphNode.getRailNode();
			final Block nodeBlock = railNode.getBlock();

			// walk until the next block is reached
			if (nodeBlock != null && nodeBlock.equals(nextBlock)) {
				break;
			}

			// reserve this node for the locomotive
			railNode.setReserved(true);
			railNode.setReservedLocomotiveId(locomotive.getId());

			// draw the blocked not in the blocked color in the ui
			logger.trace("Drawing the node in the blocked color in the UI!");
			modelFacade.sendModelChangedEvent(railNode);
		}

		// reserve the nextBlock for the locomotive
		nextBlock.reserveForLocomotive(locomotive);
	}

	private boolean checkNodes(final DefaultLocomotive locomotive, final Block nextBlock) {

		// check if all nodes from the current block to and including the next block are
		// free
		if (nextBlock.isReserved()) {

			logger.info("BlockID: " + nextBlock.getId() + " is reserved already!");
			return false;
		}

		// check if all RailNodes are free!
		for (final GraphNode graphNode : locomotive.getRoute()
				.getSubListStartingFromRailNode(locomotive.getRailNode())) {

			final Block nodeBlock = graphNode.getRailNode().getBlock();

			// walk until the next block is reached
			if (nodeBlock != null && nodeBlock.equals(nextBlock)) {
				break;
			}

			// is this rail node reserved for another locomotive
			if (graphNode.getRailNode().isReservedExcluding(locomotive.getId())) {
				return false;
			}
		}

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
		for (final GraphNode graphNode : route.getSubListStartingFromRailNode(locomotive.getRailNode())) {

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
