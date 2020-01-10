package de.wfb.dialogs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Edge;
import de.wfb.model.node.EdgeDirection;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.events.LocomotiveSelectedEvent;
import de.wfb.rail.events.NodeClickedEvent;
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

	private EdgeDirection edgeDirection = null;

	public void clear() {

	}

	public void setup() {

		northButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				edgeDirection = EdgeDirection.NORTH;
			}
		});
		GridPane.setColumnIndex(northButton, 2);
		GridPane.setRowIndex(northButton, 1);

		eastButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				edgeDirection = EdgeDirection.EAST;
			}
		});
		GridPane.setColumnIndex(eastButton, 3);
		GridPane.setRowIndex(eastButton, 2);

		southButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				edgeDirection = EdgeDirection.SOUTH;
			}
		});
		GridPane.setColumnIndex(southButton, 2);
		GridPane.setRowIndex(southButton, 3);

		westButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				edgeDirection = EdgeDirection.WEST;
			}
		});
		GridPane.setColumnIndex(westButton, 1);
		GridPane.setRowIndex(westButton, 2);

		okButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				if (node == null || defaultLocomotive == null || edgeDirection == null) {

					logger.warn("Need a locomotive, a selected node and a direction to place a locomotive!");
					return;
				}

				final RailNode railNode = (RailNode) node;
				final Edge edge = railNode.getEdge(edgeDirection);

				final GraphNode graphNode = edge.getOutGraphNode();

				graphNode.setReserved(true);
				graphNode.setReservedLocomotiveId(defaultLocomotive.getId());

				logger.info("Put Locomotive " + defaultLocomotive.getId() + " " + defaultLocomotive.getName() + " "
						+ defaultLocomotive.getAddress() + " onto node " + node.getId() + " onto GraphNode id = "
						+ graphNode.getId());

				getScene().getWindow().hide();

				edgeDirection = null;
				defaultLocomotive = null;
				node = null;
			}
		});
		GridPane.setColumnIndex(okButton, 1);
		GridPane.setRowIndex(okButton, 4);

		final GridPane gridPane = new GridPane();
		gridPane.getChildren().addAll(northButton, eastButton, southButton, westButton, okButton);

		setSpacing(5);
		setPadding(new Insets(10, 10, 10, 10));
		getChildren().addAll(gridPane);
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
