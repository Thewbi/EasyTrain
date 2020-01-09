package de.wfb.model.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;

import de.wfb.rail.events.FeedbackBlockEvent;
import de.wfb.rail.events.FeedbackBlockState;
import de.wfb.rail.events.FeedbackBlockUpdateEvent;

/**
 * In Startup.java, a thread is started. That thread constantly queries the
 * intellibox for block updates. Once it parses block status updates from the
 * response it will send a FeedbackBlockUpdateEvent with all updates blocks.
 *
 * This services handles the event.
 *
 * It will remember the state of the blocks. If the block did indeed change
 * their state, this service will send out FeedbackBlockEvents (!=
 * FeedbackBlockUpdateEvent).
 *
 * The layout will handle FeedbackBlockEvents and redraw blocks.
 */
public class DefaultFeedbackBlockService implements FeedbackBlockService, ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(DefaultFeedbackBlockService.class);

	private final FeedbackBlockState[] feedbackBlockState = new FeedbackBlockState[FeedbackBlockService.BLOCKSTATE_COUNT];

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 * ctor
	 */
	public DefaultFeedbackBlockService() {

		for (int i = 0; i < FeedbackBlockService.BLOCKSTATE_COUNT; i++) {
			feedbackBlockState[i] = FeedbackBlockState.UNKNOWN;
		}
	}

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		if (event instanceof FeedbackBlockUpdateEvent) {

			final FeedbackBlockUpdateEvent feedbackBlockUpdateEvent = (FeedbackBlockUpdateEvent) event;
			processFeedbackBlockUpdateEvent(feedbackBlockUpdateEvent);
		}

	}

	private void processFeedbackBlockUpdateEvent(final FeedbackBlockUpdateEvent feedbackBlockUpdateEvent) {

		logger.info("processFeedbackBlockUpdateEvent()");

		for (int i = 0; i < FeedbackBlockService.BLOCKSTATE_COUNT; i++) {

			// if the new state is not unknown and the state changed, remember the change
			// and send an event
			if (feedbackBlockState[i] != feedbackBlockUpdateEvent.getFeedbackBlockState()[i]
					&& feedbackBlockUpdateEvent.getFeedbackBlockState()[i] != FeedbackBlockState.UNKNOWN) {

				feedbackBlockState[i] = feedbackBlockUpdateEvent.getFeedbackBlockState()[i];

				sendFeedbackBlockEvent(i, feedbackBlockState[i]);
			}
		}
	}

	private void sendFeedbackBlockEvent(final int feedbackBlockNumber, final FeedbackBlockState newFeedbackBlockState) {

		final FeedbackBlockEvent feedbackBlockEvent = new FeedbackBlockEvent(this, feedbackBlockNumber,
				newFeedbackBlockState);

		applicationEventPublisher.publishEvent(feedbackBlockEvent);
	}

}
