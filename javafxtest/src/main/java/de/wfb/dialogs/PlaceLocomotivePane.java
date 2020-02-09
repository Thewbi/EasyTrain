package de.wfb.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.Node;
import de.wfb.model.service.RoutingService;
import de.wfb.rail.events.LocomotiveSelectedEvent;
import de.wfb.rail.events.NodeClickedEvent;
import de.wfb.rail.ui.ShapeType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class PlaceLocomotivePane extends VBox implements ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(PlaceLocomotivePane.class);

	private final Button okButton = new Button("Ok");

	private final Button northButton = new Button("North");

	private final Button eastButton = new Button("East");

	private final Button southButton = new Button("South");

	private final Button westButton = new Button("West");

	private DefaultLocomotive defaultLocomotive;

	private Node node;

	private Direction edgeDirection = null;

	final GridPane gridPane = new GridPane();

	@Autowired
	private RoutingService routingService;

	public PlaceLocomotivePane() {
		getChildren().addAll(gridPane);
	}

	public void clear() {
		gridPane.getChildren().clear();
	}

	public void setup() {

		final List<Button> directionalButtons = setupDirectionsButtons();

		okButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent actionEvent) {

				try {

					routingService.placeLocomotive(node, defaultLocomotive, edgeDirection);

					getScene().getWindow().hide();

					edgeDirection = null;
					defaultLocomotive = null;
					node = null;

				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		});
		GridPane.setColumnIndex(okButton, 1);
		GridPane.setRowIndex(okButton, 5);

		gridPane.getChildren().addAll(okButton);
		gridPane.getChildren().addAll(directionalButtons);

		setSpacing(5);
		setPadding(new Insets(10, 10, 10, 10));
	}

	private List<Button> setupDirectionsButtons() {

		final List<Button> result = new ArrayList<>();

		if (node != null) {

			if (node.getShapeType() == ShapeType.STRAIGHT_VERTICAL) {

				// north
				northButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						edgeDirection = Direction.NORTH;
					}
				});
				GridPane.setColumnIndex(northButton, 1);
				GridPane.setRowIndex(northButton, 1);
				result.add(northButton);

				// south
				southButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						edgeDirection = Direction.SOUTH;
					}
				});
				GridPane.setColumnIndex(southButton, 1);
				GridPane.setRowIndex(southButton, 2);
				result.add(southButton);
			}

			if (node.getShapeType() == ShapeType.STRAIGHT_HORIZONTAL) {

				// east
				eastButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						edgeDirection = Direction.EAST;
					}
				});
				GridPane.setColumnIndex(eastButton, 2);
				GridPane.setRowIndex(eastButton, 1);
				result.add(eastButton);

				// west
				westButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						edgeDirection = Direction.WEST;
					}
				});
				GridPane.setColumnIndex(westButton, 1);
				GridPane.setRowIndex(westButton, 1);
				result.add(westButton);
			}
		}

		return result;
	}

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		if (event instanceof LocomotiveSelectedEvent) {

			final LocomotiveSelectedEvent locomotiveSelectedEvent = (LocomotiveSelectedEvent) event;
			defaultLocomotive = locomotiveSelectedEvent.getLocomotive();

		} else if (event instanceof NodeClickedEvent) {

			final NodeClickedEvent nodeClickedEvent = (NodeClickedEvent) event;
			node = nodeClickedEvent.getNode();
		}
	}

}
