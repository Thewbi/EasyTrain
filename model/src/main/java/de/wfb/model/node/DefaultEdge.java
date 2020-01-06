package de.wfb.model.node;

public class DefaultEdge implements Edge {

	private GraphNode inGraphNode;

	private GraphNode outGraphNode;

	public GraphNode getInGraphNode() {
		return inGraphNode;
	}

	public void setInGraphNode(final GraphNode inGraphNode) {
		this.inGraphNode = inGraphNode;
	}

	public GraphNode getOutGraphNode() {
		return outGraphNode;
	}

	public void setOutGraphNode(final GraphNode outGraphNode) {
		this.outGraphNode = outGraphNode;
	}

}
