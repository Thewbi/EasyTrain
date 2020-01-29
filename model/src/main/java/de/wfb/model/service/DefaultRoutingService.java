package de.wfb.model.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

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
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.service.Route;

public class DefaultRoutingService implements RoutingService {

	private static final int MAX_ROUTE_FINDING_STEPS = 5000;

	private static final Logger logger = LogManager.getLogger(DefaultRoutingService.class);

	@Autowired
	private ModelService modelService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private GraphColorStrategy graphColorStrategy;

	@Autowired
	private ProtocolFacade protocolFacade;

	@Override
	public Route route(final Node start, final Node end, final boolean routeOverReservedGraphNodes) {

		GraphNode graphNodeStart = null;
		GraphNode graphNodeEnd = null;

		// green route
		// @formatter:off
		graphNodeStart = start.getGraphNodeOne().getColor() == Color.GREEN ? start.getGraphNodeOne() : start.getGraphNodeTwo();
		graphNodeEnd = end.getGraphNodeOne().getColor() == Color.GREEN ? end.getGraphNodeOne() : end.getGraphNodeTwo();
		// @formatter:on

		logger.trace("GREEN");

		final Route greenRoute = route(graphNodeStart, graphNodeEnd, routeOverReservedGraphNodes);
		if (CollectionUtils.isNotEmpty(greenRoute.getGraphNodes())) {

			return greenRoute;
		}

		// blue route
		// @formatter:off
		graphNodeStart = start.getGraphNodeOne().getColor() == Color.BLUE ? start.getGraphNodeOne() : start.getGraphNodeTwo();
		graphNodeEnd = end.getGraphNodeOne().getColor() == Color.BLUE ? end.getGraphNodeOne() : end.getGraphNodeTwo();
		// @formatter:on

		logger.trace("BLUE");

		return route(graphNodeStart, graphNodeEnd, routeOverReservedGraphNodes);
	}

	@Override
	public Route route(final GraphNode graphNodeStart, final GraphNode graphNodeEnd,
			final boolean routeOverReservedGraphNodes) {

		final List<GraphNode> nodeList = new ArrayList<>();

		final Stack<SwitchingFrame> switchingNodeStack = new Stack<>();

		GraphNode currentNodeGraphNode = graphNodeStart;

		nodeList.add(currentNodeGraphNode);

		int loopBreaker = MAX_ROUTE_FINDING_STEPS;

		// current node is the end node -> done
		while (currentNodeGraphNode.getId() != graphNodeEnd.getId()) {

			// prevent endless loops
			loopBreaker--;
			if (loopBreaker <= 0) {

				throw new RuntimeException("Failed to find Route from " + graphNodeStart.getId() + " TO "
						+ graphNodeEnd.getId() + " after " + MAX_ROUTE_FINDING_STEPS + " steps!");
			}

			// current node has only one child -> go to that child, current node = child
			if (currentNodeGraphNode.getChildren().size() == 1) {

				final GraphNode child = currentNodeGraphNode.getChildren().get(0);

				// if the node cannot be traversed, backtrack to the next option
				if (!canTraverseGraphNode(child, routeOverReservedGraphNodes)) {

					currentNodeGraphNode = backtrack(switchingNodeStack, nodeList);

					if (currentNodeGraphNode == null) {
						logger.warn("No route found!");
						nodeList.clear();
						return new Route();
					}

					nodeList.add(currentNodeGraphNode);

					continue;
				}

				logger.trace("FROM " + currentNodeGraphNode.getId() + " TO " + child.getId());

				currentNodeGraphNode = child;
				nodeList.add(currentNodeGraphNode);

				continue;
			}

			// current node has more than one child -> look at routing map, go to node that
			// leads to the end node
			final Set<GraphNode> viaSet = currentNodeGraphNode.getRoutingTable().get(graphNodeEnd.getId());

			if (CollectionUtils.isEmpty(viaSet) || viaSet.size() != 2) {

				currentNodeGraphNode = backtrack(switchingNodeStack, nodeList);

				if (currentNodeGraphNode == null) {
					logger.warn("No route found!");
					nodeList.clear();
					return new Route();
				}

				nodeList.add(currentNodeGraphNode);

				continue;
			}

			final Iterator<GraphNode> iterator = viaSet.iterator();
			final GraphNode firstOption = iterator.next();

			final GraphNode secondOption = iterator.next();
			final SwitchingFrame switchingFrame = new SwitchingFrame(currentNodeGraphNode, secondOption);

			currentNodeGraphNode = firstOption;
			nodeList.add(firstOption);

			switchingNodeStack.push(switchingFrame);
		}

		return new Route(nodeList);
	}

	/**
	 * Removes graphnodes from the nodelist until the graphnode is found that is the
	 * switching node of the topmost stack element.
	 *
	 * It then removes this topmost stack element and return the second option of
	 * that stack element
	 *
	 * If the stack is empty, there is no route
	 *
	 * @param nodeList
	 * @param switchingNodeStack
	 *
	 * @return
	 */
	private GraphNode backtrack(final Stack<SwitchingFrame> switchingNodeStack, final List<GraphNode> nodeList) {

		if (switchingNodeStack.isEmpty()) {
			return null;
		}

		final SwitchingFrame topMostSwitchingFrame = switchingNodeStack.pop();

		GraphNode temp = nodeList.get(nodeList.size() - 1);
		do {
			nodeList.remove(nodeList.size() - 1);

			temp = nodeList.get(nodeList.size() - 1);
		} while (temp.getId() != topMostSwitchingFrame.getSwitchingNode().getId());

		return topMostSwitchingFrame.getOtherOption();
	}

	private boolean canTraverseGraphNode(final GraphNode graphNode, final boolean routeOverReservedGraphNodes) {

		// build routes over reserved graphnodes or not
		if (graphNode.getRailNode().isReserved() && !routeOverReservedGraphNodes) {
			return false;
		}

		// blocking a graph node, means to make a rail node non-traversable in this
		// direction
		if (graphNode.isBlocked()) {
			return false;
		}

		return true;
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

				// loop up to the next switch
				while (CollectionUtils.isNotEmpty(currentGraphNode.getChildren())
						&& currentGraphNode.getChildren().size() < 2) {

					// prepare a set if none exists, yet
					if (!switchingGraphNode.getRoutingTable().containsKey(currentGraphNode.getId())) {
						switchingGraphNode.getRoutingTable().put(currentGraphNode.getId(), new HashSet<GraphNode>());
					}

					// insert immediate child into the routing table
					switchingGraphNode.getRoutingTable().get(currentGraphNode.getId()).add(child);

					currentGraphNode = currentGraphNode.getChildren().get(0);
				}

				// prepare a set if none exists, yet
				if (!switchingGraphNode.getRoutingTable().containsKey(currentGraphNode.getId())) {
					switchingGraphNode.getRoutingTable().put(currentGraphNode.getId(), new HashSet<GraphNode>());
				}

				// insert immediate child into the routing table
				switchingGraphNode.getRoutingTable().get(currentGraphNode.getId()).add(child);

				if (CollectionUtils.isNotEmpty(currentGraphNode.getChildren())
						&& currentGraphNode.getChildren().size() >= 2) {

					// currentGraphNode is a switching node. Connect switching nodes together.
					// remember the node via which this switching child can be reached!
					// This infrastructure is needed for the second phase of the routing table
					// algorithm

					// the entry stores the GraphNode over which the switching node can be reached
					// and the switching node itself
					final SwitchingNodeEntry switchingNodeEntry = new SwitchingNodeEntry();
					switchingNodeEntry.setConnectingGraphNode(child);
					switchingNodeEntry.setSwitchingGraphNode(currentGraphNode);

					// set the entry into the source switching node
					switchingGraphNode.getSwitchingGraphNodeChildren().add(switchingNodeEntry);
				}
			}
		}

		// start the iterative learning

		// iterative learning, unless there is no more change
		boolean change = true;
		while (change) {

			change = false;

			// over all switching nodes
			for (final GraphNode switchingGraphNode : switchingNodes) {

				// learn from the switching children
				for (final SwitchingNodeEntry switchingNodeEntry : switchingGraphNode.getSwitchingGraphNodeChildren()) {

					// over the switching children's routing table
					final Set<Entry<Integer, Set<GraphNode>>> entrySet = switchingNodeEntry.getSwitchingGraphNode()
							.getRoutingTable().entrySet();

					for (final Map.Entry<Integer, Set<GraphNode>> entry : entrySet) {

						// in order for the algorithm to stop, check if any new information can be
						// learned or not
						if (switchingGraphNode.getRoutingTable().containsKey(entry.getKey())) {

							final Set<GraphNode> set = switchingGraphNode.getRoutingTable().get(entry.getKey());

							if (set.contains(switchingNodeEntry.getConnectingGraphNode())) {
								continue;
							}
						}

						// new info learned
						change = true;

						// prepare a set if none exists, yet
						if (!switchingGraphNode.getRoutingTable().containsKey(entry.getKey())) {
							switchingGraphNode.getRoutingTable().put(entry.getKey(), new HashSet<GraphNode>());
						}

						final Set<GraphNode> set = switchingGraphNode.getRoutingTable().get(entry.getKey());
						set.add(switchingNodeEntry.getConnectingGraphNode());
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
		route.switchTurnouts(applicationEventPublisher, protocolFacade);
	}

	/**
	 * For testing
	 *
	 * @param modelService
	 */
	public void setModelService(final ModelService modelService) {
		this.modelService = modelService;
	}

}
