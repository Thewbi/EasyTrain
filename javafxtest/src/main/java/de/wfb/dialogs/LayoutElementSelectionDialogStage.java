package de.wfb.dialogs;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class LayoutElementSelectionDialogStage extends Stage {

	private static final Logger logger = LogManager.getLogger(LayoutElementSelectionDialogStage.class);

	/**
	 * ctor
	 */
	public LayoutElementSelectionDialogStage() {

		setTitle("Layout Element Selection");

		setScene(createContentGrid());
	}

	private Scene createContentGrid() {

		try {
			final SidePane sidePane = new SidePane();
			sidePane.setup();

			return new Scene(sidePane);

		} catch (final FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

}
