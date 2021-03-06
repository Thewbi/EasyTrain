package de.wfb.javafxtest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.javafxtest.controls.CustomGridPane;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ScrollPaneDemo extends Application {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ScrollPaneDemo.class);

	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {

		stage.setScene(createScene());

		stage.setTitle("ScrollPaneDemo");
		stage.setWidth(800);
		stage.setHeight(600);

		stage.show();
	}

	private Scene createScene() {

		final CustomGridPane customGridPane = new CustomGridPane();
		customGridPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

		final Group group = new Group(customGridPane);

		final ScrollPane scrollPane = new ScrollPane(group);
		scrollPane.setStyle("-fx-background: rgb(150, 150, 150);");
		scrollPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
			}
		});

		final StackPane stackPane = new StackPane();
		stackPane.getChildren().addAll(scrollPane);
		stackPane.setBackground(new Background(new BackgroundFill(Color.DARKGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
		stackPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
			}
		});

		final Scene scene = new Scene(stackPane);

		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {

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

		return scene;
	}

}
