package de.wbi.model.serializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.node.GraphNode;
import de.wfb.rail.converter.Converter;
import de.wfb.rail.service.Route;

public class DefaultRouteDeserializer implements Converter<String, Route> {

	private static final Logger logger = LogManager.getLogger(DefaultRouteDeserializer.class);

	@Autowired
	private ModelFacade modelFacade;

	@Override
	public void convert(final String source, final Route target) {
		throw new RuntimeException("Not implemented yet!");
	}

	@Override
	public Route convert(final String routeAsString) {

		final Route route = new Route();

		final String[] routeSplit = routeAsString.split("\\|");

		final String locomotiveId = routeSplit[0];
		final String graphNodes = routeSplit[1];

		route.setLocomotiveId(Integer.parseInt(locomotiveId));

		final String[] graphNodesSplit = graphNodes.split(",");

		for (final String graphNodeIdAsString : graphNodesSplit) {

			final int graphNodeId = Integer.parseInt(graphNodeIdAsString);
			final GraphNode graphNode = modelFacade.getGraphNodeById(graphNodeId);

			if (graphNode == null) {
				logger.error("GraphNode id " + graphNodeId + " Not found!");
			}

			route.getGraphNodes().add(graphNode);
		}

		return route;
	}

	/**
	 * For testing
	 *
	 * @return
	 */
	public ModelFacade getModelFacade() {
		return modelFacade;
	}

	/**
	 * For testing
	 *
	 * @param modelFacade
	 */
	public void setModelFacade(final ModelFacade modelFacade) {
		this.modelFacade = modelFacade;
	}

}
