package de.wfb.model.driving;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.rail.service.Block;

public class LocomotiveEntry {

	private Locomotive locomotive;

	private GraphNode startGraphNode;

	private Block startBlock;

	private final List<Block> visitedBlocks = new ArrayList<>();

	public Locomotive getLocomotive() {
		return locomotive;
	}

	public void setLocomotive(final Locomotive locomotive) {
		this.locomotive = locomotive;
	}

	public GraphNode getStartGraphNode() {
		return startGraphNode;
	}

	public void setStartGraphNode(final GraphNode startGraphNode) {
		this.startGraphNode = startGraphNode;
	}

	public Block getStartBlock() {
		return startBlock;
	}

	public void setStartBlock(final Block startBlock) {
		this.startBlock = startBlock;
	}

	public List<Block> getVisitedBlocks() {
		return visitedBlocks;
	}

	public Block getCurrentBlock() {

		if (CollectionUtils.isEmpty(visitedBlocks)) {
			return startBlock;
		}

		return visitedBlocks.get(visitedBlocks.size() - 1);
	}

	@Override
	public String toString() {
		return "LocomotiveEntry [locomotive=" + locomotive + ", startGraphNode=" + startGraphNode + ", startBlock="
				+ startBlock + ", visitedBlocks=" + visitedBlocks + "]";
	}
}
