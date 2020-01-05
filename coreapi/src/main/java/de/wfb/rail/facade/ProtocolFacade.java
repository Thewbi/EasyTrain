package de.wfb.rail.facade;

public interface ProtocolFacade {

	void nodeClicked(int x, int y);

	void throttleLocomotive(short locomotiveAddress, double throttleValue, boolean dirForward);

	void event();

	void connect() throws Exception;

	void disconnect();

}
