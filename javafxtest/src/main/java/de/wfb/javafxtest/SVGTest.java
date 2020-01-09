package de.wfb.javafxtest;

import java.util.function.IntFunction;

import de.wfb.javafxtest.controls.ZoomingPane;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

/**
 * Install Java 13
 *
 * <!-- optional Download a JavaFX runtime: https://gluonhq.com/products/javafx/
 * Runtime used: openjfx-11.0.2_osx-x64_bin-sdk.zip Unzip the archive to
 * /Users/bischowg/Downloads/javafx-sdk-11.0.2 -->
 *
 * https://github.com/javafx-maven-plugin/javafx-maven-plugin
 *
 * https://github.com/openjfx/javafx-maven-plugin
 *
 *
 * cd /Users/bischowg/Documents/workspace_javafx/javafxtest mvn clean javafx:run
 */
public class SVGTest extends Application {

	private static final int SIZE = 16;

	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage stage) {

		stage.setScene(createScene());

		stage.setTitle("SVGIcons");
		stage.setWidth(800);
		stage.setHeight(600);

		stage.show();
	}

	private Scene createScene() {

		final VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.setPadding(new Insets(10));

		vbox.getChildren().add(createRow(this::lines));
		vbox.getChildren().add(createRow(this::curve));
		vbox.getChildren().add(createRow(this::arc));

		final Slider slider = new Slider(0.5, 2, 1);

		final ZoomingPane zoomingPane = new ZoomingPane(vbox);
		zoomingPane.zoomFactorProperty().bind(slider.valueProperty());

		final BorderPane borderPane = new BorderPane(zoomingPane, null, null, slider, null);

		final Scene scene = new Scene(borderPane);

		return scene;
	}

	private HBox createRow(final IntFunction<SVGPath> path) {

		final HBox row = new HBox(10);
		row.setAlignment(Pos.CENTER);

		// draw four shapes in increasing size from left to right
		for (int i = 2; i < 6; i++) {
			row.getChildren().add(path.apply(i * SIZE));
		}

		return row;
	}

	private SVGPath lines(final int size) {

		final EventHandler<MouseEvent> mouseClickEventHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
			}
		};

		final SVGPath path = new SVGPath();
		path.setFill(Color.ALICEBLUE);
		path.setStroke(Color.BLUE);
		path.setContent(
				"M0," + size + "L" + size / 2 + ",0 " + size + "," + size + " " + size / 2 + "," + 2 * size / 3 + "z");

		path.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickEventHandler);

		return path;
	}

	private SVGPath curve(final int size) {

		final SVGPath path = new SVGPath();
		path.setFill(Color.HONEYDEW);
		path.setStroke(Color.GREEN);
		path.setContent("M0,0Q" + size + ",0," + size + "," + size + "L0," + size + "z");

		return path;
	}

	private SVGPath arc(final int size) {

		final SVGPath path = new SVGPath();
		path.setFill(Color.MISTYROSE);
		path.setStroke(Color.RED);
		path.setContent("M0,0A" + size / 2 + "," + size + ",0,1,0," + size + ",0z");

		return path;
	}

}