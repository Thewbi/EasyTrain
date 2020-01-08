package de.wfb.model.facade;

import java.util.Optional;

import de.wfb.model.Model;
import de.wfb.model.node.Node;
import de.wfb.rail.ui.ShapeType;

public interface ModelFacade {

	Optional<Node> getNode(int x, int y);

	void nodeClicked(int x, int y);

	void addNode(int x, int y, ShapeType shapeType);

	void storeModel();

	void connect(Node nodeA, Node nodeB);

	Model getModel();

}
