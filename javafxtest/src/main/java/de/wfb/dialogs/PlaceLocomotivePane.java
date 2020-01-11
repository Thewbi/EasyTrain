package de.wfb.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.events.LocomotiveSelectedEvent;
import de.wfb.rail.events.NodeClickedEvent;
import de.wfb.rail.service.Block;
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

					PlaceLocomotivePane.placeLocomotive(node, defaultLocomotive, edgeDirection);

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

				northButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						edgeDirection = Direction.NORTH;
					}
				});
				GridPane.setColumnIndex(northButton, 1);
				GridPane.setRowIndex(northButton, 1);
				result.add(northButton);

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

				eastButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						edgeDirection = Direction.EAST;
					}
				});
				GridPane.setColumnIndex(eastButton, 2);
				GridPane.setRowIndex(eastButton, 1);
				result.add(eastButton);

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

	public static void placeLocomotive(final Node node, final DefaultLocomotive locomotive,
			final Direction edgeDirection) {

		if (node == null || locomotive == null || edgeDirection == null) {

			final String msg = "Need a locomotive, a selected node and a direction to place a locomotive!";
			logger.error(msg);

			throw new IllegalArgumentException(msg);
		}

		// set the direction into the locomotive so later when the locomotive
		// starts moving, the P50X protocol knows weather to make the locomotive move in
		// forwards or reverse direction
		locomotive.setOrientation(edgeDirection);
		locomotive.setRailNode((RailNode) node);

		final RailNode railNode = (RailNode) node;
		final Block block = railNode.getBlock();
		if (block == null) {

			// put the locomotive onto the rail node
			if (railNode.isReserved()) {
				throw new IllegalArgumentException("RailNode is reserved already!");
			}

			railNode.setReserved(true);
			railNode.setReservedLocomotiveId(locomotive.getId());

		} else {

			// if the rail node is part of a block, reserve the entire block
			block.reserveByLocomotive(locomotive);

			logger.info("Put Locomotive " + locomotive.getName() + " onto Block " + block.getId());

		}

		// Put the Loco on both Graph nodes of the current RailNode
//		final Edge edge = railNode.getEdge(edgeDirection);
//
//		GraphNode graphNode = null;
//
//		graphNode = edge.getOutGraphNode();
//		graphNode.setReserved(true);
//		graphNode.setReservedLocomotiveId(defaultLocomotive.getId());
//
//		graphNode = edge.getInGraphNode();
//		graphNode.setReserved(true);
//		graphNode.setReservedLocomotiveId(defaultLocomotive.getId());

//		logger.info("Put Locomotive " + defaultLocomotive.getId() + " " + defaultLocomotive.getName() + " "
//				+ defaultLocomotive.getAddress() + " onto node " + node.getId());

		logger.info("Put Locomotive " + locomotive.getName() + " onto node " + node.getId());
	}
}
