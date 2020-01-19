package de.wfb.model.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;

import de.wfb.dot.DefaultDotSerializer;
import de.wfb.model.Model;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Color;
import de.wfb.model.node.DefaultRailNode;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.events.FeedbackBlockEvent;
import de.wfb.rail.events.FeedbackBlockState;
import de.wfb.rail.events.LocomotiveModelChangedEvent;
import de.wfb.rail.events.ModelChangedEvent;
import de.wfb.rail.events.NodeClickedEvent;
import de.wfb.rail.events.NodeHighlightedEvent;
import de.wfb.rail.events.NodeSelectedEvent;
import de.wfb.rail.events.OperationType;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.ui.ShapeType;

public class DefaultModelService implements ModelService, ApplicationListener<ApplicationEvent> {

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

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		if (event instanceof FeedbackBlockEvent) {

			final FeedbackBlockEvent feedbackBlockEvent = (FeedbackBlockEvent) event;
			processFeedbackBlockEvent(feedbackBlockEvent);
		}
	}

	private void processFeedbackBlockEvent(final FeedbackBlockEvent feedbackBlockEvent) {

		// update the state of all rail nodes that are part of the block to used
		//
		// Send a model change event to update the front-end

		final int feedbackBlockNumber = feedbackBlockEvent.getFeedbackBlockNumber();
		final FeedbackBlockState feedbackBlockState = feedbackBlockEvent.getFeedbackBlockState();

		logger.trace("processFeedbackBlockEvent() feedbackBlockNumber: " + feedbackBlockNumber + 1
				+ " feedbackBlockState: " + feedbackBlockState);

		final List<Node> feedbackBlockNodes = retrieveNodesOfFeedbackBlock(feedbackBlockNumber + 1);
		if (CollectionUtils.isEmpty(feedbackBlockNodes)) {
			logger.trace("FeedbackBlockNodes are empty! Aborting!");
			return;
		}

		for (final Node node : feedbackBlockNodes) {

			// tell the rail node, that the feedback block it belongs to is used
			node.setFeedbackBlockUsed(feedbackBlockState == FeedbackBlockState.BLOCKED);

			// the model changed because some of the nodes are now used.
			// Publish an event after model changes
			final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, model, node.getX(), node.getY(),
					node.isHighlighted(), node.isFeedbackBlockUsed(), node.isSelected());

			applicationEventPublisher.publishEvent(modelChangedEvent);
		}
	}

	private List<Node> retrieveNodesOfFeedbackBlock(final int feedbackBlockNumber) {

		final List<Node> result = new ArrayList<>();
		for (final Map.Entry<Integer, Node> entry : model.getIdMap().entrySet()) {

			final RailNode railNode = (RailNode) entry.getValue();

			if (railNode.getFeedbackBlockNumber() == feedbackBlockNumber) {
				result.add(railNode);
			}
		}

		return result;
	}

	/**
	 * The user clicked a tile. No ShapeType was selected for editing.
	 */
	@Override
	public void nodeClicked(final int x, final int y, final boolean shiftSelected) {

		logger.trace("nodeClicked x = " + x + " y = " + y);

		final Node node = model.getNode(x, y);

		// remove selection from all selected nodes
		if (!shiftSelected) {

			// @formatter:off

			model.getAllRailNodes().stream()
	            .filter(railNode -> railNode.isSelected()).forEach(railNode -> {

	            	railNode.setSelected(false);
					railNode.setHighlighted(false);

					sendModelChangedEvent(railNode);
	            });

			// @formatter:on
		}

		if (node == null) {

			sendNodeClickedEvent(null);
			return;
		}

		logger.trace(node.getId() + ") nodeClicked x = " + x + " y = " + y);
		logger.trace("nodeClicked node id = " + node.getId() + " node = " + node.getClass().getSimpleName());

		// store the currently selected node in the model
		if (shiftSelected) {

			node.setSelected(true);
			model.setSelectedNode(node);

		} else {

			sendNodeClickedEvent(node);

			// switch turnouts
			// if (node instanceof TurnoutNode) {
			if (ShapeType.isTurnout(node.getShapeType())) {

				// final TurnoutNode turnoutNode = (TurnoutNode) node;

				// change the node in the UI for visual feedback
				logger.info("toggleTurnout()");
				node.toggleTurnout();
			}
		}

		// tell the UI
		logger.info("sendModelChangedEvent()");
		sendModelChangedEvent(x, y, node.isHighlighted(), node.isFeedbackBlockUsed(), node.isSelected());
	}

	private void sendNodeClickedEvent(final Node node) {

		logger.trace("sendNodeClickedEvent");

		final NodeClickedEvent nodeClickedEvent = new NodeClickedEvent(this, node);
		applicationEventPublisher.publishEvent(nodeClickedEvent);
	}

	@SuppressWarnings("unused")
	private void sendNodeSelectedEvent(final Node node) {

		logger.trace("sendNodeSelectedEvent() node: " + node);

		final NodeSelectedEvent nodeSelectedEvent = new NodeSelectedEvent(this, model, node);
		applicationEventPublisher.publishEvent(nodeSelectedEvent);

		final boolean hightlighted = true;
		final NodeHighlightedEvent nodeHighlightedEvent = new NodeHighlightedEvent(this, model, node, node.getX(),
				node.getY(), hightlighted);
		applicationEventPublisher.publishEvent(nodeHighlightedEvent);
	}

	@Override
	public void sendModelChangedEvent(final int x, final int y, final boolean hightlighted, final boolean blocked,
			final boolean selected) {

		logger.trace("sendModelChangedEvent() x: " + x + " y: " + y);

		final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, model, x, y, hightlighted, blocked,
				selected);

		logger.trace("Sending ModelChangedEvent ...");
		applicationEventPublisher.publishEvent(modelChangedEvent);
	}

	@Override
	public void sendModelChangedEvent(final RailNode railNode) {

		final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, model, railNode.getX(), railNode.getY(),
				railNode.isHighlighted(), railNode.isFeedbackBlockUsed(), railNode.isSelected());

		logger.trace("Sending ModelChangedEvent ...");
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
			sendModelChangedEvent(x, y, false, false, false);
			return;

		default:
			break;
		}

		try {

			logger.info("Creating new node!");
			final int feedbackBlockNumber = -1;
			final Node newNode = nodeFactory.create(x, y, shapeType, feedbackBlockNumber);

			// for fast retrieval by id
			model.getIdMap().put(newNode.getId(), newNode);
			model.setNode(x, y, newNode);
			newNode.connect(model);

			logger.info("addNode() New node id = " + newNode.getId() + " added!");

			sendModelChangedEvent(x, y, newNode.isHighlighted(), newNode.isFeedbackBlockUsed(), newNode.isSelected());

		} catch (final Exception e) {
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

		try {
			modelPersistenceService.loadLocomotiveModel(model, "persistence/locomotives.json");
		} catch (final IOException e) {
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
	public void storeLocomotiveModel() {
		try {
			modelPersistenceService.storeLocomotiveModel(model, "persistence/locomotives.json");
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void manualConnectTo(final Node nodeA, final Node nodeB) {

		final DefaultRailNode railNodeA = (DefaultRailNode) nodeA;
		final DefaultRailNode railNodeB = (DefaultRailNode) nodeB;

		railNodeA.manualConnectTo(railNodeB);
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

	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public void resetGraphColors() {

		for (final Map.Entry<Integer, Node> entry : model.getIdMap().entrySet()) {

			final RailNode railNode = (RailNode) entry.getValue();
			railNode.getGraphNodeOne().setColor(Color.NONE);
			railNode.getGraphNodeTwo().setColor(Color.NONE);
		}
	}

	@Override
	public List<Node> getSelectedNodes() {

		final List<Node> selectedNodes = new ArrayList<>();
		for (final Map.Entry<Integer, Node> entry : model.getIdMap().entrySet()) {

			final RailNode railNode = (RailNode) entry.getValue();

			if (railNode.isSelected()) {
				selectedNodes.add(railNode);
			}
		}

		return selectedNodes;
	}

	@Override
	public List<RailNode> getAllRailNodes() {
		return model.getAllRailNodes();
	}

	@Override
	public List<DefaultLocomotive> getLocomotives() {
		return model.getLocomotives();
	}

	@Override
	public void addLocomotive(final DefaultLocomotive defaultLocomotive) {
		model.getLocomotives().add(defaultLocomotive);

		// send event
		final LocomotiveModelChangedEvent event = new LocomotiveModelChangedEvent(this, defaultLocomotive,
				OperationType.ADDED);
		applicationEventPublisher.publishEvent(event);
	}

	@Override
	public void deleteLocomotive(final DefaultLocomotive defaultLocomotive) {
		model.getLocomotives().remove(defaultLocomotive);

		// send event
		final LocomotiveModelChangedEvent event = new LocomotiveModelChangedEvent(this, defaultLocomotive,
				OperationType.REMOVED);
		applicationEventPublisher.publishEvent(event);
	}

	@Override
	public int retrieveNextLocomotiveId() {
		return model.retrieveNextLocomotiveId();
	}

}
