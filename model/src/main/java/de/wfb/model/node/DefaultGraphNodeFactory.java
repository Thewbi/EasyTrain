package de.wfb.model.node;

import de.wfb.rail.factory.Factory;

public class DefaultGraphNodeFactory implements Factory<GraphNode> {

	private int graphNodeId = 0;

	@Override
	public GraphNode create(final Object... args) throws Exception {

		final RailNode railNode = (RailNode) args[0];

		final GraphNode result = new GraphNode();
		result.setId(graphNodeId);
		result.setRailNode(railNode);
		result.setX(railNode.getX());
		result.setY(railNode.getY());

		graphNodeId++;

		return result;
	}

}
