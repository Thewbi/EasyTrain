package de.wfb.model.service;

import java.io.File;
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
import de.wfb.rail.converter.Converter;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.ui.ShapeType;

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
	public void loadModel(final Model model, final String path) throws IOException {

		logger.info("Trying to load '" + Paths.get(path).toAbsolutePath() + "'");

		if (!Files.exists(Paths.get(path))) {
			return;
		}

		final JsonReader reader = new JsonReader(new FileReader(path));

		final Type type = new TypeToken<Collection<JsonNode>>() {
		}.getType();

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final Collection<JsonNode> nodeArray = gson.fromJson(reader, type);

		if (CollectionUtils.isEmpty(nodeArray)) {

			logger.info("'" + Paths.get(path).toAbsolutePath() + "' contains no data!");
			return;
		}

		logger.info("'" + Paths.get(path).toAbsolutePath() + "' contains " + nodeArray.size() + " nodes!");

		int maxId = Integer.MIN_VALUE;

		for (final JsonNode jsonNode : nodeArray) {

			final Node node = nodeFactory.create(jsonNode.getX(), jsonNode.getY(),
					ShapeType.valueOf(jsonNode.getShapeType()), jsonNode.getId());

			model.getIdMap().put(node.getId(), node);
			model.setNode(jsonNode.getX(), jsonNode.getY(), node);

			if (jsonNode.getId() > maxId) {
				maxId = jsonNode.getId();
			}
		}

		idService.setCurrentId(maxId);

		for (final JsonNode jsonNode : nodeArray) {

			final Node node = model.getIdMap().get(jsonNode.getId());

			if (CollectionUtils.isNotEmpty(jsonNode.getLeftList())) {

				for (final int leftNodeId : jsonNode.getLeftList()) {

					final Node leftNode = model.getIdMap().get(leftNodeId);
					node.getLeftList().add(leftNode);
				}
			}

			if (CollectionUtils.isNotEmpty(jsonNode.getRightList())) {

				for (final int rightNodeId : jsonNode.getRightList()) {

					final Node rightNode = model.getIdMap().get(rightNodeId);
					node.getRightList().add(rightNode);
				}
			}

			modelService.sendModelChangedEvent(jsonNode.getX(), jsonNode.getY());
		}
	}

}
