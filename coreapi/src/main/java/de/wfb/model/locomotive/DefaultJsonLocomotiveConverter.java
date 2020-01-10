package de.wfb.model.locomotive;

import de.wfb.rail.converter.Converter;

public class DefaultJsonLocomotiveConverter implements Converter<DefaultLocomotiveJson, DefaultLocomotive> {

	@Override
	public void convert(final DefaultLocomotiveJson source, final DefaultLocomotive target) {

		target.setAddress(source.getAddress());
		target.setName(source.getName());
	}

}
