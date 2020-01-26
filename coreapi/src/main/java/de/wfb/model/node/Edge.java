package de.wfb.model.node;

public interface Edge {

	Direction getDirection();

	void setDirection(Direction direction);

	GraphNode getInGraphNode();

	void setInGraphNode(GraphNode inGraphNode);

	GraphNode getOutGraphNode();

	void setOutGraphNode(GraphNode outGraphNode);

	GraphNode getNextOutGraphNode();

	void setNextOutGraphNode(GraphNode nextOutGraphNode);

}
