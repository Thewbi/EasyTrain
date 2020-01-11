package de.wfb.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import de.wfb.model.node.RailNode;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.BlockService;

public class DefaultBlockService implements BlockService {

	private static final Logger logger = LogManager.getLogger(DefaultBlockService.class);

	private final HashMap<Integer, Block> idBlockMap = new HashMap<>();

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

		final int feedbackBlockNumber = railNode.getFeedbackBlockNumber();

		if (feedbackBlockNumber < 0) {
			return;
		}

		if (!idBlockMap.containsKey(feedbackBlockNumber)) {

			final DefaultBlock defaultBlock = new DefaultBlock();
			defaultBlock.setId(feedbackBlockNumber);

			logger.info("Adding Block " + feedbackBlockNumber);

			idBlockMap.put(feedbackBlockNumber, defaultBlock);
		}

		final Block block = idBlockMap.get(feedbackBlockNumber);

		logger.info("Inserting node " + railNode.getId() + " into Block " + block.getId());

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

}
