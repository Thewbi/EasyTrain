package de.wfb.model.locomotive;

import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.facade.ModelFacade;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.factory.Factory;

public class DefaultLocomotiveFactory implements Factory<Locomotive> {

	@Autowired
	private ProtocolFacade protocolFacade;

	@Autowired
	private ModelFacade modelFacade;

	@Override
	public Locomotive create(final Object... args) throws Exception {

		final short address = (short) args[0];
		final boolean direction = (boolean) args[1];
		final String name = (String) args[2];
		final double speed = (double) args[3];

		// add the locomotive to the model
		final int locomotiveId = modelFacade.retrieveNextLocomotiveId();

		final DefaultLocomotive locomotive = new DefaultLocomotive();
		locomotive.setId(locomotiveId);
		locomotive.setAddress(address);
		locomotive.setDirection(direction);
		locomotive.setName(name);
		locomotive.setSpeed(speed);
		locomotive.setProtocolFacade(protocolFacade);

		return locomotive;
	}

}
