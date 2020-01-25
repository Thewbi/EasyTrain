package de.wfb.model.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.RailNode;
import de.wfb.rail.service.Block;

/**
 * Implementation of a Feedback Block.
 */
public class DefaultBlock implements Block {

	private int id;

	private final List<RailNode> nodes = new ArrayList<>();

	@Override
	public void addNode(final RailNode railNode) {
		nodes.add(railNode);
	}

	@Override
	public void reserveForLocomotive(final DefaultLocomotive locomotive) {

		if (CollectionUtils.isEmpty(getNodes())) {
			return;
		}

		// reserve all the block's nodes for this locomotive
		for (final RailNode blockRailNode : getNodes()) {

			if (locomotive == null) {

				blockRailNode.setReserved(false);
				blockRailNode.setReservedLocomotiveId(-1);

			} else {

				if (blockRailNode.isReserved()) {

					throw new IllegalArgumentException(
							"Block is reserved already by Locomotive " + blockRailNode.getReservedLocomotiveId());
				}

				blockRailNode.setReserved(true);
				blockRailNode.setReservedLocomotiveId(locomotive.getId());
			}
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
	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Block ID: " + id + "";
	}

}
