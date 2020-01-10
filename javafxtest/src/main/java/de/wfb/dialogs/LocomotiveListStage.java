package de.wfb.dialogs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LocomotiveListStage extends Stage {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(LocomotiveListStage.class);

	@Autowired
	private LocomotiveListPane locomotiveListPane;

	@Autowired
	private LocomotiveAddPane locomotiveAddPane;

	/**
	 * ctor
	 */
	public LocomotiveListStage() {
		setTitle("Locomotive List");
	}

	public void initialize() {
		setScene(createContentGrid());
	}

	private Scene createContentGrid() {

		locomotiveListPane.clear();
		locomotiveListPane.setup();

		final BorderPane borderPane = new BorderPane();
		borderPane.setCenter(locomotiveListPane);
		borderPane.setBottom(locomotiveAddPane);

		return new Scene(borderPane);
	}

	public void synchronizeModel() {
		locomotiveListPane.synchronizeModel();
	}

}
