package de.wfb.model.service;

import java.util.List;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.strategy.GraphColorStrategy;
import de.wfb.rail.service.Route;

public class DefaultRoutingService extends BaseRoutingService {

	private static final int MAX_ROUTE_FINDING_STEPS = 5000000;
	// private static final int MAX_ROUTE_FINDING_STEPS = -1;

	// private static final boolean DEBUG_ROUTING = true;
	private static final boolean DEBUG_ROUTING = false;

	private static final Logger logger = LogManager.getLogger(DefaultRoutingService.class);

	@Autowired
	private GraphColorStrategy graphColorStrategy;

	@Override
	public Route route(final DefaultLocomotive locomotive, final Node start, final Node end,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks) throws Exception {

		GraphNode graphNodeStart = start.getGraphNodeOne();
		GraphNode graphNodeEnd = end.getGraphNodeOne();

		logger.info("Routing from GN-ID: " + graphNodeStart.getId() + " -> GN-ID: " + graphNodeEnd.getId());

		Route route = route(locomotive, graphNodeStart, graphNodeEnd, routeOverReservedGraphNodes,
				routeOverBlockedFeedbackBlocks);

		if (route.isNotEmpty()) {
			return route;
		}

		graphNodeStart = start.getGraphNodeOne();
		graphNodeEnd = end.getGraphNodeTwo();

		logger.info("Routing from GN-ID: " + graphNodeStart.getId() + " -> GN-ID: " + graphNodeEnd.getId());

		route = route(locomotive, graphNodeStart, graphNodeEnd, routeOverReservedGraphNodes,
				routeOverBlockedFeedbackBlocks);

		if (route.isNotEmpty()) {
			return route;
		}

		graphNodeStart = start.getGraphNodeTwo();
		graphNodeEnd = end.getGraphNodeOne();

		logger.info("Routing from GN-ID: " + graphNodeStart.getId() + " -> GN-ID: " + graphNodeEnd.getId());

		route = route(locomotive, graphNodeStart, graphNodeEnd, routeOverReservedGraphNodes,
				routeOverBlockedFeedbackBlocks);

		if (route.isNotEmpty()) {
			return route;
		}

		graphNodeStart = start.getGraphNodeTwo();
		graphNodeEnd = end.getGraphNodeTwo();

		logger.info("Routing from GN-ID: " + graphNodeStart.getId() + " -> GN-ID: " + graphNodeEnd.getId());

		route = route(locomotive, graphNodeStart, graphNodeEnd, routeOverReservedGraphNodes,
				routeOverBlockedFeedbackBlocks);

		if (route.isNotEmpty()) {
			return route;
		}

		return new Route();

//		// green route
//
//		// @formatter:off
//		graphNodeStart = start.getGraphNodeOne().getColor() == Color.GREEN ? start.getGraphNodeOne() : start.getGraphNodeTwo();
//		graphNodeEnd = end.getGraphNodeOne().getColor() == Color.GREEN ? end.getGraphNodeOne() : end.getGraphNodeTwo();
//		// @formatter:on
//
//		logger.trace("GREEN");
//
//		final Route greenRoute = route(locomotive, graphNodeStart, graphNodeEnd, routeOverReservedGraphNodes,
//				routeOverBlockedFeedbackBlocks);
//		if (CollectionUtils.isNotEmpty(greenRoute.getGraphNodes())) {
//			return greenRoute;
//		}
//
//		// blue route
//
//		// @formatter:off
//		graphNodeStart = start.getGraphNodeOne().getColor() == Color.BLUE ? start.getGraphNodeOne() : start.getGraphNodeTwo();
//		graphNodeEnd = end.getGraphNodeOne().getColor() == Color.BLUE ? end.getGraphNodeOne() : end.getGraphNodeTwo();
//		// @formatter:on
//
//		logger.trace("BLUE");

	}

	@Override
	public Route route(final DefaultLocomotive locomotive, final GraphNode graphNodeStart, final GraphNode graphNodeEnd,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks) throws Exception {

		return null;

//		final StringBuffer stringBuffer = new StringBuffer();
//
//		if (DEBUG_ROUTING) {
//
//			final String startMsg = "Route finding GN Start ID: " + graphNodeStart.getId() + " - GN End ID: "
//					+ graphNodeEnd.getId();
//			stringBuffer.append(startMsg).append("\n");
//			logger.info(startMsg);
//		}
//
//		final List<GraphNode> nodeList = new ArrayList<>();
//
//		final Stack<SwitchingFrame> switchingNodeStack = new Stack<>();
//
//		GraphNode currentNodeGraphNode = graphNodeStart;
//
//		if (DEBUG_ROUTING) {
//			if (nodeList.contains(currentNodeGraphNode)) {
//				stringBuffer.append("A - Node contained already! GN ID: " + currentNodeGraphNode.getId() + "\n");
//			}
//		}
//
//		nodeList.add(currentNodeGraphNode);
//
//		int loopBreaker = MAX_ROUTE_FINDING_STEPS;
//
//		// current node is the end node -> done
//		while (currentNodeGraphNode.getId() != graphNodeEnd.getId()) {
//
//			// prevent endless loops
//			loopBreaker--;
//			if (loopBreaker <= 0) {
//
//				final String msg = "Failed to find Route from " + graphNodeStart.getId() + " TO " + graphNodeEnd.getId()
//						+ " after " + MAX_ROUTE_FINDING_STEPS + " steps!";
//
//				if (DEBUG_ROUTING) {
//					stringBuffer.append(msg).append("\n");
//				}
//
//				logger.error(msg);
//
//				FileUtils.writeStringToFile(new File("routing_log.txt"), stringBuffer.toString(), "UTF-8");
//
//				throw new Exception(msg);
//			}
//
//			// current node has only one child -> go to that child, current node = child
//			if (currentNodeGraphNode.getChildren().size() == 1) {
//
//				final GraphNode oldNode = currentNodeGraphNode;
//
//				final GraphNode child = currentNodeGraphNode.getChildren().get(0);
//
//				currentNodeGraphNode = goToNode(locomotive, child, routeOverReservedGraphNodes,
//						routeOverBlockedFeedbackBlocks, switchingNodeStack, nodeList, stringBuffer);
//				if (currentNodeGraphNode == null) {
//
//					logger.warn("No route found!");
//
//					currentNodeGraphNode = backtrack(switchingNodeStack, nodeList);
//					logger.info("A BACKTRACK Returns: " + currentNodeGraphNode.getId());
//				}
//
//				if (DEBUG_ROUTING) {
//
//					final String temp = oldNode.getId() + " -> " + currentNodeGraphNode.getId();
//					logger.info(temp);
//
//					stringBuffer.append(temp).append("\n");
//				}
//
//				continue;
//			}
//
//			final Set<GraphNode> viaSet = currentNodeGraphNode.getRoutingTable().get(graphNodeEnd.getId());
//
////			if (currentNodeGraphNode.getId() == 3304) {
////				logger.info(graphNodeEnd.getId() + " viaSet: " + viaSet);
////			}
//
//			if (CollectionUtils.isEmpty(viaSet)) {
//
//				logger.info("Backtrack reason: Empty via set!");
//				currentNodeGraphNode = backtrack(switchingNodeStack, nodeList);
//
//				if (currentNodeGraphNode == null) {
//
//					if (DEBUG_ROUTING) {
//						stringBuffer.append("No route found!").append("\n");
//					}
//
//					logger.warn("No route found!");
//					nodeList.clear();
//
//					if (DEBUG_ROUTING) {
//						FileUtils.writeStringToFile(new File("routing_log.txt"), stringBuffer.toString(), "UTF-8");
//					}
//
//					return new Route();
//				}
//
//				logger.info("B BACKTRACK Returns: " + currentNodeGraphNode.getId());
//
//				if (nodeList.contains(currentNodeGraphNode)) {
//					stringBuffer.append("B - Node contained already! GN ID: " + currentNodeGraphNode.getId() + "\n");
//				}
//				nodeList.add(currentNodeGraphNode);
//
//				continue;
//			}
//
//			if (viaSet.size() == 1) {
//
//				final GraphNode oldNode = currentNodeGraphNode;
//
//				// go to that one via node
//				currentNodeGraphNode = goToNode(locomotive, viaSet.iterator().next(), routeOverReservedGraphNodes,
//						routeOverBlockedFeedbackBlocks, switchingNodeStack, nodeList, stringBuffer);
//				if (currentNodeGraphNode == null) {
//
//					stringBuffer.append("No route found!").append("\n");
//					logger.warn("No route found!");
//					nodeList.clear();
//
//					FileUtils.writeStringToFile(new File("routing_log.txt"), stringBuffer.toString(), "UTF-8");
//
//					return new Route();
//				}
//
//				if (DEBUG_ROUTING) {
//
//					final String temp = oldNode.getId() + " -> " + currentNodeGraphNode.getId();
//
//					stringBuffer.append(temp).append("\n");
//					logger.info(temp);
//				}
//
//				continue;
//			}
//
//			// current node has more than one child -> look at routing map, go to node that
//			// leads to the end node
//
//			final Iterator<GraphNode> iterator = viaSet.iterator();
//			final GraphNode firstOption = iterator.next();
//			final GraphNode secondOption = iterator.next();
//
//			// save second option in a SwitchingFrame
//			final SwitchingFrame switchingFrame = new SwitchingFrame(currentNodeGraphNode, secondOption);
//			logger.info("Adding switchingFrame: " + switchingFrame);
//			switchingNodeStack.push(switchingFrame);
//
//			if (DEBUG_ROUTING) {
//
//				final String temp = currentNodeGraphNode.getId() + " -> " + firstOption.getId();
//				stringBuffer.append(temp).append("\n");
//				logger.info(temp);
//			}
//
//			// try the first option
//			currentNodeGraphNode = firstOption;
//			if (nodeList.contains(firstOption)) {
//
//				stringBuffer.append("C - Node contained already! GN ID: " + currentNodeGraphNode.getId() + "\n");
//
//				logger.info("backtrack reason: already contained!");
//
//				currentNodeGraphNode = backtrack(switchingNodeStack, nodeList);
//				logger.info("C BACKTRACK Returns: " + currentNodeGraphNode.getId());
//
//				logger.info("backtrack reason: already contained!");
//
//				if (currentNodeGraphNode == null) {
//					return new Route();
//				}
//				continue;
//
//			} else {
//				nodeList.add(firstOption);
////				switchingNodeStack.push(switchingFrame);
//			}
//
//		}
//
//		FileUtils.writeStringToFile(new File("routing_log.txt"), stringBuffer.toString(), "UTF-8");
//
//		return new Route(nodeList);
	}

	private GraphNode goToNode(final DefaultLocomotive locomotive, final GraphNode node,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks,
			final Stack<SwitchingFrame> switchingNodeStack, final List<GraphNode> nodeList,
			final StringBuffer stringBuffer) {

		GraphNode tempNode = node;

		if (nodeList.contains(tempNode)) {

//			if (DEBUG_ROUTING) {
//				stringBuffer.append("D - Node contained already! GN ID: " + currentNodeGraphNode.getId() + "\n");
//			}
			tempNode = backtrack(switchingNodeStack, nodeList);
			logger.info("D BACKTRACK Returns: " + tempNode.getId());
		}

		// if the node cannot be traversed, backtrack to the next option
		if (!canTraverseGraphNode(locomotive, tempNode, routeOverReservedGraphNodes, routeOverBlockedFeedbackBlocks)) {

			if (DEBUG_ROUTING) {
				stringBuffer.append("backtrack");
			}

			final GraphNode currentNodeGraphNode = backtrack(switchingNodeStack, nodeList);
			if (currentNodeGraphNode == null) {
				return null;
			}

			logger.info("D BACKTRACK Returns: " + currentNodeGraphNode.getId());

			if (nodeList.contains(currentNodeGraphNode)) {

				if (DEBUG_ROUTING) {
					stringBuffer.append("D - Node contained already! GN ID: " + currentNodeGraphNode.getId() + "\n");
				}
//				throw new RuntimeException("Node contained already!");

				return null;
			}

			nodeList.add(currentNodeGraphNode);

			return currentNodeGraphNode;
		}

//		if (DEBUG_ROUTING) {
//			logger.info(currentNodeGraphNode.getId() + " -> " + child.getId());
//		}

//		currentNodeGraphNode = child;
		nodeList.add(node);

		return node;
	}

	/**
	 * Removes graphnodes from the nodelist until the graphnode is found that is the
	 * switching node of the topmost stack element.<br />
	 * <br />
	 *
	 * It then removes this topmost stack element and return the second option of
	 * that stack element<br />
	 * <br />
	 *
	 * If the stack is empty, there is no route<br />
	 * <br />
	 *
	 * @param nodeList
	 * @param switchingNodeStack
	 *
	 * @return
	 */
	private GraphNode backtrack(final Stack<SwitchingFrame> switchingNodeStack, final List<GraphNode> nodeList) {

		logger.info("backtrack");

		if (switchingNodeStack.isEmpty()) {

			logger.info("switchingNodeStack is Empty");
			return null;
		}

		final SwitchingFrame topMostSwitchingFrame = switchingNodeStack.pop();

		logger.info("topMostSwitchingFrame: " + topMostSwitchingFrame);

		GraphNode temp = nodeList.get(nodeList.size() - 1);
		while (temp.getId() != topMostSwitchingFrame.getSwitchingNode().getId()) {

			nodeList.remove(nodeList.size() - 1);
			temp = nodeList.get(nodeList.size() - 1);
		}

		logger.info("StackSize: " + switchingNodeStack.size() + " Backtrack is returning GetOtherOption GN-ID: "
				+ topMostSwitchingFrame.getOtherOption().getId());

		return topMostSwitchingFrame.getOtherOption();
	}

	@Override
	public void buildRoutingTables() {

//		// find all nodes that have more than one child = nodes where the algorithm has
//		// to make a decision
//		final List<GraphNode> switchingNodes = getModelService().getSwitchingNodes();
//		if (CollectionUtils.isEmpty(switchingNodes)) {
//			return;
//		}
//
//		// tell every switching node to insert all immediate children into their routing
//		// tables and also insert their switching node children
//
//		for (final GraphNode switchingGraphNode : switchingNodes) {
//
//			// over all children of the switching graph node
//			for (final GraphNode child : switchingGraphNode.getChildren()) {
//
//				// walk until the next switching node was found
//				GraphNode currentGraphNode = child;
//
//				// walk to the next switching node
//				while (CollectionUtils.isNotEmpty(currentGraphNode.getChildren())
//						&& currentGraphNode.getChildren().size() < 2) {
//
//					// prepare a set if none exists, yet
//					if (!switchingGraphNode.getRoutingTable().containsKey(currentGraphNode.getId())) {
//						final Set<GraphNodeEntry> set = new HashSet<GraphNodeEntry>();
//						switchingGraphNode.getRoutingTable().put(currentGraphNode.getId(), set);
//					}
//
//					// insert immediate child into the routing table
//					switchingGraphNode.getRoutingTable().get(currentGraphNode.getId()).add(child);
//
//					currentGraphNode = currentGraphNode.getChildren().get(0);
//				}
//
//				// handle the switching node
//
//				// prepare a set if none exists, yet
//				if (!switchingGraphNode.getRoutingTable().containsKey(currentGraphNode.getId())) {
//					switchingGraphNode.getRoutingTable().put(currentGraphNode.getId(), new HashSet<GraphNode>());
//				}
//
//				// insert immediate child into the routing table
//				switchingGraphNode.getRoutingTable().get(currentGraphNode.getId()).add(child);
//
//				if (CollectionUtils.isNotEmpty(currentGraphNode.getChildren())
//						&& currentGraphNode.getChildren().size() >= 2) {
//
//					// currentGraphNode is a switching node. Connect switching nodes together.
//					// remember the node via which this switching child can be reached!
//					// This infrastructure is needed for the second phase of the routing table
//					// algorithm
//
//					// the entry stores the GraphNode over which the switching node can be reached
//					// and the switching node itself
//					final SwitchingNodeEntry switchingNodeEntry = new SwitchingNodeEntry();
//					switchingNodeEntry.setConnectingGraphNode(child);
//					switchingNodeEntry.setSwitchingGraphNode(currentGraphNode);
//
//					// set the entry into the source switching node
//					switchingGraphNode.getSwitchingGraphNodeChildren().add(switchingNodeEntry);
//				}
//			}
//		}
//
//		// start the iterative learning
//
//		// iterative learning, unless there is no more change
//		int i = 1;
//		boolean change = true;
//		while (change) {
//
//			logger.info("Iteration: " + i);
//			i++;
//
//			change = false;
//
//			// over all switching nodes
//			for (final GraphNode switchingGraphNode : switchingNodes) {
//
//				// System.out.println("GN-ID: " + switchingGraphNode.getId());
//
//				final Map<Integer, Set<GraphNode>> ownRoutingTable = switchingGraphNode.getRoutingTable();
//
//				// learn from the switching children
//				for (final SwitchingNodeEntry switchingNodeEntry : switchingGraphNode.getSwitchingGraphNodeChildren()) {
//
//					// over the switching children's routing table
//					final Map<Integer, Set<GraphNode>> childRoutingTable = switchingNodeEntry.getSwitchingGraphNode()
//							.getRoutingTable();
//
//					// copy the entries over
//					for (final Map.Entry<Integer, Set<GraphNode>> entry : childRoutingTable.entrySet()) {
//
//						// in order for the algorithm to stop, check if any new information can be
//						// learned
//						if (ownRoutingTable.containsKey(entry.getKey())) {
//
//							final Set<GraphNode> set = ownRoutingTable.get(entry.getKey());
//
//							if (set.contains(switchingNodeEntry.getConnectingGraphNode())) {
//								// if (set.containsAll(entry.getValue())) {
//								continue;
//							}
//						}
//
//						// new info learned
//						change = true;
//
//						// prepare a set if none exists, yet
//						if (!ownRoutingTable.containsKey(entry.getKey())) {
//							ownRoutingTable.put(entry.getKey(), new HashSet<GraphNode>());
//						}
//
//						final Set<GraphNode> set = ownRoutingTable.get(entry.getKey());
//
//						set.add(switchingNodeEntry.getConnectingGraphNode());
//
////						final Iterator<GraphNode> iterator = entry.getValue().iterator();
////						while (iterator.hasNext()) {
////							set.add(iterator.next());
////						}
//					}
//				}
//			}
//		}
	}

//	@Override
//	public void colorGraph() {
//		graphColorStrategy.execute();
//	}

}
