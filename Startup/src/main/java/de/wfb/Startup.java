package de.wfb;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.wfb.dialogs.BlockNavigationPane;
import de.wfb.dialogs.LocomotiveListStage;
import de.wfb.dialogs.PlaceLocomotivePane;
import de.wfb.dialogs.PlaceLocomotiveStage;
import de.wfb.dialogs.SidePane;
import de.wfb.dialogs.ThrottleStage;
import de.wfb.javafxtest.controller.LayoutGridController;
import de.wfb.javafxtest.controls.CustomGridPane;
import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.DefaultRoutingService;
import de.wfb.model.service.RoutingService;
import de.wfb.rail.events.BlockExitedEvent;
import de.wfb.rail.events.FeedbackBlockEvent;
import de.wfb.rail.events.FeedbackBlockState;
import de.wfb.rail.facade.DefaultProtocolFacade;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.BlockService;
import de.wfb.rail.service.Route;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * NEXT:
 * <ol>
 *
 * <li>In der UI protocolId der Weichen einstellen</li>
 * <li>Model file explicit speichern</li>
 * <li>Model file explicit laden</li>
 * <li>Bei ungespeicherten Änderungen im Model nicht beenden</li>
 *
 * <li>connect disconnect einbauen</li>
 *
 * </ol>
 */
public class Startup extends Application {

	private static final Logger logger = LogManager.getLogger(Startup.class);

	private EventHandler<WindowEvent> closeWindowEventHandler;

	private SidePane sidePane;

	private ModelFacade modelFacade;

	private ProtocolFacade protocolFacade;

	private EvtSenCommandThread evtSenCommandThread;

	private RoutingService routingService;

	private DefaultDebugFacade defaultDebugFacade;

	private LocomotiveListStage locomotiveListStage;

	private PlaceLocomotiveStage placeLocomotiveStage;

	private BlockService blockService;

	private LayoutGridController layoutGridController;

	private CustomGridPane customGridPane;

	private BlockNavigationPane blockNavigationPane;

	private CustomThreadPoolScheduler customThreadPoolScheduler;

	private FeedbackBlockState feedbackBlockState = FeedbackBlockState.BLOCKED;

	public static void main(final String[] args) {

		logger.trace("main");
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {

		logger.trace("start");

		closeWindowEventHandler = new EventHandler<WindowEvent>() {

			@Override
			public void handle(final WindowEvent event) {

				Platform.runLater(new Runnable() {

					@Override
					public void run() {

						final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
						alert.setTitle("Exit?");
						alert.setHeaderText("Confirm Exit.");
						alert.setContentText("Exit?");

						alert.showAndWait();

						if (alert.getResult().equals(ButtonType.OK)) {

							logger.trace("OK");
							protocolFacade.disconnect();
							try {
								Thread.sleep(2000);
							} catch (final InterruptedException e) {
								logger.error(e.getMessage(), e);
							}
							evtSenCommandThread.stop();
							Platform.exit();

						} else if (alert.getResult().equals(ButtonType.YES)) {

							logger.trace("OK");
							protocolFacade.disconnect();
							try {
								Thread.sleep(2000);
							} catch (final InterruptedException e) {
								logger.error(e.getMessage(), e);
							}
							evtSenCommandThread.stop();
							Platform.exit();

						} else {

							logger.trace("Cancel");
							event.consume();

							// I think, there is no way to prevent the main window from closing.
							// If the the user did abort the exit, just display the main window again.
							stage.show();

						}
					}
				});
			}
		};

		// https://stackoverflow.com/questions/31886204/where-is-javaconfigapplicationcontext-class-nowadays
		final ApplicationContext context = new AnnotationConfigApplicationContext(ConfigurationClass.class);

		// facades
		modelFacade = context.getBean(ModelFacade.class);
		protocolFacade = context.getBean(DefaultProtocolFacade.class);
		defaultDebugFacade = context.getBean(DefaultDebugFacade.class);

		// services
		routingService = context.getBean(DefaultRoutingService.class);
		blockService = context.getBean(BlockService.class);

		// threads
		evtSenCommandThread = context.getBean(EvtSenCommandThread.class);
		customThreadPoolScheduler = context.getBean(CustomThreadPoolScheduler.class);

		// UI
		sidePane = context.getBean(SidePane.class);
		layoutGridController = context.getBean(LayoutGridController.class);
		customGridPane = context.getBean(CustomGridPane.class);
		blockNavigationPane = context.getBean(BlockNavigationPane.class);
		locomotiveListStage = context.getBean(LocomotiveListStage.class);
		placeLocomotiveStage = context.getBean(PlaceLocomotiveStage.class);

		// load the model
		try {
			logger.info("Loading model ...");
			modelFacade.loadModel();
			modelFacade.connectModel();
			logger.info("Loading model done.");
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}

		// build blocks
		blockService.determineBlocks();

		// build the routing tables
		routingService.buildRoutingTables();
		routingService.colorGraph();

		// UI setup
		locomotiveListStage.initModality(Modality.WINDOW_MODAL);
		locomotiveListStage.initialize();

		placeLocomotiveStage.initModality(Modality.WINDOW_MODAL);
		placeLocomotiveStage.initialize();

		blockNavigationPane.setup();

		DefaultLocomotive defaultLocomotive = null;
		RailNode blockRailNode = null;

		// DEBUG - place the first locomotive on the first block
		defaultLocomotive = modelFacade.getLocomotives().get(0);
		blockRailNode = blockService.getAllBlocks().get(0).getNodes().get(0);
		PlaceLocomotivePane.placeLocomotive(blockRailNode, defaultLocomotive, Direction.EAST);

		// DEBUG - place the second locomotive onto the second block
		defaultLocomotive = modelFacade.getLocomotives().get(1);
		blockRailNode = blockService.getAllBlocks().get(1).getNodes().get(0);
		PlaceLocomotivePane.placeLocomotive(blockRailNode, defaultLocomotive, Direction.SOUTH);

		// connect to the intellibox
		try {
			protocolFacade.connect();
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}

		// locomotive throttle
		final ThrottleStage throttleStage = context.getBean(ThrottleStage.class);
		throttleStage.initialize();
		throttleStage.initModality(Modality.WINDOW_MODAL);
		throttleStage.setX(0);
		throttleStage.setY(0);

		throttleStage.show();

		stage.setScene(createScene(stage, closeWindowEventHandler));
		stage.setTitle("Easy Train (Beta v0.1)");
		stage.setWidth(800);
		stage.setHeight(600);

		stage.show();

		// stop the application after it's last window was closed as opposed to keep a
		// thread without any windows
		// in the background
		Platform.setImplicitExit(true);

		// https://stackoverflow.com/questions/26619566/javafx-stage-close-handler
		stage.setOnCloseRequest(closeWindowEventHandler);

		startP50XXEvtSenCommandThread();
	}

	private void startP50XXEvtSenCommandThread() {

		logger.info("startP50XXEvtSenCommandThread()");

		final Thread thread = new Thread(evtSenCommandThread);
		thread.start();
	}

	@Override
	public void stop() {

		customThreadPoolScheduler.stop();
		customThreadPoolScheduler.shutdown();

		logger.trace("Startup.stop()");
		Platform.exit();
	}

	private Scene createScene(final Stage stage, final EventHandler<WindowEvent> closeWindowHandler) {

		logger.trace("createScene");

		customGridPane.Initialize();
		customGridPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

		final Group group = new Group(customGridPane);

		final ScrollPane scrollPane = new ScrollPane(group);
		scrollPane.setStyle("-fx-background: rgb(150, 150, 150);");
		scrollPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {

				logger.trace(e.getSource());
			}
		});

		final StackPane stackPane = new StackPane();
		stackPane.getChildren().addAll(scrollPane);
		stackPane.setBackground(new Background(new BackgroundFill(Color.DARKGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
		stackPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent e) {

				logger.trace(e.getSource());
			}
		});

		final BorderPane borderPane = new BorderPane();
		borderPane.setTop(createMenu(stage, layoutGridController));
		borderPane.setCenter(stackPane);
		borderPane.setRight(createDetailsView());
		borderPane.setBottom(blockNavigationPane);

		borderPane.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(final KeyEvent event) {

				switch (event.getCode()) {

				case UP:
					customGridPane.zoomIn();
					break;

				case DOWN:
					customGridPane.zoomOut();
					break;

				default:
					break;
				}
			}
		});

		final Scene scene = new Scene(borderPane);

		scene.setOnKeyPressed(event -> {

			if (event.getCode() == KeyCode.SHIFT) {

				logger.trace("SHIFT pressed");
				layoutGridController.setShiftState(true);
				customGridPane.setShiftState(true);
			}
		});

		scene.setOnKeyReleased(event -> {

			if (event.getCode() == KeyCode.SHIFT) {

				logger.trace("SHIFT released");
				layoutGridController.setShiftState(false);
				customGridPane.setShiftState(false);
			}
		});

		return scene;
	}

	private Node createDetailsView() {

		final StackPane stackPane = new StackPane();
		stackPane.setMinWidth(300);

		try {
			sidePane.setup();
			stackPane.getChildren().add(sidePane);
		} catch (final FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

		return stackPane;
	}

	private MenuBar createMenu(final Stage stage, final LayoutGridController layoutGridController) {

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
				routingService.colorGraph();

				final List<de.wfb.model.node.Node> selectedNodes = modelFacade.getSelectedNodes();

				if (CollectionUtils.isNotEmpty(selectedNodes) && selectedNodes.size() >= 2) {

					logger.trace("Selected Nodes found!");

					final de.wfb.model.node.Node nodeA = selectedNodes.get(0);
					final de.wfb.model.node.Node nodeB = selectedNodes.get(1);

					final Route route = routingService.route(nodeA, nodeB);
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

				modelFacade.storeModel();
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

		// add menuItems to the Menus
		fileMenu.getItems().addAll(newItem, openFileItem, saveItem, exitItem);
		routingMenu.getItems().addAll(findRouteMenuItem);
		editMenu.getItems().addAll(connectItem, copyItem, pasteItem, locomotiveListItem, placeLocomotiveItem);
		serialMenu.getItems().addAll(serialConnectItem, serialDisconnectItem);
		debugMenu.getItems().addAll(routingNodeMenuItem, feedbackBlockEventMenuItem, removeLocomotiveItem);

		// add Menus to the MenuBar
		menuBar.getMenus().addAll(fileMenu, routingMenu, debugMenu, editMenu, serialMenu, helpMenu);

		return menuBar;
	}

}
