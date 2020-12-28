package de.wfb.rail.facade;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.node.Node;
import de.wfb.rail.events.FeedbackBlockUpdateEvent;
import de.wfb.rail.service.ProtocolService;
import de.wfb.rail.service.TurnoutService;

public class DefaultProtocolFacade implements ProtocolFacade {

	private static final Logger logger = LogManager.getLogger(DefaultProtocolFacade.class);

	@Autowired
	private ProtocolService protocolService;

	@Autowired
	private TurnoutService turnoutService;

	@Override
	public Node nodeClicked(final int x, final int y) {

		logger.info("nodeClicked()");

		return protocolService.nodeClicked(x, y);
	}

	@Override
	public void throttleLocomotive(final short locomotiveAddress, final double throttleValue,
			final boolean dirForward) {

		logger.info("throttleLocomotive() locomotiveAddress=" + locomotiveAddress + " speed=" + throttleValue + " directionForward=" + dirForward);

		protocolService.throttleLocomotive(locomotiveAddress, throttleValue, dirForward);
	}

	@Override
	public boolean event() {

		logger.trace("event() - Sending P50XXEventCommand");

		return protocolService.event();
	}

	@Override
	public void connectToIntellibox() throws Exception {

		connect();

		logger.info("xSensOff ...");
		xSenseOff();
		logger.info("xSensOff done.");

		// query the turnouts to find out their state on the layout so that the software
		// can draw them in the correct state and also send the correct commands when
		// the user of the route service want to switch them
		turnoutService.startQueryingFromQueue();
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

		logger.info("xSenseOff()");

		protocolService.xSensOff();
	}

	@Override
	public boolean turnoutStatus(final short protocolId) {

		logger.trace("turnoutStatus()");

		return protocolService.turnoutStatus(protocolId);
	}

	@Override
	public void turnTurnout(final Node node) {

		logger.trace("turnTurnout() Node RN-ID: " + node.getId());

		protocolService.turnTurnout(node);
	}

	@Override
	public List<FeedbackBlockUpdateEvent> eventSenseCommand() {
		return protocolService.eventSenseCommand();
	}

}
