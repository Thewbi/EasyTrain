package de.wfb.model.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.RailNode;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.BlockGroup;

/**
 * Implementation of a Feedback Block.
 */
public class DefaultBlock implements Block {

	private static final Logger logger = LogManager.getLogger(DefaultBlock.class);

	private int id;

	private final List<RailNode> nodes = new ArrayList<>();

	private BlockGroup blockGroup;

	@Override
	public void addNode(final RailNode railNode) {
		nodes.add(railNode);
	}

	@Override
	public void reserveForLocomotive(final DefaultLocomotive locomotive) {

		if (blockGroup == null) {
			reserveForLocomotiveSingular(locomotive);
		} else {
			blockGroup.reserveForLocomotive(locomotive);
		}
	}

	@Override
	public void reserveForLocomotiveSingular(final DefaultLocomotive locomotive) {

		// TODO: if the
		// TODO: check if the block is part of a block group and if so reserved all the
		// other blocks in th group
		// .... block.reserveForLocomotive(locomotive);

		if (CollectionUtils.isEmpty(getNodes())) {
			return;
		}

		logger.info("Reserve Block: " + getId() + " for locomotive: " + locomotive.getId());

		// reserve all the block's nodes for this locomotive
		for (final RailNode blockRailNode : getNodes()) {

			if (locomotive == null) {

				blockRailNode.setReserved(false);
				blockRailNode.setReservedLocomotiveId(-1);

			} else {

				if (blockRailNode.isReserved() && (blockRailNode.getReservedLocomotiveId() != locomotive.getId())) {

					throw new IllegalArgumentException(
							"Block is reserved already by Locomotive " + blockRailNode.getReservedLocomotiveId());
				}

				blockRailNode.setReserved(true);
				blockRailNode.setReservedLocomotiveId(locomotive.getId());
			}
		}
	}

	@Override
	public void free() {

		// TODO: if this block is part of a block group, free all the other blocks in
		// the group. Prevent recursion!
		if (blockGroup == null) {
			freeSingular();
		} else {
			blockGroup.free();
		}
	}

	@Override
	public void freeSingular() {

		logger.trace("block.getNodes().size: " + getNodes().size());

		getNodes().stream().filter(node -> node.isReserved()).forEach(node -> {

			logger.trace("Resetting node ID = " + node.getId() + " in block!");

			node.free();
		});
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
	public boolean isFeedbackBlockUsed() {

		if (CollectionUtils.isEmpty(getNodes())) {
			return false;
		}

		for (final RailNode blockRailNode : getNodes()) {

			if (blockRailNode.isFeedbackBlockUsed()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int getReservedForLocomotive() {

		if (CollectionUtils.isEmpty(getNodes())) {
			return -1;
		}

		for (final RailNode blockRailNode : getNodes()) {

			if (blockRailNode.isReserved()) {
				return blockRailNode.getReservedLocomotiveId();
			}
		}

		return -1;
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

	public BlockGroup getBlockGroup() {
		return blockGroup;
	}

	@Override
	public void setBlockGroup(final BlockGroup blockGroup) {
		this.blockGroup = blockGroup;
	}

}
