package de.wfb.rail.service;

import de.wfb.model.node.RailNode;

public interface TurnoutService {

	void queueStateRequest(RailNode node);

	void startQueryingFromQueue();

}
