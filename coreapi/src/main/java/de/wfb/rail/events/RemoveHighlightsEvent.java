package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

public class RemoveHighlightsEvent extends ApplicationEvent {

	public RemoveHighlightsEvent(final Object source) {
		super(source);
	}

}
