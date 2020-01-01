package de.wfb.model.service;

public interface IdService {

	int getNextId();

	int getCurrentId();

	void setCurrentId(int currentId);

}
