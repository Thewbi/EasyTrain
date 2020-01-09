package de.wfb.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.node.Color;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.events.NodeHighlightedEvent;
import de.wfb.rail.events.RemoveHighlightsEvent;
import de.wfb.rail.ui.ShapeType;

public class DefaultRoutingService implements RoutingService {

	private static final Logger logger = LogManager.getLogger(DefaultRoutingService.class);

	@Autowired
	private ModelService modelService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public List<GraphNode> route(final Node start, final Node end) {

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

		logger.trace("BLUE");

		return route(graphNodeStart, graphNodeEnd);
	}

	@Override
	public List<GraphNode> route(final GraphNode graphNodeStart, final GraphNode graphNodeEnd) {

		logger.info("Start: " + graphNodeStart.getId() + " End: " + graphNodeEnd.getId());

		final StringBuffer stringBuffer = new StringBuffer();

		final List<GraphNode> route = new ArrayList<>();

		GraphNode currentNode = graphNodeStart;

		route.add(currentNode);

		// current node is the end node -> done
		while (currentNode.getId() != graphNodeEnd.getId()) {

			stringBuffer.append(currentNode.getId()).append(", ");

			// current node has only one child -> go to child, current node = child
			if (currentNode.getChildren().size() == 1) {

				currentNode = currentNode.getChildren().get(0);
				route.add(currentNode);

				continue;
			}

			// current node has more than one child -> look at routing map, go to node that
			// leads to the end node
			currentNode = currentNode.getRoutingTable().get(graphNodeEnd.getId());
			route.add(currentNode);

			if (currentNode == null) {

				logger.info("No route found!");
				route.clear();

				return new ArrayList<>();
			}
		}

		stringBuffer.append(graphNodeEnd.getId());
		route.add(graphNodeEnd);

		logger.info(stringBuffer.toString());

		return route;
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

		modelService.resetGraphColors();
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

	@Override
	public void highlightRoute(final List<GraphNode> route) {

		if (CollectionUtils.isEmpty(route)) {
			return;
		}

		// remove all highlights
		final RemoveHighlightsEvent removeHighlightsEvent = new RemoveHighlightsEvent(this);
		applicationEventPublisher.publishEvent(removeHighlightsEvent);

		for (final GraphNode graphNode : route) {

			final RailNode railNode = graphNode.getRailNode();
			railNode.setHighlighted(true);

			logger.trace("Sending NodeHighlightedEvent!");

			final boolean hightlighted = true;
			final NodeHighlightedEvent nodeHighlightedEvent = new NodeHighlightedEvent(this, modelService.getModel(),
					railNode, railNode.getX(), railNode.getY(), hightlighted);

			applicationEventPublisher.publishEvent(nodeHighlightedEvent);
		}
	}

	@Override
	public void switchTurnouts(final List<GraphNode> route) {

		logger.trace("switchTurnouts()");

		if (CollectionUtils.isEmpty(route)) {
			return;
		}

		// follow the route
		int index = 0;
		for (final GraphNode graphNode : route) {

			if (ShapeType.isTurnout(graphNode.getRailNode().getShapeType())) {

				// if the turnout is NOT traversed in switching direction, continue
				if (graphNode.getChildren().size() < 2) {

					logger.info("Index = " + index + " Turnout found. Not in switching order!");

					index++;
					continue;
				}

				logger.info("Index = " + index + " Turnout found in switching order!");
				final RailNode turnoutNode = graphNode.getRailNode();

				logger.info("Turnout ShapeType = " + turnoutNode.getShapeType().name());

				final int nextIndex = index + 1;
				if (nextIndex < route.size()) {

					final GraphNode nextGraphNode = route.get(nextIndex);

					turnoutNode.switchToGraphNode(applicationEventPublisher, modelService.getModel(), nextGraphNode);
				}
			}

			index++;
		}

	}

}
