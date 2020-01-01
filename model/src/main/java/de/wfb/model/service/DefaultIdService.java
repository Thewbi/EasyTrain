package de.wfb.model.service;

public class DefaultIdService implements IdService {

	private int currentId = -1;

	public int getNextId() {

		currentId++;

		return currentId;
	}

	public int getCurrentId() {
		return currentId;
	}

	public void setCurrentId(final int currentId) {
		this.currentId = currentId;
	}

}
