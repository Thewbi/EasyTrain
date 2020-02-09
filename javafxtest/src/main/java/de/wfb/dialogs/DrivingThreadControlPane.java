package de.wfb.dialogs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.rail.controller.TimedDrivingThreadController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class DrivingThreadControlPane extends HBox {

	private static final Logger logger = LogManager.getLogger(DrivingThreadControlPane.class);

	@Autowired
	private TimedDrivingThreadController timedDrivingThreadController;

	private final Button togglePauseButton = new Button();

	private final Button singleStepButton = new Button();

	public void clear() {

		getChildren().clear();

	}

	public void setup() {

		togglePauseButton.setText("TogglePause");
		togglePauseButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent actionEvent) {
				timedDrivingThreadController.togglePaused();
			}
		});

		singleStepButton.setText("SingleStep");
		singleStepButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent actionEvent) {
				timedDrivingThreadController.addSingleStep();
			}
		});

		getChildren().addAll(togglePauseButton, singleStepButton);

	}

}
