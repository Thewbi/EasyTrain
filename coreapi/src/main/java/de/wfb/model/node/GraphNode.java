package de.wfb.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GraphNode {

	private static final boolean DUMP_ROUTING_TABLE = false;

	private static final Logger logger = LogManager.getLogger(GraphNode.class);

	private int id;

	private int x;

	private int y;

	private final List<GraphNode> children = new ArrayList<>();

	private final List<SwitchingNodeEntry> switchingGraphNodeChildren = new ArrayList<>();

//	private final Map<Integer, Set<GraphNode>> routingTable = new HashMap<>();
	private final Map<Integer, Set<GraphNodeEntry>> routingTable = new HashMap<>();

	private RailNode railNode;

//	private Color color = Color.NONE;

	/**
	 * blocking a graph node, means to make a rail node non-traversable in this
	 * direction
	 */
	private boolean blocked;

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public List<GraphNode> getChildren() {
		return children;
	}

//	public Map<Integer, Set<GraphNode>> getRoutingTable() {
//		return routingTable;
//	}

	public Map<Integer, Set<GraphNodeEntry>> getRoutingTable() {
		return routingTable;
	}

	public List<SwitchingNodeEntry> getSwitchingGraphNodeChildren() {
		return switchingGraphNodeChildren;
	}

	public RailNode getRailNode() {
		return railNode;
	}

	public void setRailNode(final RailNode railNode) {
		this.railNode = railNode;
	}

//	public Color getColor() {
//		return color;
//	}
//
//	public void setColor(final Color color) {
//		this.color = color;
//	}

	@Override
	public String toString() {

		final StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append("ID: ").append(getId());

//		if (DUMP_ROUTING_TABLE) {
//
//			if (!routingTable.entrySet().isEmpty()) {
//
//				for (final Map.Entry<Integer, Set<GraphNode>> entry : routingTable.entrySet()) {
//
//					stringBuffer.append("\n").append("Reach ").append(entry.getKey()).append(" via ")
//							.append(entry.getValue().getId());
//				}
//			}
//		}

		return stringBuffer.toString();
	}

	public String dumpRoutingTable() {

		if (routingTable.entrySet().isEmpty()) {
			return "Empty";
		}

		final StringBuffer stringBuffer = new StringBuffer();

		for (final Map.Entry<Integer, Set<GraphNodeEntry>> entry : routingTable.entrySet()) {

			stringBuffer.append("\n").append("Reach ").append(entry.getKey()).append(" via ");

			final Set<GraphNodeEntry> entrySet = entry.getValue();
			final Iterator<GraphNodeEntry> entrySetIterator = entrySet.iterator();
			while (entrySetIterator.hasNext()) {
				final GraphNodeEntry graphNodeEntry = entrySetIterator.next();

				stringBuffer.append(graphNodeEntry.getGraphNode().getId()).append(",");
			}
		}

		return stringBuffer.toString();
	}

	public String dumpSwitchingTable() {

		if (switchingGraphNodeChildren.isEmpty()) {
			return "Empty";
		}

		final StringBuffer stringBuffer = new StringBuffer();

		for (final SwitchingNodeEntry entry : switchingGraphNodeChildren) {

			stringBuffer.append("\n").append("Reach Switch ").append(entry.getSwitchingGraphNode().getId())
					.append(" via ").append(entry.getConnectingGraphNode().getId());
		}

		return stringBuffer.toString();
	}

	public Direction getExitDirection() {

		logger.trace(railNode);

		final Edge[] edges = railNode.getEdges();

		for (int i = 0; i < 4; i++) {

			final Edge edge = edges[i];

			if (edge == null) {
				continue;
			}

//			final GraphNode outGraphNode = edge.getOutGraphNode();
			final GraphNode outGraphNode = edge.getOutGraphNodes().get(0);

			if (outGraphNode == null) {
				continue;
			}

			if (outGraphNode.equals(this)) {
				return edges[i].getDirection();
			}
		}

		return null;
	}

	public Direction getDirection() {

		for (int i = 0; i < 4; i++) {

			final Edge edge = getRailNode().getEdges()[i];
			if (edge == null) {
				continue;
			}

			for (final GraphNode outGraphNode : edge.getOutGraphNodes()) {

				if (outGraphNode.equals(this)) {

					return edge.getDirection();
				}
			}
		}

		return null;
	}

	public Direction getInverseDirection() {

		final Direction dir = getDirection();

		if (dir == null) {
			return null;
		}

		if (dir == Direction.NONE) {
			return Direction.NONE;
		}

		if (dir == Direction.NORTH) {
			return Direction.SOUTH;
		}

		if (dir == Direction.EAST) {
			return Direction.WEST;
		}

		if (dir == Direction.SOUTH) {
			return Direction.NORTH;
		}

		if (dir == Direction.WEST) {
			return Direction.EAST;
		}

		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final GraphNode other = (GraphNode) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int getX() {
		return x;
	}

	public void setX(final int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(final int y) {
		this.y = y;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(final boolean blocked) {
		this.blocked = blocked;
	}

}
