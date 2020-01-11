package de.wfb;

import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

public class CustomThreadPoolScheduler extends ThreadPoolTaskScheduler {

	private static final Logger logger = LogManager.getLogger(CustomThreadPoolScheduler.class);

	private final Map<Object, ScheduledFuture<?>> scheduledTasks = new IdentityHashMap<>();

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(final Runnable task, final long period) {

		logger.info("scheduleAtFixedRate()");

		final ScheduledFuture<?> future = super.scheduleAtFixedRate(task, period);

		final ScheduledMethodRunnable runnable = (ScheduledMethodRunnable) task;
		scheduledTasks.put(runnable.getTarget(), future);

		return future;
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(final Runnable task, final Date startTime, final long period) {

		logger.info("scheduleAtFixedRate()");

		final ScheduledFuture<?> future = super.scheduleAtFixedRate(task, startTime, period);

		final ScheduledMethodRunnable runnable = (ScheduledMethodRunnable) task;
		scheduledTasks.put(runnable.getTarget(), future);

		return future;
	}

	public void stop() {

		logger.info("stop()");

		for (final Map.Entry<Object, ScheduledFuture<?>> entry : scheduledTasks.entrySet()) {

			logger.info("stopping future!");
			final ScheduledFuture<?> future = entry.getValue();
			if (!future.cancel(true)) {
				logger.info("Cancel failed!");
			}
			logger.info(future.isDone());
		}
	}

}
