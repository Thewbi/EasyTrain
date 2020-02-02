package de.wfb.rail.facade;

import java.util.List;

import de.wfb.model.node.Node;
import de.wfb.rail.events.FeedbackBlockUpdateEvent;

public interface ProtocolFacade {

	void nodeClicked(int x, int y);

	void throttleLocomotive(short locomotiveAddress, double throttleValue, boolean dirForward);

	/**
	 * Sends P50XXEventCommand.
	 *
	 * @return
	 */
	boolean event();

	void connect() throws Exception;

	void disconnect();

	void sense(int feebackContactId);

	/**
	 * Sends P50XXSensOffCommand.
	 */
	void xSenseOff();

	boolean turnoutStatus(short protocolId);

	void turnTurnout(Node node);

	List<FeedbackBlockUpdateEvent> eventSenseCommand();

}
