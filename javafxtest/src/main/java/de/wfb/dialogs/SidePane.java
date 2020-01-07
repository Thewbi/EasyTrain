package de.wfb.dialogs;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.model.node.Node;
import de.wfb.rail.events.NodeSelectedEvent;
import javafx.scene.layout.GridPane;

public class SidePane extends GridPane implements ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(SidePane.class);

	@Autowired
	private LayoutElementSelectionPane layoutElementSelectionPane;

	@Autowired
	private TurnoutDetailsPane turnoutDetailsPane;

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		logger.trace("onApplicationEvent " + event.getClass().getSimpleName());

		if (event instanceof NodeSelectedEvent) {

			final NodeSelectedEvent nodeSelectedEvent = (NodeSelectedEvent) event;

			final Node node = nodeSelectedEvent.getNode();
			turnoutDetailsPane.clear();
			turnoutDetailsPane.setup(node);
		}
	}

	public void setup() throws FileNotFoundException {

		logger.trace("setup()");

		layoutElementSelectionPane.setup();
		turnoutDetailsPane.setup(null);

		GridPane.setColumnIndex(layoutElementSelectionPane, 1);
		GridPane.setRowIndex(layoutElementSelectionPane, 1);
		getChildren().add(layoutElementSelectionPane);

		GridPane.setColumnIndex(turnoutDetailsPane, 1);
		GridPane.setRowIndex(turnoutDetailsPane, 2);
		getChildren().add(turnoutDetailsPane);
	}

}
