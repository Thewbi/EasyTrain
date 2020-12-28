package de.wfb.rail.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;

import de.wfb.configuration.ConfigurationConstants;
import de.wfb.configuration.ConfigurationService;
import de.wfb.model.facade.ModelFacade;
import de.wfb.model.facade.RoutingFacade;
import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.Direction;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.DrivingService;
import de.wfb.rail.events.BlockEnteredEvent;
import de.wfb.rail.events.BlockExitedEvent;
import de.wfb.rail.events.FeedbackBlockEvent;
import de.wfb.rail.events.RouteAddedEvent;
import de.wfb.rail.events.RouteFinishedEvent;
import de.wfb.rail.facade.ProtocolFacade;

/**
 * Slow down the locomotive to 20% of is driving speed when the locomotive is
 * approaching the last block of it's route or a blocked block.<br />
 * <br />
 *
 * This is important because some blocks on the real layout are designed very,
 * very short.<br />
 * <br />
 *
 * If decoder speed curves are very flat, the locomotive will take some time to
 * actually come to a halt. If the block is short and the locomotive takes a
 * long time to stop, the locomotive exits the block before it even stops.<br />
 * <br />
 *
 * This is not a problem per se because UNLESS THE LOCOMOTIVE RUNS OVER A
 * TURNOUT, the current block still remains reserved for the locomotive and the
 * system still correctly keeps track of the locomotive. It cannot be anywhere
 * else but in close vincinity to the block because there was no turnout
 * nearby.<br />
 * <br />
 *
 * The real problem occurs if THERE IS A TURNOUT directly after the LAST BLOCK
 * OF A ROUTE! The locomotive might run past that turnout and stop. Now the
 * system has lost track of where the locomotive actually is! It has lost track
 * because it does not take the slow down into account. For the system, the
 * locomotive now is located on the block still and has not run past a switch!
 * The routing algorithm will start computing routes from the last block of the
 * last route and not from the actual location of the locomotive on the real
 * layout which makes a difference in this situation!<br />
 * <br />
 *
 * If the user starts a new route in that inconsistent situation, the locomotive
 * is already passed a turnout. If the new route needs that exact turnout to
 * change direction (thrown or closed) then the locomotive is literaly on the
 * wrong track (No pun intended!) The system has no way of telling what
 * happened, it assumes the routing is correct ant it will make the locomotive
 * go and wait for it to arrive at the next block which might never happen
 * because the locomotive run over a incorrectly setup turnout.<br />
 * <br />
 *
 * By slowing down the locomotive, the hopes are that the locomotive stops on
 * the last block of a route before going over the next turnout. That way, the
 * system assumes a location that actually matches the real situation. Routing
 * still works correctly.<br />
 * <br />
 */
public class DefaultDrivingService implements DrivingService, ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(DefaultDrivingService.class);

	@Autowired
	private RoutingFacade routingFacade;

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private BlockService blockService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private ProtocolFacade protocolFacade;

	@Autowired
	private ConfigurationService configurationService;

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		if (event instanceof RouteAddedEvent) {

			processRouteAddedEvent((RouteAddedEvent) event);

		} else if (event instanceof BlockEnteredEvent) {

			processBlockEnteredEvent((BlockEnteredEvent) event);

		} else if (event instanceof BlockExitedEvent) {

			processBlockExitedEvent((BlockExitedEvent) event);

		} else if (event instanceof RouteFinishedEvent) {

			processRouteFinishedEvent((RouteFinishedEvent) event);

		} else if (event instanceof FeedbackBlockEvent) {

			processFeedbackBlockEvent((FeedbackBlockEvent) event);

		}
	}

	/**
	 * After a XEvtSen command was executed, system learns about the states of
	 * blocks from the response of the command.<br />
	 * <br />
	 *
	 * For every block signaled as free or blocked, the system will publish a
	 * separate FeedbackBlockEvent.<br />
	 * <br />
	 *
	 * This method will react to the events. When a block is free or used, it will
	 * determine the locomotive that is on the block using a heuristic:<br />
	 * <br />
	 *
	 * The heuristic is: A block is reserved by at most one route. The route belongs
	 * to at most one locomotive. So when a block is used or freed the corresponding
	 * locomotive must be the locomotive that owns the route.<br />
	 * <br />
	 *
	 * If someone manually moves a locomotive onto a block, this heuristic does not
	 * hold any more and the software fails. That is why manual and automatic
	 * operation do not go together well.<br />
	 * <br />
	 */
	private void processFeedbackBlockEvent(final FeedbackBlockEvent event) {

		logger.trace("processFeedbackBlockEvent() BlockNumber: " + event.getFeedbackBlockNumber() + " BlockState: "
				+ event.getFeedbackBlockState());

		switch (event.getFeedbackBlockState()) {

		case BLOCKED:
			processFeedbackBlockBlocked(event);
			break;

		case FREE:
			processFeedbackBlockFree(event);
			break;

		default:
			break;
		}
	}

	/**
	 * Handler for when a block is freed.
	 *
	 * Converts a FeedbackBlockEvent into a BlockExitedEvent.
	 *
	 * @param event the event which has a feedback block state of type free.
	 */
	private void processFeedbackBlockFree(final FeedbackBlockEvent event) {

		logger.trace("processFeedbackBlockFree()");

		Locomotive locomotive = null;
		Block block = null;
		Route route = null;

		// determine the block
		final int feedbackBlockNumber = event.getFeedbackBlockNumber();

		logger.trace("feedbackBlockNumber: " + feedbackBlockNumber);

		block = blockService.getBlockById(feedbackBlockNumber);
		if (block == null) {
			logger.trace("No block!");
		} else {
			logger.trace("block: " + block);

			// determine the route
			route = routingFacade.getRouteByBlock(block);
			if (route == null) {
				logger.trace("No route!");
			} else {
				logger.trace("route: " + route);

				// determine the locomotive
				locomotive = route.getLocomotive();
				if (locomotive == null) {
					logger.trace("No locomotive!");
				} else {
					logger.trace("locomotive: " + locomotive);
				}
			}
		}

		// It will determine which locomotive has exited that block. Because
		// only a single locomotive can reserve a route to this block.
		//
		// based on that knowledge it will send the BlockExitedEvent
		final BlockExitedEvent blockExitedEvent = new BlockExitedEvent(this, block, locomotive);
		applicationEventPublisher.publishEvent(blockExitedEvent);
	}

	/**
	 * Determines which locomotive has entered that block. Because only a single
	 * locomotive can reserve a route to this block.<br />
	 * <br />
	 *
	 * Based on that knowledge it will send the BlockEnteredEvent<br />
	 * <br />
	 *
	 * @param event
	 */
	private void processFeedbackBlockBlocked(final FeedbackBlockEvent event) {

		final int feedbackBlockNumber = event.getFeedbackBlockNumber();

		logger.trace("processFeedbackBlockBlocked() feedbackBlockNumber: " + feedbackBlockNumber);

		// get block
		final Block block = blockService.getBlockById(feedbackBlockNumber);
		if (block == null) {

			logger.info("No Block! feedbackBlockNumber: " + feedbackBlockNumber);
			return;
		}

		// get route that currently owns the block
		final Route route = routingFacade.getRouteByBlock(block);
		if (route == null) {

			logger.trace("No Route! feedbackBlockNumber: " + feedbackBlockNumber);
			return;
		}

		// get locomotive that executes the route
		final Locomotive locomotive = route.getLocomotive();
		if (locomotive == null) {
			logger.info("locomotive is null");
		}

		// send the BlockEnteredEvent
		final BlockEnteredEvent blockEnteredEvent = new BlockEnteredEvent(this, block, locomotive);
		applicationEventPublisher.publishEvent(blockEnteredEvent);
	}

	private void processRouteFinishedEvent(final RouteFinishedEvent event) {

		logger.trace("processRouteFinishedEvent()");

		// disable the rest of the method if the timed driving thread is used to
		// simulate a layout
		if (configurationService.getConfigurationAsBoolean(ConfigurationConstants.TIMED_DRIVING_THREAD_ACTIVE)) {
			return;
		}

		if (configurationService.getConfigurationAsBoolean(ConfigurationConstants.AUTOMATED_DRIVING_ACTIVE)) {

			// abort because the rest of the method stops the locomotive and removes it's
			// route. The automated driving only works if the locomotive keeps going and
			// always has a route.
			return;
		}

		// CONFLICT: this code makes the random routing controller fail!

		// retrieve the locomotive that has finished it's route
		final Locomotive locomotive = event.getLocomotive();

		// remove the route from the locomotive
		logger.trace("Removing Route " + locomotive.getRoute());
		final Route route = locomotive.getRoute();
		route.setLocomotive(null);
		locomotive.setRoute(null);

		// DEBUG
		final RailNode railNode = locomotive.getRailNode();
		final GraphNode graphNode = locomotive.getGraphNode();
		logger.trace("The locomotive '" + locomotive + "' is now on the RailNode ID: " + railNode.getId()
				+ " and on GraphNode ID: " + graphNode.getId() + ". Its orientation is: "
				+ locomotive.getOrientation().name());

		// removing graphNode from the locomotive
		logger.trace("Removing graphNode from locomotive!");
		locomotive.setGraphNode(null);

		// stop the locomotive
		locomotiveStop(locomotive);
	}

	/**
	 * Internal Block Exited Event. (Not caused by P50X XEvtSen command).<br/>
	 * <br/>
	 *
	 * After a XEvtSen returns, the system will publish FeedbackBlockEventS. Also
	 * the simulator (TimedDrivingThread) will publish fake FeedbackBlockEventS so
	 * that the system can be tested without sending P50X XEvtSen commands.<br/>
	 * <br/>
	 *
	 * Those FeedbackBlockEventS are handled by the DefaultDrivingService (this
	 * class). It will determine which locomotive did cause the FeedbackBlockEvent
	 * and it will publish a BlockExitedEvent.<br/>
	 * <br/>
	 *
	 * The BlockExitedEvent is handled by the DefaultDrivingService (this class) It
	 * will compute new routes.
	 *
	 * @param event
	 */
	private void processBlockExitedEvent(final BlockExitedEvent event) {

		if (event.getBlock() == null) {
			return;
		}

		final Locomotive locomotive = event.getLocomotive();

		final int id = (locomotive == null) ? -1 : locomotive.getId();

		logger.trace("processBlockExitedEvent() BlockID: " + event.getBlock().getId() + " locomotive ID: " + id);

		// continueAllRoutes(locomotive);
		continueAllRoutes(null);
	}

	private void processBlockEnteredEvent(final BlockEnteredEvent event) {

		logger.info("processBlockEnteredEvent() Block ID: " + event.getBlock().getId());

		final Locomotive locomotive = event.getLocomotive();
		final Block enteredBlock = event.getBlock();
		final Route route = locomotive.getRoute();

		logger.trace("Locomotive: " + locomotive + " EnteredBlock: " + enteredBlock + " Route: " + route);

		if (locomotive != null && enteredBlock != null) {

			final RailNode blockRailNode = enteredBlock.getNodes().get(0);

			logger.info("Putting locomotive onto RailNode: " + blockRailNode + " Locomotive: " + locomotive);
			locomotive.setRailNode(blockRailNode);

			if (!configurationService.getConfigurationAsBoolean(ConfigurationConstants.TIMED_DRIVING_THREAD_ACTIVE)) {

				// CONFLICT: conflicts random driving controller
				final GraphNode positionalGraphNode = route.findGraphNode(blockRailNode);
				
				logger.trace("Putting locomotive onto GraphNode: " + positionalGraphNode);
				locomotive.setGraphNode(positionalGraphNode);
			}

			if (route != null) {

				// disable the rest of the method if the timed driving thread is used to
				// simulate a layout
				if (!configurationService
						.getConfigurationAsBoolean(ConfigurationConstants.TIMED_DRIVING_THREAD_ACTIVE)) {

					final RailNode railNode = enteredBlock.getNodes().get(0);

					// CONFLICT: conflicts random driving controller
					final GraphNode graphNode = route.findGraphNode(railNode);

					if (graphNode == null) {
						
						logger.error("GraphNode is null because GraphNode " + railNode + " cannot be found in route!");

					} else {

						logger.trace("Assuming GraphNode ID: " + graphNode.getId());
						logger.trace("GraphNode Direction: " + graphNode.getDirection());

						// locomotive.isDirection() == true means forward
						final Direction dir = locomotive.isDirection() ? graphNode.getDirection()
								: graphNode.getInverseDirection();

						logger.trace("Assuming Direction: " + dir);

						locomotive.setOrientation(dir);

					}

				}

				// if route did finish, stop the locomotive
				if (route.endsWith(enteredBlock)) {

					logger.info("Locomotive is on the last block of it's route!");
					logger.info("Locomotive: " + locomotive);

					// free the last section
					freeRouteExceptUpToBlock(enteredBlock, locomotive);

					final RouteFinishedEvent routeFinishedEvent = new RouteFinishedEvent(this, route, locomotive);
					applicationEventPublisher.publishEvent(routeFinishedEvent);

					return;
				}
			}
		}

		// free the last section
		freeRouteExceptUpToBlock(enteredBlock, locomotive);

		proceedToNextRouteSection(locomotive);
	}

	private void processRouteAddedEvent(final RouteAddedEvent event) {

		logger.trace("processRouteAddedEvent()");

		final Route route = event.getRoute();
		final Locomotive locomotive = event.getLocomotive();

		logger.trace(route);

		proceedToNextRouteSection(locomotive);
	}

	private void proceedToNextRouteSection(final Locomotive locomotive) {

		logger.info("proceedToNextRouteSection()");

		// try to reserve the next section + switch turnouts
		final Block nextBlock = reserveUpToIncludingNextBlock(locomotive);
		if (nextBlock == null) {

			logger.trace("Could not reserve up to next block");

			locomotiveStop(locomotive);

			return;
		}

		logger.trace("nextBlock ID: " + nextBlock.getId());

		final Route route = locomotive.getRoute();

		// DEBUG
		logger.trace(route);

		final double speed = determineSpeed(locomotive, nextBlock, route);

		locomotiveGo(locomotive, speed);
	}

	private double determineSpeed(final Locomotive locomotive, final Block nextBlock, final Route route) {

		final double drivingSpeedAbsolute = configurationService
				.getConfigurationAsDouble(ConfigurationConstants.DRIVING_SPEED_ABSOLUTE);
		final double drivingSpeedSlowPercentage = configurationService
				.getConfigurationAsDouble(ConfigurationConstants.DRIVING_SPEED_SLOW_PERCENTAGE);

		double speed = drivingSpeedAbsolute;

		// see class documentation for the motivation of this part
		if (route.endsWith(nextBlock)) {

			logger.trace("EndBlock found ID: " + nextBlock.getId());
			speed = drivingSpeedAbsolute / 100.0d * drivingSpeedSlowPercentage;

		} else {

			// find next block successor
			final Block successorBlock = locomotive.getRoute().findSuccessorBlock(nextBlock);
			if (successorBlock != null) {

				logger.trace("Found successor block ID: " + successorBlock.getId());

				if (successorBlock.isFeedbackBlockUsed()) {

					logger.trace("Blocked route ahead detected!");

					// slow down locomotive because a blocked node was detected
					logger.trace("Slowing down locomotive ...");
					speed = drivingSpeedAbsolute / 100.0d * drivingSpeedSlowPercentage;
				}
			}
		}
		return speed;
	}

	@Override
	public void locomotiveGo(final Locomotive locomotive, final double speed) {

		logger.info(">>>>>>>>>> GO Locomotive GO! locomotive ID: " + locomotive.getId() + " Speed: " + speed);

		final Direction locomotiveOrientation = locomotive.getOrientation();
		final GraphNode graphNode = locomotive.getGraphNode();

		if (graphNode == null) {

			logger.info("no graph node! Cannot make locomotive go!");
			return;
		}

		logger.trace("locomotive.getGraphNode() GN-ID: " + graphNode.getId());

		final Direction graphNodeExitDirection = graphNode.getExitDirection();

		logger.trace("locomotiveOrientation: " + locomotiveOrientation + " graphNodeExitDirection: "
				+ graphNodeExitDirection);

		final boolean forward = !Direction.isInverseDirection(locomotiveOrientation, graphNodeExitDirection);

		final short address = locomotive.getAddress();

		// @formatter:off

		logger.trace("Locomotive GO - Locomotive: " + locomotive);
		if (graphNodeExitDirection != null) {
			logger.trace("Locomotive GO - GN: " + graphNode.getId() + " graphNodeExitDirection: " + graphNodeExitDirection.name());
		}
		if (locomotiveOrientation != null) {
			logger.trace("Locomotive GO - GN: " + graphNode.getId() + " locomotiveOrientation: " + locomotiveOrientation.name());
		}
		logger.trace("Locomotive GO - forward: " + forward);
		logger.trace("Locomotive GO - Address: " + address);

		// @formatter:on

		locomotive.setDirection(forward);
		locomotive.start(speed);
	}

	/**
	 * Calls stop() on the locomotive
	 */
	@Override
	public void locomotiveStop(final Locomotive locomotive) {

		// DEBUG
		logger.trace("<<<<<<<<<<< STOP Locomotive STOP!");
		final short address = locomotive.getAddress();
		logger.trace("Locomotive STOP Address: " + address);

		locomotive.stop();
	}

	/**
	 * Free all nodes on the route up to block
	 *
	 * @param block      free all nodes up to this block
	 * @param locomotive
	 */
	private void freeRouteExceptUpToBlock(final Block block, final Locomotive locomotive) {

		final Route route = locomotive.getRoute();

		// DEBUG
		logger.trace("freeRouteExceptUpToBlock() blockID: " + block.getId() + " locomotive: " + locomotive + " Route: "
				+ route);

		final List<GraphNode> deleteList = new ArrayList<>();

		// go through the entire route.
		// If a RailNode and/or a Block is reserved for this locomotive, free it
		for (final GraphNode graphNode : route.getGraphNodes()) {

			final RailNode railNode = graphNode.getRailNode();

			// DEBUG
			logger.trace("Freeing GraphNode: " + graphNode + " RailNode: ID: " + railNode.getId() + " Reserved: "
					+ railNode.isReserved() + " ReservedLocomotiveID: " + railNode.getReservedLocomotiveId());

			final Block nodeBlock = railNode.getBlock();

			// arrived at the block, stop the for loop because the relevant subset of the
			// route was processed
			if (nodeBlock != null && nodeBlock.equals(block)) {

				// remove the highlight
				nodeBlock.getNodes().stream().filter(node -> node.isReserved()).forEach(node -> {

					railNode.setHighlighted(false);

					// send model changed event
					modelFacade.sendModelChangedEvent(railNode);

				});

				logger.trace("Done!");

				break;

			} else {

				// remove this node from the route
				deleteList.add(graphNode);

				if (railNode.isReserved()) {

					if (railNode.getReservedLocomotiveId() == locomotive.getId()) {

						// either free a single node or if the node is part of a block, free the entire
						// block
						final Block railNodeBlock = railNode.getBlock();

						logger.trace("RailNode ID: " + railNode.getId() + " Block: " + railNodeBlock);

						// if the node is not part of a block, only free the node
						if (railNodeBlock == null) {

							logger.trace("FREE RAILNODE ID: " + railNode.getId() + " ReservedLocomotiveID: "
									+ railNode.getReservedLocomotiveId());

							// freeNode(railNode);
							railNode.free();

						} else {

							// if the node is attached to a block, free the entire block
							railNodeBlock.free();

						}
					}
				}
			}
		}

		logger.trace("deleteList.size(): " + deleteList.size());

		// from the start of the route remove all nodes that the train did pass by
		if (CollectionUtils.isNotEmpty(deleteList)) {

			for (final GraphNode graphNode : deleteList) {

				final GraphNode firstGraphNode = route.getGraphNodes().get(0);

				if (firstGraphNode.getId() != graphNode.getId()) {
					throw new IllegalArgumentException("Removing wrong node!");
				}

				logger.trace("Removing node GN.ID: " + firstGraphNode.getId() + " RN.ID: "
						+ firstGraphNode.getRailNode().getId() + " from the route!");

				route.getGraphNodes().remove(0);
			}
		}

		continueAllRoutes(locomotive);
	}

	private void continueAllRoutes(final Locomotive excludedLocomotive) {
		
		logger.info("continueAllRoutes()");

		final double drivingSpeedAbsolute = configurationService
				.getConfigurationAsDouble(ConfigurationConstants.DRIVING_SPEED_ABSOLUTE);
		final double drivingSpeedSlowPercentage = configurationService
				.getConfigurationAsDouble(ConfigurationConstants.DRIVING_SPEED_SLOW_PERCENTAGE);

		logger.trace("Continuing all routes ... excludedLocomotive = " + excludedLocomotive);

		final List<Locomotive> locomotives = modelFacade.getLocomotives();

		logger.trace("All locomotives: " + locomotives);

		// tell all other locomotives to recompute their routes
		for (final Locomotive locomotive : locomotives) {

			logger.trace("Trying locomotive: " + locomotive);

			// skip the current locomotive
			if (locomotive == excludedLocomotive) {

				logger.trace("Locomotive: " + locomotive + " is excluded!");
				continue;
			}

			if (locomotive.getRoute() == null) {

				logger.trace("Locomotive: " + locomotive + " has no route!");
				continue;
			}

			logger.trace("Resuming locomotive: " + locomotive);

			// make this locomotive resume its route == reserve it's path and start it
			final Block nextBlock = reserveUpToIncludingNextBlock(locomotive);
			if (nextBlock == null) {

				logger.trace("Stopping locomotive ...");
				locomotiveStop(locomotive);

			} else {

				logger.trace("Continuing locomotive to next block ...");

				// a locomotive has to check if the block that was just released is part of
				// it's current route before continuing to that block. If the block is not
				// free, the locomotive is not allowed to drive to the block because this
				// results in a crash
				//
				// The code below waits for x seconds then checks the block again. If the block
				// is still free after the wait, the locomotive continues.
				//
				// This also fixes errors where blocks are signaled free for a split second by a
				// random event

				// wait in a thread
				// http://tutorials.jenkov.com/java-concurrency/creating-and-starting-threads.html
				final Runnable runnable = () -> {

					try {

						final long millis = 5000;

						// sleep for 5 seconds, then check if the block is still empty
						logger.trace("Sleeping for " + millis + " milli seconds ...");
						Thread.sleep(millis);
						logger.trace("Sleeping done!");

						logger.trace("Next Block: " + nextBlock);

						// if, after waiting, the block is still free, continue route
						if (!nextBlock.isFeedbackBlockUsed()) {

							logger.trace("Continuing locomotive!");

							// continue locomotive
							double speed = drivingSpeedAbsolute;
							if (locomotive.getRoute() != null && locomotive.getRoute().endsWith(nextBlock)) {

								logger.trace("Reducing speed ...");
								speed = drivingSpeedAbsolute / 100.0d * drivingSpeedSlowPercentage;
							}
							locomotiveGo(locomotive, speed);
						}

					} catch (final InterruptedException e) {
						logger.error(e.getMessage(), e);
					}

				};

				final Thread thread = new Thread(runnable);
				thread.start();

			}
		}
	}

	/**
	 * Processes a subsection of a route.<br />
	 * <br />
	 * <ul>
	 * <li />Reserve nodes on the subsection of the route.
	 * <li />Switch turnouts on the subsection of the route.
	 * </ul>
	 *
	 * @param locomotive
	 * @return
	 */
	private Block reserveUpToIncludingNextBlock(final Locomotive locomotive) {

		logger.trace("reserveUpToIncludingNextBlock()");

		final Route route = locomotive.getRoute();
		if (route == null) {
			return null;
		}

		// find the next block on the route
		final Block nextBlock = findNextBlock(locomotive);
		if (nextBlock == null) {

			logger.info("No next node!");
			return null;
		}

		logger.trace("Next Block.ID: " + nextBlock.getId() + " locomotiveID: " + locomotive.getId());

		// check if the nodes are free
		if (!checkNodes(locomotive, nextBlock)) {

			logger.trace("Nodes are NOT free! BlockID: " + nextBlock.getId());
			return null;
		}

		logger.trace("Nodes are free! BlockID: " + nextBlock.getId());

		// reserve the nodes including the nodes in nextBlock
		reserveNodes(locomotive, nextBlock);

		// switch the turnouts on this section of the route
		final Block currentBlock = findCurrentBlock(route, locomotive);
		switchTurnouts(locomotive, currentBlock, nextBlock);

		return nextBlock;
	}

	private void switchTurnouts(final Locomotive locomotive, final Block currentBlock, final Block nextBlock) {

		logger.trace("Switch turnouts up to Block.ID: " + nextBlock.getId());

		final Route route = locomotive.getRoute();
		final RailNode railNode = nextBlock.getNodes().get(0);

		final List<GraphNode> subList = route.getSubListUpToRailNode(railNode);

		// DEBUG: output the sublist
		for (final GraphNode graphNode : subList) {

			logger.trace("SubList RailNode.ID: " + graphNode.getRailNode().getId());
		}

		logger.trace("SwitchTurnouts sublist ...");
		Route.switchTurnouts(subList, applicationEventPublisher, protocolFacade);
	}

	/**
	 * Given a locomotive (and a Route) and a endBlock, reserves all graphnodes on
	 * the route from the current graphnode to including the endblock.
	 *
	 * @param locomotive
	 * @param endOfReservationBlock
	 */
	private void reserveNodes(final Locomotive locomotive, final Block endOfReservationBlock) {

		// once, the function knows, all blocks are free, reserve all blocks
		for (final GraphNode graphNode : locomotive.getRoute()
				.getSubListStartingFromRailNode(locomotive.getRailNode())) {

			final RailNode railNode = graphNode.getRailNode();
			final Block nodeBlock = railNode.getBlock();

			// walk until the next block is reached
			if (nodeBlock != null && nodeBlock.equals(endOfReservationBlock)) {
				break;
			}

			// reserve this node for the locomotive
			railNode.setReserved(true);
			railNode.setReservedLocomotiveId(locomotive.getId());

			// draw the blocked not in the blocked color in the ui
			logger.trace("Drawing the node in the blocked color in the UI!");
			modelFacade.sendModelChangedEvent(railNode);
		}

		// reserve the nextBlock for the locomotive
		endOfReservationBlock.reserveForLocomotive(locomotive);
	}

	/**
	 * Check if all nodes from the current block to and including the next block are
	 * free.
	 *
	 * @param locomotive
	 * @param nextBlock
	 * @return
	 */
	private boolean checkNodes(final Locomotive locomotive, final Block nextBlock) {

		// @formatter:off

		final boolean trace = false;
		if (trace) {
			logger.info("Locomotive-ID: " + locomotive.getId());
			logger.info("Block-ID: " + nextBlock.getId());
			logger.info("Block-Reserved: " + nextBlock.isReserved());
			logger.info("Block-ReservedForLocomotive: " + nextBlock.getReservedForLocomotive());
			logger.info("Block-isFeedbackBlockUsed: " + nextBlock.isFeedbackBlockUsed());
		}

		if (
				(
					nextBlock.isReserved() && nextBlock.getReservedForLocomotive() != locomotive.getId()
				)
				||
				nextBlock.isFeedbackBlockUsed()
		)
		{

			logger.trace("BlockID: " + nextBlock.getId() + " is reserved already for ID: "
					+ nextBlock.getReservedForLocomotive() + " used by some object!");

			return false;
		}

		// @formatter:on

		// check if all RailNodes are free!
		for (final GraphNode graphNode : locomotive.getRoute()
				.getSubListStartingFromRailNode(locomotive.getRailNode())) {

			final Block nodeBlock = graphNode.getRailNode().getBlock();

			// walk until the next block is reached
			if (nodeBlock != null && nodeBlock.equals(nextBlock)) {
				break;
			}

			// is this rail node reserved for another locomotive
			if (graphNode.getRailNode().isReservedExcluding(locomotive.getId())) {
				return false;
			}
		}

		return true;
	}

	private Block findCurrentBlock(final Route route, final Locomotive locomotive) {

		if (locomotive.getRailNode() == null) {
			return null;
		}

		return locomotive.getRailNode().getBlock();
	}

	private Block findNextBlock(final Locomotive locomotive) {

		logger.trace("Find next Block for locomotive ID: " + locomotive.getId());

		final Route route = locomotive.getRoute();
		final Block currentBlock = findCurrentBlock(route, locomotive);

		logger.trace("currentBlock = " + currentBlock);

		if (locomotive.getRailNode() == null) {

			logger.info("locomotive has no RailNode!");
			return null;
		}

		// start from the RailNode the locomotive currently is on
		for (final GraphNode graphNode : route.getSubListStartingFromRailNode(locomotive.getRailNode())) {

			final Block nodeBlock = graphNode.getRailNode().getBlock();

			// if the RailNode has a block, and the block is not the current block
			// the next block was found
			if (nodeBlock != null && (currentBlock == null || nodeBlock.getId() != currentBlock.getId())) {

				return nodeBlock;
			}
		}

		return null;
	}

	@Override
	public void locomotiveStopAll() {

		final List<Locomotive> locomotives = modelFacade.getLocomotives();

		if (CollectionUtils.isEmpty(locomotives)) {
			return;
		}

		for (final Locomotive locomotive : locomotives) {

			locomotive.immediateStop();
		}
	}

	@Override
	public void locomotiveStartAll() {
		
		logger.info("locomotiveStartAll()");
		
		final List<Locomotive> locomotives = modelFacade.getLocomotives();

		if (CollectionUtils.isEmpty(locomotives)) {
			return;
		}
		
		final double drivingSpeedAbsolute = configurationService
				.getConfigurationAsDouble(ConfigurationConstants.DRIVING_SPEED_ABSOLUTE);

		for (final Locomotive locomotive : locomotives) {
			
			locomotive.setStopped(false);
			
			// do not start locomotives that have no route
			if (RouteUtils.isEmpty(locomotive.getRoute())) {
				continue;
			}
			
			locomotive.start(drivingSpeedAbsolute);
			
		}
	}

}
