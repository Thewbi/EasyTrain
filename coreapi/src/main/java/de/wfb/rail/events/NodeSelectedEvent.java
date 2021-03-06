package de.wfb.rail.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;

import de.wfb.model.Model;
import de.wfb.model.node.Node;

public class NodeSelectedEvent extends ApplicationEvent {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(NodeSelectedEvent.class);

	private final Model model;

	private final Node node;

	public NodeSelectedEvent(final Object source, final Model model, final Node node) {

		super(source);
		this.model = model;
		this.node = node;
	}

	public Model getModel() {
		return model;
	}

	public Node getNode() {
		return node;
	}

}
