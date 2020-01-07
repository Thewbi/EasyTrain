package de.wfb.model.node;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.Model;

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
	public void connectTo(final RailNode railNodeB) {

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
				}

				if (!innerEdge.getOutGraphNode().getChildren().contains(westEdge.getInGraphNode())) {
					innerEdge.getOutGraphNode().getChildren().add(westEdge.getInGraphNode());
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
				}

				if (!innerEdge.getOutGraphNode().getChildren().contains(southEdge.getInGraphNode())) {
					innerEdge.getOutGraphNode().getChildren().add(southEdge.getInGraphNode());
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
				}

				if (!innerEdge.getOutGraphNode().getChildren().contains(eastEdge.getInGraphNode())) {
					innerEdge.getOutGraphNode().getChildren().add(eastEdge.getInGraphNode());
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
				}

				if (!innerEdge.getOutGraphNode().getChildren().contains(northEdge.getInGraphNode())) {
					innerEdge.getOutGraphNode().getChildren().add(northEdge.getInGraphNode());
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
					innerEdge.getOutGraphNode().getChildren().remove(northEdge.getInGraphNode());
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
					innerEdge.getOutGraphNode().getChildren().remove(eastEdge.getInGraphNode());
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
					innerEdge.getOutGraphNode().getChildren().remove(southEdge.getInGraphNode());
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
					innerEdge.getOutGraphNode().getChildren().remove(westEdge.getInGraphNode());
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
	public void toggleTurnout() {

		logger.info("toggle()");

		thrown = !thrown;
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

}
