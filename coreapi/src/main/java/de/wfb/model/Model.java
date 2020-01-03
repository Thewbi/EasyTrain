package de.wfb.model;

import java.util.Map;

import de.wfb.model.node.Node;

public interface Model {

	Node getNode(int x, int y);

	void setNode(int x, int y, Node node);

	Node getSelectedNode();

	void setSelectedNode(Node selectedNode);

	void removeNode(int x, int y);

	Map<Integer, Node> getIdMap();

}
