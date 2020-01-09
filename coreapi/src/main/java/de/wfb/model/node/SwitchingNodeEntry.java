package de.wfb.model.node;

import de.wfb.model.node.GraphNode;

public class SwitchingNodeEntry {

	private GraphNode connectingGraphNode;

	private GraphNode switchingGraphNode;

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

}
