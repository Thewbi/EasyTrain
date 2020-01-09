package de.wfb.rail.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;

import de.wfb.model.node.Node;

public class NodeClickedEvent extends ApplicationEvent {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(NodeClickedEvent.class);

	private final Node node;

	public NodeClickedEvent(final Object source, final Node node) {
		super(source);

		this.node = node;
	}

	public Node getNode() {
		return node;
	}

}
