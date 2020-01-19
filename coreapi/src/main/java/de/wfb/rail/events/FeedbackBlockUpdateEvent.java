package de.wfb.rail.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;

import de.wfb.model.service.FeedbackBlockService;

/**
 * Event sent after receiving the response from a P50XXEvtSenCommand. Causes
 * FeedbackBlockEvents.
 *
 * It describes the state of the contacts of the S88 that signaled a change.
 *
 * The method
 * de.wfb.model.service.DefaultFeedbackBlockService.processFeedbackBlockUpdateEvent(FeedbackBlockUpdateEvent)
 * digests this event. It checks if the new state of the contacts is really a
 * change compared to the current information.
 *
 * For every contact that did really change, a individual FeedbackBlockEvent is
 * sent.
 */
public class FeedbackBlockUpdateEvent extends ApplicationEvent {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(FeedbackBlockUpdateEvent.class);

	private final FeedbackBlockState[] feedbackBlockState = new FeedbackBlockState[FeedbackBlockService.BLOCKSTATE_COUNT];

	/**
	 * ctor
	 *
	 * @param source
	 */
	public FeedbackBlockUpdateEvent(final Object source) {
		super(source);

		for (int i = 0; i < FeedbackBlockService.BLOCKSTATE_COUNT; i++) {
			feedbackBlockState[i] = FeedbackBlockState.UNKNOWN;
		}
	}

	public FeedbackBlockState[] getFeedbackBlockState() {
		return feedbackBlockState;
	}

}
