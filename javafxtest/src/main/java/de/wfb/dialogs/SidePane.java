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
	private SignalDetailsPane signalDetailsPane;

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

		signalDetailsPane.clear();
		signalDetailsPane.setup(node);

		railDetailsPane.clear();
		railDetailsPane.setup(node);
	}

	public void setup() throws FileNotFoundException {

		logger.trace("setup()");

		int rowIndex = 0;

		rowIndex++;
		layoutElementSelectionPane.setup();
		final TitledPane layoutElementTitledPane = new TitledPane();
		GridPane.setColumnIndex(layoutElementTitledPane, 1);
		GridPane.setRowIndex(layoutElementTitledPane, rowIndex);
		layoutElementTitledPane.setText("Layout Element Selection");
		layoutElementTitledPane.setContent(layoutElementSelectionPane);
		getChildren().add(layoutElementTitledPane);

		rowIndex++;
		railDetailsPane.setup(null);
		final TitledPane railDetailsTitledPane = new TitledPane();
		GridPane.setColumnIndex(railDetailsTitledPane, 1);
		GridPane.setRowIndex(railDetailsTitledPane, rowIndex);
		railDetailsTitledPane.setText("Rail Details");
		railDetailsTitledPane.setContent(railDetailsPane);
		getChildren().add(railDetailsTitledPane);

		rowIndex++;
		turnoutDetailsPane.setup(null);
		final TitledPane turnoutDetailsTitledPane = new TitledPane();
		GridPane.setColumnIndex(turnoutDetailsTitledPane, 1);
		GridPane.setRowIndex(turnoutDetailsTitledPane, rowIndex);
		turnoutDetailsTitledPane.setText("Turnout Details");
		turnoutDetailsTitledPane.setContent(turnoutDetailsPane);
		getChildren().add(turnoutDetailsTitledPane);

		rowIndex++;
		signalDetailsPane.setup(null);
		final TitledPane signalDetailsTitledPane = new TitledPane();
		GridPane.setColumnIndex(signalDetailsTitledPane, 1);
		GridPane.setRowIndex(signalDetailsTitledPane, rowIndex);
		signalDetailsTitledPane.setText("Signal Details");
		signalDetailsTitledPane.setContent(signalDetailsPane);
		getChildren().add(signalDetailsTitledPane);

		rowIndex++;
		throttlePane.setup();
		final TitledPane throttleTitledPane = new TitledPane();
		GridPane.setColumnIndex(throttleTitledPane, 1);
		GridPane.setRowIndex(throttleTitledPane, rowIndex);
		throttleTitledPane.setText("Locomotive Throttle");
		throttleTitledPane.setContent(throttlePane);
		getChildren().add(throttleTitledPane);

		rowIndex++;
		routingPane.setup();
		final TitledPane routingTitledPane = new TitledPane();
		GridPane.setColumnIndex(routingTitledPane, 1);
		GridPane.setRowIndex(routingTitledPane, rowIndex);
		routingTitledPane.setText("Routing");
		routingTitledPane.setContent(routingPane);
		getChildren().add(routingTitledPane);
	}

}
