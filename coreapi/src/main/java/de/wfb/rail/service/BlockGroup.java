package de.wfb.rail.service;

import java.util.List;

import de.wfb.model.locomotive.Locomotive;

public interface BlockGroup {

	void reserveForLocomotive(Locomotive locomotive);

	void free();

	List<Block> getBlocks();

}
