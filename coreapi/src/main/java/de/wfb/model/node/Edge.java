package de.wfb.model.node;

public interface Edge {

	GraphNode getInGraphNode();

	void setInGraphNode(GraphNode inGraphNode);

	GraphNode getOutGraphNode();

	void setOutGraphNode(GraphNode outGraphNode);

	Direction getDirection();

	void setDirection(Direction direction);

	GraphNode getNextOutGraphNode();

	void setNextOutGraphNode(GraphNode nextOutGraphNode);

}
