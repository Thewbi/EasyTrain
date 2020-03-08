package de.wfb.model.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.Direction;
import de.wfb.rail.converter.Converter;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.Route;

public class PreRecordedRoutingService extends BaseRoutingService {

	private static final Logger logger = LogManager.getLogger(PreRecordedRoutingService.class);

	private final List<Route> routes = new ArrayList<Route>();

	@Autowired
	private Converter<String, Route> routeDeserializer;

	@Override
	public void initialize() {

//		final String filename = "routelog_quick_revert_one_loco.txt";
//		final String filename = "routelog_quick_deadlock_2.txt";
		final String filename = "routelog.txt";

		logger.info("Initializing from file: \"" + filename + "\"");

		try {
			// final List<String> readLines = FileUtils.readLines(new
			// File("routelog_quick_revert.txt"), "UTF-8");
			final List<String> readLines = FileUtils.readLines(new File(filename), "UTF-8");

			for (final String routeAsString : readLines) {

				if (routeAsString.startsWith(">>>>>>>>>>>>>>")) {

					continue;
				}

				logger.info("RouteAsString: " + routeAsString);

				final Route route = routeDeserializer.convert(routeAsString);

				logger.info("Router: " + route);

				routes.add(route);
			}

			logger.info(routes.size() + " routes added!");

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Route startLocomotiveToBlock(final Locomotive locomotive, final Direction locomotiveOrientation,
			final Block startBlock, final Direction startEdgeDirection, final Block endBlock,
			final boolean routeOverReservedNodes, final boolean routeOverBlockedFeedbackBlocks) {

		logger.info("Searching route for locomotive.getId() =  " + locomotive.getId());

		Route result = null;
		for (final Route route : routes) {

			if (route.getLocomotiveId() == locomotive.getId()) {

				result = route;
				break;
			}
		}

		logger.info("Route found: " + result);

		routes.remove(result);

		return result;
	}

	@Override
	public void removeRoutesAll() {
		// nop
	}

}
