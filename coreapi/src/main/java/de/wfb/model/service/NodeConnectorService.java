package de.wfb.model.service;

import de.wfb.model.node.Node;

public interface NodeConnectorService {

	void connect(Node node);

	void disconnect(Node oldNode);

}
