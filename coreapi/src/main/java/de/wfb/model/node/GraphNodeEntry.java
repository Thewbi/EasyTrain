package de.wfb.model.node;

public class GraphNodeEntry {

	public GraphNodeEntry(final GraphNode graphNode, final int distance) {
		super();
		this.graphNode = graphNode;
		this.distance = distance;
	}

	private int distance;

	private GraphNode graphNode;

	public int getDistance() {
		return distance;
	}

	public void setDistance(final int distance) {
		this.distance = distance;
	}

	public GraphNode getGraphNode() {
		return graphNode;
	}

	public void setGraphNode(final GraphNode graphNode) {
		this.graphNode = graphNode;
	}

}
