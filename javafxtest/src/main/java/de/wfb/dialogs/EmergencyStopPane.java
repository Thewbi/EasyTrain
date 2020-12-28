package de.wfb.dialogs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.service.DrivingService;
import de.wfb.model.service.RoutingService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class EmergencyStopPane extends HBox {

	private static final Logger logger = LogManager.getLogger(EmergencyStopPane.class);

	private final Button stopButton = new Button();
	
	private final Button startButton = new Button();

	private final Button resetButton = new Button();

	@Autowired
	private DrivingService drivingService;

	@Autowired
	private RoutingService routingService;

	public void setup() {

		stopButton.setText("Stop");
		stopButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent actionEvent) {

				logger.info("Stop");
				drivingService.locomotiveStopAll();

			}
		});
		
		startButton.setText("Continue");
		startButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent actionEvent) {

				logger.info("Continue");
				drivingService.locomotiveStartAll();

			}
		});

		resetButton.setText("Reset");
		resetButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent actionEvent) {

				logger.info("Reset");
				drivingService.locomotiveStopAll();

				routingService.removeRoutesAll();
				routingService.removeHighlightedRoute();

			}
		});

		setSpacing(5);
		setPadding(new Insets(10, 10, 10, 10));

		getChildren().addAll(resetButton, startButton, stopButton);
	}

}
