package de.wfb.rail.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;

/**
 * After a P50XXEvtSenCommand command was executed, the system learns about the
 * states of blocks from the response of the command.<br />
 * <br />
 *
 * The state change is send into the system using a FeedbackBlockUpdateEvent.
 * That FeedbackBlockUpdateEvent is handled by ???<br />
 * <br />
 *
 * For every block signaled as free or blocked, the system will publish a
 * separate FeedbackBlockEvent.<br />
 * <br />
 */
public class FeedbackBlockEvent extends ApplicationEvent {

	private static final Logger logger = LogManager.getLogger(FeedbackBlockEvent.class);

	private final int feedbackBlockNumber;

	private final FeedbackBlockState feedbackBlockState;

	/**
	 * ctor<br />
	 * <br />
	 *
	 * FeedbackBlockUpdateEvents is sent after the Intellibox sends the state of a
	 * S88 feedback module.<br />
	 * <br />
	 *
	 * FeedbackBlockUpdateEvents are broken down into individual
	 * FeedbackBlockEvent.<br />
	 * <br />
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

		logger.info("ctor feedbackBlockNumber: " + feedbackBlockNumber + " feedbackBlockState: " + feedbackBlockState);
	}

	public int getFeedbackBlockNumber() {
		return feedbackBlockNumber;
	}

	public FeedbackBlockState getFeedbackBlockState() {
		return feedbackBlockState;
	}

}
