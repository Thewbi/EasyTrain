package de.wfb.model.service;

import java.io.IOException;

import de.wfb.model.Model;

public interface ModelPersistenceService {

	void storeModel(Model model, String path) throws IOException;

	void loadModel(Model model, String path) throws IOException;

}
