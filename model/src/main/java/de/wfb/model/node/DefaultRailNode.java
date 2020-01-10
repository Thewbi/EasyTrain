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

	// private final int debugRailNodeID = 172;
	private final int debugRailNodeID = -1;

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

		logger.trace("toggle()");

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

		logger.trace("OUT EDGE: " + outEdge.getDirection().name());

		updateSwitchState(outEdge);

		// tell the UI
		sendModelChangedEvent(applicationEventPublisher, model, this);
	}

	private void updateSwitchState(final Edge outEdge) {

		logger.info("ShapteType: " + getShapeType().name() + " Direction: " + outEdge.getDirection().name());

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

	/**
	 * If nodes have been connected manually (to cross gaps for example) this
	 * function will connect the graph nodes.
	 */
	@Override
	public void manualConnectTo(final RailNode railNodeB) {

		final StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(getId()).append("(").append(getX()).append(", ").append(getY()).append(") ");
		stringBuffer.append(railNodeB.getId()).append("(").append(railNodeB.getX()).append(", ")
				.append(railNodeB.getY()).append(") ");
		logger.info(stringBuffer.toString());

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

		if (getId() == debugRailNodeID) {
			logger.info("connect() ID = " + getId() + " X: " + getX() + " Y: " + getY());
		}

		// north
		final RailNode northNode = (RailNode) model.getNode(getX(), getY() - 1);
		if (northNode != null) {
			connectNorth(northNode);
		} else {
			if (getId() == debugRailNodeID) {
				logger.info("connect() NorthNode is null!");
			}
		}

		// east
		final RailNode eastNode = (RailNode) model.getNode(getX() + 1, getY());
		if (eastNode != null) {
			connectEast(eastNode);
		} else {
			if (getId() == debugRailNodeID) {
				logger.info("connect() EastNode is null!");
			}
		}

		// south
		final RailNode southNode = (RailNode) model.getNode(getX(), getY() + 1);
		if (southNode != null) {
			connectSouth(southNode);
		} else {
			if (getId() == debugRailNodeID) {
				logger.info("connect() SouthNode is null!");
			}
		}

		// west
		final RailNode westNode = (RailNode) model.getNode(getX() - 1, getY());
		if (westNode != null) {
			connectWest(westNode);
		} else {
			if (getId() == debugRailNodeID) {
				logger.info("connect() WestNode is null!");
			}
		}
	}

	private void connectWest(final RailNode westNode) {

		final Edge westEdge = getWestEdge();
		if (westEdge == null) {
			if (getId() == debugRailNodeID) {
				logger.warn("WestEdge is null!");
			}
			return;
		}

		final Edge eastEdge = westNode.getEastEdge();
		if (eastEdge == null) {
			if (getId() == debugRailNodeID) {
				logger.warn("Returning because of eastEdge!");
			}
			return;
		}

		if (!westEdge.getOutGraphNode().getChildren().contains(eastEdge.getInGraphNode())) {

			westEdge.getOutGraphNode().getChildren().add(eastEdge.getInGraphNode());
			westEdge.setNextOutGraphNode(eastEdge.getInGraphNode());

			if (getId() == debugRailNodeID) {

				logger.warn("Connecting West GN " + westEdge.getOutGraphNode().getId() + " to GN "
						+ eastEdge.getInGraphNode().getId());
			}
		}

		if (!eastEdge.getOutGraphNode().getChildren().contains(westEdge.getInGraphNode())) {

			eastEdge.getOutGraphNode().getChildren().add(westEdge.getInGraphNode());
			eastEdge.setNextOutGraphNode(westEdge.getInGraphNode());
		}
	}

	private void connectSouth(final RailNode southNode) {

		final Edge southEdge = getSouthEdge();
		if (southEdge == null) {
			if (getId() == debugRailNodeID) {
				logger.warn("SouthEdge is null!");
			}
			return;
		}

		final Edge northEdge = southNode.getNorthEdge();
		if (northEdge == null) {
			if (getId() == debugRailNodeID) {
				logger.warn("Returning because of northEdge!");
			}
			return;
		}

		if (!southEdge.getOutGraphNode().getChildren().contains(northEdge.getInGraphNode())) {

			southEdge.getOutGraphNode().getChildren().add(northEdge.getInGraphNode());
			southEdge.setNextOutGraphNode(northEdge.getInGraphNode());

			if (getId() == debugRailNodeID) {

				logger.warn("Connecting South GN " + southEdge.getOutGraphNode().getId() + " to GN "
						+ northEdge.getInGraphNode().getId());
			}
		}

		if (!northEdge.getOutGraphNode().getChildren().contains(southEdge.getInGraphNode())) {

			northEdge.getOutGraphNode().getChildren().add(southEdge.getInGraphNode());
			northEdge.setNextOutGraphNode(southEdge.getInGraphNode());
		}
	}

	private void connectEast(final RailNode eastNode) {

		final Edge eastEdge = getEastEdge();
		if (eastEdge == null) {
			if (getId() == debugRailNodeID) {
				logger.warn("EastEdge is null!");
			}
			return;
		}

		final Edge westEdge = eastNode.getWestEdge();
		if (westEdge == null) {
			if (getId() == debugRailNodeID) {
				logger.warn("Returning because of westEdge!");
			}
			return;
		}

		if (!eastEdge.getOutGraphNode().getChildren().contains(westEdge.getInGraphNode())) {

			eastEdge.getOutGraphNode().getChildren().add(westEdge.getInGraphNode());
			eastEdge.setNextOutGraphNode(westEdge.getInGraphNode());

			if (getId() == debugRailNodeID) {

				logger.warn("Connecting East GN " + eastEdge.getOutGraphNode().getId() + " to GN "
						+ westEdge.getInGraphNode().getId());
			}
		}

		if (!westEdge.getOutGraphNode().getChildren().contains(eastEdge.getInGraphNode())) {

			westEdge.getOutGraphNode().getChildren().add(eastEdge.getInGraphNode());
			westEdge.setNextOutGraphNode(eastEdge.getInGraphNode());
		}
	}

	private void connectNorth(final RailNode northNode) {

		final Edge northEdge = getNorthEdge();
		if (northEdge == null) {
			if (getId() == debugRailNodeID) {
				logger.warn("NorthEdge is null!");
			}
			return;
		}

		final Edge southEdge = northNode.getSouthEdge();
		if (southEdge == null) {
			if (getId() == debugRailNodeID) {
				logger.warn("Returning because of southEdge!");
			}
			return;
		}

		if (getId() == debugRailNodeID) {

			for (final GraphNode graphNode : northEdge.getOutGraphNode().getChildren()) {

				logger.warn(northEdge.getOutGraphNode().getId() + " -> " + graphNode.getId());
			}
		}

		if (!northEdge.getOutGraphNode().getChildren().contains(southEdge.getInGraphNode())) {

			northEdge.getOutGraphNode().getChildren().add(southEdge.getInGraphNode());
			northEdge.setNextOutGraphNode(southEdge.getInGraphNode());

			if (getId() == debugRailNodeID) {

				logger.warn("Connecting North GN " + northEdge.getOutGraphNode().getId() + " to GN "
						+ southEdge.getInGraphNode().getId());
			}

		} else {
			if (getId() == debugRailNodeID) {
				logger.warn("NorthEdge already connected!");
			}
		}

		if (!southEdge.getOutGraphNode().getChildren().contains(northEdge.getInGraphNode())) {

			southEdge.getOutGraphNode().getChildren().add(northEdge.getInGraphNode());
			southEdge.setNextOutGraphNode(northEdge.getInGraphNode());

		} else {
			if (getId() == debugRailNodeID) {
				logger.warn("SouthEdge already connected!");
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
