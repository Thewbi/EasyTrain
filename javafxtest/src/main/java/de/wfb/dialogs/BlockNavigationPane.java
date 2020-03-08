package de.wfb.dialogs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.service.RoutingService;
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
 * and a driving direction to move. It will compute a route and assign the rout
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
		getChildren().add(locomotiveComboBox);

		startBlockComboBox.getItems().addAll(blockService.getAllBlocks());
		startBlockComboBox.getSelectionModel().select(19);
		getChildren().add(startBlockComboBox);

		endBlockComboBox.getItems().addAll(blockService.getAllBlocks());
		endBlockComboBox.getSelectionModel().select(40);
		getChildren().add(endBlockComboBox);

		startDirectionComboBox.getItems().add(Direction.NORTH);
		startDirectionComboBox.getItems().add(Direction.EAST);
		startDirectionComboBox.getItems().add(Direction.SOUTH);
		startDirectionComboBox.getItems().add(Direction.WEST);
		startDirectionComboBox.getSelectionModel().select(1);
		getChildren().add(startDirectionComboBox);

		startButton.setText("Start");
		startButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent actionEvent) {

				logger.info("BlockNavigationPane - StartButton");

				final DefaultLocomotive locomotive = (DefaultLocomotive) locomotiveComboBox.getValue();
				final Direction locomotiveOrientation = locomotive.getOrientation();
				final Block startBlock = (Block) startBlockComboBox.getValue();
				final Direction startEdgeDirection = (Direction) startDirectionComboBox.getValue();
				final Block endBlock = (Block) endBlockComboBox.getValue();

				// @formatter:off

				final StringBuffer stringBuffer = new StringBuffer();

				stringBuffer.append("\n");
				stringBuffer.append("LOCOMOTIVE: ").append(locomotive.getName()).append("\n");
				stringBuffer.append("LOCOMOTIVE ID: ").append(locomotive.getId()).append("\n");
				stringBuffer.append("LOCOMOTIVE ORIENTATION: ").append(locomotiveOrientation.name()).append("\n");
				stringBuffer.append("START BLOCK: ").append(startBlock).append("\n");
				stringBuffer.append("START DIRECTION: ").append(startEdgeDirection.name()).append("\n");
				stringBuffer.append("END BLOCK: ").append(endBlock).append("\n");

				logger.info(stringBuffer.toString());

				// @formatter:on

				final boolean routeOverReservedNodes = false;
				final boolean routeOverBlockedFeedbackBlocks = false;
				final Route route = routingService.startLocomotiveToBlock(locomotive, locomotiveOrientation, startBlock,
						startEdgeDirection, endBlock, routeOverReservedNodes, routeOverBlockedFeedbackBlocks);

				logger.info("Route: " + route);

				routingService.attachRouteToLocomotive(locomotive, route);
			}
		});
		getChildren().add(startButton);

		setSpacing(5);
		setPadding(new Insets(10, 10, 10, 10));
	}

}
