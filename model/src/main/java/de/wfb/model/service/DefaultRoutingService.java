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
import de.wfb.model.node.SwitchingNodeEntry;
import de.wfb.model.strategy.GraphColorStrategy;
import de.wfb.rail.service.Route;

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
	public Route route(final Node start, final Node end) {

		GraphNode graphNodeStart = null;
		GraphNode graphNodeEnd = null;

		// green route
		// @formatter:off
		graphNodeStart = start.getGraphNodeOne().getColor() == Color.GREEN ? start.getGraphNodeOne() : start.getGraphNodeTwo();
		graphNodeEnd = end.getGraphNodeOne().getColor() == Color.GREEN ? end.getGraphNodeOne() : end.getGraphNodeTwo();
		// @formatter:on

		logger.info("GREEN");

		final Route greenRoute = route(graphNodeStart, graphNodeEnd);
		if (CollectionUtils.isNotEmpty(greenRoute.getGraphNodes())) {

			return greenRoute;
		}

		// blue route
		// @formatter:off
		graphNodeStart = start.getGraphNodeOne().getColor() == Color.BLUE ? start.getGraphNodeOne() : start.getGraphNodeTwo();
		graphNodeEnd = end.getGraphNodeOne().getColor() == Color.BLUE ? end.getGraphNodeOne() : end.getGraphNodeTwo();
		// @formatter:on

		logger.trace("BLUE");

		return route(graphNodeStart, graphNodeEnd);
	}

	@Override
	public Route route(final GraphNode graphNodeStart, final GraphNode graphNodeEnd) {

		logger.trace("Start: " + graphNodeStart.getId() + " End: " + graphNodeEnd.getId());

		final StringBuffer stringBuffer = new StringBuffer();

		final List<GraphNode> nodeList = new ArrayList<>();

		GraphNode currentNodeGraphNode = graphNodeStart;

		nodeList.add(currentNodeGraphNode);

		int loopBreaker = MAX_ROUTE_FINDING_STEPS;

		// current node is the end node -> done
		while (currentNodeGraphNode.getId() != graphNodeEnd.getId()) {

			if (loopBreaker <= 0) {

				throw new RuntimeException("Failed to find Route from " + graphNodeStart.getId() + " TO "
						+ graphNodeEnd.getId() + " after " + MAX_ROUTE_FINDING_STEPS + " steps!");
			}

			loopBreaker--;

			logger.trace("ROUTE: " + currentNodeGraphNode.getId());

			stringBuffer.append(currentNodeGraphNode.getId()).append(", ");

			// current node has only one child -> go to child, current node = child
			if (currentNodeGraphNode.getChildren().size() == 1) {

				logger.trace("FROM " + currentNodeGraphNode.getId() + " TO "
						+ currentNodeGraphNode.getChildren().get(0).getId());

				currentNodeGraphNode = currentNodeGraphNode.getChildren().get(0);
				nodeList.add(currentNodeGraphNode);

				continue;
			}

			// current node has more than one child -> look at routing map, go to node that
			// leads to the end node
			currentNodeGraphNode = currentNodeGraphNode.getRoutingTable().get(graphNodeEnd.getId());

			if (currentNodeGraphNode == null) {

				logger.info("No route found!");
				nodeList.clear();

				return new Route();

			} else {

				logger.trace("Routing Table returns: " + currentNodeGraphNode.getId());

				// TODO: throw exception if the routing table returns a node that is not
				// connected
				// to the current node.

//				for (GraphNode graphNode : getChildren()) {
//
//				}

				nodeList.add(currentNodeGraphNode);
			}

		}

		stringBuffer.append(graphNodeEnd.getId());
//		nodeList.add(graphNodeEnd);

		logger.info(stringBuffer.toString());

		final Route route = new Route();
		route.getGraphNodes().addAll(nodeList);

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
	public void highlightRoute(final Route route) {
		route.highlightRoute(applicationEventPublisher);
	}

	@Override
	public void switchTurnouts(final Route route) {
		route.switchTurnouts(applicationEventPublisher);
	}

}
