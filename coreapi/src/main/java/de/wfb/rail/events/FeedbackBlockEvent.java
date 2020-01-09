package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

public class FeedbackBlockEvent extends ApplicationEvent {

	private final int feedbackBlockNumber;

	private final FeedbackBlockState feedbackBlockState;

	/**
	 * ctor
	 *
	 * @param source
	 * @param feedbackBlockNumber
	 * @param feedbackBlockState
	 */
	public FeedbackBlockEvent(final Object source, final int feedbackBlockNumber,
			final FeedbackBlockState feedbackBlockState) {

		super(source);
		this.feedbackBlockNumber = feedbackBlockNumber;
		this.feedbackBlockState = feedbackBlockState;
	}

	public int getFeedbackBlockNumber() {
		return feedbackBlockNumber;
	}

	public FeedbackBlockState getFeedbackBlockState() {
		return feedbackBlockState;
	}

}
