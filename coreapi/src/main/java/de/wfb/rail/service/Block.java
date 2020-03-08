package de.wfb.rail.service;

import java.util.List;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.RailNode;

/**
 * Feedback block.<br />
 * <br />
 *
 * Feedback blocks are an area of RailNodes on the Layout. They have a physical
 * counterpart on the real world layout.<br />
 * <br />
 *
 * The Intellibox can send events when rolling stock enters Feedback Blocks and
 * when rolling stock leaves Feedback blocks.<br />
 * <br />
 */
public interface Block {

	int getId();

	void addNode(RailNode railNode);

	List<RailNode> getNodes();

	void reserveForLocomotive(Locomotive locomotive);

	void reserveForLocomotiveSingular(Locomotive locomotive);

	boolean isReserved();

	boolean isFeedbackBlockUsed();

	int getReservedForLocomotive();

	void free();

	void freeSingular();

	void setBlockGroup(BlockGroup blockGroup);

}
