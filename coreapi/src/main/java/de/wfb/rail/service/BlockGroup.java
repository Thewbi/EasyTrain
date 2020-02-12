package de.wfb.rail.service;

import java.util.List;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.rail.service.Block;

public interface BlockGroup {

	void reserveForLocomotive(DefaultLocomotive locomotive);

	void free();

	List<Block> getBlocks();

}
