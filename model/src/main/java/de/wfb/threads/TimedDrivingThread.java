package de.wfb.threads;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Edge;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.rail.events.BlockEnteredEvent;
import de.wfb.rail.events.BlockExitedEvent;
import de.wfb.rail.events.RouteFinishedEvent;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.Route;

public class TimedDrivingThread {

	private static final Logger logger = LogManager.getLogger(TimedDrivingThread.class);

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Scheduled(fixedRate = 1000)
	public void moveLocomotives() {

		final Date date = new Date();

		logger.trace("moveLocomotives " + date);

		final List<DefaultLocomotive> locomotives = modelFacade.getLocomotives();

		if (CollectionUtils.isEmpty(locomotives)) {

			logger.warn("No locomotives found!");
			return;
		}

		for (final DefaultLocomotive locomotive : locomotives) {

			final Route route = locomotive.getRoute();
			if (route == null) {

				logger.warn("No route found!");
				continue;
			}

			processLocomotive(locomotive);
		}
	}

	private void processLocomotive(final DefaultLocomotive locomotive) {

		logger.info("processLocomotive()");

		// find the graph node the locomotive is on currently
		final RailNode currentRailNode = locomotive.getRailNode();
		final GraphNode currentGraphNode = locomotive.getRoute().findGraphNode(currentRailNode);

		final Route route = locomotive.getRoute();

		if (currentGraphNode.equals(route.getLastGraphNode())) {

			logger.info("Locomotive is on the last graph node of it's route!");
			applicationEventPublisher.publishEvent(new RouteFinishedEvent(this, route, locomotive));
		}

		final List<GraphNode> children = currentGraphNode.getChildren();
		if (CollectionUtils.isEmpty(children)) {

			logger.warn("GraphNode has no children");
			return;
		}

		// process all children of the graph node the locomotive is currently on
		for (final GraphNode graphNode : children) {

			if (graphNode.getRailNode().getReservedLocomotiveId() != locomotive.getId()) {

				logger.warn("Child GraphNode ID: " + graphNode.getId() + " Not reserved for locomotive! Reserved for: "
						+ graphNode.getRailNode().getReservedLocomotiveId() + " Locomotive ID: " + locomotive.getId());
				continue;
			}

			final RailNode newRailNode = moveLocomotiveToNextGraphNode(locomotive, currentGraphNode, graphNode);

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

			// do not visit the next child
			break;
		}
	}

	private RailNode moveLocomotiveToNextGraphNode(final DefaultLocomotive locomotive, final GraphNode currentGraphNode,
			final GraphNode graphNode) {
		logger.info("MOVED FROM GN: " + currentGraphNode.getId() + " TO GN: " + graphNode.getId());

		// move the locomotive to the next graph node
		final RailNode newRailNode = graphNode.getRailNode();
		locomotive.setRailNode(newRailNode);

		// compute the orientation of the locomotive on this rail
		for (int i = 0; i < 4; i++) {

			final Edge edge = newRailNode.getEdges()[i];
			if (edge == null) {

				continue;
			}

			if (edge.getOutGraphNode().equals(graphNode)) {

				locomotive.setOrientation(edge.getDirection());
			}
		}
		return newRailNode;
	}

	private void sendEnteringMessage(final Block block, final DefaultLocomotive defaultLocomotive) {

		logger.info("Entering block " + block);

		final BlockEnteredEvent blockEnteredEvent = new BlockEnteredEvent(this, block, defaultLocomotive);
		applicationEventPublisher.publishEvent(blockEnteredEvent);
	}

	private void sendLeavingMessage(final Block block, final DefaultLocomotive defaultLocomotive) {

		logger.info("Leaving block " + block);

		final BlockExitedEvent blockExitedEvent = new BlockExitedEvent(this, block, defaultLocomotive);
		applicationEventPublisher.publishEvent(blockExitedEvent);
	}

}
