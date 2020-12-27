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
import de.wfb.model.locomotive.Locomotive;
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

	/**
	 * FeedbackBlockUpdateEvent is broken down into several FeedbackBlockEvents.
	 * <br />
	 * <br />
	 * This handler will change the FeedbackBlockUsed-State on all blocks of the
	 * node.<br />
	 * <br />
	 *
	 * The new FeedbackBlockUsed-State dependes on wether the block is BLOCKED or
	 * not.
	 *
	 * @param feedbackBlockEvent
	 */
	private void processFeedbackBlockEvent(final FeedbackBlockEvent feedbackBlockEvent) {

		// update the state of all rail nodes that are part of the block to used
		// Send a model change event to update the front-end

		final int feedbackBlockNumber = feedbackBlockEvent.getFeedbackBlockNumber();

		final FeedbackBlockState feedbackBlockState = feedbackBlockEvent.getFeedbackBlockState();

		logger.trace("processFeedbackBlockEvent() feedbackBlockNumber: " + feedbackBlockNumber + " feedbackBlockState: "
				+ feedbackBlockState);

		final List<Node> feedbackBlockNodes = retrieveNodesOfFeedbackBlock(feedbackBlockNumber);
		if (CollectionUtils.isEmpty(feedbackBlockNodes)) {

			logger.trace("FeedbackBlockNodes are empty! Aborting!");
			return;
		}

		for (final Node node : feedbackBlockNodes) {

			logger.trace("Block-Node ID: " + node.getId() + " feedbackBlockState: " + feedbackBlockState);

			// tell the rail node, that the feedback block it belongs to is used
			node.setFeedbackBlockUsed(feedbackBlockState == FeedbackBlockState.BLOCKED);

			// the model changed because some of the nodes are now used.
			// Publish an event after model changes
			final boolean containsLocomotive = false;
			final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(this, model, node.getX(), node.getY(),
					node.isHighlighted(), node.isFeedbackBlockUsed(), node.isSelected(), node.isReserved(),
					containsLocomotive);

			applicationEventPublisher.publishEvent(modelChangedEvent);
		}
	}

	private List<Node> retrieveNodesOfFeedbackBlock(final Integer feedbackBlockNumber) {

		final List<Node> result = new ArrayList<>();
		for (final Map.Entry<Integer, Node> entry : model.getIdMap().entrySet()) {

			final RailNode railNode = (RailNode) entry.getValue();

			if (railNode.getFeedbackBlockNumber() != null && railNode.getFeedbackBlockNumber() == feedbackBlockNumber) {

				logger.trace("RailNode ID: " + railNode.getId() + " FeedbackBlockNumber ID: "
						+ railNode.getFeedbackBlockNumber());

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

		logger.info("nodeClicked x = " + x + " y = " + y);

		final Node node = model.getNode(x, y);

		// remove selection from all selected nodes
		if (!shiftSelected) {

			// @formatter:off

			model.getAllRailNodes()
			    .stream()
	            .filter(railNode -> railNode.isSelected())
	            .forEach(railNode -> {

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

			// remove all highlights and select the currently selected node
			removeAllHighlights();
			node.setHighlighted(true);

			sendNodeClickedEvent(node);

			// switch turnouts
			if (ShapeType.isTurnout(node.getShapeType())) {

				logger.trace("toggleTurnout()");

				// change thrown status of the node
				node.toggleTurnout();
			}

			// switch signals
			if (ShapeType.isSignal(node.getShapeType())) {

				logger.trace("toggleSignal()");

				// change thrown status of the node
				node.toggleSignal();
			}
		}

		// tell the UI

		// @formatter:off
		sendModelChangedEvent(x,
				              y,
				              node.isHighlighted(),
				              node.isFeedbackBlockUsed(),
				              node.isSelected(),
				              node.isReserved());
		// @formatter:on
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
			final boolean selected, final boolean reserved) {

		logger.trace("sendModelChangedEvent() x: " + x + " y: " + y);

		// @formatter:off

		final boolean containsLocomotive = false;
		final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(
				this,
				model,
				x,
				y,
				hightlighted,
				blocked,
				selected,
				reserved,
				containsLocomotive);

		// @formatter:on

		applicationEventPublisher.publishEvent(modelChangedEvent);
	}

	@Override
	public void sendModelChangedEvent(final RailNode railNode) {

		final Object sender = this;
		final int x = railNode.getX();
		final int y = railNode.getY();
		final boolean highlighted = railNode.isHighlighted();

		final boolean blocked = railNode.isFeedbackBlockUsed();

		final boolean reserved = railNode.isReserved();

		final boolean selected = railNode.isSelected();

		final boolean containsLocomotive = false;

		// @formatter:off

		final ModelChangedEvent modelChangedEvent = new ModelChangedEvent(
				sender,
				model,
				x,
				y,
				highlighted,
				blocked,
				selected,
				reserved,
				containsLocomotive);

		// @formatter:on

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
			sendModelChangedEvent(x, y, false, false, false, false);
			return;

		default:
			break;
		}

		try {

			logger.trace("Creating new node!");
			final int feedbackBlockNumber = -1;
			final Node newNode = nodeFactory.create(x, y, shapeType, feedbackBlockNumber);

			// for fast retrieval by id
			model.getIdMap().put(newNode.getId(), newNode);
			model.setNode(x, y, newNode);
			newNode.connect(model);

			logger.trace("addNode() New node id = " + newNode.getId() + " added!");

			sendModelChangedEvent(x, y, newNode.isHighlighted(), newNode.isFeedbackBlockUsed(), newNode.isSelected(),
					newNode.isReserved());

		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void loadModel(final String modelFile) {

		try {
			model.setCurrentModelFile(modelFile);
			modelPersistenceService.loadModel(model, modelFile);
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void loadLocomotivesModel(final String locomotivesModelFile) {

		try {
			model.setCurrentLocomotiveModelFile(locomotivesModelFile);
			modelPersistenceService.loadLocomotiveModel(model, locomotivesModelFile);
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void storeModel(final String modelFile) {

		logger.info("storeModel()");

		try {
			modelPersistenceService.storeModel(model, modelFile);
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void storeLocomotiveModel(final String locomotivesModelFile) {
		try {
			modelPersistenceService.storeLocomotiveModel(model, locomotivesModelFile);
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
	public List<Locomotive> getLocomotives() {
		return model.getLocomotives();
	}

	@Override
	public void addLocomotive(final Locomotive locomotive) {
		model.getLocomotives().add(locomotive);

		final LocomotiveModelChangedEvent event = new LocomotiveModelChangedEvent(this, locomotive,
				OperationType.ADDED);
		applicationEventPublisher.publishEvent(event);
	}

	@Override
	public void deleteLocomotive(final Locomotive locomotive) {

		model.getLocomotives().remove(locomotive);

		// send event
		final LocomotiveModelChangedEvent event = new LocomotiveModelChangedEvent(this, locomotive,
				OperationType.REMOVED);
		applicationEventPublisher.publishEvent(event);
	}

	@Override
	public int retrieveNextLocomotiveId() {
		return model.retrieveNextLocomotiveId();
	}

	@Override
	public String getCurrentLocomotivesModel() {
		return model.getCurrentLocomotiveModelFile();
	}

	@Override
	public String getCurrentModel() {
		return model.getCurrentModelFile();
	}

	/**
	 * For testing
	 *
	 * @param applicationEventPublisher
	 */
	public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	/**
	 * For Testing
	 *
	 * @param model
	 */
	public void setModel(final Model model) {
		this.model = model;
	}

	@Override
	public void clear() {
		model.clear();
	}

	@Override
	public void removeAllHighlights() {

		for (final RailNode railNode : model.getAllRailNodes()) {

			final boolean hightlighted = false;
			railNode.setHighlighted(hightlighted);
			final NodeHighlightedEvent nodeHighlightedEvent = new NodeHighlightedEvent(this, model, railNode,
					railNode.getX(), railNode.getY(), hightlighted);

			applicationEventPublisher.publishEvent(nodeHighlightedEvent);
		}
	}

	@Override
	public GraphNode getGraphNodeById(final int id) {
		return model.getGraphNodeById(id);
	}

	@Override
	public void reserveNode(final Node node, final int locomotiveId) {
		node.setReserved(true);
		node.setReservedLocomotiveId(locomotiveId);
	}

	@Override
	public void blockNode(final Node node) {
		node.getGraphNodeOne().setBlocked(true);
		node.getGraphNodeTwo().setBlocked(true);
	}

	@Override
	public void reserveNodeToggle(final Node node, final int locomotiveId) {

		if (node.isReserved()) {

			node.setReserved(false);
			node.setReservedLocomotiveId(-1);

		} else {

			node.setReserved(true);
			node.setReservedLocomotiveId(locomotiveId);

		}
	}

	@Override
	public void blockNodeToggle(final Node node) {

		node.getGraphNodeOne().setBlocked(!node.getGraphNodeOne().isBlocked());
		node.getGraphNodeTwo().setBlocked(!node.getGraphNodeTwo().isBlocked());
	}

	@Override
	public List<RailNode> getTurnoutsByAddress(final int address) {

		final List<RailNode> result = new ArrayList<>();
		for (final RailNode turnoutNode : getAllRailNodes()) {

			if (turnoutNode.getProtocolTurnoutId() != null && turnoutNode.getProtocolTurnoutId() == address) {

				result.add(turnoutNode);
			}
		}

		return result;
	}

}
