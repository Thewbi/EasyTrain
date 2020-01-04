package de.wfb.rail.facade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.rail.service.ProtocolService;

public class DefaultProtocolFacade implements ProtocolFacade {

	private static final Logger logger = LogManager.getLogger(DefaultProtocolFacade.class);

	@Autowired
	private ProtocolService protocolService;

	@Override
	public void nodeClicked(final int x, final int y) {
		protocolService.nodeClicked(x, y);
	}

	@Override
	public void connect() throws Exception {

		logger.info("connect()");

		protocolService.connect();
	}

	@Override
	public void disconnect() {
		protocolService.disconnect();
	}

	@Override
	public void throttleLocomotive(final short locomotiveAddress, final double throttleValue,
			final boolean dirForward) {
		protocolService.throttleLocomotive(locomotiveAddress, throttleValue, dirForward);
	}

}
