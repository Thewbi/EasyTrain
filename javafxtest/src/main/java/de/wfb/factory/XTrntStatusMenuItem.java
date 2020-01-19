package de.wfb.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.model.node.Node;
import de.wfb.rail.events.NodeClickedEvent;
import de.wfb.rail.facade.ProtocolFacade;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

public class XTrntStatusMenuItem extends MenuItem implements ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(XTrntStatusMenuItem.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Autowired
	private ProtocolFacade protocolFacade;

	private Node node;

	/**
	 * ctor
	 *
	 * @param title
	 */
	public XTrntStatusMenuItem(final String title) {

		super(title);

		setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("xTrntStatusMenuItem node : " + node);

				if (node != null) {

					final boolean isThrown = protocolFacade.turnoutStatus(node.getProtocolTurnoutId().shortValue());
					logger.info("Result: " + isThrown);
				}
			}
		});
	}

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		logger.trace("onApplicationEvent: " + event);

		if (event instanceof NodeClickedEvent) {

			final NodeClickedEvent nodeClickedEvent = (NodeClickedEvent) event;
			handleNodeClickedEvent(nodeClickedEvent);
		}
	}

	private void handleNodeClickedEvent(final NodeClickedEvent nodeClickedEvent) {
		node = nodeClickedEvent.getNode();
	}

}
