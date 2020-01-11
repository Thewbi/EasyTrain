package de.wfb.rail.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.DrivingService;
import de.wfb.rail.events.BlockEnteredEvent;
import de.wfb.rail.events.BlockExitedEvent;
import de.wfb.rail.events.RouteAddedEvent;
import de.wfb.rail.events.RouteFinishedEvent;

public class DefaultDrivingService implements DrivingService, ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(DefaultDrivingService.class);

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
		}
	}

	private void processRouteFinishedEvent(final RouteFinishedEvent event) {

		logger.info("processRouteFinishedEvent()");

		final DefaultLocomotive defaultLocomotive = event.getDefaultLocomotive();

		logger.info("Removing Route " + defaultLocomotive.getRoute());

		defaultLocomotive.setRoute(null);
	}

	private void processBlockExitedEvent(final BlockExitedEvent event) {

		logger.info("processBlockExitedEvent()");

		freeRouteExceptBlock(event.getBlock(), event.getLocomotive());
		logger.info(event.getLocomotive().getRoute());
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

	private void freeRouteExceptBlock(final Block oldBlock, final DefaultLocomotive locomotive) {

		logger.info("freeRouteExceptBlock()");

//		final Block currentBlock = locomotive.getRailNode().getBlock();
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
		nextBlock.reserveByLocomotive(locomotive);

		return true;
	}

	private Block findCurrentBlock(final Route route, final DefaultLocomotive locomotive) {

		if (locomotive.getRailNode() == null) {
			return null;
		}

		return locomotive.getRailNode().getBlock();
	}

	private Block findNextBlock(final Route route, final DefaultLocomotive locomotive) {

		final Block currentBlock = findCurrentBlock(route, locomotive);

		logger.info("currentBlock = " + currentBlock);

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
