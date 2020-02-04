package de.wfb.model.service;

import de.wfb.model.node.GraphNode;

public class SwitchingFrame {

	private final GraphNode switchingNode;

	private final GraphNode otherOption;

	public SwitchingFrame(final GraphNode switchingNode, final GraphNode otherOption) {
		super();
		this.switchingNode = switchingNode;
		this.otherOption = otherOption;
	}

	public GraphNode getSwitchingNode() {
		return switchingNode;
	}

	public GraphNode getOtherOption() {
		return otherOption;
	}

	@Override
	public String toString() {
		return "SwitchingFrame [switchingNode=" + switchingNode.getId() + ", otherOption=" + otherOption.getId() + "]";
	}

}
