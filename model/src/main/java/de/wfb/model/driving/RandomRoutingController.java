package de.wfb.model.driving;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.RoutingController;
import de.wfb.model.service.RoutingService;
import de.wfb.rail.converter.Converter;
import de.wfb.rail.events.FeedbackBlockEvent;
import de.wfb.rail.events.RouteFinishedEvent;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.BlockService;
import de.wfb.rail.service.Route;

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

//	private static final int LOCOMOTIVE_COUNT_DEFAULT = 1;
	private static final int LOCOMOTIVE_COUNT_DEFAULT = 2;

	private static final int STOP_COUNT_DEFAULT = 2;

	private final int locomotiveCount = LOCOMOTIVE_COUNT_DEFAULT;

	private final Random random = new Random();

	private final Set<Integer> requiredBlocks = new HashSet<>();

	private final List<Block> ignoredBlocks = new ArrayList<>();

	private List<DefaultLocomotive> locomotives = new ArrayList<>();

	private final List<DefaultLocomotive> activeLocomotives = new ArrayList<>();

	private final Map<DefaultLocomotive, LocomotiveEntry> locomotiveContext = new HashMap<>();

	private boolean started = false;

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private BlockService blockService;

	@Autowired
	private RoutingService routingService;

	@Autowired
	private Converter<Route, String> routeSerializer;

	@Override
	public void initialize() throws IOException, Exception {

		logger.info("initialize()");

		requiredBlocks.add(76);
		requiredBlocks.add(21);

		ignoredBlocks.add(blockService.getBlockById(1));
		ignoredBlocks.add(blockService.getBlockById(2));
		ignoredBlocks.add(blockService.getBlockById(3));
		ignoredBlocks.add(blockService.getBlockById(4));
		ignoredBlocks.add(blockService.getBlockById(5));
		ignoredBlocks.add(blockService.getBlockById(6));
		ignoredBlocks.add(blockService.getBlockById(7));
		ignoredBlocks.add(blockService.getBlockById(8));
		ignoredBlocks.add(blockService.getBlockById(9));
		ignoredBlocks.add(blockService.getBlockById(10));
		ignoredBlocks.add(blockService.getBlockById(11));

		ignoredBlocks.add(blockService.getBlockById(35));
		ignoredBlocks.add(blockService.getBlockById(36));
		ignoredBlocks.add(blockService.getBlockById(37));

		ignoredBlocks.add(blockService.getBlockById(41));
		ignoredBlocks.add(blockService.getBlockById(44));
		ignoredBlocks.add(blockService.getBlockById(45));
		ignoredBlocks.add(blockService.getBlockById(46));

		ignoredBlocks.add(blockService.getBlockById(50));
		ignoredBlocks.add(blockService.getBlockById(51));
		ignoredBlocks.add(blockService.getBlockById(52));
		ignoredBlocks.add(blockService.getBlockById(53));
//		ignoredBlocks.add(blockService.getBlockById(54));
		ignoredBlocks.add(blockService.getBlockById(55));
		ignoredBlocks.add(blockService.getBlockById(56));
		ignoredBlocks.add(blockService.getBlockById(57));
		ignoredBlocks.add(blockService.getBlockById(58));
		ignoredBlocks.add(blockService.getBlockById(59));

		ignoredBlocks.add(blockService.getBlockById(60));
		ignoredBlocks.add(blockService.getBlockById(61));
		ignoredBlocks.add(blockService.getBlockById(62));
		ignoredBlocks.add(blockService.getBlockById(63));
		ignoredBlocks.add(blockService.getBlockById(64));

		ignoredBlocks.add(blockService.getBlockById(81));
		ignoredBlocks.add(blockService.getBlockById(86));

		ignoredBlocks.add(blockService.getBlockById(91));
		ignoredBlocks.add(blockService.getBlockById(93));

		ignoredBlocks.add(blockService.getBlockById(103));

		ignoredBlocks.add(blockService.getBlockById(111));
		ignoredBlocks.add(blockService.getBlockById(112));

		locomotives = modelFacade.getLocomotives();

		RailNode railNode = (RailNode) modelFacade.getNodeById(498);
		routingService.placeLocomotive(railNode, locomotives.get(0), Direction.EAST);
		railNode = (RailNode) modelFacade.getNodeById(489);
		routingService.placeLocomotive(railNode, locomotives.get(1), Direction.EAST);

		logger.info("building context ...");

		// create the locomotiveContext for all locomotives that have been placed on the
		// layout
		for (final DefaultLocomotive locomotive : locomotives) {

			final GraphNode graphNode = locomotive.getGraphNode();
			if (graphNode == null) {

				logger.info("Locomotive has no graph node!");
				continue;
			}

			final Block block = blockService.getBlockByGraphNode(graphNode);
			if (block == null) {

				logger.info("Locomotive graph node has no block!");
				continue;
			}

			final LocomotiveEntry locomotiveEntry = new LocomotiveEntry();
			locomotiveEntry.setLocomotive(locomotive);
			locomotiveEntry.setStartBlock(block);
			locomotiveEntry.setStartGraphNode(graphNode);
			locomotiveEntry.getVisitedBlocks().clear();

			logger.info(locomotiveEntry);

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

				final DefaultLocomotive randomLocomotive = selectRandomLocomotiveFromContext(random);

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
	public void start() throws IOException, Exception {

		logger.info("Starting ...");

		started = true;

		if (locomotiveContext.isEmpty()) {

			logger.error("The context is empty! Did you place any locomotives onto the track?");
			return;
		}

		// add routes to all active locomotives
		for (final DefaultLocomotive locomotive : activeLocomotives) {

			logger.info("Starting locomotive: " + locomotive);

			addNewRouteToLocomotive(locomotive);
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
		logger.info("processFeedbackBlockEvent()");
	}

	private void processRouteFinishedEvent(final RouteFinishedEvent routeFinishedEvent) throws IOException, Exception {

		logger.info("processRouteFinishedEvent()");

		final DefaultLocomotive locomotive = routeFinishedEvent.getDefaultLocomotive();

		final Block block = locomotive.getRailNode().getBlock();
		final LocomotiveEntry locomotiveEntry = locomotiveContext.get(locomotive);
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

		} else if (locomotiveEntry.getVisitedBlocks().size() == STOP_COUNT_DEFAULT) {

			logger.trace("processRouteFinishedEvent() - required block count visited!");

			// if the locomotive has had enough routes

			final GraphNode graphNodeStart = locomotiveEntry.getLocomotive().getGraphNode();
			final GraphNode graphNodeEnd = locomotiveEntry.getStartGraphNode();
			final boolean routeOverReservedGraphNodes = false;
			final boolean routeOverBlockedFeedbackBlocks = false;

			logger.info("GraphNodeStart: " + graphNodeStart);
			logger.info("graphNodeEnd: " + graphNodeEnd);

			final Block startBlock = locomotiveEntry.getCurrentBlock();
			final Block endBlock = locomotiveEntry.getStartBlock();

			logger.trace("Trying to find a route from startBlock: " + startBlock + " to: " + endBlock);

			final Route route = routingService.startLocomotiveToBlock(locomotive, locomotive.getOrientation(),
					startBlock, locomotive.getOrientation(), endBlock, routeOverReservedGraphNodes,
					routeOverBlockedFeedbackBlocks);

			logger.trace("Trying to find a route from startBlock: " + startBlock + " to: " + endBlock
					+ " done! Route is " + route);

			routingService.attachRouteToLocomotive(locomotive, route);

			String routeAsString = routeSerializer.convert(route);
			routeAsString += "\n";
			FileUtils.writeStringToFile(new File("routelog.txt"), routeAsString, "UTF-8", true);

		} else {

			logger.trace("processRouteFinishedEvent() - required block count NOT visited yet!");

			addNewRouteToLocomotive(locomotive);
		}
	}

	private void addNewRouteToLocomotive(final DefaultLocomotive locomotive) throws IOException, Exception {

		logger.info("addNewRouteToLocomotive() locomotive = " + locomotive);

		final LocomotiveEntry locomotiveEntry = locomotiveContext.get(locomotive);

		logger.info("addNewRouteToLocomotive() locomotiveEntry = " + locomotiveEntry);

		Route route = null;
		Block startBlock = null;
		Block endBlock = null;

		final int loopBreakerRetries = 100;
		int loopBreaker = loopBreakerRetries;

		while (route == null && loopBreaker > 0) {

			loopBreaker--;

			final Block selectRandomBlock = selectRandomBlock(random);

			// always go forward! Do not invert startEdgeDirection
			final Direction locomotiveOrientation = locomotive.getOrientation();
			final Direction startEdgeDirection = locomotive.getOrientation();
			startBlock = locomotiveEntry.getCurrentBlock();
			endBlock = selectRandomBlock;

			logger.info("Trying to find a route from startBlock: " + startBlock + " to: " + endBlock);

			final boolean routeOverReservedNodes = true;
			final boolean routeOverBlockedFeedbackBlocks = false;

			route = routingService.startLocomotiveToBlock(locomotive, locomotiveOrientation, startBlock,
					startEdgeDirection, endBlock, routeOverReservedNodes, routeOverBlockedFeedbackBlocks);

			logger.info("Route finding result: " + route);
		}

		if (route == null) {

			logger.info("Could not find a route from startBlock: " + startBlock + " to: " + endBlock
					+ " after loopBreakerRetries: " + loopBreakerRetries);

		} else {

			routingService.attachRouteToLocomotive(locomotive, route);

			String routeAsString = routeSerializer.convert(route);
			routeAsString += "\n";
			FileUtils.writeStringToFile(new File("routelog.txt"), routeAsString, "UTF-8", true);
		}
	}

	@SuppressWarnings("unused")
	private DefaultLocomotive selectRandomLocomotive(final Random random) {

		final int min = 0;
		final int max = locomotives.size() - 1;
		final int index = random.nextInt((max - min) + 1) + min;

		final DefaultLocomotive randomLocomotive = locomotives.get(index);

		return randomLocomotive;
	}

	private DefaultLocomotive selectRandomLocomotiveFromContext(final Random random) {

		if (locomotiveContext.isEmpty()) {
			return null;
		}

		final Object[] values = locomotiveContext.values().toArray();
		final LocomotiveEntry randomValue = (LocomotiveEntry) values[random.nextInt(values.length)];

		return randomValue.getLocomotive();
	}

	private Block selectRandomBlock(final Random random) {

		final List<Block> allBlocks = blockService.getAllBlocks();

		allBlocks.removeAll(ignoredBlocks);

		final int min = 0;
		final int max = allBlocks.size() - 1;
		final int index = random.nextInt((max - min) + 1) + min;

		final Block block = allBlocks.get(index);

		return block;
	}

	@Override
	public boolean isStarted() {
		return started;
	}

}
