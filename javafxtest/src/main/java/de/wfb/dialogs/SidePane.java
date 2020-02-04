package de.wfb.dialogs;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.model.node.Node;
import de.wfb.rail.events.NodeClickedEvent;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

public class SidePane extends GridPane implements ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(SidePane.class);

	@Autowired
	private LayoutElementSelectionPane layoutElementSelectionPane;

	@Autowired
	private TurnoutDetailsPane turnoutDetailsPane;

	@Autowired
	private RailDetailsPane railDetailsPane;

	@Autowired
	private ThrottlePane throttlePane;

	@Autowired
	private RoutingPane routingPane;

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		logger.trace("onApplicationEvent " + event.getClass().getSimpleName());

		if (event instanceof NodeClickedEvent) {

			final NodeClickedEvent nodeClickedEvent = (NodeClickedEvent) event;

			processNodeClickedEvent(nodeClickedEvent);
		}
	}

	private void processNodeClickedEvent(final NodeClickedEvent nodeClickedEvent) {

		final Node node = nodeClickedEvent.getNode();

		turnoutDetailsPane.clear();
		turnoutDetailsPane.setup(node);

		railDetailsPane.clear();
		railDetailsPane.setup(node);
	}

	public void setup() throws FileNotFoundException {

		logger.trace("setup()");

		layoutElementSelectionPane.setup();
		final TitledPane layoutElementTitledPane = new TitledPane();
		GridPane.setColumnIndex(layoutElementTitledPane, 1);
		GridPane.setRowIndex(layoutElementTitledPane, 1);
		layoutElementTitledPane.setText("Layout Element Selection");
		layoutElementTitledPane.setContent(layoutElementSelectionPane);
		getChildren().add(layoutElementTitledPane);

		railDetailsPane.setup(null);
		final TitledPane railDetailsTitledPane = new TitledPane();
		GridPane.setColumnIndex(railDetailsTitledPane, 1);
		GridPane.setRowIndex(railDetailsTitledPane, 2);
		railDetailsTitledPane.setText("Rail Details");
		railDetailsTitledPane.setContent(railDetailsPane);
		getChildren().add(railDetailsTitledPane);

		turnoutDetailsPane.setup(null);
		final TitledPane turnoutDetailsTitledPane = new TitledPane();
		GridPane.setColumnIndex(turnoutDetailsTitledPane, 1);
		GridPane.setRowIndex(turnoutDetailsTitledPane, 3);
		turnoutDetailsTitledPane.setText("Turnout Details");
		turnoutDetailsTitledPane.setContent(turnoutDetailsPane);
		getChildren().add(turnoutDetailsTitledPane);

		throttlePane.setup();
		final TitledPane throttleTitledPane = new TitledPane();
		GridPane.setColumnIndex(throttleTitledPane, 1);
		GridPane.setRowIndex(throttleTitledPane, 4);
		throttleTitledPane.setText("Locomotive Throttle");
		throttleTitledPane.setContent(throttlePane);
		getChildren().add(throttleTitledPane);

		routingPane.setup();
		final TitledPane routingTitledPane = new TitledPane();
		GridPane.setColumnIndex(routingTitledPane, 1);
		GridPane.setRowIndex(routingTitledPane, 5);
		routingTitledPane.setText("Routing");
		routingTitledPane.setContent(routingPane);
		getChildren().add(routingTitledPane);
	}

}
