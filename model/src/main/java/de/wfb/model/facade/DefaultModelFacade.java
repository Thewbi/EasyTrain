package de.wfb.model.facade;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.Model;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Node;
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
	public void storeModel() {
		modelService.storeModel();
	}

	@Override
	public void storeLocomotiveModel() {
		modelService.storeLocomotiveModel();
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
	public void loadModel() {
		modelService.loadModel();
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

}
