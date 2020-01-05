package de.wfb;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.wfb.dialogs.SidePane;
import de.wfb.dialogs.ThrottleStage;
import de.wfb.javafxtest.controller.LayoutGridController;
import de.wfb.javafxtest.controls.CustomGridPane;
import de.wfb.model.service.ModelService;
import de.wfb.rail.facade.DefaultProtocolFacade;
import de.wfb.rail.facade.ProtocolFacade;
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

	private ModelService modelService;

	private ProtocolFacade protocolFacade;

	private EvtSenCommandThread evtSenCommandThread;

	// private boolean running = true;

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

							logger.info("OK");
							protocolFacade.disconnect();
							try {
								Thread.sleep(2000);
							} catch (final InterruptedException e) {
								logger.error(e.getMessage(), e);
							}
//							running = false;
							evtSenCommandThread.stop();
							Platform.exit();

						} else if (alert.getResult().equals(ButtonType.YES)) {

							logger.info("OK");
							protocolFacade.disconnect();
							try {
								Thread.sleep(2000);
							} catch (final InterruptedException e) {
								logger.error(e.getMessage(), e);
							}
//							running = false;
							evtSenCommandThread.stop();
							Platform.exit();

						} else {

							logger.info("Cancel");
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
		modelService = context.getBean(ModelService.class);
		sidePane = context.getBean(SidePane.class);
		protocolFacade = context.getBean(DefaultProtocolFacade.class);
		evtSenCommandThread = context.getBean(EvtSenCommandThread.class);

		// load the model
		try {
			logger.info("Loading model ...");
			modelService.loadModel();
			logger.info("Loading model done.");
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}

		// connect to the intellibox
		try {
			protocolFacade.connect();
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
		// display the layout element selection view
//		final LayoutElementSelectionDialogStage layoutElementSelectionDialogStage = context
//				.getBean(LayoutElementSelectionDialogStage.class);
//		layoutElementSelectionDialogStage.initModality(Modality.WINDOW_MODAL);
//		layoutElementSelectionDialogStage.show();

		// layoutElementSelectionDialogStage =
		// context.getBean(LayoutElementSelectionDialogStage.class);

		// locomotive throttle
		final ThrottleStage throttleStage = context.getBean(ThrottleStage.class);
		throttleStage.initialize();
		throttleStage.initModality(Modality.WINDOW_MODAL);
		throttleStage.setX(0);
		throttleStage.setY(0);

		throttleStage.show();

		stage.setScene(createScene(stage, closeWindowEventHandler, context));
		stage.setTitle("ScrollPaneDemo");
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
		logger.info("Startup.stop()");
		Platform.exit();
	}

	private Scene createScene(final Stage stage, final EventHandler<WindowEvent> closeWindowHandler,
			final ApplicationContext context) {

		logger.trace("createScene");

		final LayoutGridController layoutGridController = context.getBean(LayoutGridController.class);

		final CustomGridPane customGridPane = context.getBean(CustomGridPane.class);
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

				logger.info("SHIFT pressed");
				layoutGridController.setShiftState(true);
				customGridPane.setShiftState(true);
			}
		});

		scene.setOnKeyReleased(event -> {

			if (event.getCode() == KeyCode.SHIFT) {

				logger.info("SHIFT released");
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

		final MenuItem saveItem = new MenuItem("Save");
		saveItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("Save Menu clicked!");

				modelService.storeModel();
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

		// add menuItems to the Menus
		fileMenu.getItems().addAll(newItem, openFileItem, exitItem, saveItem);
		editMenu.getItems().addAll(connectItem, copyItem, pasteItem);
		serialMenu.getItems().addAll(serialConnectItem, serialDisconnectItem);

		// add Menus to the MenuBar
		menuBar.getMenus().addAll(fileMenu, editMenu, serialMenu, helpMenu);

		return menuBar;
	}

}
