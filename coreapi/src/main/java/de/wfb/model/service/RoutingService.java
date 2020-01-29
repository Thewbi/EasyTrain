package de.wfb.model.service;

import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.rail.service.Route;

public interface RoutingService {

	Route route(GraphNode graphNodeStart, GraphNode graphNodeEnd, boolean routeOverReservedGraphNodes);

	Route route(Node nodeStart, Node nodeEnd, boolean routeOverReservedGraphNodes);

	void buildRoutingTables();

	void colorGraph();

	void highlightRoute(Route route);

	void switchTurnouts(Route route);

}
