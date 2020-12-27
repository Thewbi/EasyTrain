package de.wfb.model.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.Edge;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.events.NodeHighlightedEvent;
import de.wfb.rail.events.RouteAddedEvent;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.BlockService;
import de.wfb.rail.service.Route;
import de.wfb.rail.service.RouteUtils;

public abstract class BaseRoutingService implements RoutingService {

	private static final Logger logger = LogManager.getLogger(BaseRoutingService.class);

	/** The Java randomizer object that is able to create random numbers */
	private final Random random = new Random();

	@Autowired
	private ModelService modelService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private ProtocolFacade protocolFacade;

	@Autowired
	private BlockService blockService;

	private List<Block> ignoredBlocks = new ArrayList<>();

	@Override
	public Route route(final Locomotive locomotive, final GraphNode graphNodeStart, final GraphNode graphNodeEnd,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks)
			throws IOException, Exception {
		return null;
	}

	@Override
	public Route route(final Locomotive locomotive, final Node nodeStart, final Node nodeEnd,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks)
			throws IOException, Exception {
		return null;
	}

	@Override
	public void buildRoutingTables() {
		// nop
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

	protected boolean canTraverseGraphNode(final Locomotive locomotive, final GraphNode graphNode,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks) {

		final Node railNode = graphNode.getRailNode();

		final boolean graphNodeIsReserved = railNode.isReserved();

		boolean reservedForLocomotive = false;
		if (locomotive != null) {
			reservedForLocomotive = railNode.getReservedLocomotiveId() == locomotive.getId();
		}

		final boolean debugOutput = false;
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

			logger.warn("Cannot traverse GN ID: " + graphNode.getId() + " Reason: reserved!");
			return false;
		}

		// blocking a graph node, means to make a rail node non-traversable in this
		// direction
		if (graphNode.isBlocked()) {

			logger.trace("Cannot traverse GN ID: " + graphNode.getId() + " Reason: blocked!");
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

			// TODO: there is a case where a locomotive has just entered a block and the
			// block is
			// therefore feedbackBlockUsed = true
			// but the block was not reserved for that locomotive for some reason! The
			// feedback block is
			// then cannot be traversed by the locomotive because it is used by the
			// locomotive but not reserved by that
			// locomotive
			// if (graphNode.getRailNode().getBlock() == )

			logger.info("Cannot traverse GN ID: " + graphNode.getId()
					+ " Reason: RailNode-FeedbackBlockUsed but not reserved for this locomotive-ID: "
					+ locomotive.getId());

			return false;
		}

		return true;
	}

	/**
	 * Takes a java randomizer object and returns one block of the set of all blocks
	 * randomly.
	 *
	 * @param random the java randomizer
	 * @return one randomly selected block
	 */
	private Block selectRandomBlock(final Random random) {

		final List<Block> allBlocks = blockService.getAllBlocks();
		allBlocks.removeAll(getIgnoredBlocks());

		final int min = 0;
		final int max = allBlocks.size() - 1;
		final int index = random.nextInt((max - min) + 1) + min;

		return allBlocks.get(index);
	}

	protected List<Block> getIgnoredBlocks() {

		ignoredBlocks = new ArrayList<>();

		ignoredBlocks.add(blockService.getBlockById(1));
		ignoredBlocks.add(blockService.getBlockById(2));
		ignoredBlocks.add(blockService.getBlockById(3));
		ignoredBlocks.add(blockService.getBlockById(4));
		ignoredBlocks.add(blockService.getBlockById(5));
		ignoredBlocks.add(blockService.getBlockById(6));
		ignoredBlocks.add(blockService.getBlockById(7));
		ignoredBlocks.add(blockService.getBlockById(8));
		ignoredBlocks.add(blockService.getBlockById(9));
		ignoredBlocks.add(blockService.getBlockById(10));
		ignoredBlocks.add(blockService.getBlockById(11));

		ignoredBlocks.add(blockService.getBlockById(35));
		ignoredBlocks.add(blockService.getBlockById(36));
		ignoredBlocks.add(blockService.getBlockById(37));

		ignoredBlocks.add(blockService.getBlockById(41));
		ignoredBlocks.add(blockService.getBlockById(44));
		ignoredBlocks.add(blockService.getBlockById(45));
		ignoredBlocks.add(blockService.getBlockById(46));
		ignoredBlocks.add(blockService.getBlockById(47));

		ignoredBlocks.add(blockService.getBlockById(50));
		ignoredBlocks.add(blockService.getBlockById(51));
		ignoredBlocks.add(blockService.getBlockById(52));
		ignoredBlocks.add(blockService.getBlockById(53));
//		ignoredBlocks.add(blockService.getBlockById(54));
		ignoredBlocks.add(blockService.getBlockById(55));
		ignoredBlocks.add(blockService.getBlockById(56));
		ignoredBlocks.add(blockService.getBlockById(57));
		ignoredBlocks.add(blockService.getBlockById(58));
		ignoredBlocks.add(blockService.getBlockById(59));

		ignoredBlocks.add(blockService.getBlockById(60));
		ignoredBlocks.add(blockService.getBlockById(61));
		ignoredBlocks.add(blockService.getBlockById(62));
		ignoredBlocks.add(blockService.getBlockById(63));
		ignoredBlocks.add(blockService.getBlockById(64));

		ignoredBlocks.add(blockService.getBlockById(81));
		ignoredBlocks.add(blockService.getBlockById(86));

		ignoredBlocks.add(blockService.getBlockById(91));
		ignoredBlocks.add(blockService.getBlockById(93));

		ignoredBlocks.add(blockService.getBlockById(103));

		ignoredBlocks.add(blockService.getBlockById(111));
		ignoredBlocks.add(blockService.getBlockById(112));

		return ignoredBlocks;
	}

	@Override
	public Route startLocomotiveToRandomBlock(final Locomotive locomotive, final Direction locomotiveOrientation,
			final Block startBlock, final Direction startEdgeDirection, final boolean routeOverReservedNodes,
			final boolean routeOverBlockedFeedbackBlocks) {

		logger.trace("startLocomotiveToRandomBlock() ...");

		Route route = null;
		Block randomBlock = null;
		int loopBreaker = 9999;

		// loop until a route was found or the loopBreaker prevents a endless loop
		while ((RouteUtils.isEmpty(route) || route.sizeInBlocks() <= 1) && loopBreaker > 0) {

			loopBreaker--;
			logger.trace("LoopBreaker: " + loopBreaker);

			randomBlock = selectRandomBlock(random);

			logger.trace("Trying to find route to block " + randomBlock);

			route = startLocomotiveToBlock(locomotive, locomotiveOrientation, startBlock, startEdgeDirection,
					randomBlock, routeOverReservedNodes, routeOverBlockedFeedbackBlocks);
		}

		logger.trace("startLocomotiveToRandomBlock(). Result route is " + route + " Route isEmpty: "
				+ RouteUtils.isEmpty(route));

		// there is a bug, where a locomotive got stuck, this is a means to debug that
		// behaviour.
		if (RouteUtils.isEmpty(route)) {
			throw new RuntimeException("Could not find random route for locomotive " + locomotive);
		}

		return route;
	}

	/**
	 * Computes a route but does not attach the route to the locomotive, does not
	 * highlight the route, does not switch turnouts.
	 */
	@Override
	public Route startLocomotiveToBlock(final Locomotive locomotive, final Direction locomotiveOrientation,
			final Block startBlock, final Direction startEdgeDirection, final Block endBlock,
			final boolean routeOverReservedNodes, final boolean routeOverBlockedFeedbackBlocks) {

		final RailNode railNode = locomotive.getRailNode();

		final boolean debug = true;
		if (debug) {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("Locomotive: " + locomotive + " ");
			stringBuffer.append("locomotiveOrientation: " + locomotiveOrientation + " ");
			stringBuffer.append("startBlock: " + startBlock + " ");
			stringBuffer.append("startEdgeDirection: " + startEdgeDirection + " ");
			stringBuffer.append("endBlock: " + endBlock + " ");
			stringBuffer.append("routeOverReservedNodes: " + routeOverReservedNodes + " ");
			stringBuffer.append("routeOverBlockedFeedbackBlocks: " + routeOverBlockedFeedbackBlocks + " ");
			stringBuffer.append("Locomotive RailNode is: " + railNode);
			
			logger.info(stringBuffer.toString());
		}

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

		final GraphNode startGraphNode = edge.getOutGraphNodes().get(0);
		logger.trace("Assign GraphNode " + startGraphNode.getId() + " to locomotive!");
		locomotive.setGraphNode(startGraphNode);

		// DEBUG
		if (startEdgeDirection == locomotive.getOrientation()) {
			logger.trace("Locomotive is going forwards!");
		} else {
			logger.trace("Locomotive is going backwards (reverse)!");
		}

		// create a route
		final Route route = createRoute(locomotive, endBlock, startGraphNode, routeOverReservedNodes,
				routeOverBlockedFeedbackBlocks);

		if (RouteUtils.isNotEmpty(route)) {
			logger.info("Route retrieved: " + route);
		}

		return route;
	}

	public Route createRoute(final Locomotive locomotive, final Block endBlock, final GraphNode startGraphNode,
			final boolean routeOverReservedNodes, final boolean routeOverBlockedFeedbackBlocks) {

		logger.trace("createRoute() to endBlock: " + endBlock);

		final RailNode endRailNode = endBlock.getNodes().get(0);

		Route routeA = new Route();
		try {

			try {
				routeA = route(locomotive, startGraphNode, endRailNode.getGraphNodeOne(), routeOverReservedNodes,
						routeOverBlockedFeedbackBlocks);
			} catch (final Exception e) {
//				logger.error(e.getMessage(), e);
				routeA = new Route();
			}

			if (routeA == null || routeA.isEmpty()) {
				routeA = route(locomotive, startGraphNode, endRailNode.getGraphNodeTwo(), routeOverReservedNodes,
						routeOverBlockedFeedbackBlocks);
			}

		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
			routeA = new Route();
		}

		if (routeA.isEmpty()) {
			logger.warn("No route found!");
		}

		return routeA;
	}

	@Override
	public void attachRouteToLocomotive(final Locomotive locomotive, final Route route) {

		if (RouteUtils.isEmpty(route)) {

			logger.info("Route is null!");
			return;
		}

		logger.trace("For Locomotive " + locomotive + " new Route: " + route);

		if (locomotive.getGraphNode().getId() != route.getGraphNodes().get(0).getId()) {

			final String msg = "GraphNode ids do not match for locomotive: " + locomotive;
			logger.warn(msg);
		}

		// set the route into the locomotive, this causes the TimedDrivingThread to move
		// the locomotive starting with the next timed iteration
		locomotive.setRoute(route);
		route.setLocomotive(locomotive);

		// highlight the entire route
		logger.trace("highlighting the route!");
		route.highlightRoute(applicationEventPublisher);

		// send an event that a locomotive now has a route
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
	public void placeLocomotive(final Node node, final Locomotive locomotive, final Direction edgeDirection) {

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
		final GraphNode outGraphNode = railNode.getEdge(edgeDirection).getOutGraphNodes().get(0);
		logger.info("Placing locomotive " + locomotive + " to graphNode: " + outGraphNode);
		locomotive.setGraphNode(outGraphNode);

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
