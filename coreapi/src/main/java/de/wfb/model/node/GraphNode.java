package de.wfb.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphNode {

	private int id;

	private final List<GraphNode> children = new ArrayList<>();

	private final List<SwitchingNodeEntry> switchingGraphNodeChildren = new ArrayList<>();

	private final Map<Integer, GraphNode> routingTable = new HashMap<>();

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

	public Map<Integer, GraphNode> getRoutingTable() {
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

		stringBuffer.append("ID: ").append(getId()).append("\n");

		for (final Map.Entry<Integer, GraphNode> entry : routingTable.entrySet()) {

			stringBuffer.append("Reach ").append(entry.getKey()).append(" via ").append(entry.getValue().getId())
					.append("\n");
		}

		return stringBuffer.toString();
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

}
