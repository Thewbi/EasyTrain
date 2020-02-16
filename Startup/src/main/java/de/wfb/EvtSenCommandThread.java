package de.wfb;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import de.wfb.rail.events.FeedbackBlockUpdateEvent;
import de.wfb.rail.facade.ProtocolFacade;

public class EvtSenCommandThread {

	private static final boolean ACTIVE = true;
//	private static final boolean ACTIVE = false;

	private static final Logger logger = LogManager.getLogger(EvtSenCommandThread.class);

	private boolean running = true;

	@Autowired
	private ProtocolFacade protocolFacade;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Scheduled(fixedRate = 1000)
	public void threadFunc() {

		logger.trace("EvtSenCommandThread - threadFunc() ACTIVE = " + ACTIVE);

		if (!ACTIVE) {

			logger.trace("threadFunc() is deactivated!");
			return;
		}

		// send the P50XXEventCommand
		logger.trace("EventThread ...");
		if (protocolFacade.event()) {

			logger.trace("Event Sense command ...");

			// final P50XXEvtSenCommand eventSenseCommand = eventSenseCommand(inputStream,
			// outputStream);
			final List<FeedbackBlockUpdateEvent> eventSenseCommand = protocolFacade.eventSenseCommand();

			if (!CollectionUtils.isEmpty(eventSenseCommand)) {

				for (final FeedbackBlockUpdateEvent feedbackBlockUpdateEvent : eventSenseCommand) {

					applicationEventPublisher.publishEvent(feedbackBlockUpdateEvent);
				}
			}

			logger.trace("Event Sense command done.");
		}
		logger.trace("EventThread done.");
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(final boolean running) {
		this.running = running;
	}

	public void stop() {
		running = false;
	}

}
