package de.wfb.model.facade;

import java.util.List;
import java.util.Optional;

import de.wfb.model.Model;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.ui.ShapeType;

public interface ModelFacade {

	Optional<Node> getNode(int x, int y);

	void nodeClicked(int x, int y, boolean shiftClicked);

	void addNode(int x, int y, ShapeType shapeType);

	void storeModel(String modelFile);

	void storeLocomotiveModel(String locomotivesModelFile);

	void manualConnectTo(Node nodeA, Node nodeB);

	Model getModel();

	List<Node> getSelectedNodes();

	void debugRoute();

	void loadModel(String modelFile);

	void loadLocomotivesModel(String locomotivesModelFile);

	void connectModel();

	List<DefaultLocomotive> getLocomotives();

	void addLocomotive(DefaultLocomotive defaultLocomotive);

	void deleteLocomotive(DefaultLocomotive locomotive);

	int retrieveNextLocomotiveId();

	Optional<DefaultLocomotive> getLocomotiveById(int reservedLocomotiveId);

	Optional<DefaultLocomotive> getLocomotiveByAddress(short locomotiveAddress);

	void sendModelChangedEvent(int x, int y, boolean hightlighted, boolean blocked, boolean selected, boolean reserved);

	void sendModelChangedEvent(RailNode railNode);

	String getCurrentLocomotivesModel();

	String getCurrentModel();

	void clear();

}
