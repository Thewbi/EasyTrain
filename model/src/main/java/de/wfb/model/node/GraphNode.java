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

}
