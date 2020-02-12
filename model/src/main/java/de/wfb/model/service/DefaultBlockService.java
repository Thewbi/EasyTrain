package de.wfb.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.BlockGroup;
import de.wfb.rail.service.BlockService;

public class DefaultBlockService implements BlockService {

	private static final Logger logger = LogManager.getLogger(DefaultBlockService.class);

	private final HashMap<Integer, Block> idBlockMap = new HashMap<>();

	private final List<BlockGroup> blockGroups = new ArrayList<BlockGroup>();

	@Autowired
	private ModelService modelService;

	@Override
	public void determineBlocks() {

		final List<RailNode> allRailNodes = modelService.getAllRailNodes();

		if (CollectionUtils.isEmpty(allRailNodes)) {
			return;
		}

		for (final RailNode railNode : allRailNodes) {

			processBlock(railNode);
		}
	}

	private void processBlock(final RailNode railNode) {

		final Integer feedbackBlockNumber = railNode.getFeedbackBlockNumber();
		if (feedbackBlockNumber == null || feedbackBlockNumber < 0) {
			return;
		}

		if (!idBlockMap.containsKey(feedbackBlockNumber)) {

			final DefaultBlock defaultBlock = new DefaultBlock();
			defaultBlock.setId(feedbackBlockNumber);

			logger.trace("Adding Block " + feedbackBlockNumber);

			idBlockMap.put(feedbackBlockNumber, defaultBlock);
		}

		final Block block = idBlockMap.get(feedbackBlockNumber);

		logger.trace("Inserting node " + railNode.getId() + " into Block " + block.getId());

		block.addNode(railNode);
		railNode.setBlock(block);
	}

	@Override
	public List<Block> getAllBlocks() {

		final List<Block> result = new ArrayList<>();

		for (final Map.Entry<Integer, Block> entry : idBlockMap.entrySet()) {

			result.add(entry.getValue());
		}

		return result;
	}

	@Override
	public Block getBlockById(final int feedbackBlockNumber) {

		if (!idBlockMap.containsKey(feedbackBlockNumber)) {
			return null;
		}
		return idBlockMap.get(feedbackBlockNumber);
	}

	@Override
	public Block getBlockByGraphNode(final GraphNode graphNode) {

		final RailNode railNode = graphNode.getRailNode();

		if (null == railNode) {
			return null;
		}

		final Integer feedbackBlockNumber = railNode.getFeedbackBlockNumber();
		if (feedbackBlockNumber == null || feedbackBlockNumber < 0) {
			return null;
		}

		if (idBlockMap.containsKey(feedbackBlockNumber)) {

			return idBlockMap.get(feedbackBlockNumber);
		}

		return null;
	}

	@Override
	public void createBlockGroups() {

		if (CollectionUtils.isEmpty(idBlockMap.values())) {
			return;
		}

		final BlockGroup blockGroup = new DefaultBlockGroup();

		// 17,21,28,29,30,43,44,45,46,47
		addBlockToBlockGroup(17, blockGroup);
		addBlockToBlockGroup(21, blockGroup);
		addBlockToBlockGroup(28, blockGroup);
		addBlockToBlockGroup(29, blockGroup);
		addBlockToBlockGroup(30, blockGroup);
		addBlockToBlockGroup(43, blockGroup);
		addBlockToBlockGroup(44, blockGroup);
		addBlockToBlockGroup(45, blockGroup);
		addBlockToBlockGroup(46, blockGroup);
		addBlockToBlockGroup(47, blockGroup);

		blockGroups.add(blockGroup);
	}

	private void addBlockToBlockGroup(final int blockId, final BlockGroup blockGroup) {
		final Block block = idBlockMap.get(blockId);
		blockGroup.getBlocks().add(block);
		block.setBlockGroup(blockGroup);
	}

	public List<BlockGroup> getBlockGroups() {
		return blockGroups;
	}

}
