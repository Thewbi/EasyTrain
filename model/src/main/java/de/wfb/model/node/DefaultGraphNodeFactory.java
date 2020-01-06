package de.wfb.model.node;

import de.wfb.rail.factory.Factory;

public class DefaultGraphNodeFactory implements Factory<GraphNode> {

	private int graphNodeId = 0;

	@Override
	public GraphNode create(final Object... args) throws Exception {

		final GraphNode result = new GraphNode();
		result.setId(graphNodeId);

		graphNodeId++;

		return result;
	}

}
