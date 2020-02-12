package de.wfb.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.controller.TimedDrivingThreadController;

/**
 * Is used when the user uses the debug buttons that control the
 * TimedDrivingThread such as start and pause and the single step button.
 */
public class DefaultTimedDrivingThreadController implements TimedDrivingThreadController {

	private static final Logger logger = LogManager.getLogger(DefaultTimedDrivingThreadController.class);

	private int singleStepCount;
//	private int singleStepCount = 290;

	private boolean paused = true;

	@Override
	public int decrementSingleStep() {

		if (singleStepCount == 0) {

			logger.trace("singleStepCount is " + singleStepCount);
			return 0;
		}

		final int oldValue = singleStepCount;
		singleStepCount--;

		logger.trace("singleStepCount is " + singleStepCount);

		return oldValue;
	}

	@Override
	public boolean isPaused() {
		return paused;
	}

	@Override
	public void addSingleStep() {
		singleStepCount++;

		logger.trace("singleStepCount is " + singleStepCount);
	}

	@Override
	public void togglePaused() {
		paused = !paused;

		logger.trace("pause is " + paused);
	}

}
