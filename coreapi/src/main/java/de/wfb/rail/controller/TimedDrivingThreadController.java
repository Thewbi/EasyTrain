package de.wfb.rail.controller;

public interface TimedDrivingThreadController {

	void addSingleStep();

	int decrementSingleStep();

	boolean isPaused();

	void togglePaused();

}
