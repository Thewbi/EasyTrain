package de.wfb.model.facade;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.Model;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.ModelService;
import de.wfb.rail.ui.ShapeType;

public class DefaultModelFacade implements ModelFacade {

	@Autowired
	private ModelService modelService;

	@Override
	public Optional<Node> getNode(final int x, final int y) {
		return modelService.getNode(x, y);
	}

	@Override
	public void nodeClicked(final int x, final int y, final boolean shiftClicked) {
		modelService.nodeClicked(x, y, shiftClicked);
	}

	@Override
	public void addNode(final int x, final int y, final ShapeType shapeType) {
		modelService.addNode(x, y, shapeType);
	}

	@Override
	public void storeModel(final String modelFile) {
		modelService.storeModel(modelFile);
	}

	@Override
	public void storeLocomotiveModel(final String locomotivesModelFile) {
		modelService.storeLocomotiveModel(locomotivesModelFile);
	}

	@Override
	public void manualConnectTo(final Node nodeA, final Node nodeB) {
		modelService.manualConnectTo(nodeA, nodeB);
	}

	@Override
	public Model getModel() {
		return modelService.getModel();
	}

	@Override
	public List<Node> getSelectedNodes() {
		return modelService.getSelectedNodes();
	}

	@Override
	public void debugRoute() {
		modelService.debugRoute();
	}

	@Override
	public void loadModel(final String modelFile) {
		modelService.loadModel(modelFile);
	}

	@Override
	public void loadLocomotivesModel(final String locomotivesModelFile) {
		modelService.loadLocomotivesModel(locomotivesModelFile);
	}

	@Override
	public void connectModel() {
		modelService.connectModel();
	}

	@Override
	public List<DefaultLocomotive> getLocomotives() {
		return modelService.getLocomotives();
	}

	@Override
	public void addLocomotive(final DefaultLocomotive defaultLocomotive) {
		modelService.addLocomotive(defaultLocomotive);
	}

	@Override
	public void deleteLocomotive(final DefaultLocomotive defaultLocomotive) {
		modelService.deleteLocomotive(defaultLocomotive);
	}

	@Override
	public int retrieveNextLocomotiveId() {
		return modelService.retrieveNextLocomotiveId();
	}

	@Override
	public Optional<DefaultLocomotive> getLocomotiveById(final int id) {
		return modelService.getLocomotives().stream().filter(locomotive -> locomotive.getId() == id).findFirst();
	}

	@Override
	public Optional<DefaultLocomotive> getLocomotiveByAddress(final short locomotiveAddress) {
		return modelService.getLocomotives().stream().filter(locomotive -> locomotive.getAddress() == locomotiveAddress)
				.findFirst();
	}

	@Override
	public void sendModelChangedEvent(final int x, final int y, final boolean hightlighted, final boolean blocked,
			final boolean selected, final boolean reserved) {
		modelService.sendModelChangedEvent(x, y, hightlighted, blocked, selected, reserved);
	}

	@Override
	public void sendModelChangedEvent(final RailNode railNode) {
		modelService.sendModelChangedEvent(railNode);
	}

	@Override
	public String getCurrentLocomotivesModel() {
		return modelService.getCurrentLocomotivesModel();
	}

	@Override
	public String getCurrentModel() {
		return modelService.getCurrentModel();
	}

	@Override
	public void clear() {
		modelService.clear();
	}

	@Override
	public GraphNode getGraphNodeById(final int id) {
		return modelService.getGraphNodeById(id);
	}

	@Override
	public void reserveNode(final Node node) {
		modelService.reserveNode(node);
	}

	@Override
	public void blockNode(final Node node) {
		modelService.blockNode(node);
	}

	@Override
	public void reserveNodeToggle(final Node node) {
		modelService.reserveNodeToggle(node);
	}

	@Override
	public void blockNodeToggle(final Node node) {
		modelService.blockNodeToggle(node);
	}

}
