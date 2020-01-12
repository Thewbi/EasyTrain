package de.wfb.model.facade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.locomotive.DefaultLocomotive;
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

		for (final RailNode railNode : block.getNodes()) {

			logger.info("RailNode: ID " + railNode.getId() + " Reserved: " + railNode.isReserved()
					+ " ReservedLocomotiveID: " + railNode.getReservedLocomotiveId());
		}

		for (final DefaultLocomotive locomotive : modelService.getLocomotives()) {

			if (locomotive.getRoute() == null) {
				continue;
			}

			final Route route = locomotive.getRoute();

			logger.info("RRoute: " + route);

			final boolean reservesBlock = route.reservesBlock(block);

			logger.info("BBlock reserves RRoute: " + reservesBlock);

			if (reservesBlock) {
				return route;
			}
		}

		return null;
	}
}
