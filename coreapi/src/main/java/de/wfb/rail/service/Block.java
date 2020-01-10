package de.wfb.rail.service;

import de.wfb.model.node.RailNode;

public interface Block {

	int getId();

	void addNode(RailNode railNode);

}
