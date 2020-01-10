package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

import de.wfb.model.locomotive.DefaultLocomotive;

public class LocomotiveSelectedEvent extends ApplicationEvent {

	private final DefaultLocomotive locomotive;

	public LocomotiveSelectedEvent(final Object source, final DefaultLocomotive locomotive) {
		super(source);

		this.locomotive = locomotive;
	}

	public DefaultLocomotive getLocomotive() {
		return locomotive;
	}

}
