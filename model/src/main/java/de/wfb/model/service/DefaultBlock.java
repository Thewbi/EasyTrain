package de.wfb.model.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.RailNode;
import de.wfb.rail.service.Block;

public class DefaultBlock implements Block {

	private int id;

	private final List<RailNode> nodes = new ArrayList<>();

	@Override
	public void addNode(final RailNode railNode) {
		nodes.add(railNode);
	}

	@Override
	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Override
	public void reserveByLocomotive(final DefaultLocomotive defaultLocomotive) {

		if (CollectionUtils.isEmpty(getNodes())) {
			return;
		}

		// reserve all the block's nodes for this locomotive
		for (final RailNode blockRailNode : getNodes()) {

			if (blockRailNode.isReserved()) {

				throw new IllegalArgumentException(
						"Block is reserved already by Locomotive " + blockRailNode.getReservedLocomotiveId());
			}

			blockRailNode.setReserved(true);
			blockRailNode.setReservedLocomotiveId(defaultLocomotive.getId());
		}
	}

	@Override
	public boolean isReserved() {

		if (CollectionUtils.isEmpty(getNodes())) {
			return false;
		}

		for (final RailNode blockRailNode : getNodes()) {

			if (blockRailNode.isReserved()) {
				return true;
			}
		}

		return false;
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

	@Override
	public List<RailNode> getNodes() {
		return nodes;
	}

	@Override
	public String toString() {
		return "Block " + id + "";
	}

}
