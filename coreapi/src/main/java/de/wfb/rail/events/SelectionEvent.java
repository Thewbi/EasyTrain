package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

public class SelectionEvent extends ApplicationEvent {

	private final String message;

	private final int x;

	private final int y;

	public SelectionEvent(final Object source, final String message, final int x, final int y) {
		super(source);
		this.message = message;
		this.x = x;
		this.y = y;
	}

	public String getMessage() {
		return message;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
