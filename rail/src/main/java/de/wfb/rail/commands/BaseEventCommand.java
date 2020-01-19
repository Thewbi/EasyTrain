package de.wfb.rail.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.rail.events.FeedbackBlockState;
import de.wfb.rail.events.FeedbackBlockUpdateEvent;

public abstract class BaseEventCommand implements Command {

	private static Logger logger = LogManager.getLogger(BaseEventCommand.class);

	@Autowired
	protected ApplicationEventPublisher applicationEventPublisher;

	protected int index = 0;

	protected int s88ID;

	protected byte inputDescriptor1;

	protected byte inputDescriptor2;

	/**
	 * Outputs all contacts that are signaled in use by this state change event!
	 *
	 * @param s88id
	 * @param inputDescriptor1
	 * @param inputDescriptor2
	 */
	protected void sendFeedbackBlockUpdateEvents(final int s88id, final byte inputDescriptor1,
			final byte inputDescriptor2) {

		logger.info("retrieveConnectionIds()");

		final FeedbackBlockUpdateEvent feedbackBlockUpdateEvent = new FeedbackBlockUpdateEvent(this);

		final int base = 16 * (s88id - 1);

		int currentOffset = 1;

		// inputDescriptor1 (byte 1) ausgaenge 1 - 8 am modul (laut
		// https://tams-online.de/WebRoot/Store11/Shops/642f1858-c39b-4b7d-af86-f6a1feaca0e4/MediaGallery/15_Download/Sonstiges/interface.txt)
		for (byte i = 7; i >= 0; i--) {

			final int value = ((inputDescriptor1 >> i) & 1);
			final int offset = base + currentOffset;

			logger.info("Contact " + offset + (value == 0 ? ") UNUSED" : ") USED"));

			feedbackBlockUpdateEvent.getFeedbackBlockState()[offset - 1] = value == 0 ? FeedbackBlockState.FREE
					: FeedbackBlockState.BLOCKED;

			currentOffset++;
		}

		// inputDescriptor2 (byte 2) ausgaenge 9 - 16 am modul (laut
		// https://tams-online.de/WebRoot/Store11/Shops/642f1858-c39b-4b7d-af86-f6a1feaca0e4/MediaGallery/15_Download/Sonstiges/interface.txt)
		for (byte i = 7; i >= 0; i--) {

			final int value = ((inputDescriptor2 >> i) & 1);
			final int offset = base + currentOffset;

			logger.info("Contact " + offset + (value == 0 ? ") UNUSED" : ") USED"));

			feedbackBlockUpdateEvent.getFeedbackBlockState()[offset - 1] = value == 0 ? FeedbackBlockState.FREE
					: FeedbackBlockState.BLOCKED;

			currentOffset++;
		}

		// send event
		logger.trace("applicationEventPublisher: " + applicationEventPublisher);
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(feedbackBlockUpdateEvent);
		}
	}

	public ApplicationEventPublisher getApplicationEventPublisher() {
		return applicationEventPublisher;
	}

	public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
}
