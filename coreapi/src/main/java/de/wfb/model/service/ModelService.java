package de.wfb.model.service;

import java.util.List;
import java.util.Optional;

import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.ui.ShapeType;

public interface ModelService {

	Optional<Node> getNode(int x, int y);

	void nodeClicked(int x, int y);

	void addNode(int x, int y, ShapeType shapeType);

	void storeModel();

	void loadModel();

	void sendModelChangedEvent(int x, int y);

	void connect(Node nodeA, Node nodeB);

	void connectModel();

	void debugRoute();

	Node getNodeById(int id);

	List<GraphNode> getSwitchingNodes();

	RailNode getArbitraryNode();

}
