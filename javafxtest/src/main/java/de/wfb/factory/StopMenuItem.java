package de.wfb.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.service.DrivingService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

public class StopMenuItem extends MenuItem {

	private static final Logger logger = LogManager.getLogger(StopMenuItem.class);

	@Autowired
	private DrivingService drivingService;

	/**
	 * ctor
	 *
	 * @param title
	 */
	public StopMenuItem(final String title) {

		super(title);

		setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("Stop");

				try {
					drivingService.locomotiveStopAll();
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		});
	}

}
