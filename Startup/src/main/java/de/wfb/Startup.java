package de.wfb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.wfb.dialogs.BlockNavigationPane;
import de.wfb.dialogs.LocomotiveListStage;
import de.wfb.dialogs.PlaceLocomotiveStage;
import de.wfb.dialogs.ThrottleStage;
import de.wfb.factory.DefaultSceneFactory;
import de.wfb.model.facade.ModelFacade;
import de.wfb.model.service.DefaultRoutingService;
import de.wfb.model.service.RoutingService;
import de.wfb.rail.facade.DefaultProtocolFacade;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.service.BlockService;
import de.wfb.rail.service.TurnoutService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

	private ModelFacade modelFacade;

	private ProtocolFacade protocolFacade;

	private EvtSenCommandThread evtSenCommandThread;

	private RoutingService routingService;

	private LocomotiveListStage locomotiveListStage;

	private PlaceLocomotiveStage placeLocomotiveStage;

	private BlockService blockService;

	private TurnoutService turnoutService;

	private BlockNavigationPane blockNavigationPane;

	private CustomThreadPoolScheduler customThreadPoolScheduler;

	private Factory<Scene> sceneFactory;

	public static void main(final String[] args) {

		logger.trace("main");
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {

		logger.trace("start");

		closeWindowEventHandler = createCloseWindowEventHandler(stage);

		final ApplicationContext context = loadApplicationContext();

		// load the model
		loadModel();

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

		// connect to the intellibox
		connectToIntellibox();

		// query the turnouts to find out their state on the layout so that the software
		// can draw them in the correct state and also send the correct commands when
		// the user of the route service want to switch them
//		turnoutService.startQueryingFromQueue();

		// locomotive throttle
		createAndShowThrottle(context);

		// stop the application after it's last window was closed as opposed to keep a
		// thread without any windows
		// in the background
		Platform.setImplicitExit(true);

		createAndShowScene(stage);
	}

	private void createAndShowScene(final Stage stage) throws Exception {

		stage.setScene(sceneFactory.create(stage, closeWindowEventHandler));

		// https://stackoverflow.com/questions/26619566/javafx-stage-close-handler
		stage.setOnCloseRequest(closeWindowEventHandler);

		stage.setTitle("Easy Train (Beta v0.1)");
		stage.setWidth(1400);
		stage.setHeight(1000);

		stage.show();
	}

	private void createAndShowThrottle(final ApplicationContext context) {

		final ThrottleStage throttleStage = context.getBean(ThrottleStage.class);
		throttleStage.initialize();
		throttleStage.initModality(Modality.WINDOW_MODAL);
		throttleStage.setX(0);
		throttleStage.setY(0);
		throttleStage.show();
	}

	private void connectToIntellibox() {

		try {
			protocolFacade.connect();
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void loadModel() {

		try {
			logger.info("Loading model ...");
			modelFacade.loadModel();
			modelFacade.connectModel();
			logger.info("Loading model done.");
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private ApplicationContext loadApplicationContext() {

		// https://stackoverflow.com/questions/31886204/where-is-javaconfigapplicationcontext-class-nowadays
		final ApplicationContext context = new AnnotationConfigApplicationContext(ConfigurationClass.class);

		// facades
		modelFacade = context.getBean(ModelFacade.class);
		protocolFacade = context.getBean(DefaultProtocolFacade.class);

		// services
		routingService = context.getBean(DefaultRoutingService.class);
		blockService = context.getBean(BlockService.class);
		turnoutService = context.getBean(TurnoutService.class);

		// threads
		evtSenCommandThread = context.getBean(EvtSenCommandThread.class);
		customThreadPoolScheduler = context.getBean(CustomThreadPoolScheduler.class);

		// UI
		blockNavigationPane = context.getBean(BlockNavigationPane.class);
		locomotiveListStage = context.getBean(LocomotiveListStage.class);
		placeLocomotiveStage = context.getBean(PlaceLocomotiveStage.class);

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

	@Override
	public void stop() {

		customThreadPoolScheduler.stop();
		customThreadPoolScheduler.shutdown();

		logger.trace("Startup.stop()");
		Platform.exit();
	}

}
