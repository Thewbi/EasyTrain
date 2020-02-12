package de.wfb.model.node;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.Model;
import de.wfb.model.facade.ModelFacade;
import de.wfb.rail.events.ModelChangedEvent;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.service.Block;

public class DefaultRailNode extends BaseNode implements RailNode {

	private static final int NOT_RESERVED = -1;

	private static final int NORTH_INDEX = 0;

	private static final int EAST_INDEX = 1;

	private static final int SOUTH_INDEX = 2;

	private static final int WEST_INDEX = 3;

	private static final Logger logger = LogManager.getLogger(DefaultRailNode.class);

	private final Edge[] edges = new Edge[4];

	/** for turnouts, One-Nodes exit the rail towards one ends */
	private GraphNode graphNodeOne;

	/** for turnouts, Two-Nodes exit the rail towards the two ends */
	private GraphNode graphNodeTwo;

	private Block block;

	private Integer protocolTurnoutId;

	/** thrown = true ==> */
	private boolean thrown;

	private List<RailNode> manualConnections;

	private Integer feedbackBlockNumber;

	/**
	 * This field is used to communicate with the UI. It will draw nodes with a
	 * different color when they are blocked!
	 */
	private boolean feedbackBlockUsed;

	private boolean selected;

	private boolean highlighted;

	// private final int debugRailNodeID = 172;
	private final int debugRailNodeID = -1;

	/** is this graph node currently reserved by a locomotive */
	private boolean reserved;

	/** the id of the locomotive that currently reserves this field */
	private int reservedLocomotiveId = NOT_RESERVED;

	/** null means that the rail node is traversable in all directions */
	private Direction traverse = null;

	private ModelFacade modelFacade;

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
	public void switchToGraphNode(final ApplicationEventPublisher applicationEventPublisher,
			final ProtocolFacade protocolFacade, final Model model, final GraphNode nextGraphNode) {

		logger.trace("switchToGraphNode() RailNode.ID: " + getId());

		final Edge outEdge = findOutEdge(nextGraphNode);
		if (outEdge == null) {

			logger.warn("Switch is not connected to nextGraphNode! Cannot switch!");
			return;
		}

		logger.trace("OUT EDGE: " + outEdge.getDirection().name());

		updateSwitchState(outEdge, protocolFacade);

		// tell the UI
		sendModelChangedEvent(applicationEventPublisher, model, this);
	}

	private void updateSwitchState(final Edge outEdge, final ProtocolFacade protocolFacade) {

		logger.trace("ShapeType: " + getShapeType().name() + " Direction: " + outEdge.getDirection().name());

		switch (getShapeType()) {

		case SWITCH_LEFT_0:
			process(protocolFacade, outEdge.getDirection() == Direction.NORTH);
			break;

		case SWITCH_RIGHT_0:
			process(protocolFacade, outEdge.getDirection() == Direction.SOUTH);
			break;

		case SWITCH_LEFT_90:
			process(protocolFacade, outEdge.getDirection() == Direction.EAST);
			break;

		case SWITCH_RIGHT_90:
			process(protocolFacade, outEdge.getDirection() == Direction.WEST);
			break;

		case SWITCH_LEFT_180:
			process(protocolFacade, outEdge.getDirection() == Direction.SOUTH);
			break;

		case SWITCH_RIGHT_180:
			process(protocolFacade, outEdge.getDirection() == Direction.NORTH);
			break;

		case SWITCH_LEFT_270:
			process(protocolFacade, outEdge.getDirection() == Direction.WEST);
			break;

		case SWITCH_RIGHT_270:
			process(protocolFacade, outEdge.getDirection() == Direction.EAST);
			break;

		default:
			break;
		}
	}

	private void process(final ProtocolFacade protocolFacade, final boolean newThrown) {

		boolean oldThrown = isThrown();

		if (isFlipped() != null && isFlipped()) {
			oldThrown = !oldThrown;
		}

		logger.trace("OldThrown: " + oldThrown + " NewThrown: " + newThrown);

		if (newThrown != oldThrown) {

			logger.trace("Switch Turnout. RailNode.ID: " + getId());

			if (isFlipped() != null && isFlipped()) {
				setThrown(!newThrown);
			} else {
				setThrown(newThrown);
			}

			protocolFacade.turnTurnout(this);
		}
	}

	public void sendModelChangedEvent(final ApplicationEventPublisher applicationEventPublisher, final Model model,
			final RailNode railNode) {

		logger.trace("sendModelChangedEvent()");

		final Object sender = this;
		final int x = railNode.getX();
		final int y = railNode.getY();
		final boolean highlighted = railNode.isHighlighted();
		final boolean feedbackBlockUsed = railNode.isFeedbackBlockUsed();
		final boolean selected = railNode.isSelected();
		final boolean reserved = railNode.isReserved();
		final boolean containsLocomotive = false;

		// @formatter:off

		final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(
				sender,
				model,
				x,
				y,
				highlighted,
				feedbackBlockUsed,
				selected,
				reserved,
				containsLocomotive);

		// @formatter:on

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

		// @formatter:off

		final StringBuffer stringBuffer = new StringBuffer();

		stringBuffer
			.append(getId())
			.append("(").append(getX()).append(", ").append(getY()).append(") ")
			.append(railNodeB.getId())
			.append("(").append(railNodeB.getX()).append(", ").append(railNodeB.getY()).append(") ");

		logger.trace(stringBuffer.toString());

		// @formatter:on

		// find orientation

		// north
		if (railNodeB.getY() < getY()) {

			logger.trace("north");
			connectNorth(railNodeB);
		}

		// east
		if (railNodeB.getX() > getX()) {

			logger.trace("east");
			connectEast(railNodeB);
		}

		// south
		if (railNodeB.getY() > getY()) {

			logger.trace("south");
			connectSouth(railNodeB);
		}

		// west
		if (railNodeB.getX() < getX()) {

			logger.trace("west");
			connectWest(railNodeB);
		}

		// because the automatic connection only looks into the immediate vincinity of
		// nodes on the grid, but manually connected nodes can be separat over long
		// distances, there is a manual connection list, which stores all manual
		// connections
		logger.trace("Add manual Connection!");
		if (getManualConnections() == null) {
			setManualConnections(new ArrayList<RailNode>());
		}

		if (!getManualConnections().contains(railNodeB)) {

			logger.trace("ADDING");
			getManualConnections().add(railNodeB);
		}
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

			logger.trace("ADDING");

			westEdge.getOutGraphNode().getChildren().add(eastEdge.getInGraphNode());
			westEdge.setNextOutGraphNode(eastEdge.getInGraphNode());

			if (getId() == debugRailNodeID) {

				logger.warn("Connecting West GN " + westEdge.getOutGraphNode().getId() + " to GN "
						+ eastEdge.getInGraphNode().getId());
			}
		}

		if (!eastEdge.getOutGraphNode().getChildren().contains(westEdge.getInGraphNode())) {

			logger.trace("ADDING");

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

			logger.trace("ADDING");

			southEdge.getOutGraphNode().getChildren().add(northEdge.getInGraphNode());
			southEdge.setNextOutGraphNode(northEdge.getInGraphNode());

			if (getId() == debugRailNodeID) {

				logger.warn("Connecting South GN " + southEdge.getOutGraphNode().getId() + " to GN "
						+ northEdge.getInGraphNode().getId());
			}
		}

		if (!northEdge.getOutGraphNode().getChildren().contains(southEdge.getInGraphNode())) {

			logger.trace("ADDING");

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

			logger.trace("ADDING");

			eastEdge.getOutGraphNode().getChildren().add(westEdge.getInGraphNode());
			eastEdge.setNextOutGraphNode(westEdge.getInGraphNode());

			if (getId() == debugRailNodeID) {

				logger.warn("Connecting East GN " + eastEdge.getOutGraphNode().getId() + " to GN "
						+ westEdge.getInGraphNode().getId());
			}
		}

		if (!westEdge.getOutGraphNode().getChildren().contains(eastEdge.getInGraphNode())) {

			logger.trace("ADDING");

			westEdge.getOutGraphNode().getChildren().add(eastEdge.getInGraphNode());
			westEdge.setNextOutGraphNode(eastEdge.getInGraphNode());
		}
	}

	private void connectNorth(final RailNode northNode) {

		final Edge northEdge = getNorthEdge();
		if (northEdge == null) {

			// DEBUG
			if (getId() == debugRailNodeID) {
				logger.warn("NorthEdge is null!");
			}

			return;
		}

		final Edge southEdge = northNode.getSouthEdge();
		if (southEdge == null) {

			// DEBUG
			if (getId() == debugRailNodeID) {
				logger.warn("Returning because of southEdge!");
			}

			return;
		}

		// DEBUG
		if (getId() == debugRailNodeID) {

			for (final GraphNode graphNode : northEdge.getOutGraphNode().getChildren()) {

				logger.warn(northEdge.getOutGraphNode().getId() + " -> " + graphNode.getId());
			}
		}

		if (northEdge.getOutGraphNode().getChildren().contains(southEdge.getInGraphNode())) {

			// DEBUG
			if (getId() == debugRailNodeID) {
				logger.warn("NorthEdge already connected!");
			}

		} else {

			logger.trace("ADDING");

			northEdge.getOutGraphNode().getChildren().add(southEdge.getInGraphNode());
			northEdge.setNextOutGraphNode(southEdge.getInGraphNode());

			// DEBUG
			if (getId() == debugRailNodeID) {

				logger.warn("Connecting North GN " + northEdge.getOutGraphNode().getId() + " to GN "
						+ southEdge.getInGraphNode().getId());
			}
		}

		if (southEdge.getOutGraphNode().getChildren().contains(northEdge.getInGraphNode())) {

			// DEBUG
			if (getId() == debugRailNodeID) {
				logger.warn("SouthEdge already connected!");
			}

		} else {

			logger.trace("ADDING");

			southEdge.getOutGraphNode().getChildren().add(northEdge.getInGraphNode());
			southEdge.setNextOutGraphNode(northEdge.getInGraphNode());

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

		final String idFormatted = String.format("% 4d", getId());
		final String shapeTypeFormatted = String.format("%20s", getShapeType());

		final String graphNodeOneIdFormatted = String.format("% 4d", graphNodeOne.getId());
		final String graphNodeTwoIdFormatted = String.format("% 4d", graphNodeTwo.getId());
		final String xFormatted = String.format("% 4d", getX());
		final String yFormatted = String.format("% 4d", getY());

		final String coordinatesFormatted = "(" + xFormatted + ", " + yFormatted + ")";

		// @formatter:off

		stringBuffer
			.append("ID: ").append(idFormatted)
			.append("    (x, y): ").append(coordinatesFormatted)
			.append("    ST: ").append(shapeTypeFormatted)
			.append("    GN-1: ").append(graphNodeOneIdFormatted)
			.append("    GN-2: ").append(graphNodeTwoIdFormatted);

		// @formatter:on

		return stringBuffer.toString();

//		return toStringLineBreaks();
	}

	@SuppressWarnings("unused")
	private String toStringLineBreaks() {

		// @formatter:off

		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("\n");

		stringBuffer.append("ID: ").append(getId()).append("\n");

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
		stringBuffer
			.append("GraphNode One: ")
			.append(getGraphNodeOne());
//			.append(" COLOR: ")
//			.append(getGraphNodeOne().getColor().name());

		stringBuffer.append("\n");
		stringBuffer
			.append("GraphNode Two: ")
			.append(getGraphNodeTwo());
//			.append(" COLOR: ")
//			.append(getGraphNodeTwo().getColor().name());

		stringBuffer.append("\n");

		// @formatter:on

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
	public Edge getEdge(final Direction edgeDirection) {

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
	public void setEdge(final Direction edgeDirection, final Edge edge) {

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
	public void updateBlockedGraphNode() {

		logger.trace("updateBlockedGraphNode()");

		// reset
		if (getGraphNodeOne() != null) {
			getGraphNodeOne().setBlocked(false);
		}
		if (getGraphNodeTwo() != null) {
			getGraphNodeTwo().setBlocked(false);
		}

		final Direction traverse = getTraverse();
		if (traverse == null) {
			return;
		}

		final Edge edge = getEdge(traverse);
		if (edge != null) {

			final GraphNode inGraphNode = edge.getInGraphNode();

			logger.trace("inGraphNode = " + inGraphNode.getId());

			if (inGraphNode != null) {

				logger.info("inGraphNode GN ID = " + inGraphNode.getId() + " is blocked!");
				inGraphNode.setBlocked(true);
			}
		}
	}

	@Override
	public void free() {

		setHighlighted(false);
		setReserved(false);
		setReservedLocomotiveId(-1);

		// send model changed event
		modelFacade.sendModelChangedEvent(this);
	}

	@Override
	public boolean isReservedExcluding(final int locomotiveId) {
		return reservedLocomotiveId != NOT_RESERVED && reservedLocomotiveId != locomotiveId;
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
		// return isFlipped() == null ? thrown : (isFlipped() ? !thrown : thrown);
		return thrown;
	}

	@Override
	public void setThrown(final boolean thrown) {
		this.thrown = thrown;
	}

	@Override
	public Integer getFeedbackBlockNumber() {
		return feedbackBlockNumber;
	}

	@Override
	public void setFeedbackBlockNumber(final Integer feedbackBlockNumber) {
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

	@Override
	public boolean isReserved() {
		return reserved;
	}

	@Override
	public void setReserved(final boolean reserved) {
		logger.trace("RailNodeID: " + getId() + " Reserved: " + reserved);
		this.reserved = reserved;
	}

	@Override
	public int getReservedLocomotiveId() {
		return reservedLocomotiveId;
	}

	@Override
	public void setReservedLocomotiveId(final int reservedLocomotiveId) {
		if (reservedLocomotiveId != -1) {
			logger.trace("RN-ID: " + getId() + " Reserved for LocomotiveId: " + reservedLocomotiveId);
		}
		this.reservedLocomotiveId = reservedLocomotiveId;
	}

	@Override
	public Block getBlock() {
		return block;
	}

	@Override
	public void setBlock(final Block block) {
		this.block = block;
	}

	@Override
	public Edge[] getEdges() {
		return edges;
	}

	@Override
	public Direction getTraverse() {
		return traverse;
	}

	@Override
	public void setTraverse(final Direction traverse) {
		this.traverse = traverse;
	}

	@Override
	public List<RailNode> getManualConnections() {
		return manualConnections;
	}

	public void setManualConnections(final List<RailNode> manualConnections) {
		this.manualConnections = manualConnections;
	}

	public ModelFacade getModelFacade() {
		return modelFacade;
	}

	public void setModelFacade(final ModelFacade modelFacade) {
		this.modelFacade = modelFacade;
	}

}
