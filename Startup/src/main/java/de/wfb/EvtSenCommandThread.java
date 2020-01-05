package de.wfb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.rail.facade.ProtocolFacade;

public class EvtSenCommandThread implements Runnable {

	private static final Logger logger = LogManager.getLogger(EvtSenCommandThread.class);

	private boolean running = true;

	@Autowired
	private ProtocolFacade protocolFacade;

	@Override
	public void run() {

		logger.info("Starting thread!");

		while (running) {

			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				logger.error(e.getMessage(), e);
			}

			protocolFacade.event();
		}

		logger.info("Terminating thread!");
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(final boolean running) {
		this.running = running;
	}

	public void stop() {
		running = false;
	}

}
