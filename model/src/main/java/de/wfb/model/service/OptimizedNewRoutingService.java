package de.wfb.model.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.GraphNodeEntry;
import de.wfb.model.node.SwitchingNodeEntry;
import de.wfb.rail.service.Route;

/**
 * <pre>
 * 338 - 1366
 *
 * 338 - 1580
 *
 * -- straight ahead only
 * 488 -> 338
 *
 * -- over turnout in non-switching direction
 * 1324 -> 1360
 *
 * -- over turnout in switching direction
 * 1361 -> 1325
 * 1361 -> 1346
 *
 * -- over blocked graph node against driving direction!
 * 1844 -> 1848
 *
 * -- over blocked graph in driving direction!
 * 1849 -> 1845
 *
 * -- durch Schattenbahnhof
 * 474 -> 538
 * 474 -> 1349
 * 474 -> 1617
 * 474 -> 1439
 * 474 -> 3001
 * 474 -> 3104
 * 474 -> 3363
 * 474 -> 3425
 *
 * 764 -> 534
 * 764 -> 556
 * 764 -> 3425
 * </pre>
 */
public class OptimizedNewRoutingService extends BaseRoutingService {

	private static final Logger logger = LogManager.getLogger(OptimizedNewRoutingService.class);

	@Override
	public Route route(final DefaultLocomotive locomotive, final GraphNode graphNodeStart, final GraphNode graphNodeEnd,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks)
			throws IOException, Exception {

		final Route route = new Route();
		route.getGraphNodes().add(graphNodeStart);
		final Stack<SwitchingFrame> switchingNodeStack = new Stack<>();
		final Set<GraphNode> visitedNodes = new HashSet<GraphNode>();

		GraphNode currNode = graphNodeStart;

		while (currNode != graphNodeEnd) {

			currNode = findNextNode(currNode, graphNodeEnd, locomotive, switchingNodeStack, route, visitedNodes);

			if (currNode == null) {
				return new Route();
			}

			route.getGraphNodes().add(currNode);
			visitedNodes.add(currNode);
		}

		return route;
	}

	private GraphNode findNextNode(final GraphNode currNode, final GraphNode graphNodeEnd,
			final DefaultLocomotive locomotive, final Stack<SwitchingFrame> switchingNodeStack, final Route route,
			final Set<GraphNode> visitedNodes) throws Exception {

		GraphNode nextNode = null;

		if (currNode.getChildren().size() == 1) {

			nextNode = currNode.getChildren().get(0);

		} else if (currNode.getChildren().size() == 2) {

			final Set<GraphNodeEntry> set = currNode.getRoutingTable().get(graphNodeEnd.getId());

			if (set == null || set.isEmpty()) {
				throw new Exception("No route found!");
			}

			final Iterator<GraphNodeEntry> iterator = set.iterator();

			GraphNodeEntry smallest = null;
			while (iterator.hasNext()) {

				final GraphNodeEntry graphNodeEntry = iterator.next();

				if (smallest == null) {
					smallest = graphNodeEntry;
				} else {
					if (graphNodeEntry.getDistance() < smallest.getDistance()) {
						smallest = graphNodeEntry;
					}
				}
			}

			nextNode = smallest.getGraphNode();
		}

		return nextNode;
	}

	@Override
	public void buildRoutingTables() {

		// @formatter:off

		// find all nodes that have more than one child = nodes where the algorithm has
		// to make a decision
		final List<GraphNode> switchingNodes = getModelService().getSwitchingNodes();
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

				// walk to the next switching node
				int steps = 1;
				while (CollectionUtils.isNotEmpty(currentGraphNode.getChildren()) && currentGraphNode.getChildren().size() < 2) {

					// prepare a set if none exists, yet
					if (!switchingGraphNode.getRoutingTable().containsKey(currentGraphNode.getId())) {

						final Set<GraphNodeEntry> set = new HashSet<>();
						switchingGraphNode.getRoutingTable().put(currentGraphNode.getId(), set);
					}

					// insert immediate child into the routing table
					switchingGraphNode.getRoutingTable().get(currentGraphNode.getId()).add(new GraphNodeEntry(child, steps));

					currentGraphNode = currentGraphNode.getChildren().get(0);
					steps++;
				}

				// prepare a set if none exists, yet
				if (!switchingGraphNode.getRoutingTable().containsKey(currentGraphNode.getId())) {

					final Set<GraphNodeEntry> set = new HashSet<>();
					switchingGraphNode.getRoutingTable().put(currentGraphNode.getId(), set);
				}

				// insert immediate child into the routing table
				switchingGraphNode.getRoutingTable().get(currentGraphNode.getId()).add(new GraphNodeEntry(child, steps));

				// handle the switching node - add it to the switching node table
				if (CollectionUtils.isNotEmpty(currentGraphNode.getChildren()) && currentGraphNode.getChildren().size() >= 2) {

					// currentGraphNode is a switching node. Connect switching nodes together.
					// remember the node via which this switching child can be reached!
					// This infrastructure is needed for the second phase of the routing table
					// algorithm

					// the entry stores the GraphNode over which the switching node can be reached
					// and the switching node itself
					final SwitchingNodeEntry switchingNodeEntry = new SwitchingNodeEntry();
					switchingNodeEntry.setConnectingGraphNode(child);
					switchingNodeEntry.setSwitchingGraphNode(currentGraphNode);
					switchingNodeEntry.setDistance(steps);

					// set the entry into the source switching node
					switchingGraphNode.getSwitchingGraphNodeChildren().add(switchingNodeEntry);
				}
			}
		}

		// start the iterative learning

		// iterative learning, unless there is no more change
		int i = 1;
		boolean change = true;
		while (change) {

			logger.info("Iteration: " + i);
			i++;

			change = false;

			// over all switching nodes
			for (final GraphNode switchingGraphNode : switchingNodes) {

				final Map<Integer, Set<GraphNodeEntry>> ownRoutingTable = switchingGraphNode.getRoutingTable();

				// learn from the switching children
				for (final SwitchingNodeEntry switchingNodeEntry : switchingGraphNode.getSwitchingGraphNodeChildren()) {

					// over the switching children's routing table
					final Map<Integer, Set<GraphNodeEntry>> childRoutingTable = switchingNodeEntry.getSwitchingGraphNode().getRoutingTable();

					// copy the entries over
					for (final Map.Entry<Integer, Set<GraphNodeEntry>> entry : childRoutingTable.entrySet()) {

						final int nodeId = entry.getKey();


						Set<GraphNodeEntry> ownViaSet = ownRoutingTable.get(nodeId);
						if (ownViaSet == null) {
							ownViaSet = new HashSet<>();
							ownRoutingTable.put(nodeId, ownViaSet);

							change = true;
						}

						final Set<GraphNodeEntry> childViaSet = entry.getValue();

						for (final GraphNodeEntry childGraphNodeEntry : childViaSet) {

							boolean found = false;
							for (final GraphNodeEntry ownGraphNodeEntry : ownViaSet) {

								//if (ownGraphNodeEntry.getGraphNode() == childGraphNodeEntry.getGraphNode()) {
								if (ownGraphNodeEntry.getGraphNode() == switchingNodeEntry.getConnectingGraphNode()) {

									found = true;

									if (childGraphNodeEntry.getDistance() + switchingNodeEntry.getDistance() < ownGraphNodeEntry.getDistance()) {

										change = true;
										ownGraphNodeEntry.setDistance(childGraphNodeEntry.getDistance() + switchingNodeEntry.getDistance());
									}
								}
							}

							if (!found) {
								change = true;
//								ownViaSet.add(new GraphNodeEntry(childGraphNodeEntry.getGraphNode(), childGraphNodeEntry.getDistance() + switchingNodeEntry.getDistance()));
								ownViaSet.add(new GraphNodeEntry(switchingNodeEntry.getConnectingGraphNode(), childGraphNodeEntry.getDistance() + switchingNodeEntry.getDistance()));
							}

						}





//						// in order for the algorithm to stop, check if any new information can be learned
//						if (ownRoutingTable.containsKey(entry.getKey())) {
//
//							final Set<GraphNodeEntry> set = ownRoutingTable.get(entry.getKey());
//
//							if (set.contains(switchingNodeEntry.getConnectingGraphNode())) {
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
					}
				}
			}
		}

		// @formatter:on
	}

}
