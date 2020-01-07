package de.wfb.model.node;

public interface Edge {

	GraphNode getInGraphNode();

	void setInGraphNode(GraphNode inGraphNode);

	GraphNode getOutGraphNode();

	void setOutGraphNode(GraphNode outGraphNode);

}
