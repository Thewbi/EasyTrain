package de.wfb.model.service;

import java.io.IOException;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.Route;

public interface RoutingService {

	Route route(DefaultLocomotive locomotive, GraphNode graphNodeStart, GraphNode graphNodeEnd,
			boolean routeOverReservedGraphNodes, boolean routeOverBlockedFeedbackBlocks) throws IOException, Exception;

	Route route(DefaultLocomotive locomotive, Node nodeStart, Node nodeEnd, boolean routeOverReservedGraphNodes,
			boolean routeOverBlockedFeedbackBlocks) throws IOException, Exception;

	/**
	 * Determine the correct graph node based on the direction the user wants the
	 * route to be traversed by the locomotive.<br />
	 * <br />
	 *
	 * The startEdgeDirection is not the direction in which the front part of the
	 * faces!<br />
	 * <br />
	 *
	 * If startEdgeDirection and locomotive defaultLocomotive.getOrientation() point
	 * in the same direction, the locomotive will move in forwards direction.<br />
	 * <br />
	 *
	 * If they do not point into the same direction, the locomotive will move in
	 * reverse direction.<br />
	 * <br />
	 *
	 * @param locomotive
	 * @param locomotiveOrientation
	 * @param startBlock
	 * @param startEdgeDirection
	 * @param endBlock
	 * @return
	 */
	Route startLocomotiveToBlock(DefaultLocomotive locomotive, Direction locomotiveOrientation, Block startBlock,
			Direction startEdgeDirection, Block endBlock, boolean routeOverReservedNodes,
			boolean routeOverBlockedFeedbackBlocks);

	void attachRouteToLocomotive(DefaultLocomotive locomotive, Route route);

	void placeLocomotive(Node node, DefaultLocomotive locomotive, Direction edgeDirection);

	void buildRoutingTables();

	void highlightRoute(Route route);

	void switchTurnouts(Route route);

	void removeHighlightedRoute();

	void highlightNode(Node node);

	void initialize();

}
