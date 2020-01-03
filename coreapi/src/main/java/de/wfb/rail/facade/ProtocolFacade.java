package de.wfb.rail.facade;

public interface ProtocolFacade {

	void nodeClicked(int x, int y);

	void connect() throws Exception;

	void disconnect();

}
