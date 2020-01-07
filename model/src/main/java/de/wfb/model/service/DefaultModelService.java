package de.wfb.model.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.dot.DefaultDotSerializer;
import de.wfb.model.Model;
import de.wfb.model.node.DefaultRailNode;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.events.ModelChangedEvent;
import de.wfb.rail.events.NodeSelectedEvent;
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

		// store the currently selected node in the model
		logger.info("setSelectedNode()");
		model.setSelectedNode(node);

		logger.info("sendNodeSelectedEvent()");
		sendNodeSelectedEvent(node);

		// switch turnouts
		// if (node instanceof TurnoutNode) {
		if (ShapeType.isTurnout(node.getShapeType())) {

			// final TurnoutNode turnoutNode = (TurnoutNode) node;

			// change the node in the UI for visual feedback
			node.toggleTurnout();

			// tell the UI
			sendModelChangedEvent(x, y);
		}

	}

	private void sendNodeSelectedEvent(final Node node) {

		logger.trace("sendNodeSelectedEvent() node: " + node);

		final NodeSelectedEvent nodeSelectedEvent = new NodeSelectedEvent(this, model, node);

		applicationEventPublisher.publishEvent(nodeSelectedEvent);
	}

	@Override
	public void sendModelChangedEvent(final int x, final int y) {

		logger.trace("sendModelChangedEvent() x: " + x + " y: " + y);

		final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, model, x, y);

		applicationEventPublisher.publishEvent(modelChangedEvent);
	}

	@Override
	public void addNode(final int x, final int y, final ShapeType shapeType) {

		logger.trace("addNode() x: " + x + " y: " + y + " shapeType: " + shapeType);

		// remove a node that was at that location beforehand
		final Node oldNode = model.getNode(x, y);
		if (oldNode != null) {

			oldNode.disconnect(model);
			model.removeNode(x, y);
			model.getIdMap().remove(oldNode.getId());
		}

		// abort operation for certain shape types
		switch (shapeType) {

		case NONE:
			return;

		case REMOVE:
			sendModelChangedEvent(x, y);
			return;

		default:
			break;
		}

		try {
			logger.info("Creating new node!");
			final Node newNode = nodeFactory.create(x, y, shapeType);

			// for fast retrieval by id
			model.getIdMap().put(newNode.getId(), newNode);
			model.setNode(x, y, newNode);
			newNode.connect(model);

			logger.info("addNode() New node id = " + newNode.getId() + " added!");

			sendModelChangedEvent(x, y);
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void storeModel() {

		logger.trace("storeModel()");

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

	@Override
	public void connect(final Node nodeA, final Node nodeB) {

		final DefaultRailNode railNodeA = (DefaultRailNode) nodeA;
		final DefaultRailNode railNodeB = (DefaultRailNode) nodeB;

		railNodeA.connectTo(railNodeB);
	}

	@Override
	public void connectModel() {
		model.connectModel();
	}

	@Override
	public void debugRoute() {

		final Node node = model.getSelectedNode();

		dumpNode(node);
	}

	private void dumpNode(final Node node) {

		if (node instanceof DefaultRailNode) {

			final DefaultRailNode defaultRailNode = (DefaultRailNode) node;

			DefaultDotSerializer defaultDotSerializer = null;

			defaultDotSerializer = new DefaultDotSerializer();
			defaultDotSerializer.serialize(defaultRailNode.getGraphNodeOne());

			defaultDotSerializer = new DefaultDotSerializer();
			defaultDotSerializer.serialize(defaultRailNode.getGraphNodeTwo());
		}
	}

	@Override
	public Node getNodeById(final int id) {
		return model.getIdMap().get(id);
	}

	@Override
	public List<GraphNode> getSwitchingNodes() {
		return model.getSwitchingNodes();
	}

	@Override
	public RailNode getArbitraryNode() {

		if (model.getIdMap().size() == 0) {
			return null;
		}

		return (RailNode) model.getIdMap().entrySet().iterator().next().getValue();
	}

}
