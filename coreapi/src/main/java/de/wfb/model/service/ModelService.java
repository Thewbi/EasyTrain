package de.wfb.model.service;

import java.util.List;
import java.util.Optional;

import de.wfb.model.Model;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.ui.ShapeType;

public interface ModelService {

	Optional<Node> getNode(int x, int y);

	void nodeClicked(int x, int y, boolean shiftClicked);

	void addNode(int x, int y, ShapeType shapeType);

	void storeModel();

	void storeLocomotiveModel();

	void loadModel();

	void sendModelChangedEvent(int x, int y, boolean hightlighted, boolean blocked, boolean selected);

	void sendModelChangedEvent(RailNode railNode);

	void manualConnectTo(Node nodeA, Node nodeB);

	void connectModel();

	void debugRoute();

	Node getNodeById(int id);

	List<GraphNode> getSwitchingNodes();

	RailNode getArbitraryNode();

	Model getModel();

	void resetGraphColors();

	List<Node> getSelectedNodes();

	List<RailNode> getAllRailNodes();

	List<DefaultLocomotive> getLocomotives();

	void addLocomotive(DefaultLocomotive defaultLocomotive);

	void deleteLocomotive(DefaultLocomotive defaultLocomotive);

	int retrieveNextLocomotiveId();

}
