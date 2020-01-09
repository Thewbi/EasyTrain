package de.wfb.model.facade;

import java.util.List;
import java.util.Optional;

import de.wfb.model.Model;
import de.wfb.model.node.Node;
import de.wfb.rail.ui.ShapeType;

public interface ModelFacade {

	Optional<Node> getNode(int x, int y);

	void nodeClicked(int x, int y, boolean shiftClicked);

	void addNode(int x, int y, ShapeType shapeType);

	void storeModel();

	void connect(Node nodeA, Node nodeB);

	Model getModel();

	List<Node> getSelectedNodes();

	void debugRoute();

	void loadModel();

	void connectModel();

}
