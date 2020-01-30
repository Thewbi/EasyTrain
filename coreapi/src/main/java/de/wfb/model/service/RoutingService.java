package de.wfb.model.service;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.rail.service.Route;

public interface RoutingService {

	Route route(DefaultLocomotive locomotive, GraphNode graphNodeStart, GraphNode graphNodeEnd,
			boolean routeOverReservedGraphNodes);

	Route route(DefaultLocomotive locomotive, Node nodeStart, Node nodeEnd, boolean routeOverReservedGraphNodes);

	void buildRoutingTables();

	void colorGraph();

	void highlightRoute(Route route);

	void switchTurnouts(Route route);

}
