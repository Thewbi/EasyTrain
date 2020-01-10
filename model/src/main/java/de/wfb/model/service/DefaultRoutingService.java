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
import de.wfb.model.node.SwitchingNodeEntry;
import de.wfb.model.strategy.GraphColorStrategy;
import de.wfb.rail.events.NodeHighlightedEvent;
import de.wfb.rail.events.RemoveHighlightsEvent;
import de.wfb.rail.ui.ShapeType;

public class DefaultRoutingService implements RoutingService {

	private static final int MAX_ROUTE_FINDING_STEPS = 1400;

	private static final Logger logger = LogManager.getLogger(DefaultRoutingService.class);

	@Autowired
	private ModelService modelService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private GraphColorStrategy graphColorStrategy;

	@Override
	public List<GraphNode> route(final Node start, final Node end) {

		GraphNode graphNodeStart = null;
		GraphNode graphNodeEnd = null;

		// green route
		// @formatter:off
		graphNodeStart = start.getGraphNodeOne().getColor() == Color.GREEN ? start.getGraphNodeOne() : start.getGraphNodeTwo();
		graphNodeEnd = end.getGraphNodeOne().getColor() == Color.GREEN ? end.getGraphNodeOne() : end.getGraphNodeTwo();
		// @formatter:on

		logger.info("GREEN");
		List<GraphNode> result = route(graphNodeStart, graphNodeEnd);

		if (CollectionUtils.isNotEmpty(result)) {
			return result;
		}

		// blue route
		// @formatter:off
		graphNodeStart = start.getGraphNodeOne().getColor() == Color.BLUE ? start.getGraphNodeOne() : start.getGraphNodeTwo();
		graphNodeEnd = end.getGraphNodeOne().getColor() == Color.BLUE ? end.getGraphNodeOne() : end.getGraphNodeTwo();
		// @formatter:on

		logger.trace("BLUE");

		result = route(graphNodeStart, graphNodeEnd);

		return result;
	}

	@Override
	public List<GraphNode> route(final GraphNode graphNodeStart, final GraphNode graphNodeEnd) {

		logger.info("Start: " + graphNodeStart.getId() + " End: " + graphNodeEnd.getId());

		final StringBuffer stringBuffer = new StringBuffer();

		final List<GraphNode> route = new ArrayList<>();

		GraphNode currentNodeGraphNode = graphNodeStart;

		route.add(currentNodeGraphNode);

		int loopBreaker = MAX_ROUTE_FINDING_STEPS;

		// current node is the end node -> done
		while (currentNodeGraphNode.getId() != graphNodeEnd.getId()) {

			if (loopBreaker <= 0) {

				throw new RuntimeException("Failed to find Route from " + graphNodeStart.getId() + " TO "
						+ graphNodeEnd.getId() + " after " + MAX_ROUTE_FINDING_STEPS + " steps!");
			}

			loopBreaker--;

			logger.info("ROUTE: " + currentNodeGraphNode.getId());

			stringBuffer.append(currentNodeGraphNode.getId()).append(", ");

			// current node has only one child -> go to child, current node = child
			if (currentNodeGraphNode.getChildren().size() == 1) {

				logger.info("FROM " + currentNodeGraphNode.getId() + " TO "
						+ currentNodeGraphNode.getChildren().get(0).getId());

				currentNodeGraphNode = currentNodeGraphNode.getChildren().get(0);
				route.add(currentNodeGraphNode);

				continue;
			}

			// current node has more than one child -> look at routing map, go to node that
			// leads to the end node
			currentNodeGraphNode = currentNodeGraphNode.getRoutingTable().get(graphNodeEnd.getId());

			if (currentNodeGraphNode == null) {

				logger.info("No route found!");
				route.clear();

				return new ArrayList<>();

			} else {

				logger.info("Routing Table returns: " + currentNodeGraphNode.getId());

				// TODO: throw exception if the routing table returns a node that is not
				// connected
				// to the current node.

//				for (GraphNode graphNode : getChildren()) {
//
//				}

				route.add(currentNodeGraphNode);
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
		// tables and also insert their switching node children

		for (final GraphNode switchingGraphNode : switchingNodes) {

			// over all children of the switching graph node
			for (final GraphNode child : switchingGraphNode.getChildren()) {

				// walk until the next switching node was found
				GraphNode currentGraphNode = child;
				while (CollectionUtils.isNotEmpty(currentGraphNode.getChildren())
						&& currentGraphNode.getChildren().size() < 2) {

					// insert immediate child into the routing table
					switchingGraphNode.getRoutingTable().put(currentGraphNode.getId(), child);

					currentGraphNode = currentGraphNode.getChildren().get(0);
				}

				// currentGraphNode is a switching node. Connect switching nodes together

				final SwitchingNodeEntry switchingNodeEntry = new SwitchingNodeEntry();
				switchingNodeEntry.setConnectingGraphNode(child);
				switchingNodeEntry.setSwitchingGraphNode(currentGraphNode);

				// TODO: remember the node via this switching child can be reached!
				switchingGraphNode.getSwitchingGraphNodeChildren().add(switchingNodeEntry);
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
				for (final SwitchingNodeEntry switchingNodeEntry : switchingGraphNode.getSwitchingGraphNodeChildren()) {

					// over the switching children's routing table
					for (final Map.Entry<Integer, GraphNode> entry : switchingNodeEntry.getSwitchingGraphNode()
							.getRoutingTable().entrySet()) {

						if (switchingGraphNode.getRoutingTable().containsKey(entry.getKey())) {
							continue;
						}

						// new info learned
						change = true;

						// BUG: the value of the routing table entry has to be the graph node
						// immediately
						// connected to this graph node

						// switchingGraphNode.getRoutingTable().put(entry.getKey(), entry.getValue());
						switchingGraphNode.getRoutingTable().put(entry.getKey(),
								switchingNodeEntry.getConnectingGraphNode());
					}
				}
			}
		}
	}

	@Override
	public void colorGraph() {
		graphColorStrategy.execute();
	}

	@Override
	public void highlightRoute(final List<GraphNode> route) {

		if (CollectionUtils.isEmpty(route)) {
			return;
		}

		// remove all highlights
		final RemoveHighlightsEvent removeHighlightsEvent = new RemoveHighlightsEvent(this);
		applicationEventPublisher.publishEvent(removeHighlightsEvent);

		// highlight each node in the graph
		for (final GraphNode graphNode : route) {

			logger.trace("Highlighting :" + graphNode.getId());

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

				logger.trace("Turnout ShapeType = " + turnoutNode.getShapeType().name());

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
