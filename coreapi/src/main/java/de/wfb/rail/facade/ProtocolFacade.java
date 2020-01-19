package de.wfb.rail.facade;

import de.wfb.model.node.RailNode;

public interface ProtocolFacade {

	void nodeClicked(int x, int y);

	void throttleLocomotive(short locomotiveAddress, double throttleValue, boolean dirForward);

	/**
	 * Sends P50XXEventCommand.
	 */
	void event();

	void connect() throws Exception;

	void disconnect();

	void sense(int feebackContactId);

	/**
	 * Sends P50XXSensOffCommand.
	 */
	void xSenseOff();

	void turnoutStatus(RailNode node);

}
