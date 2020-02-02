package de.wfb.rail.service;

import java.util.List;

import de.wfb.model.node.Node;
import de.wfb.rail.events.FeedbackBlockUpdateEvent;

public interface ProtocolService {

	void nodeClicked(int x, int y);

	void throttleLocomotive(short locomotiveAddress, double throttleValue, boolean dirForward);

	boolean event();

	void connect() throws Exception;

	void disconnect();

	void sense(int feedbackContactId);

	void xSensOff();

	boolean turnoutStatus(short protocolId);

	void turnTurnout(Node node);

	List<FeedbackBlockUpdateEvent> eventSenseCommand();

}
