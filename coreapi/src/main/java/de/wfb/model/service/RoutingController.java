package de.wfb.model.service;

import java.io.IOException;

public interface RoutingController {

	void initialize() throws IOException, Exception;

	void start() throws IOException, Exception;

	boolean isStarted();

}
