package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.rail.service.Route;

public class RouteFinishedEvent extends ApplicationEvent {

	private final Route route;

	private final DefaultLocomotive defaultLocomotive;

	public RouteFinishedEvent(final Object source, final Route route, final DefaultLocomotive defaultLocomotive) {
		super(source);
		this.route = route;
		this.defaultLocomotive = defaultLocomotive;
	}

	public Route getRoute() {
		return route;
	}

	public DefaultLocomotive getDefaultLocomotive() {
		return defaultLocomotive;
	}

}