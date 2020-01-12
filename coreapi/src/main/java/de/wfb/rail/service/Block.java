package de.wfb.rail.service;

import java.util.List;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.RailNode;

/**
 * Feedback block.
 *
 * Feedback blocks are an area of RailNodes on the Layout. They have a physical
 * counterpart on the real world layout.
 *
 * The Intellibox can send events when rolling stock enters Feedback Blocks and
 * when rolling stock leaves Feedback blocks.
 */
public interface Block {

	int getId();

	void addNode(RailNode railNode);

	List<RailNode> getNodes();

	void reserveForLocomotive(DefaultLocomotive defaultLocomotive);

	boolean isReserved();

}
