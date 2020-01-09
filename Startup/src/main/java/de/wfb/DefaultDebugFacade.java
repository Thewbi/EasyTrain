package de.wfb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

public class DefaultDebugFacade {

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void publishEvent(final ApplicationEvent event) {
		applicationEventPublisher.publishEvent(event);
	}

}
