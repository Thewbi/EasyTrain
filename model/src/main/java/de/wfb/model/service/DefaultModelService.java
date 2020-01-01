package de.wfb.model.service;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.Model;
import de.wfb.model.node.Node;
import de.wfb.model.node.TurnoutNode;
import de.wfb.rail.events.ModelChangedEvent;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.ui.ShapeType;

public class DefaultModelService implements ModelService {

	private static final Logger logger = LogManager.getLogger(DefaultModelService.class);

	@Autowired
	private Model model;

	/** https://www.baeldung.com/spring-events */
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private Factory<Node> nodeFactory;

	@Autowired
	private NodeConnectorService nodeConnectorService;

	@Autowired
	private ModelPersistenceService modelPersistenceService;

	@Override
	public Optional<Node> getNode(final int x, final int y) {
		return Optional.ofNullable(model.getNode(x, y));
	}

	/**
	 * The user clicked a tile. No ShapeType was selected for editing.
	 */
	@Override
	public void nodeClicked(final int x, final int y) {

		logger.info("nodeClicked x = " + x + " y = " + y);

		final Node node = model.getNode(x, y);

		if (node == null) {
			logger.info("nodeClicked node is null");
			return;
		}

		logger.info("nodeClicked node id = " + node.getId() + " node = " + node.getClass().getSimpleName());

		// switch turnouts
		if (node instanceof TurnoutNode) {

			final TurnoutNode turnoutNode = (TurnoutNode) node;

			turnoutNode.toggle();

			sendModelChangedEvent(x, y);
		}
	}

	public void sendModelChangedEvent(final int x, final int y) {

		final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, model, x, y);

		applicationEventPublisher.publishEvent(modelChangedEvent);
	}

	@Override
	public void addNode(final int x, final int y, final ShapeType shapeType) {

		// remove a node that was at that location beforehand
		final Node oldNode = model.getNode(x, y);
		if (oldNode != null) {
			nodeConnectorService.disconnect(oldNode);
			model.removeNode(x, y);
			model.getIdMap().remove(oldNode.getId());
		}

		final Node newNode = nodeFactory.create(x, y, shapeType);
		// for fast retrieval by id
		model.getIdMap().put(newNode.getId(), newNode);
		model.setNode(x, y, newNode);
		nodeConnectorService.connect(newNode);

		logger.info("addNode() New node id = " + newNode.getId() + " added!");

		sendModelChangedEvent(x, y);
	}

	@Override
	public void storeModel() {
		try {
			modelPersistenceService.storeModel(model, "persistence/model.json");
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void loadModel() {
		try {
			modelPersistenceService.loadModel(model, "persistence/model.json");
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
