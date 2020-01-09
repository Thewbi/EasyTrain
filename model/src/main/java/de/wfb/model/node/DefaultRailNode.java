package de.wfb.model.node;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.Model;
import de.wfb.rail.events.ModelChangedEvent;

public class DefaultRailNode extends BaseNode implements RailNode {

	private static final Logger logger = LogManager.getLogger(DefaultRailNode.class);

	private static final int NORTH_INDEX = 0;

	private static final int EAST_INDEX = 1;

	private static final int SOUTH_INDEX = 2;

	private static final int WEST_INDEX = 3;

	private final Edge[] edges = new Edge[4];

	/** for turnouts, One-Nodes exit the rail towards one ends */
	private GraphNode graphNodeOne;

	/** for turnouts, Two-Nodes exit the rail towards the two ends */
	private GraphNode graphNodeTwo;

	private Integer protocolTurnoutId;

	private boolean thrown;

	private final List<RailNode> manualConnections = new ArrayList<>();

	private int feedbackBlockNumber = -1;

	private boolean feedbackBlockUsed;

	private boolean selected;

	private boolean highlighted;

	/**
	 * ctor
	 */
	public DefaultRailNode() {

		// no edge is used
		for (int i = 0; i < 4; i++) {
			edges[i] = null;
		}
	}

	@Override
	public void toggleTurnout() {

		logger.info("toggle()");

		thrown = !thrown;
	}

	@Override
	public void switchToGraphNode(final ApplicationEventPublisher applicationEventPublisher, final Model model,
			final GraphNode nextGraphNode) {

		logger.info("switchToGraphNode()");

		final Edge outEdge = findOutEdge(nextGraphNode);
		if (outEdge == null) {

			logger.warn("Switch is not connected to nextGraphNode! Cannot switch!");
			return;
		}

		logger.info("OUT EDGE: " + outEdge.getDirection().name());

		updateSwitchState(outEdge);

		// tell the UI
		sendModelChangedEvent(applicationEventPublisher, model, this);
	}

	private void updateSwitchState(final Edge outEdge) {

		switch (getShapeType()) {

		case SWITCH_LEFT_0:
			this.setThrown(outEdge.getDirection() == Direction.NORTH);
			break;

		case SWITCH_RIGHT_0:
			this.setThrown(outEdge.getDirection() == Direction.SOUTH);
			break;

		case SWITCH_LEFT_90:
			this.setThrown(outEdge.getDirection() == Direction.EAST);
			break;

		case SWITCH_RIGHT_90:
			this.setThrown(outEdge.getDirection() == Direction.WEST);
			break;

		case SWITCH_LEFT_180:
			this.setThrown(outEdge.getDirection() == Direction.SOUTH);
			break;

		case SWITCH_RIGHT_180:
			this.setThrown(outEdge.getDirection() == Direction.NORTH);
			break;

		case SWITCH_LEFT_270:
			this.setThrown(outEdge.getDirection() == Direction.WEST);
			break;

		case SWITCH_RIGHT_270:
			this.setThrown(outEdge.getDirection() == Direction.EAST);
			break;

		default:
			break;
		}
	}

	public void sendModelChangedEvent(final ApplicationEventPublisher applicationEventPublisher, final Model model,
			final RailNode railNode) {

		final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, model, railNode.getX(), railNode.getY(),
				railNode.isHighlighted(), railNode.isFeedbackBlockUsed(), railNode.isSelected());

		applicationEventPublisher.publishEvent(modelChangedEvent);
	}

	private Edge findOutEdge(final GraphNode nextGraphNode) {

		for (int i = 0; i < 4; i++) {

			final Edge edge = edges[i];

			if (edge == null || edge.getNextOutGraphNode() == null) {
				continue;
			}

			if (edge.getNextOutGraphNode().equals(nextGraphNode)) {

				return edge;
			}
		}

		return null;
	}

	@Override
	public void connectTo(final RailNode railNodeB) {

		logger.info("connectTo()");

		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(getId()).append("(").append(getX()).append(", ").append(getY()).append(") ");
		stringBuffer.append(railNodeB.getId()).append("(").append(railNodeB.getX()).append(", ")
				.append(railNodeB.getY()).append(") ");

		logger.info(stringBuffer);

		// find orientation

		// north
		if (railNodeB.getY() < getY()) {
			logger.info("north");
			connectNorth(railNodeB);
		}

		// east
		if (railNodeB.getX() > getX()) {
			logger.info("east");
			connectEast(railNodeB);
		}

		// south
		if (railNodeB.getY() > getY()) {
			logger.info("south");
			connectSouth(railNodeB);
		}

		// west
		if (railNodeB.getX() < getX()) {
			logger.info("west");
			connectWest(railNodeB);
		}

		// because the automatic connection only looks into the immediate vincinity of
		// nodes on the grid, but manually connected nodes can be apart over long
		// distances, there is a manual connection list, which stores all manual
		// connections
		getManualConnections().add(railNodeB);
		railNodeB.getManualConnections().add(this);
	}

	@Override
	public void connect(final Model model) {

		logger.info("connect()");

		// north
		final RailNode northNode = (RailNode) model.getNode(getX(), getY() - 1);
		if (northNode != null) {
			connectNorth(northNode);
		}

		// east
		final RailNode eastNode = (RailNode) model.getNode(getX() + 1, getY());
		if (eastNode != null) {
			connectEast(eastNode);
		}

		// south
		final RailNode southNode = (RailNode) model.getNode(getX(), getY() + 1);
		if (southNode != null) {
			connectSouth(southNode);
		}

		// west
		final RailNode westNode = (RailNode) model.getNode(getX() - 1, getY());
		if (westNode != null) {
			connectWest(westNode);
		}
	}

	private void connectWest(final RailNode westNode) {

		final Edge westEdge = getWestEdge();
		if (westEdge != null) {

			final Edge innerEdge = westNode.getEastEdge();
			if (innerEdge != null) {

				if (!westEdge.getOutGraphNode().getChildren().contains(innerEdge.getInGraphNode())) {
					westEdge.getOutGraphNode().getChildren().add(innerEdge.getInGraphNode());
					westEdge.setNextOutGraphNode(innerEdge.getInGraphNode());
				}

				if (!innerEdge.getOutGraphNode().getChildren().contains(westEdge.getInGraphNode())) {
					innerEdge.getOutGraphNode().getChildren().add(westEdge.getInGraphNode());
					innerEdge.setNextOutGraphNode(westEdge.getInGraphNode());
				}
			}
		}
	}

	private void connectSouth(final RailNode southNode) {

		final Edge southEdge = getSouthEdge();
		if (southEdge != null) {

			final Edge innerEdge = southNode.getNorthEdge();
			if (innerEdge != null) {

				if (!southEdge.getOutGraphNode().getChildren().contains(innerEdge.getInGraphNode())) {
					southEdge.getOutGraphNode().getChildren().add(innerEdge.getInGraphNode());
					southEdge.setNextOutGraphNode(innerEdge.getInGraphNode());
				}

				if (!innerEdge.getOutGraphNode().getChildren().contains(southEdge.getInGraphNode())) {
					innerEdge.getOutGraphNode().getChildren().add(southEdge.getInGraphNode());
					innerEdge.setNextOutGraphNode(southEdge.getInGraphNode());
				}
			}
		}
	}

	private void connectEast(final RailNode eastNode) {

		final Edge eastEdge = getEastEdge();
		if (eastEdge != null) {

			final Edge innerEdge = eastNode.getWestEdge();
			if (innerEdge != null) {

				if (!eastEdge.getOutGraphNode().getChildren().contains(innerEdge.getInGraphNode())) {
					eastEdge.getOutGraphNode().getChildren().add(innerEdge.getInGraphNode());
					eastEdge.setNextOutGraphNode(innerEdge.getInGraphNode());
				}

				if (!innerEdge.getOutGraphNode().getChildren().contains(eastEdge.getInGraphNode())) {
					innerEdge.getOutGraphNode().getChildren().add(eastEdge.getInGraphNode());
					innerEdge.setNextOutGraphNode(eastEdge.getInGraphNode());
				}
			}
		}
	}

	private void connectNorth(final RailNode northNode) {

		final Edge northEdge = getNorthEdge();
		if (northEdge != null) {

			final Edge innerEdge = northNode.getSouthEdge();
			if (innerEdge != null) {

				if (!northEdge.getOutGraphNode().getChildren().contains(innerEdge.getInGraphNode())) {
					northEdge.getOutGraphNode().getChildren().add(innerEdge.getInGraphNode());
					northEdge.setNextOutGraphNode(innerEdge.getInGraphNode());
				}

				if (!innerEdge.getOutGraphNode().getChildren().contains(northEdge.getInGraphNode())) {
					innerEdge.getOutGraphNode().getChildren().add(northEdge.getInGraphNode());
					innerEdge.setNextOutGraphNode(northEdge.getInGraphNode());
				}
			}
		}
	}

	@Override
	public void disconnect(final Model model) {

		// north
		final Edge northEdge = getNorthEdge();
		if (northEdge != null) {

			final RailNode northNode = (RailNode) model.getNode(getX(), getY() - 1);

			if (northNode != null) {

				final Edge innerEdge = northNode.getSouthEdge();
				if (innerEdge != null) {

					northEdge.getOutGraphNode().getChildren().remove(innerEdge.getInGraphNode());
					northEdge.setNextOutGraphNode(null);
					innerEdge.getOutGraphNode().getChildren().remove(northEdge.getInGraphNode());
					innerEdge.setNextOutGraphNode(null);
				}
			}
		}

		// east
		final Edge eastEdge = getEastEdge();
		if (eastEdge != null) {

			final RailNode eastNode = (RailNode) model.getNode(getX() + 1, getY());

			if (eastNode != null) {

				final Edge innerEdge = eastNode.getWestEdge();
				if (innerEdge != null) {

					eastEdge.getOutGraphNode().getChildren().remove(innerEdge.getInGraphNode());
					eastEdge.setNextOutGraphNode(null);
					innerEdge.getOutGraphNode().getChildren().remove(eastEdge.getInGraphNode());
					innerEdge.setNextOutGraphNode(null);
				}
			}
		}

		// south
		final Edge southEdge = getSouthEdge();
		if (southEdge != null) {

			final RailNode southNode = (RailNode) model.getNode(getX(), getY() + 1);

			if (southNode != null) {

				final Edge innerEdge = southNode.getNorthEdge();
				if (innerEdge != null) {

					southEdge.getOutGraphNode().getChildren().remove(innerEdge.getInGraphNode());
					southEdge.setNextOutGraphNode(null);
					innerEdge.getOutGraphNode().getChildren().remove(southEdge.getInGraphNode());
					innerEdge.setNextOutGraphNode(null);
				}
			}
		}

		// west
		final Edge westEdge = getWestEdge();
		if (westEdge != null) {

			final RailNode westNode = (RailNode) model.getNode(getX() - 1, getY());

			if (westNode != null) {

				final Edge innerEdge = westNode.getEastEdge();
				if (innerEdge != null) {

					westEdge.getOutGraphNode().getChildren().remove(innerEdge.getInGraphNode());
					westEdge.setNextOutGraphNode(null);
					innerEdge.getOutGraphNode().getChildren().remove(westEdge.getInGraphNode());
					innerEdge.setNextOutGraphNode(null);
				}
			}
		}
	}

	@Override
	public String toString() {

		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("\n");

		stringBuffer.append(graphNodeOne.getId()).append(" -> ");
		if (CollectionUtils.isNotEmpty(graphNodeOne.getChildren())) {

			for (final GraphNode childGraphNode : graphNodeOne.getChildren()) {
				stringBuffer.append(childGraphNode.getId()).append(", ");
			}
		}

		stringBuffer.append("\n");

		stringBuffer.append(graphNodeTwo.getId()).append(" -> ");
		if (CollectionUtils.isNotEmpty(graphNodeTwo.getChildren())) {

			for (final GraphNode childGraphNode : graphNodeTwo.getChildren()) {
				stringBuffer.append(childGraphNode.getId()).append(", ");
			}
		}

		stringBuffer.append("\n");
		stringBuffer.append("GraphNode One: ").append(getGraphNodeOne()).append(" COLOR: ")
				.append(getGraphNodeOne().getColor().name());

		stringBuffer.append("\n");
		stringBuffer.append("GraphNode Two: ").append(getGraphNodeTwo()).append(" COLOR: ")
				.append(getGraphNodeTwo().getColor().name());

		stringBuffer.append("\n");

		return stringBuffer.toString();
	}

	@Override
	public Edge getNorthEdge() {
		return edges[NORTH_INDEX];
	}

	@Override
	public void setNorthEdge(final Edge edge) {
		edges[NORTH_INDEX] = edge;
	}

	@Override
	public Edge getEastEdge() {
		return edges[EAST_INDEX];
	}

	@Override
	public void setEastEdge(final Edge edge) {
		edges[EAST_INDEX] = edge;
	}

	@Override
	public Edge getSouthEdge() {
		return edges[SOUTH_INDEX];
	}

	@Override
	public void setSouthEdge(final Edge edge) {
		edges[SOUTH_INDEX] = edge;
	}

	@Override
	public Edge getWestEdge() {
		return edges[WEST_INDEX];
	}

	@Override
	public void setWestEdge(final Edge edge) {
		edges[WEST_INDEX] = edge;
	}

	@Override
	public Edge getEdge(final EdgeDirection edgeDirection) {
		switch (edgeDirection) {

		case NORTH:
			return edges[NORTH_INDEX];

		case EAST:
			return edges[EAST_INDEX];

		case SOUTH:
			return edges[SOUTH_INDEX];

		case WEST:
			return edges[WEST_INDEX];

		default:
			throw new IllegalArgumentException("Invalid direction!");
		}
	}

	@Override
	public void setEdge(final EdgeDirection edgeDirection, final Edge edge) {
		switch (edgeDirection) {

		case NORTH:
			edges[NORTH_INDEX] = edge;
			break;

		case EAST:
			edges[EAST_INDEX] = edge;
			break;

		case SOUTH:
			edges[SOUTH_INDEX] = edge;
			break;

		case WEST:
			edges[WEST_INDEX] = edge;
			break;

		default:
			throw new IllegalArgumentException("Invalid direction!");
		}
	}

	@Override
	public GraphNode getGraphNodeOne() {
		return graphNodeOne;
	}

	@Override
	public void setGraphNodeOne(final GraphNode graphNodeOne) {
		this.graphNodeOne = graphNodeOne;
	}

	@Override
	public GraphNode getGraphNodeTwo() {
		return graphNodeTwo;
	}

	@Override
	public void setGraphNodeTwo(final GraphNode graphNodeTwo) {
		this.graphNodeTwo = graphNodeTwo;
	}

	@Override
	public Integer getProtocolTurnoutId() {
		return protocolTurnoutId;
	}

	@Override
	public void setProtocolTurnoutId(final Integer protocolTurnoutId) {
		this.protocolTurnoutId = protocolTurnoutId;
	}

	@Override
	public boolean isThrown() {
		return thrown;
	}

	@Override
	public void setThrown(final boolean thrown) {
		this.thrown = thrown;
	}

	@Override
	public List<RailNode> getManualConnections() {
		return manualConnections;
	}

	@Override
	public int getFeedbackBlockNumber() {
		return feedbackBlockNumber;
	}

	@Override
	public void setFeedbackBlockNumber(final int feedbackBlockNumber) {
		this.feedbackBlockNumber = feedbackBlockNumber;
	}

	@Override
	public boolean isFeedbackBlockUsed() {
		return feedbackBlockUsed;
	}

	@Override
	public void setFeedbackBlockUsed(final boolean feedbackBlockUsed) {
		this.feedbackBlockUsed = feedbackBlockUsed;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(final boolean selected) {
		this.selected = selected;
	}

	@Override
	public boolean isHighlighted() {
		return highlighted;
	}

	@Override
	public void setHighlighted(final boolean highlighted) {
		this.highlighted = highlighted;
	}

}
