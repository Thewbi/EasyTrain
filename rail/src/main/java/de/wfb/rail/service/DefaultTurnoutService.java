package de.wfb.rail.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.node.RailNode;
import de.wfb.model.service.ModelService;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.ui.ShapeType;

public class DefaultTurnoutService implements TurnoutService {

	private static final Logger logger = LogManager.getLogger(DefaultTurnoutService.class);

	private final BlockingQueue<RailNode> turnoutStatusRequestQueue = new ArrayBlockingQueue<>(128);

	@Autowired
	private ProtocolFacade protocolFacade;

	@Autowired
	private ModelService modelService;

	@Override
	public void queueStateRequest(final RailNode node) {

		turnoutStatusRequestQueue.add(node);
	}

	@Override
	public void startQueryingFromQueue() {

		logger.info("startQueryingFromQueue()");

		if (turnoutStatusRequestQueue.isEmpty()) {
			return;
		}

		final List<RailNode> deleteList = new ArrayList<RailNode>();

		for (final RailNode node : turnoutStatusRequestQueue) {

			// make sure this is a turnout
			if (!ShapeType.isTurnout(node.getShapeType())) {
				continue;
			}

			logger.info("startQueryingFromQueue() Querying Turnout Node Status of RailNode ID: " + node.getId());

			// request and set the turnout status
			final boolean isThrown = protocolFacade.turnoutStatus(node.getProtocolTurnoutId().shortValue());

			node.setThrown(isThrown);

			logger.info("ProtocolTurnoutID: " + node.getProtocolTurnoutId() + " is "
					+ (node.isThrown() ? "THROWN" : "CLOSED"));

			// Or not? update the node in the model, send model changed event so that the ui
			// changes.
			// This method is called before the UI initialize for the first time so maybe
			// the UI already draws the turnouts correctly initially.

			deleteList.add(node);
		}

		turnoutStatusRequestQueue.removeAll(deleteList);
	}

	@Override
	public void createTurnoutGroups() {
		// nop
	}

	@SuppressWarnings("unused")
	private void buildTurnoutGroup(final int address) {

		final List<RailNode> railNodes = modelService.getTurnoutsByAddress(address);
		for (final RailNode railNode : railNodes) {
			railNode.getTurnoutGroup().addAll(railNodes);
		}
	}

}
