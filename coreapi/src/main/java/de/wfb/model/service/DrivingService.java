package de.wfb.model.service;

import de.wfb.model.locomotive.Locomotive;

public interface DrivingService {

	void locomotiveGo(Locomotive locomotive, double speed);

	void locomotiveStop(Locomotive locomotive);

	void locomotiveStopAll();

	void locomotiveStartAll();

}
