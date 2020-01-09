package de.wfb.model.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import de.wfb.model.Model;
import de.wfb.model.converter.DefaultJsonNodeConverter;
import de.wfb.model.node.JsonNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.converter.Converter;
import de.wfb.rail.factory.Factory;

public class DefaultModelPersistenceService implements ModelPersistenceService {

	private static final Logger logger = LogManager.getLogger(DefaultModelPersistenceService.class);

	@Autowired
	private ModelService modelService;

	@Autowired
	private IdService idService;

	@Autowired
	private Factory<Node> nodeFactory;

	@Override
	public void storeModel(final Model model, final String path) throws IOException {

		final List<JsonNode> jsonNodes = new ArrayList<>();

		final Converter<Node, JsonNode> jsonNodeConverter = new DefaultJsonNodeConverter();

		for (final Map.Entry<Integer, Node> entry : model.getIdMap().entrySet()) {

			final JsonNode target = new JsonNode();
			jsonNodeConverter.convert(entry.getValue(), target);
			jsonNodes.add(target);
		}

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();

		final String json = gson.toJson(jsonNodes);

		FileUtils.writeStringToFile(new File(path), json, Charset.forName("UTF-8"));

		logger.trace(json);
	}

	@Override
	public void loadModel(final Model model, final String pathToModelFile) throws IOException {

		logger.info("Trying to load '" + Paths.get(pathToModelFile).toAbsolutePath() + "'");

		if (!Files.exists(Paths.get(pathToModelFile))) {
			return;
		}

		final Collection<JsonNode> nodeArray = deserialize(pathToModelFile);
		if (CollectionUtils.isEmpty(nodeArray)) {

			logger.trace("'" + Paths.get(pathToModelFile).toAbsolutePath() + "' contains no data!");
			return;
		}

		logger.trace("'" + Paths.get(pathToModelFile).toAbsolutePath() + "' contains " + nodeArray.size() + " nodes!");

		int maxId = Integer.MIN_VALUE;

		for (final JsonNode jsonNode : nodeArray) {

			try {

				final Node node = nodeFactory.create(jsonNode);

				model.getIdMap().put(node.getId(), node);
				model.setNode(jsonNode.getX(), jsonNode.getY(), node);

				// update max ID for initializing the ID service later
				if (jsonNode.getId() > maxId) {
					maxId = jsonNode.getId();
				}
			} catch (final Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		for (final JsonNode jsonNode : nodeArray) {

			logger.trace("Looking for manual CONNECTION");

			final RailNode railNode = (RailNode) model.getIdMap().get(jsonNode.getId());

			// resolve manual connections
			logger.trace("Factoring manual connections ...");
			if (CollectionUtils.isNotEmpty(jsonNode.getManualConnections())) {

				for (final Integer nodeId : jsonNode.getManualConnections()) {

					logger.info("Manual connection to node: " + nodeId);

					final Node connectedNode = modelService.getNodeById(nodeId.intValue());

					logger.info(railNode.getId() + " manual connection to connectedNode: " + connectedNode.getId());

					if (connectedNode != null) {

						logger.info("Manual Connection resolved");
						// railNode.getManualConnections().add((RailNode) connectedNode);

						railNode.connectTo((RailNode) connectedNode);
					}
				}
			}
		}

		// initialize the ID-Service so it will only return non-used IDs
		idService.setCurrentId(maxId);

		for (final JsonNode jsonNode : nodeArray) {

			modelService.sendModelChangedEvent(jsonNode.getX(), jsonNode.getY(), false, false, false);
		}
	}

	private Collection<JsonNode> deserialize(final String path) throws FileNotFoundException {

		final JsonReader reader = new JsonReader(new FileReader(path));

		final Type type = new TypeToken<Collection<JsonNode>>() {
		}.getType();

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final Collection<JsonNode> nodeArray = gson.fromJson(reader, type);

		return nodeArray;
	}

}
