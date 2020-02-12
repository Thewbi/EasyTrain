package de.wfb.rail.service;

import java.util.List;

import de.wfb.model.node.GraphNode;

public interface BlockService {

	/**
	 * Blocks contain several RailNodes. Blocks are not persisted other than that a
	 * RailNode stores the id of the block it belongs to. This method will inspect
	 * all RailNodes and build in memory Block objects from the RailNode's block
	 * ids.
	 */
	void determineBlocks();

	List<Block> getAllBlocks();

	Block getBlockById(int feedbackBlockNumber);

	Block getBlockByGraphNode(GraphNode graphNode);

	void createBlockGroups();

}
