package de.wfb.model.service;

import de.wfb.model.locomotive.DefaultLocomotive;

public interface DrivingService {

	void locomotiveStop(DefaultLocomotive locomotive);

	void locomotiveGo(DefaultLocomotive locomotive, double speed);

}
