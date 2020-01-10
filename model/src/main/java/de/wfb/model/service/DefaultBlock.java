package de.wfb.model.service;

import java.util.ArrayList;
import java.util.List;

import de.wfb.model.node.RailNode;
import de.wfb.rail.service.Block;

public class DefaultBlock implements Block {

	private int id;

	private final List<RailNode> nodes = new ArrayList<>();

	@Override
	public void addNode(final RailNode railNode) {
		nodes.add(railNode);
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
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
		final DefaultBlock other = (DefaultBlock) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
