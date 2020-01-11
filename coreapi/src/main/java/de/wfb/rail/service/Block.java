package de.wfb.rail.service;

import java.util.List;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.RailNode;

public interface Block {

	int getId();

	void addNode(RailNode railNode);

	List<RailNode> getNodes();

	void reserveByLocomotive(DefaultLocomotive defaultLocomotive);

	boolean isReserved();

}
