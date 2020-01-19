package de.web.facade;

import org.springframework.context.ApplicationEvent;

public interface DebugFacade {

	void publishEvent(ApplicationEvent event);

}
