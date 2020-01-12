package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

/**
 * After a XEvtSen command was executed, system learns about the states of
 * blocks from the response of the command.
 *
 * For every block signaled as free or blocked, the system will publish a
 * seperate FeedbackBlockEvent.
 */
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
