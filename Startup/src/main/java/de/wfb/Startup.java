package de.wfb;

import java.awt.event.WindowEvent;
import java.util.logging.LogManager;

import org.graalvm.compiler.phases.common.NodeCounterPhase.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.apple.eawt.Application;

import de.wfb.dialogs.BlockNavigationPane;
import de.wfb.dialogs.DrivingThreadControlPane;
import de.wfb.dialogs.EmergencyStopPane;
import de.wfb.dialogs.LocomotiveListStage;
import de.wfb.dialogs.PlaceLocomotiveStage;
import de.wfb.dialogs.ThrottleStage;
import de.wfb.factory.DefaultSceneFactory;
import de.wfb.factory.GraphNodeSVGMenuItem;
import de.wfb.model.facade.ModelFacade;
import de.wfb.model.service.RoutingService;
import de.wfb.rail.facade.DefaultProtocolFacade;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.service.BlockService;
import de.wfb.rail.service.TurnoutService;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import jdk.tools.jlink.internal.Platform;

/**
 * TODO:
 * <ol>
 * <li>In der UI protocolId der Weichen einstellen</li>
 * <li>Model file explicit speichern</li>
 * <li>Model file explicit laden</li>
 * <li>Bei ungespeicherten Änderungen im Model nicht beenden</li>
 * </ol>
 */
public class Startup extends Application {

	private static final int WINDOW_HEIGHT = 800;

	private static final int WINDOW_WIDTH = 1400;

	private static final Logger logger = LogManager.getLogger(Startup.class);

	private static final boolean SHOW_THROTTLE_NONMODAL_DIALOG = false;

	private EventHandler<WindowEvent> closeWindowEventHandler;

	private ModelFacade modelFacade;

	private ProtocolFacade protocolFacade;

	private EvtSenCommandThread evtSenCommandThread;

	private RoutingService routingService;

	private LocomotiveListStage locomotiveListStage;

	private PlaceLocomotiveStage placeLocomotiveStage;

	private BlockService blockService;

	private TurnoutService turnoutService;

	private BlockNavigationPane blockNavigationPane;

	private DrivingThreadControlPane drivingThreadControlPane;

	private CustomThreadPoolScheduler customThreadPoolScheduler;

	private EmergencyStopPane emergencyStopPane;

	private Factory<Scene> sceneFactory;

	public static void main(final String[] args) {

		logger.trace("main");
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {

		logger.trace("start");

		closeWindowEventHandler = createCloseWindowEventHandler(stage);

		final ApplicationContext context = loadApplicationContext(stage);

		// load the model
		loadModel("persistence/model.json", "persistence/locomotives.json");
//		loadModel("../model/src/test/resources/DefaultRoutingServiceTest/model.json", "persistence/locomotives.json");
//		loadModel("../model/src/test/resources/DefaultRoutingServiceTest/modelLearning.json",
//				"persistence/locomotives.json");

		routingService.initialize();

		// build blocks
		blockService.determineBlocks();
		blockService.createBlockGroups();

		turnoutService.createTurnoutGroups();

		// build the routing tables
		routingService.buildRoutingTables();

		// coloring the graph is worthless as loops will paint all node in a single
		// color which invalidates the entire idea
//		routingService.colorGraph();

		// UI setup
		locomotiveListStage.initModality(Modality.WINDOW_MODAL);
		locomotiveListStage.initialize();

		placeLocomotiveStage.initModality(Modality.WINDOW_MODAL);
		placeLocomotiveStage.initialize();

		blockNavigationPane.setup();
		drivingThreadControlPane.setup();
		emergencyStopPane.setup();

		// connect to the intellibox
		try {
			protocolFacade.connectToIntellibox();
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);

			final Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Connection failed!");
			alert.setHeaderText("Connection to Intellibox failed!");
			alert.setContentText("Please connect the cable to the intellibox and try again!");
			alert.showAndWait().ifPresent(rs -> {
				if (rs == ButtonType.OK) {
					logger.info("Not connected message box. Pressed OK.");
				}
			});

		}

		// locomotive throttle
		if (SHOW_THROTTLE_NONMODAL_DIALOG) {
			createAndShowThrottle(context);
		}

		// stop the application after it's last window was closed as opposed to keep a
		// thread without any windows
		// in the background
		Platform.setImplicitExit(true);

		createAndShowScene(stage);
	}

	@Override
	public void stop() {

		logger.info("Startup.stop() - Shutting down scheduler! Active Count: "
				+ customThreadPoolScheduler.getActiveCount());

		customThreadPoolScheduler.stop();
		customThreadPoolScheduler.shutdown();

		int activeCount = 0;
		do {

			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				logger.error(e.getMessage(), e);
			}

			activeCount = customThreadPoolScheduler.getActiveCount();
			logger.info("activeCount = " + activeCount + ". Sleeping ...");

		} while (activeCount > 0);

		logger.info("Platform exit!");
		Platform.exit();
	}

	private void createAndShowThrottle(final ApplicationContext context) {

		final ThrottleStage throttleStage = context.getBean(ThrottleStage.class);
		throttleStage.initialize();
		throttleStage.initModality(Modality.WINDOW_MODAL);
		throttleStage.setX(0);
		throttleStage.setY(0);
		throttleStage.show();
	}

	private void createAndShowScene(final Stage stage) throws Exception {

		stage.setScene(sceneFactory.create(stage));

		// https://stackoverflow.com/questions/26619566/javafx-stage-close-handler
		stage.setOnCloseRequest(closeWindowEventHandler);

		stage.setTitle("Easy Train (Beta v0.2)");
		stage.setWidth(WINDOW_WIDTH);
		stage.setHeight(WINDOW_HEIGHT);

		stage.show();
	}

	private void loadModel(final String modelFile, final String locomotivesModelFile) {

		try {
			logger.info("Loading locomotives model ...");
			modelFacade.loadLocomotivesModel(locomotivesModelFile);
			logger.info("Loading locomotives model done.");

			logger.info("Loading model ...");
			modelFacade.loadModel(modelFile);
			modelFacade.connectModel();
			logger.info("Loading model done.");
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private ApplicationContext loadApplicationContext(final Stage stage) {

		// https://stackoverflow.com/questions/31886204/where-is-javaconfigapplicationcontext-class-nowadays
		final ApplicationContext context = new AnnotationConfigApplicationContext(ConfigurationClass.class);

		// facades
		modelFacade = context.getBean(ModelFacade.class);
		protocolFacade = context.getBean(DefaultProtocolFacade.class);

		// services
		routingService = context.getBean(RoutingService.class);
		blockService = context.getBean(BlockService.class);
		turnoutService = context.getBean(TurnoutService.class);

		// threads
		evtSenCommandThread = context.getBean(EvtSenCommandThread.class);
		customThreadPoolScheduler = context.getBean(CustomThreadPoolScheduler.class);

		// UI
		blockNavigationPane = context.getBean(BlockNavigationPane.class);
		drivingThreadControlPane = context.getBean(DrivingThreadControlPane.class);
		locomotiveListStage = context.getBean(LocomotiveListStage.class);
		placeLocomotiveStage = context.getBean(PlaceLocomotiveStage.class);
		emergencyStopPane = context.getBean(EmergencyStopPane.class);
		final GraphNodeSVGMenuItem graphNodeSVGMenuItem = context.getBean(GraphNodeSVGMenuItem.class);
		graphNodeSVGMenuItem.setStage(stage);

		// factories
		sceneFactory = context.getBean(DefaultSceneFactory.class);

		return context;
	}

	private EventHandler<WindowEvent> createCloseWindowEventHandler(final Stage stage) {

		return new EventHandler<WindowEvent>() {

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
	}

}
