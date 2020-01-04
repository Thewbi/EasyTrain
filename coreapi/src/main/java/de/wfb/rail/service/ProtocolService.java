package de.wfb.rail.service;

public interface ProtocolService {

	void nodeClicked(int x, int y);

	void connect() throws Exception;

	void disconnect();

	void throttleLocomotive(short locomotiveAddress, double throttleValue, boolean dirForward);

}
