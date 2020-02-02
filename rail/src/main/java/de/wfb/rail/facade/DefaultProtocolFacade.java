package de.wfb.rail.facade;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.node.Node;
import de.wfb.rail.events.FeedbackBlockUpdateEvent;
import de.wfb.rail.service.ProtocolService;

public class DefaultProtocolFacade implements ProtocolFacade {

	private static final Logger logger = LogManager.getLogger(DefaultProtocolFacade.class);

	@Autowired
	private ProtocolService protocolService;

	@Override
	public void nodeClicked(final int x, final int y) {

		logger.trace("nodeClicked()");

		protocolService.nodeClicked(x, y);
	}

	@Override
	public void throttleLocomotive(final short locomotiveAddress, final double throttleValue,
			final boolean dirForward) {

		logger.trace("throttleLocomotive()");

		protocolService.throttleLocomotive(locomotiveAddress, throttleValue, dirForward);
	}

	@Override
	public boolean event() {

		logger.trace("event() - Sending P50XXEventCommand");

		return protocolService.event();
	}

	@Override
	public void connect() throws Exception {

		logger.trace("connect()");

		protocolService.connect();
	}

	@Override
	public void disconnect() {

		logger.trace("disconnect()");

		protocolService.disconnect();
	}

	@Override
	public void sense(final int feebackContactId) {

		logger.trace("sense()");

		protocolService.sense(feebackContactId);
	}

	@Override
	public void xSenseOff() {

		logger.trace("xSenseOff()");

		protocolService.xSensOff();
	}

	@Override
	public boolean turnoutStatus(final short protocolId) {

		logger.trace("turnoutStatus()");

		return protocolService.turnoutStatus(protocolId);
	}

	@Override
	public void turnTurnout(final Node node) {

		logger.info("turnTurnout()");

		protocolService.turnTurnout(node);
	}

	@Override
	public List<FeedbackBlockUpdateEvent> eventSenseCommand() {
		return protocolService.eventSenseCommand();
	}

}
