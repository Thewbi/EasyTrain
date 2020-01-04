package de.wfb.dialogs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class ThrottleStage extends Stage {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ThrottleStage.class);

	@Autowired
	private ThrottlePane throttlePane;

	/**
	 * ctor
	 */
	public ThrottleStage() {

		setTitle("Locomotive Throttle");

		// setScene(createContentGrid());
	}

	private Scene createContentGrid() {

		throttlePane.setup();

		return new Scene(throttlePane);
	}

	public void initialize() {
		setScene(createContentGrid());
	}

}
