package de.wfb.rail.service;

import java.util.List;

import de.wfb.model.node.GraphNode;

public interface BlockService {

	void determineBlocks();

	List<Block> getAllBlocks();

	Block getBlockById(int feedbackBlockNumber);

	Block getBlockByGraphNode(GraphNode graphNode);

}
