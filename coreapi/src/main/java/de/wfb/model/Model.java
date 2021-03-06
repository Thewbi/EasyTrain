package de.wfb.model;

import java.util.List;
import java.util.Map;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;

public interface Model {

	Node getNode(int x, int y);

	void setNode(int x, int y, Node node);

	Node getSelectedNode();

	void setSelectedNode(Node selectedNode);

	void removeNode(int x, int y);

	Map<Integer, Node> getIdMap();

	void connectModel();

	List<GraphNode> getSwitchingNodes();

	List<RailNode> getAllRailNodes();

	List<Locomotive> getLocomotives();

	int retrieveNextLocomotiveId();

	String getCurrentModelFile();

	void setCurrentModelFile(final String currentModelFile);

	String getCurrentLocomotiveModelFile();

	void setCurrentLocomotiveModelFile(final String currentLocomotiveModelFile);

	void clear();

	GraphNode getGraphNodeById(int i);

}
