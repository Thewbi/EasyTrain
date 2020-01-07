package de.wfb.dot;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import de.wfb.model.node.GraphNode;

public class DefaultDotSerializer {

	private final Set<GraphNode> visitedSet = new HashSet<>();

	public void serialize(final GraphNode graphNode) {

		final StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append("digraph G {\n");

		serialize(stringBuffer, graphNode);

		stringBuffer.append("}");

		System.out.println(stringBuffer.toString());

	}

	private void serialize(final StringBuffer stringBuffer, final GraphNode graphNode) {

		if (visitedSet.contains(graphNode)) {
			return;
		}

		visitedSet.add(graphNode);

		if (CollectionUtils.isEmpty(graphNode.getChildren())) {
			return;
		}

		for (final GraphNode child : graphNode.getChildren()) {

			stringBuffer.append(graphNode.getId()).append(" -> ").append(child.getId()).append("\n");
		}

		for (final GraphNode child : graphNode.getChildren()) {

			serialize(stringBuffer, child);
		}

	}

}
