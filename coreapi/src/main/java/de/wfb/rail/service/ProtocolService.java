package de.wfb.rail.service;

import de.wfb.model.node.Node;

public interface ProtocolService {

	void nodeClicked(int x, int y);

	void throttleLocomotive(short locomotiveAddress, double throttleValue, boolean dirForward);

	void event();

	void connect() throws Exception;

	void disconnect();

	void sense(int feedbackContactId);

	void xSensOff();

	void turnoutStatus(Node node);

}
