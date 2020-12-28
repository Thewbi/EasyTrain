package de.wfb.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.service.DrivingService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

public class StartMenuItem extends MenuItem {

	private static final Logger logger = LogManager.getLogger(StartMenuItem.class);

	@Autowired
	private DrivingService drivingService;

	/**
	 * ctor
	 *
	 * @param title
	 */
	public StartMenuItem(final String title) {

		super(title);

		setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("Continue");

				try {
					drivingService.locomotiveStartAll();
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		});
	}

}

//stopButton.setText("Stop");
//stopButton.setOnAction(new EventHandler<ActionEvent>() {
//
//	@Override
//	public void handle(final ActionEvent actionEvent) {
//
//		logger.info("Stop");
//		drivingService.locomotiveStopAll();
//
//	}
//});
//
//startButton.setText("Continue");
//startButton.setOnAction(new EventHandler<ActionEvent>() {
//
//	@Override
//	public void handle(final ActionEvent actionEvent) {
//
//		logger.info("Continue");
//		drivingService.locomotiveStartAll();
//
//	}
//});
//
//resetButton.setText("Reset");
//resetButton.setOnAction(new EventHandler<ActionEvent>() {
//
//	@Override
//	public void handle(final ActionEvent actionEvent) {
//
//		logger.info("Reset");
//		drivingService.locomotiveStopAll();
//
//		routingService.removeRoutesAll();
//		routingService.removeHighlightedRoute();
//
//	}
//});