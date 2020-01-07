package de.wfb.model.node;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {

	private int id;

	private final List<GraphNode> children = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public List<GraphNode> getChildren() {
		return children;
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
