package de.wfb.model.service;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.Edge;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.events.NodeHighlightedEvent;
import de.wfb.rail.events.RouteAddedEvent;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.service.Block;
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
		return null;
	}

	@Override
	public Route route(final DefaultLocomotive locomotive, final Node nodeStart, final Node nodeEnd,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks)
			throws IOException, Exception {
		return null;
	}

	@Override
	public void buildRoutingTables() {
		// Auto-generated method stub
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

			logger.trace("Cannot traverse GN ID: " + graphNode.getId() + " Reason: reserved!");
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

	@Override
	public Route startLocomotiveToBlock(final DefaultLocomotive locomotive, final Direction locomotiveOrientation,
			final Block startBlock, final Direction startEdgeDirection, final Block endBlock,
			final boolean routeOverReservedNodes, final boolean routeOverBlockedFeedbackBlocks) {

		final RailNode railNode = locomotive.getRailNode();
		logger.trace("Locomotive RailNode is: " + railNode);
		if (railNode == null) {

			logger.error("Returning null!");
			return null;
		}

		final Edge edge = railNode.getEdge(startEdgeDirection);
		if (edge == null) {
			logger.error("The direction startEdgeDirection: " + startEdgeDirection + " does not exist! locomotive: "
					+ locomotive + " locomotive.getOrientation(): " + locomotive.getOrientation() + " RailNode-ID: "
					+ railNode.getId());
			return null;
		}

		final GraphNode startGraphNode = edge.getOutGraphNode();
		logger.info("Assign GraphNode " + startGraphNode.getId() + " to locomotive!");
		locomotive.setGraphNode(startGraphNode);

		if (startEdgeDirection == locomotive.getOrientation()) {

			logger.trace("Locomotive is going forwards!");

		} else {

			logger.trace("Locomotive is going backwards (reverse)!");

		}

		// create a route
		final Route route = createRoute(locomotive, endBlock, startGraphNode, routeOverReservedNodes,
				routeOverBlockedFeedbackBlocks);

		logger.trace("Route retrieved: " + route);

		return route;
	}

	public Route createRoute(final DefaultLocomotive locomotive, final Block endBlock, final GraphNode startGraphNode,
			final boolean routeOverReservedNodes, final boolean routeOverBlockedFeedbackBlocks) {

		logger.info("createRoute()");

		final RailNode endRailNode = endBlock.getNodes().get(0);

		Route routeA = new Route();
		try {

			try {
				routeA = route(locomotive, startGraphNode, endRailNode.getGraphNodeOne(), routeOverReservedNodes,
						routeOverBlockedFeedbackBlocks);
			} catch (final Exception e) {
				routeA = new Route();
			}

			if (routeA == null || routeA.isEmpty()) {

				routeA = route(locomotive, startGraphNode, endRailNode.getGraphNodeOne(), routeOverReservedNodes,
						routeOverBlockedFeedbackBlocks);
			}
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
			routeA = new Route();
		}

		Route routeB = new Route();
		try {

			try {
				routeB = route(locomotive, startGraphNode, endRailNode.getGraphNodeTwo(), routeOverReservedNodes,
						routeOverBlockedFeedbackBlocks);
			} catch (final Exception e) {
				routeB = new Route();
			}

			if (routeB == null || routeB.isEmpty()) {

				routeB = route(locomotive, startGraphNode, endRailNode.getGraphNodeTwo(), routeOverReservedNodes,
						routeOverBlockedFeedbackBlocks);
			}
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
			routeB = new Route();
		}

		logger.info("RouteA: " + routeA);
		logger.info("RouteB: " + routeB);

		if (routeA.isEmpty() && routeB.isEmpty()) {

			return null;

		} else if (routeA.isEmpty()) {

			return routeB;

		} else if (routeB.isEmpty()) {

			return routeA;

		} else {

			return routeA.getGraphNodes().size() < routeB.getGraphNodes().size() ? routeA : routeB;

		}
	}

	@Override
	public void attachRouteToLocomotive(final DefaultLocomotive locomotive, final Route route) {

		if (route == null) {

			logger.info("Route is null!");
			return;
		}

		logger.info(route);

		if (locomotive.getGraphNode().getId() != route.getGraphNodes().get(0).getId()) {

			throw new RuntimeException("GraphNode ids do not match for locomotive: " + locomotive);
		}

		// set the route into the locomotive, this causes the TimedDrivingThread to move
		// the locomotive starting with the next timed iteration
		locomotive.setRoute(route);
		route.setLocomotive(locomotive);

		// highlight the entire route
		logger.trace("highlighting the route!");
		route.highlightRoute(applicationEventPublisher);

		// Send an event that a locomotive now has a route
		final RouteAddedEvent routeAddedEvent = new RouteAddedEvent(this, route, locomotive);
		applicationEventPublisher.publishEvent(routeAddedEvent);

		// The DrivingService will catch the event and reserve the route so that the
		// locomotive can move to the next block
	}

	/**
	 *
	 * @param node          the RailNode to put the locomotive on. RailNode !=
	 *                      GraphNode.
	 * @param locomotive    the locomotive.
	 * @param edgeDirection this is the direction where the front facing part of the
	 *                      locomotive points to. This modelled direction has to
	 *                      match the direction of the locomotive on the real life
	 *                      layout so correct P50X commands can be produced for
	 *                      driving the locomotive forwards or backwards. This
	 *                      direction it is not necessarily the same direction in
	 *                      which the locomotive will move! If the direction are
	 *                      opposite a reverse move P50X command will be produced!
	 *                      If the orientations align, a forward move P50X command
	 *                      will be produced!
	 */
	@Override
	public void placeLocomotive(final Node node, final DefaultLocomotive locomotive, final Direction edgeDirection) {

		if (node == null || locomotive == null || edgeDirection == null) {

			final String msg = "Need a locomotive, a selected node and a direction to place a locomotive!";
			logger.error(msg);

			throw new IllegalArgumentException(msg);
		}

		final RailNode railNode = (RailNode) node;

		// the orientation in which the locomotive points forward (this is not
		// necessarily the same direction in which
		// the locomotive will move once the route starts!)
		locomotive.setOrientation(edgeDirection);
		locomotive.setRailNode(railNode);

		// place a graphnode into the locomotive

		// why was this feature deactivated?
		locomotive.setGraphNode(railNode.getEdge(edgeDirection).getOutGraphNode());

		final Block block = railNode.getBlock();
		if (block == null) {

			// put the locomotive onto the rail node
			if (railNode.isReserved()) {
				throw new IllegalArgumentException("RailNode is reserved already!");
			}

			railNode.setReserved(true);
			railNode.setReservedLocomotiveId(locomotive.getId());

		} else {

			// if the rail node is part of a block, reserve the entire block
			block.reserveForLocomotive(locomotive);

			logger.info("Put Locomotive " + locomotive.getName() + " onto Block " + block.getId());
		}

		logger.info("Put Locomotive " + locomotive.getName() + " onto node " + node.getId());
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
