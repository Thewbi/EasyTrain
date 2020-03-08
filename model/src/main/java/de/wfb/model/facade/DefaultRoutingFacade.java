package de.wfb.model.facade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.ModelService;
import de.wfb.rail.service.Block;
import de.wfb.rail.service.Route;

public class DefaultRoutingFacade implements RoutingFacade {

	private static final Logger logger = LogManager.getLogger(DefaultRoutingFacade.class);

	@Autowired
	private ModelService modelService;

	@Override
	public Route getRouteByBlock(final Block block) {

		if (block == null) {
			return null;
		}

		for (final RailNode railNode : block.getNodes()) {

			logger.trace("RailNode: ID " + railNode.getId() + " Reserved: " + railNode.isReserved()
					+ " ReservedLocomotiveID: " + railNode.getReservedLocomotiveId());
		}

		for (final Locomotive locomotive : modelService.getLocomotives()) {

			if (locomotive.getRoute() == null) {
				continue;
			}

			final Route route = locomotive.getRoute();

			logger.trace("Route: " + route);

			final boolean reservesBlock = route.reservesBlock(block);

			logger.trace("Block reserves RRoute: " + reservesBlock);

			if (reservesBlock) {
				return route;
			}
		}

		return null;
	}
}
