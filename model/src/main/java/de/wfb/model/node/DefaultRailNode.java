package de.wfb.model.node;

import org.apache.commons.collections4.CollectionUtils;

import de.wfb.model.Model;

public class DefaultRailNode extends Node implements RailNode {

	private static final int NORTH_INDEX = 0;

	private static final int EAST_INDEX = 1;

	private static final int SOUTH_INDEX = 2;

	private static final int WEST_INDEX = 3;

	private final Edge[] edges = new Edge[4];

	/** for turnouts, One-Nodes exit the rail towards one ends */
	private GraphNode graphNodeOne;

	/** for turnouts, Two-Nodes exit the rail towards the two ends */
	private GraphNode graphNodeTwo;

	public DefaultRailNode() {

		for (int i = 0; i < 4; i++) {
			edges[i] = null;
		}
	}

	@Override
	public void connect(final Model model) {

		// north
		final Edge northEdge = getNorthEdge();
		if (northEdge != null) {

			final RailNode northNode = (RailNode) model.getNode(getX(), getY() - 1);

			if (northNode != null) {

				final Edge innerEdge = northNode.getSouthEdge();
				if (innerEdge != null) {

					northEdge.getOutGraphNode().getChildren().add(innerEdge.getInGraphNode());
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

					eastEdge.getOutGraphNode().getChildren().add(innerEdge.getInGraphNode());
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

					southEdge.getOutGraphNode().getChildren().add(innerEdge.getInGraphNode());
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

					westEdge.getOutGraphNode().getChildren().add(innerEdge.getInGraphNode());
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

}
