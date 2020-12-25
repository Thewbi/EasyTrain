package de.wfb.model.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.BlockGroup;

public class DefaultBlockGroup implements BlockGroup {

	private static final Logger logger = LogManager.getLogger(DefaultBlockGroup.class);

	private final List<Block> blocks = new ArrayList<>();

	@Override
	public void reserveForLocomotive(final Locomotive locomotive) {

		if (CollectionUtils.isEmpty(blocks)) {
			return;
		}

		for (final Block block : blocks) {

			logger.trace("reserving by group: BlockID: " + block.getId());

			block.reserveForLocomotiveSingular(locomotive);
		}
	}

	@Override
	public void free() {

		if (CollectionUtils.isEmpty(blocks)) {
			return;
		}

		for (final Block block : blocks) {

			logger.trace("freeing by group: BlockID: " + block.getId());

			block.freeSingular();
		}
	}

	@Override
	public List<Block> getBlocks() {
		return blocks;
	}

}
