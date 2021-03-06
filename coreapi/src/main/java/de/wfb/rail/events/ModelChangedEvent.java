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

	private final boolean selected;

	private final boolean highlighted;

	private final boolean blocked;

	private final boolean reserved;

	private final boolean containsLocomotive;

	/**
	 * ctor
	 *
	 * @param source
	 * @param model
	 * @param x
	 * @param y
	 * @param highlighted
	 */
	public ModelChangedEvent(final Object source, final Model model, final int x, final int y,
			final boolean highlighted, final boolean blocked, final boolean selected, final boolean reserved,
			final boolean containsLocomotive) {

		super(source);
		this.model = model;
		this.x = x;
		this.y = y;
		this.selected = selected;
		this.highlighted = highlighted;
		this.blocked = blocked;
		this.reserved = reserved;
		this.containsLocomotive = containsLocomotive;
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

	public boolean isHighlighted() {
		return highlighted;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public boolean isSelected() {
		return selected;
	}

	public boolean isReserved() {
		return reserved;
	}

	public boolean isContainsLocomotive() {
		return containsLocomotive;
	}
}
