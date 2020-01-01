package de.wfb.model;

import java.util.Map;

import de.wfb.model.node.Node;

public interface Model {

	Node getNode(final int x, final int y);

	void setNode(final int x, final int y, final Node node);

	void removeNode(int x, int y);

	Map<Integer, Node> getIdMap();

}
