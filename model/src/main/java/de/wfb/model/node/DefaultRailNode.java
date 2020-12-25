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

	private GraphNode graphNodeThree;

	private GraphNode graphNodeFour;

	private Block block;

	private Integer protocolTurnoutId;

	/** thrown = true ==> */
	private boolean thrown;

	private List<RailNode> manualConnections;

	private List<RailNode> turnoutGroup = new ArrayList<>();

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

		logger.trace("toggleTurnout()");

		if (CollectionUtils.isNotEmpty(getTurnoutGroup())) {

			for (final RailNode railNode : getTurnoutGroup()) {

				logger.trace("toggleTurnout() group: RN-ID: " + railNode.getId());

				railNode.setThrown(!railNode.isThrown());
			}

		} else {

			thrown = !thrown;
		}
	}

	@Override
	public void toggleSignal() {
		thrown = !thrown;
	}

	@Override
	public void switchToGraphNode(final ApplicationEventPublisher applicationEventPublisher,
			final ProtocolFacade protocolFacade, final Model model, final GraphNode currentGraphNode,
			final GraphNode nextGraphNode) {

		logger.trace("switchToGraphNode() RailNode.ID: " + getId());

		final Edge outEdge = findOutEdge(nextGraphNode);
		if (outEdge == null) {

			logger.warn("Switch is not connected to nextGraphNode! Cannot switch!");
			return;
		}

		final Edge inEdge = findInEdge(currentGraphNode);
		if (inEdge == null) {

			logger.warn("Switch is not connected to currentGraphNode! Cannot switch!");
			return;
		}

		logger.trace("IN EDGE: " + inEdge.getDirection().name() + " OUT EDGE: " + outEdge.getDirection().name());

		updateSwitchState(applicationEventPublisher, model, inEdge, outEdge, protocolFacade);
	}

	private void updateSwitchState(final ApplicationEventPublisher applicationEventPublisher, final Model model,
			final Edge inEdge, final Edge outEdge, final ProtocolFacade protocolFacade) {

		logger.trace("ShapeType: " + getShapeType().name() + " Direction: " + outEdge.getDirection().name()
				+ " InEdge: " + inEdge + " OutEdge: " + outEdge);

		boolean newThrown = false;

		switch (getShapeType()) {

		case SWITCH_DOUBLECROSS_LEFT_TOP:
			newThrown = !Direction.isInverseDirection(inEdge.getDirection(), outEdge.getDirection());
			processSwitchState(applicationEventPublisher, model, protocolFacade, newThrown);
			break;

		case SWITCH_DOUBLECROSS_TOP_RIGHT:
			newThrown = !Direction.isInverseDirection(inEdge.getDirection(), outEdge.getDirection());
			processSwitchState(applicationEventPublisher, model, protocolFacade, newThrown);
			break;

		case SWITCH_LEFT_0:
			newThrown = outEdge.getDirection() == Direction.NORTH;
			processSwitchState(applicationEventPublisher, model, protocolFacade, newThrown);
			break;

		case SWITCH_RIGHT_0:
			newThrown = outEdge.getDirection() == Direction.SOUTH;
			processSwitchState(applicationEventPublisher, model, protocolFacade, newThrown);
			break;

		case SWITCH_LEFT_90:
			newThrown = outEdge.getDirection() == Direction.EAST;
			processSwitchState(applicationEventPublisher, model, protocolFacade, newThrown);
			break;

		case SWITCH_RIGHT_90:
			newThrown = outEdge.getDirection() == Direction.WEST;
			processSwitchState(applicationEventPublisher, model, protocolFacade, newThrown);
			break;

		case SWITCH_LEFT_180:
			newThrown = outEdge.getDirection() == Direction.SOUTH;
			processSwitchState(applicationEventPublisher, model, protocolFacade, newThrown);
			break;

		case SWITCH_RIGHT_180:
			newThrown = outEdge.getDirection() == Direction.NORTH;
			processSwitchState(applicationEventPublisher, model, protocolFacade, newThrown);
			break;

		case SWITCH_LEFT_270:
			newThrown = outEdge.getDirection() == Direction.WEST;
			processSwitchState(applicationEventPublisher, model, protocolFacade, newThrown);
			break;

		case SWITCH_RIGHT_270:
			newThrown = outEdge.getDirection() == Direction.EAST;
			processSwitchState(applicationEventPublisher, model, protocolFacade, newThrown);
			break;

		default:
			break;
		}
	}

	/**
	 * Sends a model changed event and a protocol command!
	 *
	 * @param applicationEventPublisher
	 * @param model
	 * @param protocolFacade
	 * @param newThrown
	 */
	private void processSwitchState(final ApplicationEventPublisher applicationEventPublisher, final Model model,
			final ProtocolFacade protocolFacade, final boolean newThrown) {

		boolean oldThrown = isThrown();

		if (isFlipped() != null && isFlipped()) {
			oldThrown = !oldThrown;
		}

		logger.trace("OldThrown: " + oldThrown + " NewThrown: " + newThrown);

		if (newThrown != oldThrown) {

			logger.trace("Switch Turnout. RailNode.ID: " + getId());

			// check turnout group
			if (CollectionUtils.isNotEmpty(turnoutGroup)) {

				logger.info("Update turnout group! size: " + turnoutGroup.size());

				// tell the UI
				for (final RailNode railNode : turnoutGroup) {

					logger.info("Update turnout group - SendingModel event. RN-ID: " + railNode.getId());

					// toggle thrown
					if (railNode.isFlipped() != null && railNode.isFlipped()) {
						railNode.setThrown(!newThrown);
					} else {
						railNode.setThrown(newThrown);
					}

					sendModelChangedEvent(applicationEventPublisher, model, railNode);
				}

			} else {

				logger.trace("Update individual turnout!");

				// toggle thrown
				if (isFlipped() != null && isFlipped()) {
					setThrown(!newThrown);
				} else {
					setThrown(newThrown);
				}

				// tell the UI
				sendModelChangedEvent(applicationEventPublisher, model, this);
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

	private Edge findInEdge(final GraphNode graphNode) {

		for (int i = 0; i < 4; i++) {

			final Edge edge = edges[i];

			if (edge == null || edge.getInGraphNode() == null) {
				continue;
			}

			if (edge.getInGraphNode().equals(graphNode)) {

				return edge;
			}
		}

		return null;
	}

	private Edge findOutEdge(final GraphNode graphNode) {

		for (int i = 0; i < 4; i++) {

			final Edge edge = edges[i];

			if (edge == null || edge.getNextOutGraphNode() == null) {
				continue;
			}

			if (edge.getNextOutGraphNode().equals(graphNode)) {

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

	/**
	 *
	 * @param northNode the node located north of this node
	 */
	private void connectNorth(final RailNode northNode) {

		// find own north edge
		final Edge northEdge = getNorthEdge();
		if (northEdge == null) {

			// DEBUG
			if (getId() == debugRailNodeID) {
				logger.warn("NorthEdge is null!");
			}

			return;
		}

		// find the north node's south edge
		final Edge southEdge = northNode.getSouthEdge();
		if (southEdge == null) {

			// DEBUG
			if (getId() == debugRailNodeID) {
				logger.warn("Returning because of southEdge!");
			}

			return;
		}

		for (final GraphNode tempGraphNode : northEdge.getOutGraphNodes()) {

			if (tempGraphNode.getChildren().contains(southEdge.getInGraphNode())) {

				// DEBUG
				if (getId() == debugRailNodeID) {
					logger.warn("NorthEdge already connected!");
				}

			} else {

				logger.trace("ADDING");

				tempGraphNode.getChildren().add(southEdge.getInGraphNode());
				northEdge.setNextOutGraphNode(southEdge.getInGraphNode());

				// DEBUG
				if (getId() == debugRailNodeID) {

					logger.warn("Connecting North GN " + tempGraphNode.getId() + " to GN "
							+ southEdge.getInGraphNode().getId());
				}
			}
		}

		for (final GraphNode tempGraphNode : southEdge.getOutGraphNodes()) {

			if (tempGraphNode.getChildren().contains(northEdge.getInGraphNode())) {

				// DEBUG
				if (getId() == debugRailNodeID) {
					logger.warn("SouthEdge already connected!");
				}

			} else {

				logger.trace("ADDING");

				tempGraphNode.getChildren().add(northEdge.getInGraphNode());
				southEdge.setNextOutGraphNode(northEdge.getInGraphNode());
			}
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

		for (final GraphNode tempGraphNode : eastEdge.getOutGraphNodes()) {

			if (!tempGraphNode.getChildren().contains(westEdge.getInGraphNode())) {

				logger.trace("ADDING");

				tempGraphNode.getChildren().add(westEdge.getInGraphNode());
				eastEdge.setNextOutGraphNode(westEdge.getInGraphNode());

				// DEBUG
				if (getId() == debugRailNodeID) {

					logger.warn("Connecting East GN " + tempGraphNode.getId() + " to GN "
							+ westEdge.getInGraphNode().getId());
				}
			}
		}

		for (final GraphNode tempGraphNode : westEdge.getOutGraphNodes()) {

			if (!tempGraphNode.getChildren().contains(eastEdge.getInGraphNode())) {

				logger.trace("ADDING");

				tempGraphNode.getChildren().add(eastEdge.getInGraphNode());
				westEdge.setNextOutGraphNode(eastEdge.getInGraphNode());
			}
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

		for (final GraphNode tempGraphNode : southEdge.getOutGraphNodes()) {

			if (!tempGraphNode.getChildren().contains(northEdge.getInGraphNode())) {

				logger.trace("ADDING");

				tempGraphNode.getChildren().add(northEdge.getInGraphNode());
				southEdge.setNextOutGraphNode(northEdge.getInGraphNode());

				// DEBUG
				if (getId() == debugRailNodeID) {

					logger.warn("Connecting South GN " + tempGraphNode.getId() + " to GN "
							+ northEdge.getInGraphNode().getId());
				}
			}
		}

		for (final GraphNode tempGraphNode : northEdge.getOutGraphNodes()) {

			if (!tempGraphNode.getChildren().contains(southEdge.getInGraphNode())) {

				logger.trace("ADDING");

				tempGraphNode.getChildren().add(southEdge.getInGraphNode());
				northEdge.setNextOutGraphNode(southEdge.getInGraphNode());
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

		for (final GraphNode tempGraphNode : westEdge.getOutGraphNodes()) {

			if (!tempGraphNode.getChildren().contains(eastEdge.getInGraphNode())) {

				logger.trace("ADDING");

				tempGraphNode.getChildren().add(eastEdge.getInGraphNode());
				westEdge.setNextOutGraphNode(eastEdge.getInGraphNode());

				// DEBUG
				if (getId() == debugRailNodeID) {

					logger.warn("Connecting West GN " + tempGraphNode.getId() + " to GN "
							+ eastEdge.getInGraphNode().getId());
				}
			}
		}

		for (final GraphNode tempGraphNode : eastEdge.getOutGraphNodes()) {

			if (!tempGraphNode.getChildren().contains(westEdge.getInGraphNode())) {

				logger.trace("ADDING");

				tempGraphNode.getChildren().add(westEdge.getInGraphNode());
				eastEdge.setNextOutGraphNode(westEdge.getInGraphNode());
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

					for (final GraphNode tempGraphNode : northEdge.getOutGraphNodes()) {
						tempGraphNode.getChildren().remove(innerEdge.getInGraphNode());
					}
					northEdge.setNextOutGraphNode(null);

					for (final GraphNode tempGraphNode : innerEdge.getOutGraphNodes()) {
						tempGraphNode.getChildren().remove(northEdge.getInGraphNode());
					}
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

					for (final GraphNode tempGraphNode : eastEdge.getOutGraphNodes()) {
						tempGraphNode.getChildren().remove(innerEdge.getInGraphNode());
					}
					eastEdge.setNextOutGraphNode(null);

					for (final GraphNode tempGraphNode : innerEdge.getOutGraphNodes()) {
						tempGraphNode.getChildren().remove(eastEdge.getInGraphNode());
					}
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

					for (final GraphNode tempGraphNode : southEdge.getOutGraphNodes()) {
						tempGraphNode.getChildren().remove(innerEdge.getInGraphNode());
					}
					southEdge.setNextOutGraphNode(null);

					for (final GraphNode tempGraphNode : innerEdge.getOutGraphNodes()) {
						tempGraphNode.getChildren().remove(southEdge.getInGraphNode());
					}
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

					for (final GraphNode tempGraphNode : westEdge.getOutGraphNodes()) {
						tempGraphNode.getChildren().remove(innerEdge.getInGraphNode());
					}
					westEdge.setNextOutGraphNode(null);

					for (final GraphNode tempGraphNode : innerEdge.getOutGraphNodes()) {
						tempGraphNode.getChildren().remove(westEdge.getInGraphNode());
					}
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

		final String xFormatted = String.format("% 4d", getX());
		final String yFormatted = String.format("% 4d", getY());

		final String coordinatesFormatted = "(" + xFormatted + ", " + yFormatted + ")";

		// @formatter:off

		stringBuffer
			.append("ID: ").append(idFormatted)
			.append("    (x, y): ").append(coordinatesFormatted)
			.append("    ST: ").append(shapeTypeFormatted);

		if (graphNodeOne != null) {
			final String graphNodeOneIdFormatted = String.format("% 4d", graphNodeOne.getId());
			stringBuffer.append("    GN-1: ").append(graphNodeOneIdFormatted);
		}

		if (graphNodeTwo != null) {
			final String graphNodeTwoIdFormatted = String.format("% 4d", graphNodeTwo.getId());
			stringBuffer.append("    GN-2: ").append(graphNodeTwoIdFormatted);
		}

		if (graphNodeThree != null) {
			final String graphNodeThreeIdFormatted = String.format("% 4d", graphNodeThree.getId());
			stringBuffer.append("    GN-3: ").append(graphNodeThreeIdFormatted);
		}

		if (graphNodeFour != null) {
			final String graphNodeFourIdFormatted = String.format("% 4d", graphNodeFour.getId());
			stringBuffer.append("    GN-4: ").append(graphNodeFourIdFormatted);
		}

		// @formatter:on

		return stringBuffer.toString();
	}

	@SuppressWarnings("unused")
	private String toStringLineBreaks() {

		// @formatter:off

		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("\n");

		stringBuffer.append("ID: ").append(getId());

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
		stringBuffer.append(graphNodeThree.getId()).append(" -> ");
		if (CollectionUtils.isNotEmpty(graphNodeThree.getChildren())) {

			for (final GraphNode childGraphNode : graphNodeThree.getChildren()) {
				stringBuffer.append(childGraphNode.getId()).append(", ");
			}
		}

		stringBuffer.append("\n");
		stringBuffer.append(graphNodeFour.getId()).append(" -> ");
		if (CollectionUtils.isNotEmpty(graphNodeFour.getChildren())) {

			for (final GraphNode childGraphNode : graphNodeFour.getChildren()) {
				stringBuffer.append(childGraphNode.getId()).append(", ");
			}
		}

		stringBuffer.append("\n");
		stringBuffer
			.append("GraphNode One: ")
			.append(getGraphNodeOne());

		stringBuffer.append("\n");
		stringBuffer
			.append("GraphNode Two: ")
			.append(getGraphNodeTwo());

		stringBuffer.append("\n");
		stringBuffer
			.append("GraphNode Three: ")
			.append(getGraphNodeThree());

		stringBuffer.append("\n");
		stringBuffer
			.append("GraphNode Four: ")
			.append(getGraphNodeFour());

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
		if (getGraphNodeThree() != null) {
			getGraphNodeThree().setBlocked(false);
		}
		if (getGraphNodeFour() != null) {
			getGraphNodeFour().setBlocked(false);
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
	public GraphNode getGraphNodeThree() {
		return graphNodeThree;
	}

	@Override
	public void setGraphNodeThree(final GraphNode graphNodeThree) {
		this.graphNodeThree = graphNodeThree;
	}

	@Override
	public GraphNode getGraphNodeFour() {
		return graphNodeFour;
	}

	@Override
	public void setGraphNodeFour(final GraphNode graphNodeFour) {
		this.graphNodeFour = graphNodeFour;
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

	@Override
	public void setModelFacade(final ModelFacade modelFacade) {
		this.modelFacade = modelFacade;
	}

	@Override
	public List<RailNode> getTurnoutGroup() {
		return turnoutGroup;
	}

	public void setTurnoutGroup(final List<RailNode> turnoutGroup) {
		this.turnoutGroup = turnoutGroup;
	}

}
