package de.wfb.factory;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.service.RoutingController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

public class RoutingControllerMenuItem extends MenuItem {

	private static final Logger logger = LogManager.getLogger(RoutingControllerMenuItem.class);

	@Autowired
	private RoutingController routingController;

	/**
	 * ctor
	 *
	 * @param title
	 */
	public RoutingControllerMenuItem(final String title) {

		super(title);

		setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("RoutingControllerMenuItem");

				try {
					routingController.initialize();
					routingController.start();
				} catch (final IOException e) {
					logger.error(e.getMessage(), e);
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		});
	}

}
