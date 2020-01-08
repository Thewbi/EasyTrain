package de.wfb.rail.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;

import de.wfb.model.Model;
import de.wfb.model.node.Node;

public class NodeHighlightedEvent extends ApplicationEvent {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(NodeHighlightedEvent.class);

	private final int x;

	private final int y;

	private final Model model;

	private final Node node;

	private final boolean highlighted;

	public NodeHighlightedEvent(final Object source, final Model model, final Node node, final int x, final int y,
			final boolean highlighted) {

		super(source);
		this.model = model;
		this.node = node;
		this.highlighted = highlighted;
		this.x = x;
		this.y = y;
	}

	public Model getModel() {
		return model;
	}

	public Node getNode() {
		return node;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
