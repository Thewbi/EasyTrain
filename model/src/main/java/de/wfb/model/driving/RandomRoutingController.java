package de.wfb.model.driving;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.configuration.ConfigurationConstants;
import de.wfb.configuration.ConfigurationService;
import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.GraphNode;
import de.wfb.model.service.RoutingController;
import de.wfb.model.service.RoutingService;
import de.wfb.rail.converter.Converter;
import de.wfb.rail.events.FeedbackBlockEvent;
import de.wfb.rail.events.RouteFinishedEvent;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.BlockService;
import de.wfb.rail.service.Route;
import de.wfb.rail.service.RouteUtils;

/**
 * <pre>
 * Input:
 * - list of locomotives + the GraphNode (= start_block_graph_node) + the block (= start_block) they are located on
 * - list of blocks to drive to (This list should contain blocks that are openly visible on prominent spots such as visible main stations)
 * - stop_count = amount of stops before the locomotive returns to it's starting block
 * - locomotive_count = amount of locomotives to drive around
 *
 * for (int i = 0; i < locomotive_count; i++)
 * {
 * 		1. Randomly select a locomotive.
 * 		2. Remember the block this locomotive is located as it's start_block (After driving, the locomotive has to return here)
 * 		3. find a list of stop_count blocks randomly (= block_list). Make the locomotive go over these blocks than return it to it's starting block
 * 		4. for (block : block_list)
 *        	Find a route from the current_block to block
 *        	Start the locomotive
 *        	current_block = block
 * 		5. Find route from current_block to start_block_graph_node (So that the locomotive is oriented the same way when it started)
 * 		6. Start the locomotive
 * }
 *
 * The driving service has to receive events when a locomotive finished a route.
 * It will then retrieve the locomotive from the event and look into it's data structures to find the
 * next block for that locomotive.
 *
 * That means the driving service is event driven.
 * </pre>
 */
public class RandomRoutingController implements RoutingController, ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(RandomRoutingController.class);

	private final Random random = new Random();

	private static final int LOCOMOTIVE_COUNT_DEFAULT = 2;

	private static final int STOP_COUNT_DEFAULT = 3;

	private final int locomotiveCount = LOCOMOTIVE_COUNT_DEFAULT;

	private List<Locomotive> locomotives = new ArrayList<>();

	private final List<Locomotive> activeLocomotives = new ArrayList<>();

	private final Map<Locomotive, LocomotiveEntry> locomotiveContext = new HashMap<>();

	private boolean started = false;

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private RoutingService routingService;

	@Autowired
	private Converter<Route, String> routeSerializer;

	@Autowired
	private BlockService blockService;

	@Autowired
	private ConfigurationService configurationService;

	@Override
	public void initialize() throws IOException, Exception {

		logger.trace("initialize()");

		if (configurationService.getConfigurationAsBoolean(ConfigurationConstants.WRITE_ROUTES_TO_FILE)) {

			FileUtils.writeStringToFile(new File("routelog.txt"),
					">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n", "UTF-8", true);
		}

//		requiredBlocks.add(76);
//		requiredBlocks.add(21);

		locomotives = modelFacade.getLocomotives();

//		// put locomotives on railnodes for testing
//		RailNode railNode = (RailNode) modelFacade.getNodeById(498);
//		routingService.placeLocomotive(railNode, locomotives.get(0), Direction.EAST);
//		railNode = (RailNode) modelFacade.getNodeById(489);
//		routingService.placeLocomotive(railNode, locomotives.get(1), Direction.EAST);

		logger.trace("building context ...");

		// create the locomotiveContext for all locomotives that have been placed on the
		// layout
		for (final Locomotive locomotive : locomotives) {

			final GraphNode graphNode = locomotive.getGraphNode();
			if (graphNode == null) {

				logger.trace("Locomotive has no graph node!");
				continue;
			}

			final Block block = blockService.getBlockByGraphNode(graphNode);
			if (block == null) {

				logger.trace("Locomotive graph node has no block!");
				continue;
			}

			final LocomotiveEntry locomotiveEntry = new LocomotiveEntry();
			locomotiveEntry.setLocomotive(locomotive);
			locomotiveEntry.setStartBlock(block);
			locomotiveEntry.setStartGraphNode(graphNode);
			locomotiveEntry.getVisitedBlocks().clear();

			logger.trace(locomotiveEntry);

			locomotiveContext.put(locomotive, locomotiveEntry);
		}

		logger.info("building context done.");

		if (locomotiveContext.isEmpty()) {

			logger.error("The context is empty! Did you place any locomotives onto the track?");
			return;
		}

		refillActiveLocomotives();
	}

	private void refillActiveLocomotives() {

		logger.info("Select locomotives to start ...");

		// randomly select locomotives to start when the start() method is called
		for (int i = activeLocomotives.size(); i < locomotiveCount; i++) {

			// choose random locomotive
			int loopBreaker = 10;
			boolean locomotiveFound = false;
			while (!locomotiveFound && loopBreaker > 0) {

				loopBreaker--;

				logger.info("Trying to find locomotive ...");

				final Locomotive randomLocomotive = selectRandomLocomotiveFromContext(random);

				logger.info("RandomLocomotive: " + randomLocomotive);

				if (!activeLocomotives.contains(randomLocomotive)) {

					activeLocomotives.add(randomLocomotive);
					locomotiveFound = true;
				}
			}
		}

		logger.info("Select locomotives to start done. " + activeLocomotives);
	}

	@Override
	public void stop() {

		// TODO: implement
		// let all locomotives finish their tour (return them to their start node) but
		// then do not assign them a new route any more.

		started = false;
		configurationService.setConfigurationAsBoolean(ConfigurationConstants.AUTOMATED_DRIVING_ACTIVE, started);
	}

	@Override
	public void start() throws IOException, Exception {

		logger.info("Starting ...");

		started = true;
		configurationService.setConfigurationAsBoolean(ConfigurationConstants.AUTOMATED_DRIVING_ACTIVE, started);

		if (locomotiveContext.isEmpty()) {
			logger.error("The context is empty! Did you place any locomotives onto the track?");
			return;
		}

		// add routes to all active locomotives
		for (final Locomotive locomotive : activeLocomotives) {

			final boolean addNewRoute = locomotive.getRoute() == null || locomotive.getRoute().isEmpty();

			logger.info("Starting locomotive: " + locomotive);
			logger.info("locomotive.getRoute(): " + locomotive.getRoute());
			logger.info("addNewRoute: " + addNewRoute);

			if (addNewRoute) {

				addNewRouteToLocomotive(StringUtils.EMPTY, locomotive);

			} else {

				logger.info("Not adding a new route to locomotive: " + locomotive.getId());

			}
		}

		logger.info("Starting done.");
	}

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		if (event instanceof FeedbackBlockEvent) {

			final FeedbackBlockEvent feedbackBlockEvent = (FeedbackBlockEvent) event;
			processFeedbackBlockEvent(feedbackBlockEvent);

		} else if (event instanceof RouteFinishedEvent) {

			final RouteFinishedEvent routeFinishedEvent = (RouteFinishedEvent) event;
			try {
				processRouteFinishedEvent(routeFinishedEvent);
			} catch (final IOException e) {
				logger.info(e.getMessage(), e);
			} catch (final Exception e) {
				logger.info(e.getMessage(), e);
			}
		}
	}

	private void processFeedbackBlockEvent(final FeedbackBlockEvent feedbackBlockEvent) {
		logger.trace("processFeedbackBlockEvent()");
	}

	private void processRouteFinishedEvent(final RouteFinishedEvent routeFinishedEvent) throws IOException, Exception {

		final Locomotive locomotive = routeFinishedEvent.getLocomotive();

		logger.info("processRouteFinishedEvent() locomotive: " + locomotive);

		if (configurationService.getConfigurationAsBoolean(ConfigurationConstants.AUTOMATED_DRIVING_ACTIVE)) {

			// remove the route, otherwise start() will not assign it a new route
			locomotive.setRoute(null);
		}

		final Block block = locomotive.getRailNode().getBlock();
		final LocomotiveEntry locomotiveEntry = locomotiveContext.get(locomotive);

		if (locomotiveEntry == null) {

			logger.error("locomotiveEntry is null!");
			return;
		}

		logger.info("processRouteFinishedEvent() locomotive: " + locomotive + " locomotiveEntry.loco: "
				+ locomotiveEntry.getLocomotive());

		locomotiveEntry.getVisitedBlocks().add(block);

		if (block.equals(locomotiveEntry.getStartBlock())) {

			logger.trace("processRouteFinishedEvent() - back at start block!");

			// if the locomotive arrived at is initial block

			activeLocomotives.remove(locomotive);
			locomotiveEntry.getVisitedBlocks().clear();
			locomotive.setGraphNode(locomotiveEntry.getStartGraphNode());
			locomotive.setRailNode(locomotiveEntry.getStartGraphNode().getRailNode());
			locomotive.setRoute(null);

			refillActiveLocomotives();
			start();

		} else if (locomotiveEntry.getVisitedBlocks().size() >= STOP_COUNT_DEFAULT) {

			logger.info("processRouteFinishedEvent() - required block count visited!");

			// if the locomotive has had enough routes

			final GraphNode graphNodeStart = locomotiveEntry.getLocomotive().getGraphNode();
			final GraphNode graphNodeEnd = locomotiveEntry.getStartGraphNode();
			final boolean routeOverReservedGraphNodes = true;
			final boolean routeOverBlockedFeedbackBlocks = true;

			logger.info("GraphNodeStart: " + graphNodeStart);
			logger.info("GraphNodeEnd: " + graphNodeEnd);

			final Block startBlock = locomotiveEntry.getCurrentBlock();
			final Block endBlock = locomotiveEntry.getStartBlock();

			logger.info("Trying to find a route from startBlock: " + startBlock + " to: " + endBlock);

			// reserve the start block
			startBlock.reserveForLocomotive(locomotive);

			Route route = routingService.startLocomotiveToBlock(locomotive, locomotive.getOrientation(), startBlock,
					locomotive.getOrientation(), endBlock, routeOverReservedGraphNodes, routeOverBlockedFeedbackBlocks);

			if (RouteUtils.isEmpty(route)) {

				route = routingService.startLocomotiveToRandomBlock(locomotive, locomotive.getOrientation(), startBlock,
						locomotive.getOrientation(), routeOverReservedGraphNodes, routeOverBlockedFeedbackBlocks);
			}

			logger.trace("Trying to find a route from startBlock: " + startBlock + " to: " + endBlock
					+ " done! Route is " + route);

			routingService.attachRouteToLocomotive(locomotive, route);

			if (RouteUtils.isNotEmpty(route)) {

				if (configurationService.getConfigurationAsBoolean(ConfigurationConstants.WRITE_ROUTES_TO_FILE)) {

					String routeAsString = routeSerializer.convert(route);
					routeAsString += "\n";
					FileUtils.writeStringToFile(new File("routelog.txt"), routeAsString, "UTF-8", true);
				}
			}

		} else {

			logger.info("processRouteFinishedEvent() - required block count NOT visited yet!");

			if (configurationService.getConfigurationAsBoolean(ConfigurationConstants.TIMED_DRIVING_THREAD_ACTIVE)) {

				// timed driving service will automatically make all locomotives go that have a
				// route assigned to them
				addNewRouteToLocomotive(StringUtils.EMPTY, locomotive);

			} else {

				start();

			}
		}
	}

	private void addNewRouteToLocomotive(final String label, final Locomotive locomotive)
			throws IOException, Exception {

		logger.info("addNewRouteToLocomotive() locomotive = " + locomotive);

		final LocomotiveEntry locomotiveEntry = locomotiveContext.get(locomotive);

		logger.trace("addNewRouteToLocomotive() locomotiveEntry = " + locomotiveEntry);

		Route route = null;
		Block startBlock = null;
		final Block endBlock = null;

		final int loopBreakerRetries = 9999;
		int loopBreaker = loopBreakerRetries;

		while (RouteUtils.isEmpty(route) && loopBreaker > 0) {

			loopBreaker--;
			logger.info("LoopBreaker: " + loopBreaker);

			// always go forward! Do not invert startEdgeDirection
			final Direction locomotiveOrientation = locomotive.getOrientation();
			final Direction startEdgeDirection = locomotive.getOrientation();
			startBlock = locomotiveEntry.getCurrentBlock();

			logger.trace("Trying to find a route from startBlock: " + startBlock + " to: " + endBlock);
			logger.trace("Locomotive: " + locomotive);

			final boolean routeOverReservedNodes = true;
			final boolean routeOverBlockedFeedbackBlocks = false;

			route = routingService.startLocomotiveToRandomBlock(locomotive, locomotiveOrientation, startBlock,
					startEdgeDirection, routeOverReservedNodes, routeOverBlockedFeedbackBlocks);

			logger.trace("Route finding result: " + route);
		}

		if (RouteUtils.isEmpty(route)) {

			logger.trace("Could not find a route from startBlock: " + startBlock + " to: " + endBlock
					+ " after loopBreakerRetries: " + loopBreakerRetries);

		} else {

			routingService.attachRouteToLocomotive(locomotive, route);

			if (configurationService.getConfigurationAsBoolean(ConfigurationConstants.WRITE_ROUTES_TO_FILE)) {

				String routeAsString = routeSerializer.convert(route);
				routeAsString += "\n";
				FileUtils.writeStringToFile(new File("routelog.txt"), label + routeAsString, "UTF-8", true);
			}
		}
	}

	@SuppressWarnings("unused")
	private Locomotive selectRandomLocomotive(final Random random) {

		final int min = 0;
		final int max = locomotives.size() - 1;
		final int index = random.nextInt((max - min) + 1) + min;

		final Locomotive locomotive = locomotives.get(index);

		return locomotive;
	}

	private Locomotive selectRandomLocomotiveFromContext(final Random random) {

		if (locomotiveContext.isEmpty()) {
			return null;
		}

		final Object[] values = locomotiveContext.values().toArray();
		final LocomotiveEntry randomValue = (LocomotiveEntry) values[random.nextInt(values.length)];

		return randomValue.getLocomotive();
	}

	@Override
	public boolean isStarted() {
		return started;
	}

}
