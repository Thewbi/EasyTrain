package de.wfb.javafxtest.controller;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.node.Node;
import de.wfb.rail.controller.Controller;
import de.wfb.rail.events.SelectionEvent;
import de.wfb.rail.events.ShapeTypeChangedEvent;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.ui.ShapeType;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public class LayoutGridController implements Controller, ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(LayoutGridController.class);

	private ShapeType currentShapeType = ShapeType.NONE;

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private ProtocolFacade protocolFacade;

	private boolean shiftState;

	/**
	 * The user clicked onto a square of the layout using the left mouse button.
	 */
	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		logger.trace("onApplicationEvent shiftState " + shiftState + " " + event.getClass().getSimpleName()
				+ " currentShapeType: " + currentShapeType);

		if (event instanceof SelectionEvent) {

			final SelectionEvent selectionEvent = (SelectionEvent) event;

			processSelectedEvent(selectionEvent);

		} else if (event instanceof ShapeTypeChangedEvent) {

			final ShapeTypeChangedEvent shapeTypeChangedEvent = (ShapeTypeChangedEvent) event;

			processShapeTypeChangedEvent(shapeTypeChangedEvent);
		}
	}

	private void processShapeTypeChangedEvent(final ShapeTypeChangedEvent shapeTypeChangedEvent) {
		currentShapeType = shapeTypeChangedEvent.getShapeType();
	}

	private void processSelectedEvent(final SelectionEvent selectionEvent) {

		switch (currentShapeType) {

		case NONE:
			// if no shape type is selected, perform a simple click
			modelFacade.nodeClicked(selectionEvent.getX(), selectionEvent.getY(), selectionEvent.isShiftState());

			// the protocol facade is interested about node clicks, because if the node
			// is a turnout, the protocol facade will send a command to turn the turnout on
			// the
			// model railroad
			final Node clickedNode = protocolFacade.nodeClicked(selectionEvent.getX(), selectionEvent.getY());

			if (clickedNode != null) {

				if (clickedNode.getGraphNodeOne() != null) {
					logger.info("GraphNodeOne GN-ID: " + clickedNode.getGraphNodeOne().getId() + " RoutingTable:");
					logger.info(clickedNode.getGraphNodeOne().dumpRoutingTable());
				}
				if (clickedNode.getGraphNodeTwo() != null) {
					logger.info("GraphNodeTwo GN-ID: " + clickedNode.getGraphNodeTwo().getId() + " RoutingTable:");
					logger.info(clickedNode.getGraphNodeTwo().dumpRoutingTable());
				}
				if (clickedNode.getGraphNodeThree() != null) {
					logger.info("GraphNodeThree GN-ID: " + clickedNode.getGraphNodeThree().getId() + " RoutingTable:");
					logger.info(clickedNode.getGraphNodeThree().dumpRoutingTable());
				}
				if (clickedNode.getGraphNodeFour() != null) {
					logger.info("GraphNodeFour GN-ID: " + clickedNode.getGraphNodeFour().getId() + " RoutingTable:");
					logger.info(clickedNode.getGraphNodeFour().dumpRoutingTable());
				}
			}

			break;

		default:
			// if a shape type is selected, add a node

			// if a node of the same type is here already, return
			final Optional<Node> currentNode = modelFacade.getNode(selectionEvent.getX(), selectionEvent.getY());
			if (currentNode.isPresent() && currentNode.get().getShapeType() == currentShapeType) {

				logger.info("Not creating another node!");
				return;
			}

			modelFacade.addNode(selectionEvent.getX(), selectionEvent.getY(), currentShapeType);
			break;
		}
	}

	/**
	 * Functionality for manually connecting two arbitrary nodes in the node graph.
	 * Useful if a layout was drawn where two nodes have to be connected even if
	 * they are not immediately adjacent to each other.
	 */
	public void connect() {

		logger.trace("connect");

		final List<Node> selectedNodes = modelFacade.getSelectedNodes();

		// tell the user what prevented the operation from succeeding
		if (CollectionUtils.isEmpty(selectedNodes) || selectedNodes.size() < 2) {

			final Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Connect Operation ...");
			alert.setHeaderText("Can not connect!");
			alert.setContentText(
					"Please select at least two nodes using the left mouse button while holding the shift key!");

			alert.showAndWait();

			return;
		}

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				final Iterator<Node> iterator = selectedNodes.iterator();
				final Node nodeA = iterator.next();
				final Node nodeB = iterator.next();

				connectNodes(nodeA, nodeB);
			}

			private void connectNodes(final Node nodeA, final Node nodeB) {

				final StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append("Do you want to connect the node " + nodeA.getId() + " to " + nodeB.getId() + "?");

				final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("Connect Operation ...");
				alert.setHeaderText("Confirm connection.");
				alert.setContentText(stringBuffer.toString());

				alert.showAndWait().ifPresent(type -> {

					logger.info(type);

					if (type.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
						logger.info("OK_DONE");
					} else if (type.getButtonData() == ButtonBar.ButtonData.YES) {
						logger.info("YES");
					} else if (type.getButtonData() == ButtonBar.ButtonData.NO) {
						logger.info("NO");
					} else if (type.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
						logger.info("CANCEL");
					} else {
						logger.info("Unknown");
					}
				});

				if (alert.getResult().equals(ButtonType.OK)) {
					System.out.println("Connecting ...");

					modelFacade.manualConnectTo(nodeA, nodeB);
				} else if (alert.getResult().equals(ButtonType.YES)) {
					System.out.println("Connecting ...");

					modelFacade.manualConnectTo(nodeA, nodeB);
				} else {
					System.out.println("Not Connecting ...");
				}
			}
		});
	}

	public boolean isShiftState() {
		return shiftState;
	}

	public void setShiftState(final boolean shiftState) {
		this.shiftState = shiftState;
	}

}
