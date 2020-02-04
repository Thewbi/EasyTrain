package de.wfb.dialogs;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.service.RoutingService;
import de.wfb.rail.service.Route;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class RoutingPane extends GridPane {

	private static final Logger logger = LogManager.getLogger(RoutingPane.class);

	private Label startTextfieldLabel = new Label("Start GN-ID");

	private TextField startTextfield;

	private Label endTextfieldLabel = new Label("End GN-ID");

	private TextField endTextfield;

	private Button routeButton;

	@Autowired
	private RoutingService routingService;

	@Autowired
	private ModelFacade modelFacade;

	public void setup() {

		routeButton();

		startData();
		endData();
	}

	private void routeButton() {
		routeButton = new Button();
		routeButton.setText("Route");
		GridPane.setColumnIndex(routeButton, 1);
		GridPane.setRowIndex(routeButton, 3);
		routeButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				routingService.removeHighlightedRoute();

				final int start = Integer.parseInt(startTextfield.getText());
				final GraphNode graphNodeStart = modelFacade.getGraphNodeById(start);
				graphNodeStart.dumpRoutingTable();

				final int end = Integer.parseInt(endTextfield.getText());
				final GraphNode graphNodeEnd = modelFacade.getGraphNodeById(end);

				logger.info("Start: " + start + " end: " + end);

				final DefaultLocomotive locomotive = null;
				final boolean routeOverReservedNodes = true;
				final boolean routeOverBlockedFeedbackBlocks = true;
				Route route;
				try {
					route = routingService.route(locomotive, graphNodeStart, graphNodeEnd, routeOverReservedNodes,
							routeOverBlockedFeedbackBlocks);

					logger.info(route);

					logger.info("Has duplicate GraphNodes: " + route.hasDuplicateGraphNodes());
					logger.info("Is route valid? " + route.checkRoute());

					if (CollectionUtils.isNotEmpty(route.getGraphNodes())) {

						// visually highlight the route in the UI
						routingService.highlightRoute(route);

						// switch turnouts
//						routingService.switchTurnouts(route);
					}

				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}

			}
		});
		getChildren().add(routeButton);
	}

	private void startData() {

		startTextfieldLabel = new Label("Start GN-ID");

		GridPane.setColumnIndex(startTextfieldLabel, 1);
		GridPane.setRowIndex(startTextfieldLabel, 1);

		startTextfield = new TextField();
		GridPane.setColumnIndex(startTextfield, 2);
		GridPane.setRowIndex(startTextfield, 1);

		getChildren().addAll(startTextfieldLabel, startTextfield);
	}

	private void endData() {

		endTextfieldLabel = new Label("End GN-ID");

		GridPane.setColumnIndex(endTextfieldLabel, 1);
		GridPane.setRowIndex(endTextfieldLabel, 2);

		endTextfield = new TextField();
		GridPane.setColumnIndex(endTextfield, 2);
		GridPane.setRowIndex(endTextfield, 2);

		getChildren().addAll(endTextfieldLabel, endTextfield);
	}

	public void clear() {

		if (startTextfieldLabel != null) {

			getChildren().remove(startTextfieldLabel);
			startTextfieldLabel = null;
		}

		if (startTextfield != null) {

			getChildren().remove(startTextfield);
			startTextfield = null;
		}

		if (endTextfieldLabel != null) {

			getChildren().remove(endTextfieldLabel);
			endTextfieldLabel = null;
		}

		if (endTextfield != null) {

			getChildren().remove(endTextfield);
			endTextfield = null;
		}

		if (routeButton != null) {

			getChildren().remove(routeButton);
			routeButton = null;
		}
	}

}
