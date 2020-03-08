package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.rail.service.Route;

public class RouteFinishedEvent extends ApplicationEvent {

	private final Route route;

	private final Locomotive locomotive;

	public RouteFinishedEvent(final Object source, final Route route, final Locomotive locomotive) {
		super(source);
		this.route = route;
		this.locomotive = locomotive;
	}

	public Route getRoute() {
		return route;
	}

	public Locomotive getLocomotive() {
		return locomotive;
	}

}
