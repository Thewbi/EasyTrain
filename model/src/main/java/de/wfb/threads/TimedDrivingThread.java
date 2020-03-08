package de.wfb.threads;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import de.wfb.configuration.ConfigurationConstants;
import de.wfb.configuration.ConfigurationService;
import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.Edge;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.RoutingController;
import de.wfb.rail.controller.TimedDrivingThreadController;
import de.wfb.rail.events.FeedbackBlockEvent;
import de.wfb.rail.events.FeedbackBlockState;
import de.wfb.rail.events.ModelChangedEvent;
import de.wfb.rail.events.RouteFinishedEvent;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.Route;

/**
 * This Thread is scheduled by the @EnableScheduling annotation on the
 * configuration class (de.wfb.ConfigurationClass).<br />
 * <br />
 *
 * It is simulation locomotive motion. Whenever you use EasyTrain with real
 * hardware on a real layout, you do not need this Thread.<br />
 * <br />
 *
 * Whenever a locomotive has a child graph node that is reserved for this
 * locomotive, the TimedDrivingThread will move the locomotive onto this
 * reserved child.<br />
 * <br />
 */
public class TimedDrivingThread {

	private static final Logger logger = LogManager.getLogger(TimedDrivingThread.class);

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private TimedDrivingThreadController timedDrivingThreadController;

	@Autowired
	private RoutingController routingController;

	@Autowired
	private ConfigurationService configurationService;

	private int iterationCount = 0;

//	private final int goToIteration = 290;

	private int infoCounter = 0;

//	@Scheduled(fixedRate = 100)
	@Scheduled(fixedRate = 150)
	public void threadFunc() throws Exception {

		if (infoCounter == 100) {
			infoCounter = 0;
		} else {
			infoCounter++;
		}

		final boolean active = configurationService
				.getConfigurationAsBoolean(ConfigurationConstants.TIMED_DRIVING_THREAD_ACTIVE);

		logger.trace("threadFunc() ACTIVE = " + active);

		if (!active) {

			logger.trace("threadFunc() is deactivated!");
			return;
		}

		if (!routingController.isStarted()) {

			logger.trace("Routing controller is not started!");
			return;
		}

//		if (goToIteration > 0 && iterationCount < goToIteration) {
//			iterationCount++;
//			moveLocomotives();
//			return;
//		}

		if (timedDrivingThreadController.isPaused() && timedDrivingThreadController.decrementSingleStep() <= 0) {
			return;
		}

		iterationCount++;
		logger.trace(iterationCount);

		moveLocomotives();
	}

	public void moveLocomotives() throws Exception {

		final Date date = new Date();

		logger.trace("moveLocomotives " + date);

		final List<Locomotive> locomotives = modelFacade.getLocomotives();

		if (CollectionUtils.isEmpty(locomotives)) {

			logger.warn("No locomotives found!");
			return;
		}

		for (final Locomotive locomotive : locomotives) {

			final Route route = locomotive.getRoute();
			if (route == null) {

				if (infoCounter == 0) {
					logger.info("No route found on locomotive ID: " + locomotive.getId());
				}
				continue;
			}

			processLocomotive(locomotive);
		}
	}

	private void processLocomotive(final Locomotive locomotive) throws Exception {

		logger.trace("processLocomotive() locomotive ID: " + locomotive.getId());

		// find the graph node the locomotive is on currently
		final RailNode currentRailNode = locomotive.getRailNode();
		logger.trace("RailNode: " + currentRailNode);

		final Block block = currentRailNode.getBlock();
		logger.trace("Block: " + block);

		final Route route = locomotive.getRoute();
		logger.trace("Route is null? " + (route == null));
		logger.trace("Route: " + route);

		// DEBUG - stop locomotives on blocks
//		if (block != null && (block.getId() == 43 || block.getId() == 17)) {
//			return;
//		}

//		final GraphNode currentGraphNode = route.findGraphNode(currentRailNode);
//		logger.info("GraphNode: " + currentGraphNode);
//		if (null == currentGraphNode) {
//			return;
//		}
//		locomotive.setGraphNode(currentGraphNode);
//		if (currentGraphNode != null && currentGraphNode.equals(route.getLastGraphNode())) {

		if (route.endsWith(block)) {

			logger.trace("Locomotive is on the last graph node of it's route!");
			applicationEventPublisher.publishEvent(new RouteFinishedEvent(this, route, locomotive));
			return;
		}

		final GraphNode currentGraphNode = route.findGraphNode(currentRailNode);
		logger.trace("GraphNode: " + currentGraphNode);
		if (null == currentGraphNode) {

			logger.error("Locomotive is on a node that is not part of its route!");
			throw new Exception(
					"Route: " + route + " Locomotive: " + locomotive + " currentRailNode: " + currentRailNode);
		}
		locomotive.setGraphNode(currentGraphNode);

		final List<GraphNode> children = currentGraphNode.getChildren();
		if (CollectionUtils.isEmpty(children)) {

			final String msg = "GraphNode has no children";
			logger.warn(msg);

			throw new IllegalArgumentException(msg);
		}

		// process all children of the graph node the locomotive is currently on
		boolean locomotiveWasUsed = false;
		for (final GraphNode graphNode : children) {

			final int reservedForId = graphNode.getRailNode().getReservedLocomotiveId();
			if (reservedForId != locomotive.getId()) {

				logger.trace("Child GraphNode ID: " + graphNode.getId()
						+ " Not reserved for locomotive! Reserved for LocomotiveID: "
						+ graphNode.getRailNode().getReservedLocomotiveId()
						+ " The Locomotive ID of the blocked Locomotive is: " + locomotive.getId());

				continue;
			}

			if (!route.containsGraphNode(graphNode)) {

				// this happens if both children of a turnout are part of blocks and those
				// blocks are both part of the same blockgroup. If the locomotive is on one of
				// the blocks of the
				// group all blocks get reserved.
				//
				// The result is that a child can only be visited if it is reserved for the
				// locomotive and
				// at the same time really is part of the route

				continue;
			}

			final RailNode newRailNode = moveLocomotiveToNextGraphNode(locomotive, currentGraphNode, graphNode);

			locomotiveWasUsed = true;

			processBlocks(currentGraphNode, newRailNode, locomotive);

			// do not visit the next child
			break;
		}

		if (!locomotiveWasUsed) {

			logger.trace("Locomotive ID: " + locomotive.getId() + " is waiting on GraphNode ID: "
					+ locomotive.getGraphNode().getId());

			logger.trace(locomotive.getRoute());
		}
	}

	private void processBlocks(final GraphNode currentGraphNode, final RailNode newRailNode,
			final Locomotive locomotive) {

		final Block oldBlock = currentGraphNode.getRailNode().getBlock();
		final Block newBlock = newRailNode.getBlock();

		if ((oldBlock != null) && (newBlock != null)) {

			if (oldBlock.equals(newBlock)) {

				// nop

			} else {

				sendLeavingMessage(oldBlock, locomotive);
				sendEnteringMessage(newBlock, locomotive);

			}

		} else if (oldBlock != null) {

			sendLeavingMessage(oldBlock, locomotive);

		} else if (newBlock != null) {

			sendEnteringMessage(newBlock, locomotive);

		}
	}

	private RailNode moveLocomotiveToNextGraphNode(final Locomotive locomotive, final GraphNode currentGraphNode,
			final GraphNode nextGraphNode) {

		final int currentGraphNodeId = currentGraphNode.getId();
		final int currentGraphNodeX = currentGraphNode.getX();
		final int currentGraphNodeY = currentGraphNode.getY();

		final int nextGraphNodeId = nextGraphNode.getId();
		final int nextGraphNodeX = nextGraphNode.getX();
		final int nextGraphNodeY = nextGraphNode.getY();

		logger.trace("Locomotive: " + locomotive);

		logger.trace(
				"Loc-ID: " + locomotive.getId() + " MOVED FROM GN: " + currentGraphNodeId + " x: " + currentGraphNodeX
						+ " y: " + currentGraphNodeY + " TO GN: " + nextGraphNodeId + " x: " + nextGraphNodeX + " y: "
						+ nextGraphNodeY + " locomotive.getOrientation: " + locomotive.getOrientation());

		// move the locomotive to the next graph node
		final RailNode nextRailNode = nextGraphNode.getRailNode();
		locomotive.setRailNode(nextRailNode);

		// compute the orientation of the locomotive on this rail
		for (int i = 0; i < 4; i++) {

			final Edge edge = nextRailNode.getEdges()[i];
			if (edge == null) {

				continue;
			}

			for (final GraphNode outGraphNode : edge.getOutGraphNodes()) {

				if (outGraphNode.equals(nextGraphNode)) {

					locomotive.setGraphNode(outGraphNode);
					locomotive.setOrientation(edge.getDirection());
				}
			}
		}

		// remove the highlight from the graph node, the locomotive is leaving
		boolean highlighted = false;
		boolean blocked = currentGraphNode.isBlocked();
		boolean selected = false;
		boolean reserved = false;
		boolean containsLocomotive = false;
		ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, null, currentGraphNode.getX(),
				currentGraphNode.getY(), highlighted, blocked, selected, reserved, containsLocomotive);
		applicationEventPublisher.publishEvent(modelChangedEvent);

		// add highlight to the GraphNode the locomotive moves to.
		highlighted = false;
		blocked = nextGraphNode.isBlocked();
		selected = false;
		reserved = false;
		containsLocomotive = true;
		modelChangedEvent = new ModelChangedEvent(this, null, nextGraphNode.getX(), nextGraphNode.getY(), highlighted,
				blocked, selected, reserved, containsLocomotive);
		applicationEventPublisher.publishEvent(modelChangedEvent);

		return nextRailNode;
	}

	private void sendEnteringMessage(final Block block, final Locomotive locomotive) {

		logger.trace("Entering block " + block);

		final int feedbackBlockNumber = block.getId();
		final FeedbackBlockState feedbackBlockState = FeedbackBlockState.BLOCKED;

		final FeedbackBlockEvent feedbackBlockEvent = new FeedbackBlockEvent(this, feedbackBlockNumber,
				feedbackBlockState);
		applicationEventPublisher.publishEvent(feedbackBlockEvent);
	}

	private void sendLeavingMessage(final Block block, final Locomotive locomotive) {

		logger.trace("Leaving block: " + block);

		final int feedbackBlockNumber = block.getId();
		final FeedbackBlockState feedbackBlockState = FeedbackBlockState.FREE;

		final FeedbackBlockEvent feedbackBlockEvent = new FeedbackBlockEvent(this, feedbackBlockNumber,
				feedbackBlockState);
		applicationEventPublisher.publishEvent(feedbackBlockEvent);
	}

}
