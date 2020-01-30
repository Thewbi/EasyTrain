package de.wfb.factory;

import java.io.File;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.web.facade.DebugFacade;
import de.wfb.dialogs.LocomotiveListStage;
import de.wfb.dialogs.PlaceLocomotiveStage;
import de.wfb.javafxtest.controller.LayoutGridController;
import de.wfb.javafxtest.controls.CustomGridPane;
import de.wfb.model.ViewModel;
import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.RoutingService;
import de.wfb.rail.events.BlockExitedEvent;
import de.wfb.rail.events.FeedbackBlockEvent;
import de.wfb.rail.events.FeedbackBlockState;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.BlockService;
import de.wfb.rail.service.Route;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class DefaultMenuBarFactory implements Factory<MenuBar> {

	private static final Logger logger = LogManager.getLogger(DefaultMenuBarFactory.class);

	/**
	 * The state is toggled, that is why the factory has to remember the old state
	 * and that is why it has a member variable for the state.
	 */
	private FeedbackBlockState feedbackBlockState = FeedbackBlockState.BLOCKED;

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private ProtocolFacade protocolFacade;

	@Autowired
	private DebugFacade defaultDebugFacade;

	@Autowired
	private RoutingService routingService;

	@Autowired
	private BlockService blockService;

	@Autowired
	private ViewModel viewModel;

	@Autowired
	private CustomGridPane customGridPane;

	@Autowired
	private LocomotiveListStage locomotiveListStage;

	@Autowired
	private PlaceLocomotiveStage placeLocomotiveStage;

	@Autowired
	private XTrntStatusMenuItem xTrntStatusMenuItem;

	@Autowired
	private GraphNodeSVGMenuItem graphNodeSVGMenuItem;

	@Override
	public MenuBar create(final Object... args) throws Exception {

		final Stage stage = (Stage) args[0];
		final LayoutGridController layoutGridController = (LayoutGridController) args[1];

		// create MenuBar
		final MenuBar menuBar = new MenuBar();

		// create menus
		final Menu fileMenu = new Menu("File");
		final Menu debugMenu = new Menu("Debug");
		final Menu routingMenu = new Menu("Routing");
		final Menu editMenu = new Menu("Edit");
		final Menu serialMenu = new Menu("Serial");
		final Menu helpMenu = new Menu("Help");

		// create MenuItems
		final MenuItem newItem = new MenuItem("New");
		final MenuItem openFileItem = new MenuItem("Open File");
		openFileItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("Open File Menu clicked!");

				final FileChooser fileChooser = new FileChooser();

				// set extension filter for text files
				final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Json files (*.json)",
						"*.json");
				fileChooser.getExtensionFilters().add(extFilter);

				// show save file dialog
				final File file = fileChooser.showOpenDialog(stage);
				if (file != null) {

					modelFacade.clear();
					customGridPane.getChildren().clear();

					viewModel.clear();

					modelFacade.loadModel(file.getAbsolutePath());
					modelFacade.connectModel();
				}
			}
		});

		final MenuItem exitItem = new MenuItem("Exit");
		exitItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("Exit Menu clicked!");

				final Window window = stage.getScene().getWindow();
				window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
			}
		});

		final MenuItem findRouteMenuItem = new MenuItem("Find Route");
		findRouteMenuItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.trace("Route Menu clicked!");

				// build the routing tables
				routingService.buildRoutingTables();
//				routingService.colorGraph();

				final List<de.wfb.model.node.Node> selectedNodes = modelFacade.getSelectedNodes();

				if (CollectionUtils.isNotEmpty(selectedNodes) && selectedNodes.size() >= 2) {

					logger.trace("Selected Nodes found!");

					final de.wfb.model.node.Node nodeA = selectedNodes.get(0);
					final de.wfb.model.node.Node nodeB = selectedNodes.get(1);

					final DefaultLocomotive locomotive = null;
					final boolean routeOverReservedNodes = false;
					final Route route = routingService.route(locomotive, nodeA, nodeB, routeOverReservedNodes);

					logger.info(route);

					if (CollectionUtils.isNotEmpty(route.getGraphNodes())) {

						routingService.highlightRoute(route);
						routingService.switchTurnouts(route);
					}

				} else {

					logger.info("Not enough selected nodes found!");

				}
			}
		});

		final MenuItem saveItem = new MenuItem("Save");
		saveItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("Save Menu clicked!");

				modelFacade.storeModel(modelFacade.getCurrentModel());
			}
		});

		final MenuItem connectItem = new MenuItem("Connect");
		connectItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				// connect two nodes
				layoutGridController.connect();
			}
		});
		final MenuItem copyItem = new MenuItem("Copy");
		final MenuItem pasteItem = new MenuItem("Paste");
		final MenuItem locomotiveListItem = new MenuItem("Locomotive List");
		locomotiveListItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				locomotiveListStage.initialize();
				locomotiveListStage.synchronizeModel();
				locomotiveListStage.show();
			}
		});

		final MenuItem placeLocomotiveItem = new MenuItem("Place Locomotive");
		placeLocomotiveItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				if (placeLocomotiveStage.getNode() == null) {

					final Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("No Node Selected!");
					alert.setHeaderText("No Node Selected!");

					final StringBuffer stringBuffer = new StringBuffer();
					stringBuffer.append("You have to select a node to place a locomotive!");

					alert.setContentText(stringBuffer.toString());

					alert.showAndWait();

				} else {

					placeLocomotiveStage.initialize();
					placeLocomotiveStage.synchronizeModel();
					placeLocomotiveStage.show();

				}
			}
		});

		final MenuItem serialConnectItem = new MenuItem("Serial Connect");
		serialConnectItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("Connecting: " + protocolFacade);

				try {

					// connect to the intellibox
					protocolFacade.connect();
				} catch (final Exception e) {

					logger.error(e.getMessage(), e);

					final Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Connection Failed!");
					alert.setHeaderText("Confirm Failed!");

					final StringBuffer stringBuffer = new StringBuffer();
					stringBuffer.append("Connecting to the serial port ???: ").append(e.getClass().getSimpleName())
							.append(" ").append(e.getMessage());

					alert.setContentText(stringBuffer.toString());

					alert.showAndWait();
				}
			}
		});

		final MenuItem serialDisconnectItem = new MenuItem("Serial Disconnect");
		serialDisconnectItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				protocolFacade.disconnect();
			}
		});

		final MenuItem routingNodeMenuItem = new MenuItem("Routing Node");
		routingNodeMenuItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				modelFacade.debugRoute();
			}
		});

		final MenuItem feedbackBlockEventMenuItem = new MenuItem("Feedback Block Event");
		feedbackBlockEventMenuItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				final int feedbackBlockNumber = 1;

				final FeedbackBlockEvent feedbackBlockEvent = new FeedbackBlockEvent(this, feedbackBlockNumber,
						feedbackBlockState);
				defaultDebugFacade.publishEvent(feedbackBlockEvent);

				feedbackBlockState = feedbackBlockState == FeedbackBlockState.BLOCKED ? FeedbackBlockState.FREE
						: FeedbackBlockState.BLOCKED;
			}
		});

		final MenuItem removeLocomotiveItem = new MenuItem("Remove Locomotive 2");
		removeLocomotiveItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("removeLocomotiveItem");

				final DefaultLocomotive defaultLocomotive = modelFacade.getLocomotives().get(1);
				final Block block = blockService.getAllBlocks().get(1);
				block.reserveForLocomotive(null);
				for (final RailNode railNodes : block.getNodes()) {

					railNodes.setReserved(false);
					railNodes.setReservedLocomotiveId(-1);
				}
				defaultLocomotive.setRailNode(null);
				defaultLocomotive.setGraphNode(null);

				final BlockExitedEvent blockExitedEvent = new BlockExitedEvent(this, block, defaultLocomotive);
				defaultDebugFacade.publishEvent(blockExitedEvent);
			}
		});

		final MenuItem sensorCommandMenuItem = new MenuItem("Sensor Command");
		sensorCommandMenuItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("sensorCommandItem");

				protocolFacade.sense(3);
			}

		});

		final MenuItem xSensOffMenuItem = new MenuItem("XSensOff Command");
		xSensOffMenuItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("xSensOffMenuItem");

				protocolFacade.xSenseOff();
			}

		});

//		final MenuItem xTrntStatusMenuItem = new XTrnStatusMenuItem("XTrntStatus Command");
//		xTrntStatusMenuItem.setOnAction(new EventHandler<ActionEvent>() {
//
//			@Override
//			public void handle(final ActionEvent event) {
//
//				logger.info("xTrntStatusMenuItem");
//
//				final boolean isThrown = protocolFacade.turnoutStatus((short) 28);
//				logger.info("Result: " + isThrown);
//			}
//
//		});

		// add menuItems to the Menus
		fileMenu.getItems().addAll(newItem, openFileItem, saveItem, exitItem);
		routingMenu.getItems().addAll(findRouteMenuItem);
		editMenu.getItems().addAll(connectItem, copyItem, pasteItem, locomotiveListItem, placeLocomotiveItem);
		serialMenu.getItems().addAll(serialConnectItem, serialDisconnectItem);
		debugMenu.getItems().addAll(routingNodeMenuItem, feedbackBlockEventMenuItem, removeLocomotiveItem,
				sensorCommandMenuItem, xSensOffMenuItem, xTrntStatusMenuItem, graphNodeSVGMenuItem);

		// add Menus to the MenuBar
		menuBar.getMenus().addAll(fileMenu, routingMenu, debugMenu, editMenu, serialMenu, helpMenu);

		return menuBar;
	}

}
