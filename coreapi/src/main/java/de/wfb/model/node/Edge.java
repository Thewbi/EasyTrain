package de.wfb.model.node;

import java.util.List;

public interface Edge {

	Direction getDirection();

	void setDirection(Direction direction);

	GraphNode getInGraphNode();

	void setInGraphNode(GraphNode inGraphNode);

	List<GraphNode> getOutGraphNodes();

//	void setOutGraphNode(GraphNode outGraphNode);

	GraphNode getNextOutGraphNode();

	void setNextOutGraphNode(GraphNode nextOutGraphNode);

}
