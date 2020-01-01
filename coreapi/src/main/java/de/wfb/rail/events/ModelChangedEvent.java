package de.wfb.rail.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;

import de.wfb.model.Model;

public class ModelChangedEvent extends ApplicationEvent {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ModelChangedEvent.class);

	private final int x;

	private final int y;

	private final Model model;

	/**
	 * ctor
	 *
	 * @param source
	 * @param model
	 * @param x
	 * @param y
	 */
	public ModelChangedEvent(final Object source, final Model model, final int x, final int y) {

		super(source);
		this.model = model;
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Model getModel() {
		return model;
	}
}
