package de.wfb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import de.wfb.rail.facade.ProtocolFacade;

public class EvtSenCommandThread {

	private static final boolean ACTIVE = true;
//	private static final boolean ACTIVE = false;

	private static final Logger logger = LogManager.getLogger(EvtSenCommandThread.class);

	private boolean running = true;

	@Autowired
	private ProtocolFacade protocolFacade;

	@Scheduled(fixedRate = 1000)
	public void threadFunc() {

		logger.trace("EvtSenCommandThread - threadFunc() ACTIVE = " + ACTIVE);

		if (!ACTIVE) {

			logger.trace("threadFunc() is deactivated!");
			return;
		}

		// send the P50XXEventCommand
		protocolFacade.event();
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
