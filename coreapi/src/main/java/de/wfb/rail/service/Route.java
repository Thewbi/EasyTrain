package de.wfb.rail.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.rail.events.NodeHighlightedEvent;
import de.wfb.rail.events.RemoveHighlightsEvent;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.ui.ShapeType;

public class Route {

	private static final Logger logger = LogManager.getLogger(Route.class);

	private DefaultLocomotive locomotive;

	private final List<GraphNode> graphNodes = new ArrayList<>();

	public Route() {

	}

	public Route(final List<GraphNode> nodeList) {
		graphNodes.addAll(nodeList);
	}

	public List<GraphNode> getGraphNodes() {
		return graphNodes;
	}

	@Override
	public String toString() {

		if (CollectionUtils.isEmpty(graphNodes)) {
			return "Route is empty";
		}

		final StringBuffer stringBuffer = new StringBuffer();

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

		return stringBuffer.toString();
	}

	public void highlightRoute(final ApplicationEventPublisher applicationEventPublisher) {

		logger.info("highlighting route");

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

		switchTurnouts(graphNodes, applicationEventPublisher, protocolFacade);
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
					+ graphNode.getId());

			// ignore all nodes that are no turnouts
			if (ShapeType.isNotTurnout(graphNode.getRailNode().getShapeType())) {

				index++;
				continue;
			}

			// if the turnout is NOT traversed in switching direction, continue
			if (graphNode.getChildren().size() < 2) {

				logger.info("RailNode.ID: " + graphNode.getRailNode().getId() + " Index = " + index
						+ " Turnout found. Not in switching order!");

				index++;
				continue;
			}

			final RailNode turnoutRailNode = graphNode.getRailNode();

			logger.trace("Index = " + index + " RN.ID: " + turnoutRailNode.getId() + " GN.ID: " + graphNode.getId()
					+ " Turnout found in switching order!");

			logger.trace("Turnout ShapeType = " + turnoutRailNode.getShapeType().name());

			final int nextIndex = index + 1;

			logger.trace("nextIndex = " + nextIndex + " graphNodes.size(): " + graphNodes.size());

			if (nextIndex < graphNodes.size()) {

				final GraphNode nextGraphNode = graphNodes.get(nextIndex);

				logger.info("Switching RailNode.ID: " + turnoutRailNode.getId() + " to GraphNode.ID: "
						+ nextGraphNode.getId());

				turnoutRailNode.switchToGraphNode(applicationEventPublisher, protocolFacade, null, nextGraphNode);
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
				return result;
			}

			result.add(graphNode);
		}

		return result;
	}

	public GraphNode findGraphNode(final RailNode railNode) {

		for (final GraphNode graphNode : graphNodes) {

			if (graphNode.getRailNode().equals(railNode)) {
				return graphNode;
			}
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

		for (final GraphNode graphNode : getGraphNodes()) {

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

	public DefaultLocomotive getLocomotive() {
		return locomotive;
	}

	public void setLocomotive(final DefaultLocomotive locomotive) {
		this.locomotive = locomotive;
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

}
