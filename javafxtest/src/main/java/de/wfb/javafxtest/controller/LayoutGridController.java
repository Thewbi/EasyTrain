package de.wfb.javafxtest.controller;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import de.wfb.model.node.Node;
import de.wfb.model.service.ModelService;
import de.wfb.rail.events.SelectionEvent;
import de.wfb.rail.events.ShapeTypeChangedEvent;
import de.wfb.rail.ui.ShapeType;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

@Component
public class LayoutGridController implements ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(LayoutGridController.class);

	private ShapeType currentShapeType = ShapeType.NONE;

	@Autowired
	private ModelService modelService;

	private boolean shiftState;

	private final Set<Node> selectedNodes = new HashSet<>();

	/**
	 * The user clicked onto a square of the layout using the left mouse button.
	 */
	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		logger.info("onApplicationEvent shiftState " + shiftState + " " + event.getClass().getSimpleName()
				+ " currentShapeType: " + currentShapeType);

		if (event instanceof SelectionEvent) {

			final SelectionEvent selectionEvent = (SelectionEvent) event;

			// the shift key adds the selected node onto the selected node
			if (selectionEvent.isShiftState()) {

				logger.info("Shift selection");

				final Optional<Node> nodeOptional = modelService.getNode(selectionEvent.getX(), selectionEvent.getY());
				if (nodeOptional.isPresent()) {
					selectedNodes.add(nodeOptional.get());
				}

				logger.info("SelectedNodes: " + selectedNodes);

				return;

			} else {

				// remove all nodes from the selected list
				selectedNodes.clear();

			}

			switch (currentShapeType) {

			case NONE:
				// if no shape type is selected, perform a simple click
				modelService.nodeClicked(selectionEvent.getX(), selectionEvent.getY());
				break;

			default:
				// if a shape type is selected, add a node

				// if a node of the same type is here already, return
				final Optional<Node> currentNode = modelService.getNode(selectionEvent.getX(), selectionEvent.getY());
				if (currentNode.isPresent() && currentNode.get().getShapeType() == currentShapeType) {

					logger.info("Not creating another node!");
					return;
				}

				modelService.addNode(selectionEvent.getX(), selectionEvent.getY(), currentShapeType);

				modelService.storeModel();
				break;
			}

		} else if (event instanceof ShapeTypeChangedEvent) {

			final ShapeTypeChangedEvent shapeTypeChangedEvent = (ShapeTypeChangedEvent) event;

			currentShapeType = shapeTypeChangedEvent.getShapeType();
		}
	}

	public boolean isShiftState() {
		return shiftState;
	}

	public void setShiftState(final boolean shiftState) {
		this.shiftState = shiftState;
	}

	public void connect() {

		logger.info("connect");

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

				final StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append("Do you want to connect the node " + nodeA + " to " + nodeB + "?");

				final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("Connect Operation ...");
				alert.setHeaderText("Confirm connection.");
				alert.setContentText(stringBuffer.toString());

//				final ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
//				final ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
//				final ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

				// alert.getButtonTypes().setAll(okButton, noButton, cancelButton);

//				alert.getButtonTypes().add(0, okButton);
//				alert.getButtonTypes().add(1, noButton);
//				alert.getButtonTypes().add(2, cancelButton);

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

				// alert.showAndWait();

				if (alert.getResult().equals(ButtonType.OK)) {
					System.out.println("Connecting ...");

					modelService.connect(nodeA, nodeB);
				} else if (alert.getResult().equals(ButtonType.YES)) {
					System.out.println("Connecting ...");

					modelService.connect(nodeA, nodeB);
				} else {
					System.out.println("Not Connecting ...");
				}
			}
		});
	}

}
