package de.wfb.model.service;

import java.util.List;
import java.util.Optional;

import de.wfb.model.Model;
import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.ui.ShapeType;

public interface ModelService {

	Optional<Node> getNode(int x, int y);

	void nodeClicked(int x, int y, boolean shiftClicked);

	void addNode(int x, int y, ShapeType shapeType);

	void storeModel(String modelFile);

	void storeLocomotiveModel(String locomotivesModelFile);

	void loadModel(String modelFile);

	void loadLocomotivesModel(String locomotivesModelFile);

	void sendModelChangedEvent(int x, int y, boolean hightlighted, boolean blocked, boolean selected, boolean reserved);

	void sendModelChangedEvent(RailNode railNode);

	void manualConnectTo(Node nodeA, Node nodeB);

	void connectModel();

	void debugRoute();

	Node getNodeById(int id);

	List<GraphNode> getSwitchingNodes();

	RailNode getArbitraryNode();

	Model getModel();

	List<Node> getSelectedNodes();

	List<RailNode> getAllRailNodes();

	List<Locomotive> getLocomotives();

	void addLocomotive(Locomotive locomotive);

	void deleteLocomotive(Locomotive locomotive);

	int retrieveNextLocomotiveId();

	String getCurrentLocomotivesModel();

	String getCurrentModel();

	void clear();

	void removeAllHighlights();

	GraphNode getGraphNodeById(int id);

	void reserveNode(Node node, int locomotiveId);

	void blockNode(Node node);

	void reserveNodeToggle(Node node, int locomotiveId);

	void blockNodeToggle(Node node);

	List<RailNode> getTurnoutsByAddress(int address);

}
