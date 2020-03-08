package de.wfb.model.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.GraphNode;
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
public class NewRoutingService extends BaseRoutingService {

	private static final Logger logger = LogManager.getLogger(NewRoutingService.class);

	@Override
	public Route route(final Locomotive locomotive, final GraphNode graphNodeStart, final GraphNode graphNodeEnd,
			final boolean routeOverReservedGraphNodes, final boolean routeOverBlockedFeedbackBlocks)
			throws IOException, Exception {

		final Route route = new Route();
		route.getGraphNodes().add(graphNodeStart);
		final Stack<SwitchingFrame> switchingNodeStack = new Stack<>();
		final Set<GraphNode> visitedNodes = new HashSet<GraphNode>();

		GraphNode currNode = graphNodeStart;

		while (currNode != graphNodeEnd) {

			currNode = findNextNode(currNode, locomotive, switchingNodeStack, route, visitedNodes);

			if (currNode == null) {
				return new Route();
			}

			route.getGraphNodes().add(currNode);
			visitedNodes.add(currNode);
		}

		return route;
	}

	private GraphNode findNextNode(final GraphNode currNode, final Locomotive locomotive,
			final Stack<SwitchingFrame> switchingNodeStack, final Route route, final Set<GraphNode> visitedNodes) {

		GraphNode nextNode = null;

		if (currNode.getChildren().size() == 0) {

			nextNode = bb(currNode, locomotive, switchingNodeStack, route, visitedNodes);

		} else if (currNode.getChildren().size() == 1) {

			nextNode = currNode.getChildren().get(0);
			if (!canTraverseGraphNode(locomotive, nextNode, true, true)) {

				nextNode = bb(currNode, locomotive, switchingNodeStack, route, visitedNodes);
			}

		} else if (currNode.getChildren().size() == 2) {

			final GraphNode gn1 = currNode.getChildren().get(1);
			final GraphNode gn2 = currNode.getChildren().get(0);

			final boolean traverseGN1 = canTraverseGraphNode(locomotive, gn1, true, true);
			final boolean traverseGN2 = canTraverseGraphNode(locomotive, gn2, true, true);

			if (traverseGN1) {

				final SwitchingFrame switchingFrame = new SwitchingFrame(currNode, gn2);
				switchingNodeStack.push(switchingFrame);

				nextNode = gn1;

			} else {

				if (traverseGN2) {

					nextNode = gn2;

				} else {

					nextNode = bb(currNode, locomotive, switchingNodeStack, route, visitedNodes);

				}

			}

		}

		return nextNode;

	}

	private GraphNode bb(final GraphNode currNode, final Locomotive locomotive,
			final Stack<SwitchingFrame> switchingNodeStack, final Route route, final Set<GraphNode> visitedNodes) {

		GraphNode nextNode = null;

		while (nextNode == null && !CollectionUtils.isEmpty(route.getGraphNodes())) {

			nextNode = backtrack(switchingNodeStack, route);
			if (nextNode == null) {
				continue;
			}

			if (!canTraverseGraphNode(locomotive, nextNode, true, true)) {
				nextNode = null;
				continue;
			}

			if (visitedNodes.contains(nextNode)) {
				nextNode = null;
				continue;
			}
		}

		return nextNode;
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
	private GraphNode backtrack(final Stack<SwitchingFrame> switchingNodeStack, final Route route) {

		logger.info("backtrack");

		if (switchingNodeStack.isEmpty()) {

			logger.info("switchingNodeStack is Empty");
			route.getGraphNodes().clear();
			return null;
		}

		final SwitchingFrame topMostSwitchingFrame = switchingNodeStack.pop();

		logger.info("topMostSwitchingFrame: " + topMostSwitchingFrame);

		GraphNode temp = route.getGraphNodes().get(route.getGraphNodes().size() - 1);
		while (temp.getId() != topMostSwitchingFrame.getSwitchingNode().getId()) {

			route.getGraphNodes().remove(route.getGraphNodes().size() - 1);
			temp = route.getGraphNodes().get(route.getGraphNodes().size() - 1);
		}

		logger.info("StackSize: " + switchingNodeStack.size() + " Backtrack is returning GetOtherOption GN-ID: "
				+ topMostSwitchingFrame.getOtherOption().getId());

		return topMostSwitchingFrame.getOtherOption();
	}

	@Override
	public void initialize() {
		// nop
	}

	@Override
	public void removeRoutesAll() {
		// nop
	}

}
