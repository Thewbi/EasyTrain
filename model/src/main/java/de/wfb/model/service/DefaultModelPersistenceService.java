package de.wfb.model.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
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
import de.wfb.model.locomotive.DefaultJsonLocomotiveConverter;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.locomotive.DefaultLocomotiveJson;
import de.wfb.model.locomotive.DefaultLocomotiveJsonConverter;
import de.wfb.model.node.JsonNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.converter.Converter;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.factory.Factory;

public class DefaultModelPersistenceService implements ModelPersistenceService {

	private static final Logger logger = LogManager.getLogger(DefaultModelPersistenceService.class);

	@Autowired
	private ModelService modelService;

	@Autowired
	private IdService idService;

	@Autowired
	private Factory<Node> nodeFactory;

	@Autowired
	private ProtocolFacade protocolFacade;

	@Override
	public void storeModel(final Model model, final String path) throws IOException {

		logger.info("StoreModel");

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
	public void storeLocomotiveModel(final Model model, final String path) throws IOException {

		final List<DefaultLocomotiveJson> jsonNodes = new ArrayList<>();

		final Converter<DefaultLocomotive, DefaultLocomotiveJson> converter = new DefaultLocomotiveJsonConverter();

		for (final DefaultLocomotive defaultLocomotive : model.getLocomotives()) {

			final DefaultLocomotiveJson target = new DefaultLocomotiveJson();
			converter.convert(defaultLocomotive, target);
			jsonNodes.add(target);
		}

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();

		final String json = gson.toJson(jsonNodes);

		FileUtils.writeStringToFile(new File(path), json, Charset.forName("UTF-8"));

		logger.trace(json);
	}

	@Override
	public void loadLocomotiveModel(final Model model, final String pathToModelFile) throws IOException {

		logger.info("Trying to load '" + Paths.get(pathToModelFile).toAbsolutePath() + "'");

		if (!Files.exists(Paths.get(pathToModelFile))) {
			return;
		}

		final Collection<DefaultLocomotiveJson> nodeArray = deserializeDefaultLocomotiveJson(pathToModelFile);
		if (CollectionUtils.isEmpty(nodeArray)) {

			logger.trace("'" + Paths.get(pathToModelFile).toAbsolutePath() + "' contains no data!");
			return;
		}

		logger.trace("'" + Paths.get(pathToModelFile).toAbsolutePath() + "' contains " + nodeArray.size() + " nodes!");

		final DefaultJsonLocomotiveConverter defaultJsonLocomotiveConverter = new DefaultJsonLocomotiveConverter();

		for (final DefaultLocomotiveJson defaultLocomotiveJson : nodeArray) {

			final DefaultLocomotive defaulLocomotive = new DefaultLocomotive();

			defaultJsonLocomotiveConverter.convert(defaultLocomotiveJson, defaulLocomotive);

			model.getLocomotives().add(defaulLocomotive);
			defaulLocomotive.setProtocolFacade(protocolFacade);
		}
	}

	@Override
	public void loadModel(final Model model, final String pathToModelFile) throws IOException {

		final Path absolutePathToModel = Paths.get(pathToModelFile).toAbsolutePath();

		logger.info("Trying to load '" + absolutePathToModel + "'");

		if (!Files.exists(Paths.get(pathToModelFile))) {

			logger.info("File '" + absolutePathToModel + "' does not exist!");
			return;
		}

		final Collection<JsonNode> nodeArray = deserializeJsonNode(pathToModelFile);
		if (CollectionUtils.isEmpty(nodeArray)) {

			logger.info("'" + Paths.get(pathToModelFile).toAbsolutePath() + "' contains no data!");
			return;
		}

		logger.info("'" + Paths.get(pathToModelFile).toAbsolutePath() + "' contains " + nodeArray.size() + " nodes!");

		// convert Json nodes to model nodes
		final int maxId = populateModel(model, nodeArray);

		// connect nodes that are manually connected
		processManualConnections(model, nodeArray);

		processDirection(model, nodeArray);

		// initialize the ID-Service so it will only return non-used IDs
		idService.setCurrentId(maxId);

		for (final JsonNode jsonNode : nodeArray) {

			modelService.sendModelChangedEvent(jsonNode.getX(), jsonNode.getY(), false, false, false, false);
		}
	}

	private void processDirection(final Model model, final Collection<JsonNode> nodeArray) {

		for (final RailNode railNode : model.getAllRailNodes()) {

			railNode.updateBlockedGraphNode();
		}
	}

	private int populateModel(final Model model, final Collection<JsonNode> nodeArray) {

		int maxId = Integer.MIN_VALUE;

		for (final JsonNode jsonNode : nodeArray) {

			try {

				final Node node = nodeFactory.create(jsonNode);

				if (model.getIdMap().containsKey(node.getId())) {
					throw new IllegalArgumentException("Duplicate Model id = " + node.getId());
				}

				// insert node into the id map
				model.getIdMap().put(node.getId(), node);

				// insert node into the node grid array
				model.setNode(jsonNode.getX(), jsonNode.getY(), node);

				// update max ID for initializing the ID service later
				if (jsonNode.getId() > maxId) {
					maxId = jsonNode.getId();
				}
			} catch (final Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		return maxId;
	}

	private void processManualConnections(final Model model, final Collection<JsonNode> nodeArray) {

		for (final JsonNode jsonNode : nodeArray) {

			logger.trace("Looking for manual CONNECTION");

			final RailNode railNode = (RailNode) model.getIdMap().get(jsonNode.getId());

			// resolve manual connections
			logger.trace("Factoring manual connections ...");

			if (CollectionUtils.isNotEmpty(jsonNode.getManualConnections())) {

				for (final Integer nodeId : jsonNode.getManualConnections()) {

					logger.trace("Manual connection to node: " + nodeId);

					final Node connectedNode = modelService.getNodeById(nodeId.intValue());

					if (connectedNode != null) {

						logger.trace(
								railNode.getId() + " manual connection to connectedNode: " + connectedNode.getId());

						logger.trace("Manual Connection resolved");

						railNode.manualConnectTo((RailNode) connectedNode);
					}
				}
			}
		}
	}

	private Collection<JsonNode> deserializeJsonNode(final String path) throws FileNotFoundException {

		final JsonReader reader = new JsonReader(new FileReader(path));

		final Type type = new TypeToken<Collection<JsonNode>>() {
		}.getType();

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final Collection<JsonNode> nodeArray = gson.fromJson(reader, type);

		return nodeArray;
	}

	private Collection<DefaultLocomotiveJson> deserializeDefaultLocomotiveJson(final String path)
			throws FileNotFoundException {

		final JsonReader reader = new JsonReader(new FileReader(path));

		final Type type = new TypeToken<Collection<DefaultLocomotiveJson>>() {
		}.getType();

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final Collection<DefaultLocomotiveJson> nodeArray = gson.fromJson(reader, type);

		return nodeArray;
	}

	/**
	 * For testing
	 *
	 * @param modelService
	 */
	public void setModelService(final ModelService modelService) {
		this.modelService = modelService;
	}

	/**
	 * For testing
	 *
	 * @param idService
	 */
	public void setIdService(final IdService idService) {
		this.idService = idService;
	}

	/**
	 * For testing
	 *
	 * @param nodeFactory
	 */
	public void setNodeFactory(final Factory<Node> nodeFactory) {
		this.nodeFactory = nodeFactory;
	}

	/**
	 * For testing
	 *
	 * @param protocolFacade
	 */
	public void setProtocolFacade(final ProtocolFacade protocolFacade) {
		this.protocolFacade = protocolFacade;
	}

}
