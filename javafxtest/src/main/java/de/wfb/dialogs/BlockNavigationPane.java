package de.wfb.dialogs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.Edge;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
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

	@SuppressWarnings("rawtypes")
	private final ComboBox locomotiveComboBox = new ComboBox();

	@SuppressWarnings("rawtypes")
	private final ComboBox startBlockComboBox = new ComboBox();

	@SuppressWarnings("rawtypes")
	private final ComboBox endBlockComboBox = new ComboBox();

	@SuppressWarnings("rawtypes")
	private final ComboBox startDirectionComboBox = new ComboBox();

	private final Button startButton = new Button();

	public BlockNavigationPane() {
	}

	public void clear() {
		getChildren().clear();

		locomotiveComboBox.getItems().clear();
		startBlockComboBox.getItems().clear();
		endBlockComboBox.getItems().clear();
		startDirectionComboBox.getItems().clear();
	}

	@SuppressWarnings("unchecked")
	public void setup() {

		locomotiveComboBox.getItems().addAll(modelFacade.getLocomotives());
		locomotiveComboBox.getSelectionModel().selectFirst();

		startBlockComboBox.getItems().addAll(blockService.getAllBlocks());
//		startBlockComboBox.getSelectionModel().selectFirst();
		startBlockComboBox.getSelectionModel().select(19);

		endBlockComboBox.getItems().addAll(blockService.getAllBlocks());
		endBlockComboBox.getSelectionModel().select(40);

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

				final DefaultLocomotive locomotive = (DefaultLocomotive) locomotiveComboBox.getValue();
				final Direction locomotiveOrientation = locomotive.getOrientation();
				final Block startBlock = (Block) startBlockComboBox.getValue();
				final Direction startEdgeDirection = (Direction) startDirectionComboBox.getValue();
				final Block endBlock = (Block) endBlockComboBox.getValue();

				// @formatter:off
				stringBuffer.append("\n");
				stringBuffer.append("LOCOMOTIVE: ").append(locomotive.getName()).append("\n");
				stringBuffer.append("LOCOMOTIVE ID: ").append(locomotive.getId()).append("\n");
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

				final RailNode railNode = locomotive.getRailNode();

				logger.info("Locomotive RailNode is: " + railNode);

				if (railNode == null) {
					return;
				}

				final Edge edge = railNode.getEdge(startEdgeDirection);

				if (edge == null) {
					logger.error("The direction startEdgeDirection: " + startEdgeDirection + " does not exist!");
					return;
				}

				final GraphNode startGraphNode = edge.getOutGraphNode();

				logger.info("Assign GraphNode " + startGraphNode.getId() + " to locomotive!");
				locomotive.setGraphNode(startGraphNode);

				if (startEdgeDirection == locomotive.getOrientation()) {

					logger.info("Locomotive is going forwards!");

				} else {

					logger.info("Locomotive is going backwards (reverse)!");

				}

				// create a route
				final Route route = createRoute(endBlock, startGraphNode);
				if (route == null) {

					logger.info("Route is null!");

				} else {

					logger.info(route);

					// set the route into the locomotive, this causes the TimedDrivingThread to move
					// the locomotive starting with the next timed iteration
					locomotive.setRoute(route);
					route.setLocomotive(locomotive);

					// highlight the entire route
					logger.info("highlighting the route!");
					route.highlightRoute(applicationEventPublisher);

					// Send an event that a locomotive now has a route
					final RouteAddedEvent routeAddedEvent = new RouteAddedEvent(this, route, locomotive);
					applicationEventPublisher.publishEvent(routeAddedEvent);

					// The DrivingService will catch the event and reserve the route so that the
					// locomotive can move to the next block
				}

			}

			private Route createRoute(final Block endBlock, final GraphNode startGraphNode) {

				final RailNode endRailNode = endBlock.getNodes().get(0);

				Route routeA = null;
				try {
					routeA = routingService.route(startGraphNode, endRailNode.getGraphNodeOne());
				} catch (final Exception e) {
					;
				}

				Route routeB = null;
				try {
					routeB = routingService.route(startGraphNode, endRailNode.getGraphNodeTwo());
				} catch (final Exception e) {
					;
				}

				logger.info("RouteA: " + routeA);
				logger.info("RouteB: " + routeB);

				if (routeA == null && routeB == null) {

					return null;

				} else if (routeA != null && routeB == null) {

					return routeA;

				} else if (routeA == null && routeB != null) {

					return routeB;

				} else {

					return routeA.getGraphNodes().size() < routeB.getGraphNodes().size() ? routeA : routeB;
				}
			}
		});

		setSpacing(5);
		setPadding(new Insets(10, 10, 10, 10));

		getChildren().addAll(locomotiveComboBox, startBlockComboBox, startDirectionComboBox, endBlockComboBox,
				startButton);
	}

}
