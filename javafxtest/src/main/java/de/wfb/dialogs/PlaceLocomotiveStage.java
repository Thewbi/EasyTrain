package de.wfb.dialogs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.model.node.Node;
import de.wfb.rail.events.NodeClickedEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class PlaceLocomotiveStage extends Stage implements ApplicationListener<ApplicationEvent> {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PlaceLocomotiveStage.class);

	@Autowired
	private LocomotiveListPane locomotiveListPane;

	@Autowired
	private PlaceLocomotivePane placeLocomotivePane;

	private Node node;

	/**
	 * ctor
	 */
	public PlaceLocomotiveStage() {
		setTitle("Place Locomotive");
	}

	public void initialize() {
		setScene(createContentGrid(this));
	}

	private Scene createContentGrid(final Stage stage) {

		locomotiveListPane.clear();
		locomotiveListPane.setup(stage, false);

		placeLocomotivePane.clear();
		placeLocomotivePane.setup();

		final BorderPane borderPane = new BorderPane();
		borderPane.setCenter(locomotiveListPane);
		borderPane.setBottom(placeLocomotivePane);

		return new Scene(borderPane);
	}

	public void synchronizeModel() {
		locomotiveListPane.synchronizeModel();
	}

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		if (event instanceof NodeClickedEvent) {

			final NodeClickedEvent nodeClickedEvent = (NodeClickedEvent) event;
			node = nodeClickedEvent.getNode();
		}
	}

	public Node getNode() {
		return node;
	}

}
