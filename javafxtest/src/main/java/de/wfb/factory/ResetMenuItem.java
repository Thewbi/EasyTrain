package de.wfb.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.service.DrivingService;
import de.wfb.model.service.RoutingService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

public class ResetMenuItem extends MenuItem {

	private static final Logger logger = LogManager.getLogger(ResetMenuItem.class);

	@Autowired
	private DrivingService drivingService;

	@Autowired
	private RoutingService routingService;

	/**
	 * ctor
	 *
	 * @param title
	 */
	public ResetMenuItem(final String title) {

		super(title);

		setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("Reset");

				try {

					drivingService.locomotiveStopAll();

					routingService.removeRoutesAll();
					routingService.removeHighlightedRoute();
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		});
	}
}
