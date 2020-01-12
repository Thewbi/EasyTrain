package de.wfb.dialogs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.GraphNode;
import de.wfb.model.service.RoutingService;
import de.wfb.rail.events.RouteAddedEvent;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.BlockService;
import de.wfb.rail.service.Route;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * This pane allows a user to select a locomotive, a start block, a target block
 * and a driving direction to move. It will compute a rout and assign the rout
 * to the locomotive.
 */
public class BlockNavigationPane extends HBox {

	private static final Logger logger = LogManager.getLogger(BlockNavigationPane.class);

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private BlockService blockService;

	@Autowired
	private RoutingService routingService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	private final ComboBox locomotiveComboBox = new ComboBox();

	private final ComboBox startBlockComboBox = new ComboBox();

	private final ComboBox endBlockComboBox = new ComboBox();

	private final ComboBox startDirectionComboBox = new ComboBox();

	private final Button startButton = new Button();

	private final GridPane gridPane = new GridPane();

	public BlockNavigationPane() {
	}

	public void clear() {
		getChildren().clear();

		locomotiveComboBox.getItems().clear();
		startBlockComboBox.getItems().clear();
		endBlockComboBox.getItems().clear();
		startDirectionComboBox.getItems().clear();
	}

	public void setup() {

		locomotiveComboBox.getItems().addAll(modelFacade.getLocomotives());
		locomotiveComboBox.getSelectionModel().selectFirst();

		startBlockComboBox.getItems().addAll(blockService.getAllBlocks());
		startBlockComboBox.getSelectionModel().selectFirst();

		endBlockComboBox.getItems().addAll(blockService.getAllBlocks());
		endBlockComboBox.getSelectionModel().select(2);

		startDirectionComboBox.getItems().add(Direction.NORTH);
		startDirectionComboBox.getItems().add(Direction.EAST);
		startDirectionComboBox.getItems().add(Direction.SOUTH);
		startDirectionComboBox.getItems().add(Direction.WEST);
		startDirectionComboBox.getSelectionModel().select(1);

		startButton.setText("Start");
		startButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent actionEvent) {

				final StringBuffer stringBuffer = new StringBuffer();

				final DefaultLocomotive defaultLocomotive = (DefaultLocomotive) locomotiveComboBox.getValue();
				final Direction locomotiveOrientation = defaultLocomotive.getOrientation();
				final Block startBlock = (Block) startBlockComboBox.getValue();
				final Direction startEdgeDirection = (Direction) startDirectionComboBox.getValue();
				final Block endBlock = (Block) endBlockComboBox.getValue();

				// @formatter:off
				stringBuffer.append("\n");
				stringBuffer.append("LOCOMOTIVE: ").append(defaultLocomotive.getName()).append("\n");
				stringBuffer.append("LOCOMOTIVE ID: ").append(defaultLocomotive.getId()).append("\n");
				stringBuffer.append("LOCOMOTIVE ORIENTATION: ").append(locomotiveOrientation.name()).append("\n");
				stringBuffer.append("START BLOCK: ").append(startBlock).append("\n");
				stringBuffer.append("START DIRECTION: ").append(startEdgeDirection.name()).append("\n");
				stringBuffer.append("END BLOCK: ").append(endBlock).append("\n");
				// @formatter:on

				logger.info(stringBuffer.toString());

				// determine the correct graph node based on the direction the user wants the
				// route to be
				// traversed by the locomotive.
				//
				// The startEdgeDirection is not the direction in which the front part of the
				// faces!
				//
				// If startEdgeDirection and locomotive defaultLocomotive.getOrientation() point
				// in the same direction, the locomotive will move in forwards direction.
				//
				// If they do not point into the same direction, the locomotive will move in
				// reverse direction.
				final GraphNode startGraphNode = defaultLocomotive.getRailNode().getEdge(startEdgeDirection)
						.getOutGraphNode();

				if (startEdgeDirection == defaultLocomotive.getOrientation()) {

					logger.info("Locomotive is going forwards!");

				} else {

					logger.info("Locomotive is going backwards (reverse)!");

				}

				// create a route
				Route route = null;

				try {
					route = routingService.route(startGraphNode, endBlock.getNodes().get(0).getGraphNodeOne());
				} catch (final Exception e) {
					;
				}

				if (route == null) {
					try {
						route = routingService.route(startGraphNode, endBlock.getNodes().get(0).getGraphNodeTwo());
					} catch (final Exception e) {
						;
					}
				}

				if (route == null) {

					logger.info("Route is null!");

				} else {

					logger.info(route);

					// set the route into the locomotive, this causes the TimedDrivingThread to move
					// the locomotive starting with the next timed iteration
					defaultLocomotive.setRoute(route);
					route.setLocomotive(defaultLocomotive);

					// Send an event that a locomotive now has a route
					final RouteAddedEvent routeAddedEvent = new RouteAddedEvent(this, route, defaultLocomotive);
					applicationEventPublisher.publishEvent(routeAddedEvent);

					// The DrivingService will catch the event and reserve the route so that the
					// locomotive can move to the next block
				}

			}
		});

		setSpacing(5);
		setPadding(new Insets(10, 10, 10, 10));

		getChildren().addAll(locomotiveComboBox, startBlockComboBox, startDirectionComboBox, endBlockComboBox,
				startButton);
	}

}
