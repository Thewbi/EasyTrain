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
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.DrivingService;
import de.wfb.rail.events.BlockEnteredEvent;
import de.wfb.rail.events.BlockExitedEvent;
import de.wfb.rail.events.FeedbackBlockEvent;
import de.wfb.rail.events.RouteAddedEvent;
import de.wfb.rail.events.RouteFinishedEvent;

public class DefaultDrivingService implements DrivingService, ApplicationListener<ApplicationEvent> {

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

		logger.info("processFeedbackBlockEvent()");

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

		logger.info("feedbackBlockNumber: " + feedbackBlockNumber);

		final Block block = blockService.getBlockById(feedbackBlockNumber);

		logger.info("block: " + block);

		final Route route = routingFacade.getRouteByBlock(block);

		logger.info("route: " + route);

		final DefaultLocomotive locomotive = route.getLocomotive();

		logger.info("locomotive: " + locomotive);

		// It will determine which locomotive has exited that block. Because
		// only a single locomotive can reserve a route to this block.
		//
		// based on that knowledge it will send the BlockExitedEvent
		final BlockExitedEvent blockExitedEvent = new BlockExitedEvent(this, block, locomotive);
		applicationEventPublisher.publishEvent(blockExitedEvent);
	}

	private void processFeedbackBlockBlocked(final FeedbackBlockEvent event) {

		logger.info("processFeedbackBlockBlocked()");

		final int feedbackBlockNumber = event.getFeedbackBlockNumber();

		final Block block = blockService.getBlockById(feedbackBlockNumber);

		final Route route = routingFacade.getRouteByBlock(block);

		final DefaultLocomotive locomotive = route.getLocomotive();

		// It will determine which locomotive has entered that block. Because
		// only a single locomotive can reserve a route to this block.
		//
		// based on that knowledge it will send the BlockEnteredEvent
		final BlockEnteredEvent blockEnteredEvent = new BlockEnteredEvent(this, block, locomotive);
		applicationEventPublisher.publishEvent(blockEnteredEvent);
	}

	private void processRouteFinishedEvent(final RouteFinishedEvent event) {

		logger.info("processRouteFinishedEvent()");

		final DefaultLocomotive defaultLocomotive = event.getDefaultLocomotive();

		logger.info("Removing Route " + defaultLocomotive.getRoute());

		final Route route = defaultLocomotive.getRoute();
		route.setLocomotive(null);
		defaultLocomotive.setRoute(null);

		final RailNode railNode = defaultLocomotive.getRailNode();
		final GraphNode graphNode = defaultLocomotive.getGraphNode();
		logger.info("The locomotive is now on the RailNode ID: " + railNode.getId() + " and on GraphNode ID: "
				+ graphNode.getId() + ". Its orientation is: " + defaultLocomotive.getOrientation().name());
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

		freeRouteExceptUpToBlock(event.getBlock(), event.getLocomotive());

		logger.info(event.getLocomotive().getRoute());

		// tell all other locomotives to recompute their routes
		for (final DefaultLocomotive locomotive : modelFacade.getLocomotives()) {

			if (locomotive == event.getLocomotive()) {

				continue;
			}

			reserveUpToIncludingNextBlock(locomotive);
		}
	}

	private void processBlockEnteredEvent(final BlockEnteredEvent event) {

		logger.info("processBlockEnteredEvent()");

		if (!reserveUpToIncludingNextBlock(event.getLocomotive())) {

			logger.info("Could not reserve up to next block");
			return;
		}
	}

	private void processRouteAddedEvent(final RouteAddedEvent event) {

		final Route route = event.getRoute();
		final DefaultLocomotive locomotive = event.getDefaultLocomotive();

		logger.info(route);

		if (!reserveUpToIncludingNextBlock(locomotive)) {

			logger.info("Could not reserve up to next block");
			return;
		}

		logger.info(route);
	}

	/**
	 * Free all nodes including the block that was just left
	 *
	 * @param oldBlock
	 * @param locomotive
	 */
	private void freeRouteExceptUpToBlock(final Block oldBlock, final DefaultLocomotive locomotive) {

		logger.info("freeRouteExceptUpToBlock()");

		final Block currentBlock = oldBlock;
		final Block nextBlock = findNextBlock(locomotive.getRoute(), locomotive);

		logger.info("freeRouteExceptBlock() currentBlock = " + currentBlock + " nextBlock = " + nextBlock);

		if (currentBlock == null || nextBlock == null) {
			return;
		}

		// if a RailNode is reserved for this locomotive, free it
		for (final GraphNode graphNode : locomotive.getRoute().getGraphNodes()) {

			logger.info("Freeing GraphNode: " + graphNode);

			final RailNode railNode = graphNode.getRailNode();

			logger.info("Freeing railNode: ID: " + railNode.getId() + " Reserved: " + railNode.isReserved()
					+ " ReservedLocomotiveID: " + railNode.getReservedLocomotiveId());

			final Block nodeBlock = railNode.getBlock();

			// arrived at the block
			if (nodeBlock != null && nodeBlock.equals(nextBlock)) {

				return;
			}

			if (railNode.isReserved()) {

				if (railNode.getReservedLocomotiveId() == locomotive.getId()) {

					logger.info("FREE RAILNODE ID: " + railNode.getId() + " ReservedLocomotiveID: "
							+ railNode.getReservedLocomotiveId());

					railNode.setReserved(false);
					railNode.setReservedLocomotiveId(-1);
				}
			}
		}
	}

	private boolean reserveUpToIncludingNextBlock(final DefaultLocomotive locomotive) {

		// find the next block on the route
		final Block nextBlock = findNextBlock(locomotive.getRoute(), locomotive);

		if (nextBlock == null) {

			logger.info("No next node!");
			return true;
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

	private Block findNextBlock(final Route route, final DefaultLocomotive locomotive) {

		logger.info("Find next Block for locomotive ID: " + locomotive.getId());

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
