package de.wfb.javafxtest;

import de.wfb.javafxtest.controls.ZoomingPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class ScalingDemo extends Application {

	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {

		stage.setScene(createScene());

		stage.setTitle("ScalingDemo");
		stage.setWidth(800);
		stage.setHeight(600);

		stage.show();
	}

	private Scene createScene() {

		final WebView webView = new WebView();
		webView.getEngine().load("http://www.google.com");

		final Slider slider = new Slider(0.5, 2, 1);

		final ZoomingPane zoomingPane = new ZoomingPane(webView);
		zoomingPane.zoomFactorProperty().bind(slider.valueProperty());

		final BorderPane borderPane = new BorderPane(zoomingPane, null, null, slider, null);

		final Scene scene = new Scene(borderPane);

		return scene;
	}

}
