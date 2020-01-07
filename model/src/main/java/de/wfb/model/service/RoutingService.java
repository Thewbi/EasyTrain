package de.wfb.model.service;

import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;

public interface RoutingService {

	void route(GraphNode graphNodeStart, GraphNode graphNodeEnd);

	void route(Node nodeStart, Node nodeEnd);

	void buildRoutingTables();

	void colorGraph();

}
