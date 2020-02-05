package de.wfb.model.service;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.rail.events.NodeHighlightedEvent;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.service.Route;

public abstract class BaseRoutingService implements RoutingService {

	private static final Logger logger = LogManager.getLogger(BaseRoutingService.class);

	@Autowired
	private ModelService modelService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private ProtocolFacade protocolFacade;

	@Override
	public Route route(final DefaultLocomotive locomotive, final GraphNode graphNodeStart, final GraphNode graphNodeEnd,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks)
			throws IOException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Route route(final DefaultLocomotive locomotive, final Node nodeStart, final Node nodeEnd,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks)
			throws IOException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void buildRoutingTables() {
		// TODO Auto-generated method stub

	}

	@Override
	public void highlightRoute(final Route route) {
		route.highlightRoute(applicationEventPublisher);
	}

	@Override
	public void switchTurnouts(final Route route) {
		route.switchTurnouts(applicationEventPublisher, protocolFacade);
	}

	@Override
	public void removeHighlightedRoute() {
		modelService.removeAllHighlights();
	}

	@Override
	public void highlightNode(final Node node) {
		final boolean hightlighted = true;
		final NodeHighlightedEvent nodeHighlightedEvent = new NodeHighlightedEvent(this, null, node, node.getX(),
				node.getY(), hightlighted);

		applicationEventPublisher.publishEvent(nodeHighlightedEvent);
	}

	protected boolean canTraverseGraphNode(final DefaultLocomotive locomotive, final GraphNode graphNode,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks) {

		final Node railNode = graphNode.getRailNode();

		final boolean debugOutput = railNode.getId() == 2383;

		final boolean graphNodeIsReserved = railNode.isReserved();

		boolean reservedForLocomotive = false;
		if (locomotive != null) {
			reservedForLocomotive = railNode.getReservedLocomotiveId() == locomotive.getId();
		}

		if (debugOutput) {
			logger.info("graphNodeIsReserved: " + graphNodeIsReserved);
			logger.info("locomotive: " + locomotive);
			logger.info("reservedForLocomotive: " + reservedForLocomotive);
			logger.info("routeOverReservedGraphNodes: " + routeOverReservedGraphNodes);
		}

		// build routes over reserved graphnodes
		// if the block is reserved for the locomotive for which this route is
		// created, then there is no problem and the route can pass the node!
		if (graphNodeIsReserved && !reservedForLocomotive && !routeOverReservedGraphNodes) {

			logger.info("Cannot traverse GN ID: " + graphNode.getId() + " Reason: reserved!");
			return false;
		}

		// blocking a graph node, means to make a rail node non-traversable in this
		// direction
		if (graphNode.isBlocked()) {

			logger.info("Cannot traverse GN ID: " + graphNode.getId() + " Reason: blocked!");
			return false;
		}

		// RailNode FeedbackBlockUsed. A feedback block can be used without beeing
		// reserved! Hence this case needs handling too.
		//
		// A feedback block is used without beeing explicitly reserved in at least the
		// following cases:
		//
		// * If an object without an address was put on a feedback block by hand
		// * If a wagon of a excessively long train blocks a feedback block
		// * If a locomotive or train was moved using manual control onto a feedback
		// block. Manual operation does not reserve feedback blocks
		if (graphNode.getRailNode().isFeedbackBlockUsed() && !reservedForLocomotive
				&& !routeOverBlockedFeedbackBlocks) {

			logger.info("Cannot traverse GN ID: " + graphNode.getId() + " Reason: RailNode-FeedbackBlockUsed!");
			return false;
		}

		return true;
	}

	/**
	 * For testing
	 *
	 * @param modelService
	 */
	public void setModelService(final ModelService modelService) {
		this.modelService = modelService;
	}

	public ModelService getModelService() {
		return modelService;
	}

}
