package de.wfb.model.facade;

import de.wfb.rail.service.Block;
import de.wfb.rail.service.Route;

public interface RoutingFacade {

	Route getRouteByBlock(Block block);

}
