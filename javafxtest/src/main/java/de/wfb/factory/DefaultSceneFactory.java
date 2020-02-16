package de.wfb.factory;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.dialogs.BlockNavigationPane;
import de.wfb.dialogs.DrivingThreadControlPane;
import de.wfb.dialogs.SidePane;
import de.wfb.javafxtest.controller.LayoutGridController;
import de.wfb.javafxtest.controls.CustomGridPane;
import de.wfb.rail.factory.Factory;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DefaultSceneFactory implements Factory<Scene> {

	private static final Logger logger = LogManager.getLogger(DefaultSceneFactory.class);

	@Autowired
	private CustomGridPane customGridPane;

	@Autowired
	private Factory<MenuBar> menuBarFactory;

	@Autowired
	private LayoutGridController layoutGridController;

	@Autowired
	private BlockNavigationPane blockNavigationPane;

	@Autowired
	private DrivingThreadControlPane drivingThreadControlPane;

	@Autowired
	private SidePane sidePane;

	@Override
	public Scene create(final Object... args) throws Exception {

		final Stage stage = (Stage) args[0];

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

		final MenuBar menuBar = menuBarFactory.create(stage, layoutGridController);

		final BorderPane borderPane = new BorderPane();
		borderPane.setTop(menuBar);
		borderPane.setCenter(stackPane);
		borderPane.setRight(createDetailsView());
		final VBox bottomVBox = new VBox();
		bottomVBox.getChildren().addAll(blockNavigationPane, drivingThreadControlPane);
		borderPane.setBottom(bottomVBox);

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

		final ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(stackPane);

		return scrollPane;
	}

}
