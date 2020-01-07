package de.wfb.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.node.Color;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;

public class DefaultRoutingService implements RoutingService {

	private static final Logger logger = LogManager.getLogger(DefaultRoutingService.class);

	@Autowired
	private ModelService modelService;

	@Override
	public void route(final Node start, final Node end) {

		GraphNode graphNodeStart = null;
		GraphNode graphNodeEnd = null;

		// green route
		graphNodeStart = start.getGraphNodeOne().getColor() == Color.GREEN ? start.getGraphNodeOne()
				: start.getGraphNodeTwo();
		graphNodeEnd = end.getGraphNodeOne().getColor() == Color.GREEN ? end.getGraphNodeOne() : end.getGraphNodeTwo();

		logger.info("GREEN");
		route(graphNodeStart, graphNodeEnd);

		// blue route
		graphNodeStart = start.getGraphNodeOne().getColor() == Color.BLUE ? start.getGraphNodeOne()
				: start.getGraphNodeTwo();
		graphNodeEnd = end.getGraphNodeOne().getColor() == Color.BLUE ? end.getGraphNodeOne() : end.getGraphNodeTwo();

		logger.info("BLUE");
		route(graphNodeStart, graphNodeEnd);
	}

	@Override
	public void route(final GraphNode graphNodeStart, final GraphNode graphNodeEnd) {

		logger.info("Start: " + graphNodeStart.getId() + " End: " + graphNodeEnd.getId());

		final StringBuffer stringBuffer = new StringBuffer();

		GraphNode currentNode = graphNodeStart;

		// current node is the end node -> done
		while (currentNode.getId() != graphNodeEnd.getId()) {

			stringBuffer.append(currentNode.getId()).append(", ");

			// current node has only one child -> go to child, current node = child
			if (currentNode.getChildren().size() == 1) {

				currentNode = currentNode.getChildren().get(0);
				continue;
			}

			// current node has more than one child -> look at routing map, go to node that
			// leads to the end node
			currentNode = currentNode.getRoutingTable().get(graphNodeEnd.getId());

			if (currentNode == null) {
				logger.info("No route found!");
				return;
			}
		}

		stringBuffer.append(graphNodeEnd.getId());

		logger.info(stringBuffer.toString());
	}

	@Override
	public void buildRoutingTables() {

		// find all nodes that have more than one child = nodes where the algorithm has
		// to make a decision
		final List<GraphNode> switchingNodes = modelService.getSwitchingNodes();
		if (CollectionUtils.isEmpty(switchingNodes)) {
			return;
		}

		// tell every switching node to insert all immediate children into their routing
		// tables

		for (final GraphNode switchingGraphNode : switchingNodes) {

			// over all children of the switching graph node
			for (final GraphNode child : switchingGraphNode.getChildren()) {

				// walk until the next switching node was found
				GraphNode currentGraphNode = child;
				while (currentGraphNode.getChildren().size() < 2) {
					switchingGraphNode.getRoutingTable().put(currentGraphNode.getId(), child);

					currentGraphNode = currentGraphNode.getChildren().get(0);
				}

				// currentGraphNode is a switching node. Connect switching nodes together
				switchingGraphNode.getSwitchingGraphNodeChildren().add(currentGraphNode);
			}
		}

		// start the iterative learning

		// iterating learning, unless there is no more change
		boolean change = true;
		while (change) {

			change = false;

			// over all switching nodes
			for (final GraphNode switchingGraphNode : switchingNodes) {

				// learn from the switching children
				for (final GraphNode switchingChild : switchingGraphNode.getSwitchingGraphNodeChildren()) {

					for (final Map.Entry<Integer, GraphNode> entry : switchingChild.getRoutingTable().entrySet()) {

						if (switchingGraphNode.getRoutingTable().containsKey(entry.getKey())) {
							continue;
						}

						// new info learned
						change = true;

						switchingGraphNode.getRoutingTable().put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
	}

	@Override
	public void colorGraph() {
		final RailNode railNode = modelService.getArbitraryNode();

		walkGraph(railNode.getGraphNodeOne(), Color.GREEN);
		walkGraph(railNode.getGraphNodeTwo(), Color.BLUE);
	}

	private void walkGraph(final GraphNode graphNode, final Color color) {

		final List<GraphNode> workingList = new ArrayList<>();
		final List<GraphNode> visitedList = new ArrayList<>();
		workingList.add(graphNode);

		while (CollectionUtils.isNotEmpty(workingList)) {

			final GraphNode currentGraphNode = workingList.get(0);
			workingList.remove(currentGraphNode);
			visitedList.add(currentGraphNode);

			if (currentGraphNode.getColor() != Color.NONE) {
				throw new IllegalArgumentException("GraphNode " + currentGraphNode.getId()
						+ " has a color already! COLOR = " + currentGraphNode.getColor().name());
			}

			currentGraphNode.setColor(color);

			for (final GraphNode node : currentGraphNode.getChildren()) {

				if (visitedList.contains(node)) {
					continue;
				}
				workingList.add(node);
			}
		}
	}

}
