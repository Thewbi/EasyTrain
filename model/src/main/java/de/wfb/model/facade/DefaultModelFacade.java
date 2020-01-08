package de.wfb.model.facade;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.Model;
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
	public void nodeClicked(final int x, final int y) {
		modelService.nodeClicked(x, y);
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
	public void connect(final Node nodeA, final Node nodeB) {
		modelService.connect(nodeA, nodeB);
	}

	@Override
	public Model getModel() {
		return modelService.getModel();
	}

}
