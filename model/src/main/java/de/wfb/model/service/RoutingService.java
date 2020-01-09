package de.wfb.model.service;

import java.util.List;

import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;

public interface RoutingService {

	List<GraphNode> route(GraphNode graphNodeStart, GraphNode graphNodeEnd);

	List<GraphNode> route(Node nodeStart, Node nodeEnd);

	void buildRoutingTables();

	void colorGraph();

	void highlightRoute(List<GraphNode> route);

	void switchTurnouts(List<GraphNode> route);

}
