package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

import de.wfb.model.locomotive.Locomotive;

public class LocomotiveSelectedEvent extends ApplicationEvent {

	private final Locomotive locomotive;

	public LocomotiveSelectedEvent(final Object source, final Locomotive locomotive) {
		super(source);

		this.locomotive = locomotive;
	}

	public Locomotive getLocomotive() {
		return locomotive;
	}

}
