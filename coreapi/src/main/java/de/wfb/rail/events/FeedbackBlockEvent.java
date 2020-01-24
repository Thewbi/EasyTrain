package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

/**
 * After a P50XXEvtSenCommand command was executed, the system learns about the
 * states of blocks from the response of the command.
 *
 * The state change is send into the system using a FeedbackBlockUpdateEvent.
 * That FeedbackBlockUpdateEvent is handled by
 *
 * For every block signaled as free or blocked, the system will publish a
 * separate FeedbackBlockEvent.
 */
public class FeedbackBlockEvent extends ApplicationEvent {

	private final int feedbackBlockNumber;

	private final FeedbackBlockState feedbackBlockState;

	/**
	 * ctor
	 *
	 * FeedbackBlockUpdateEvents is sent after the Intellibox sends the state of a
	 * S88 feedback module.
	 *
	 * FeedbackBlockUpdateEvents are broken down into individual FeedbackBlockEvent.
	 *
	 * @param source
	 * @param feedbackBlockNumber the address of one of the s88 feedback modules.
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
