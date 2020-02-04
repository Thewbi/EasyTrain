package de.wfb.model.node;

/**
 * This class is an entry in a routing table between switching nodes.
 */
public class SwitchingNodeEntry {

	/**
	 * The graphnode that connects the owner of this entry with the next switching
	 * node.
	 */
	private GraphNode connectingGraphNode;

	/**
	 * The next switching node.
	 */
	private GraphNode switchingGraphNode;

	private int distance;

	public GraphNode getConnectingGraphNode() {
		return connectingGraphNode;
	}

	public void setConnectingGraphNode(final GraphNode connectingGraphNode) {
		this.connectingGraphNode = connectingGraphNode;
	}

	public GraphNode getSwitchingGraphNode() {
		return switchingGraphNode;
	}

	public void setSwitchingGraphNode(final GraphNode switchingGraphNode) {
		this.switchingGraphNode = switchingGraphNode;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(final int distance) {
		this.distance = distance;
	}

}
