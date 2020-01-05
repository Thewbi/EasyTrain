package de.wfb.rail.service;

public interface ProtocolService {

	void nodeClicked(int x, int y);

	void throttleLocomotive(short locomotiveAddress, double throttleValue, boolean dirForward);

	void event();

	void connect() throws Exception;

	void disconnect();

}
