package de.wfb.rail.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;

import de.wfb.model.service.FeedbackBlockService;

public class FeedbackBlockUpdateEvent extends ApplicationEvent {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(FeedbackBlockUpdateEvent.class);

	private final FeedbackBlockState[] feedbackBlockState = new FeedbackBlockState[FeedbackBlockService.BLOCKSTATE_COUNT];

	/**
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
