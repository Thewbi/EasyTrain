package de.wfb.model.node;

import java.util.ArrayList;
import java.util.HashMap;
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

	private final Map<Integer, Set<GraphNode>> routingTable = new HashMap<>();

	private RailNode railNode;

	private Color color = Color.NONE;

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public List<GraphNode> getChildren() {
		return children;
	}

	public Map<Integer, Set<GraphNode>> getRoutingTable() {
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

	public Color getColor() {
		return color;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

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

		final StringBuffer stringBuffer = new StringBuffer();

		if (!routingTable.entrySet().isEmpty()) {

			for (final Map.Entry<Integer, Set<GraphNode>> entry : routingTable.entrySet()) {

				stringBuffer.append("\n").append("Reach ").append(entry.getKey()).append(" via ")
						.append(entry.getValue().toString());
			}
		}

		return stringBuffer.toString();
	}

	public String dumpSwitchingTable() {

		final StringBuffer stringBuffer = new StringBuffer();

		if (!switchingGraphNodeChildren.isEmpty()) {

			for (final SwitchingNodeEntry entry : switchingGraphNodeChildren) {

				stringBuffer.append("\n").append("Reach Switch ").append(entry.getSwitchingGraphNode().getId())
						.append(" via ").append(entry.getConnectingGraphNode().getId());
			}
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

			final GraphNode outGraphNode = edge.getOutGraphNode();

			if (outGraphNode == null) {
				continue;
			}

			if (outGraphNode.equals(this)) {
				return edges[i].getDirection();
			}
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

}
