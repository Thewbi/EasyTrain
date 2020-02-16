package de.wfb.rail.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;

import de.wfb.model.Model;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.rail.events.NodeHighlightedEvent;
import de.wfb.rail.events.RemoveHighlightsEvent;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.ui.ShapeType;

public class Route {

	private static final Logger logger = LogManager.getLogger(Route.class);

	private int locomotiveId;

	private DefaultLocomotive locomotive;

	private final List<GraphNode> graphNodes = new ArrayList<>();

	public Route() {

	}

	public Route(final List<GraphNode> nodeList) {
		graphNodes.addAll(nodeList);
	}

	@Override
	public String toString() {

		if (CollectionUtils.isEmpty(graphNodes)) {
			return "Route is empty";
		}

		final StringBuffer stringBuffer = new StringBuffer();
//		stringBuffer.append("Route:");

		for (final GraphNode graphNode : graphNodes) {

			logger.trace(graphNode);

			if (stringBuffer.length() != 0) {

				stringBuffer.append(" -> ");
			}

			stringBuffer.append(graphNode.getId());

			final RailNode railNode = graphNode.getRailNode();
			if (railNode != null) {

				final Block block = railNode.getBlock();
				if (block != null) {

					stringBuffer.append("-B[").append(block.getId()).append("]");
				}

				if (railNode.getReservedLocomotiveId() != -1) {
					stringBuffer.append("-R[").append(railNode.getReservedLocomotiveId()).append("]");
				}
			}

		}

		return "Route: " + stringBuffer.toString();
	}

	public void highlightRoute(final ApplicationEventPublisher applicationEventPublisher) {

		logger.trace("highlighting route");

		if (CollectionUtils.isEmpty(graphNodes)) {
			return;
		}

		// remove all highlights
		final RemoveHighlightsEvent removeHighlightsEvent = new RemoveHighlightsEvent(this);
		applicationEventPublisher.publishEvent(removeHighlightsEvent);

		// highlight each node in the graph
		for (final GraphNode graphNode : graphNodes) {

			logger.trace("Highlighting :" + graphNode.getId());

			final RailNode railNode = graphNode.getRailNode();
			railNode.setHighlighted(true);

			logger.trace("Sending NodeHighlightedEvent!");

			final boolean hightlighted = true;
			final NodeHighlightedEvent nodeHighlightedEvent = new NodeHighlightedEvent(this, null, railNode,
					railNode.getX(), railNode.getY(), hightlighted);

			applicationEventPublisher.publishEvent(nodeHighlightedEvent);
		}
	}

	public void switchTurnouts(final ApplicationEventPublisher applicationEventPublisher,
			final ProtocolFacade protocolFacade) {

		logger.info("switchTurnouts()");

		try {
			switchTurnouts(graphNodes, applicationEventPublisher, protocolFacade);
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void switchTurnouts(final List<GraphNode> graphNodes,
			final ApplicationEventPublisher applicationEventPublisher, final ProtocolFacade protocolFacade) {

		logger.info("switchTurnouts() - static");

		if (CollectionUtils.isEmpty(graphNodes)) {

			logger.info("switchTurnouts() no nodes!");
			return;
		}

		// follow the route, find all turnouts, switch them
		int index = 0;
		for (final GraphNode graphNode : graphNodes) {

			logger.trace("Index: " + index + " RailNode.ID: " + graphNode.getRailNode().getId() + " GraphNode.ID: "
					+ graphNode.getId() + " ProcotolAddressId: " + graphNode.getRailNode().getProtocolTurnoutId());

			// ignore all nodes that are no turnouts
			if (ShapeType.isNotTurnout(graphNode.getRailNode().getShapeType())) {

				logger.trace("Not a Turnout ShapeType!");

				index++;
				continue;
			}

			// if the turnout is NOT traversed in switching direction, continue
			if (graphNode.getChildren().size() < 2) {

				// DEBUG
				logger.info("RailNode.ID: " + graphNode.getRailNode().getId() + " Index = " + index
						+ " Turnout found. Not in switching order!");

				index++;
				continue;
			}

			final RailNode turnoutRailNode = graphNode.getRailNode();

			// DEBUG
			logger.trace("Index = " + index + " RN.ID: " + turnoutRailNode.getId() + " GN.ID: " + graphNode.getId()
					+ " ProcotolAddressId: " + graphNode.getRailNode().getProtocolTurnoutId()
					+ " Turnout found in switching order!");
			logger.trace("Turnout ShapeType = " + turnoutRailNode.getShapeType().name());

			final int nextIndex = index + 1;

			logger.trace("nextIndex = " + nextIndex + " graphNodes.size(): " + graphNodes.size());

			if (nextIndex <= graphNodes.size()) {

				// yolo
				final GraphNode currentGraphNode = graphNodes.get(index);
				final GraphNode nextGraphNode = graphNodes.get(nextIndex);

				logger.trace("Switching ProtocolAddress: " + turnoutRailNode.getProtocolTurnoutId() + " RailNode.ID: "
						+ turnoutRailNode.getId() + " to GraphNode.ID: " + nextGraphNode.getId());

				final Model model = null;
				turnoutRailNode.switchToGraphNode(applicationEventPublisher, protocolFacade, model, currentGraphNode,
						nextGraphNode);

				try {
					Thread.sleep(50);
				} catch (final InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}

			index++;
		}
	}

	/**
	 * Return all nodes starting from to the specified railNode up until the end of
	 * the Rout
	 *
	 * @param railNode
	 * @return
	 */
	public List<GraphNode> getSubListStartingFromRailNode(final RailNode railNode) {

		if (CollectionUtils.isEmpty(graphNodes)) {

			logger.info("Route has no graphNodes!");
			return new ArrayList<>();
		}

		final List<GraphNode> result = new ArrayList<>();

		boolean found = false;
		for (final GraphNode graphNode : graphNodes) {

			found = found | graphNode.equals(railNode.getGraphNodeOne());
			found = found | graphNode.equals(railNode.getGraphNodeTwo());

			if (found) {
				result.add(graphNode);
			}
		}

		return result;
	}

	public List<GraphNode> getSubListUpToRailNode(final RailNode railNode) {

		if (CollectionUtils.isEmpty(graphNodes)) {
			return new ArrayList<>();
		}

		final List<GraphNode> result = new ArrayList<>();

		boolean found = false;
		for (final GraphNode graphNode : graphNodes) {

			found = found | graphNode.equals(railNode.getGraphNodeOne());
			found = found | graphNode.equals(railNode.getGraphNodeTwo());

			if (found) {
				// yolo
				result.add(graphNode);
				return result;
			}

			result.add(graphNode);
		}

		return result;
	}

	public GraphNode findGraphNode(final RailNode railNode) {

		logger.trace("findGraphNode");

		for (final GraphNode graphNode : graphNodes) {

			if (graphNode.getRailNode().equals(railNode)) {
				return graphNode;
			}
		}

		// output for debugging if the railNode was not part of the route
		logger.error("RailNode RN-ID: " + railNode.getId() + " is not part of the Route!");
		for (final GraphNode graphNode : graphNodes) {
			logger.error("GN-ID: " + graphNode.getId() + " RailNode: " + graphNode.getRailNode());
		}

		return null;
	}

	public Object getLastGraphNode() {

		if (CollectionUtils.isEmpty(graphNodes)) {

			return null;
		}

		return graphNodes.get(graphNodes.size() - 1);
	}

	public boolean reservesBlock(final Block block) {

		for (final GraphNode graphNode : graphNodes) {

			final RailNode railNode = graphNode.getRailNode();

			// does the block share rail nodes with the route
			if (!block.getNodes().contains(railNode)) {
				continue;
			}

			// check if the block is reserved for the route's locomotive
			final RailNode blockRailNode = block.getNodes().get(0);
			if (blockRailNode.isReserved() && blockRailNode.getReservedLocomotiveId() == getLocomotive().getId()) {

				return true;
			}
		}

		return false;
	}

	public boolean containsBlock(final Block block) {

		for (int i = graphNodes.size() - 1; i >= 0; i--) {

			final GraphNode graphNode = graphNodes.get(i);

			if (block.getNodes().contains(graphNode.getRailNode())) {

				return true;
			}
		}

		return false;
	}

	public boolean endsWith(final Block block) {

		// the first block from the back has to be the specified block
		for (int i = graphNodes.size() - 1; i >= 0; i--) {

			final GraphNode graphNode = graphNodes.get(i);
			final Block routeBlock = graphNode.getRailNode().getBlock();

			logger.trace("Testing against block: " + routeBlock);

			if (routeBlock != null) {

				return routeBlock == block;
			}
		}

		return false;
	}

	public boolean isEmpty() {
		return CollectionUtils.isEmpty(graphNodes);
	}

	public boolean isNotEmpty() {
		return !isEmpty();
	}

	public Block findSuccessorBlock(final Block block) {

		logger.trace("Find Successor of block ID: " + block.getId());

		if (CollectionUtils.isEmpty(graphNodes)) {
			return null;
		}

		boolean currentBlockFound = false;

		for (final GraphNode graphNode : graphNodes) {

			final Block currentBlock = graphNode.getRailNode().getBlock();

			if (currentBlock == null) {
				continue;
			}

			if (currentBlock.getId() == block.getId()) {
				currentBlockFound = true;
			}

			if (currentBlockFound && (currentBlock.getId() != block.getId())) {
				return currentBlock;
			}
		}

		return null;
	}

	public boolean hasDuplicateGraphNodes() {

		if (CollectionUtils.isEmpty(graphNodes)) {
			return false;
		}

		boolean duplicatesFound = false;

		final Set<GraphNode> set = new HashSet<>();

		for (final GraphNode graphNode : graphNodes) {

			if (set.contains(graphNode)) {

				logger.info("Duplicate GN-ID: " + graphNode.getId());
				duplicatesFound = true;
			}
			set.add(graphNode);
		}

		return duplicatesFound;
	}

	public boolean checkRoute() {

		if (CollectionUtils.isEmpty(graphNodes)) {
			return true;
		}

		for (int i = 0; i < graphNodes.size() - 1; i++) {

			final GraphNode gnA = graphNodes.get(i);
			final GraphNode gnB = graphNodes.get(i + 1);

			if (!gnA.getChildren().contains(gnB)) {
				logger.info("GN-B-ID: " + gnB.getId() + " is not reachable from GN-A-ID: " + gnA.getId());
				return false;
			}
		}

		return true;
	}

	public int sizeInBlocks() {

		if (isEmpty()) {
			return 0;
		}

		final Set<Integer> blockIdSet = new HashSet<>();

		for (final GraphNode graphNode : graphNodes) {

			final Block block = graphNode.getRailNode().getBlock();
			if (block != null) {
				blockIdSet.add(block.getId());
			}

		}

		return blockIdSet.size();
	}

	public boolean containsGraphNode(final GraphNode graphNode) {

		if (isEmpty() || graphNode == null) {
			return false;
		}

		for (final GraphNode tempGraphNode : graphNodes) {

			if (tempGraphNode == graphNode) {
				return true;
			}
		}

		return false;
	}

	public List<GraphNode> getGraphNodes() {
		return graphNodes;
	}

	public DefaultLocomotive getLocomotive() {
		return locomotive;
	}

	public void setLocomotive(final DefaultLocomotive locomotive) {
		this.locomotive = locomotive;
	}

	public int getLocomotiveId() {
		return locomotiveId;
	}

	public void setLocomotiveId(final int locomotiveId) {
		this.locomotiveId = locomotiveId;
	}

}
