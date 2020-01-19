package de.wfb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import de.web.facade.DebugFacade;

public class DefaultDebugFacade implements DebugFacade {

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void publishEvent(final ApplicationEvent event) {
		applicationEventPublisher.publishEvent(event);
	}

}
