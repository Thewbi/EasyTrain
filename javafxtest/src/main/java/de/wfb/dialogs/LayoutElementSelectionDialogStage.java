package de.wfb.dialogs;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.Scene;
import javafx.stage.Stage;

//@Component
public class LayoutElementSelectionDialogStage extends Stage {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(LayoutElementSelectionDialogStage.class);

	/**
	 * ctor
	 */
	public LayoutElementSelectionDialogStage() {

		setTitle("Layout Element Selection");

		this.setScene(createContentGrid());
	}

	private Scene createContentGrid() {

		try {
			final SidePane sidePane = new SidePane();
			sidePane.setup();

			return new Scene(sidePane);

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

}
