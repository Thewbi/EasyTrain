package de.wfb.model.node;

import java.util.ArrayList;
import java.util.List;

public class DefaultEdge implements Edge {

	private Direction direction;

	private GraphNode inGraphNode;

	private final List<GraphNode> outGraphNodes = new ArrayList<>();

	private GraphNode nextOutGraphNode;

	@Override
	public GraphNode getInGraphNode() {
		return inGraphNode;
	}

	@Override
	public void setInGraphNode(final GraphNode inGraphNode) {
		this.inGraphNode = inGraphNode;
	}

//	@Override
//	public GraphNode getOutGraphNode() {
//		return outGraphNode;
//	}
//
//	@Override
//	public void setOutGraphNode(final GraphNode outGraphNode) {
//		this.outGraphNode = outGraphNode;
//	}

	@Override
	public Direction getDirection() {
		return direction;
	}

	@Override
	public void setDirection(final Direction direction) {
		this.direction = direction;
	}

	@Override
	public GraphNode getNextOutGraphNode() {
		return nextOutGraphNode;
	}

	@Override
	public void setNextOutGraphNode(final GraphNode nextOutGraphNode) {
		this.nextOutGraphNode = nextOutGraphNode;
	}

	@Override
	public List<GraphNode> getOutGraphNodes() {
		return outGraphNodes;
	}

}
