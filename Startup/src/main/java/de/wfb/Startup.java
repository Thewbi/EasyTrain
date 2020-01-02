package de.wfb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.wfb.dialogs.LayoutElementSelectionDialogStage;
import de.wfb.javafxtest.controller.LayoutGridController;
import de.wfb.javafxtest.controls.CustomGridPane;
import de.wfb.model.service.ModelService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
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

public class Startup extends Application {

	private static final Logger logger = LogManager.getLogger(Startup.class);

	public static void main(final String[] args) {

		logger.trace("main");
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {

		logger.trace("start");

		// https://stackoverflow.com/questions/31886204/where-is-javaconfigapplicationcontext-class-nowadays
		final ApplicationContext context = new AnnotationConfigApplicationContext(ConfigurationClass.class);
		final ModelService modelService = context.getBean(ModelService.class);

		// load the model
		try {
			logger.info("Loading model ...");
			modelService.loadModel();
			logger.info("Loading model done.");
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}

		stage.setScene(createScene(context));

		stage.setTitle("ScrollPaneDemo");
		stage.setWidth(800);
		stage.setHeight(600);

		stage.show();

		final LayoutElementSelectionDialogStage layoutElementSelectionDialogStage = context
				.getBean(LayoutElementSelectionDialogStage.class);
		layoutElementSelectionDialogStage.initModality(Modality.WINDOW_MODAL);
		layoutElementSelectionDialogStage.show();

		// stop the application after it's last window was closed as opposed to keep a
		// thread without any windows
		// in the background
		Platform.setImplicitExit(true);

		// https://stackoverflow.com/questions/26619566/javafx-stage-close-handler
		stage.setOnCloseRequest(event -> {

//			System.out.println("Startup.setOnCloseRequest()");

			// TODO: check for unsaved changes and prevent the application from closing
			// Tell the user about unsaved changes.
			// Ask him if he wants to quit or not

//			if (unsavedChangesExist) {
//				return;
//			}

			Platform.exit();
		});
	}

	@Override
	public void stop() {
		logger.trace("Startup.stop()");
	}

	private Scene createScene(final ApplicationContext context) {

		logger.trace("createScene");

		final LayoutGridController layoutGridController = context.getBean(LayoutGridController.class);

		final CustomGridPane customGridPane = context.getBean(CustomGridPane.class);
		customGridPane.Initialize();

		// final CustomGridPane customGridPane = new CustomGridPane();
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
		borderPane.setTop(createMenu(layoutGridController));
		borderPane.setCenter(stackPane);

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

				System.out.println("SHIFT pressed");
				layoutGridController.setShiftState(true);
				customGridPane.setShiftState(true);
			}
		});

		scene.setOnKeyReleased(event -> {

			if (event.getCode() == KeyCode.SHIFT) {

				System.out.println("SHIFT released");
				layoutGridController.setShiftState(false);
				customGridPane.setShiftState(false);
			}
		});

		return scene;
	}

	private MenuBar createMenu(final LayoutGridController layoutGridController) {

		// Create MenuBar
		final MenuBar menuBar = new MenuBar();

		// Create menus
		final Menu fileMenu = new Menu("File");
		final Menu editMenu = new Menu("Edit");
		final Menu helpMenu = new Menu("Help");

		// Create MenuItems
		final MenuItem newItem = new MenuItem("New");
		final MenuItem openFileItem = new MenuItem("Open File");
		final MenuItem exitItem = new MenuItem("Exit");

		final MenuItem connectItem = new MenuItem("Connect");
		connectItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				layoutGridController.connect();
			}
		});
		final MenuItem copyItem = new MenuItem("Copy");
		final MenuItem pasteItem = new MenuItem("Paste");

		// Add menuItems to the Menus
		fileMenu.getItems().addAll(newItem, openFileItem, exitItem);
		editMenu.getItems().addAll(connectItem, copyItem, pasteItem);

		// Add Menus to the MenuBar
		menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);

		return menuBar;
	}

}
