package de.wfb.model.locomotive;

import de.wfb.rail.converter.Converter;

public class DefaultJsonLocomotiveConverter implements Converter<DefaultLocomotiveJson, DefaultLocomotive> {

	@Override
	public void convert(final DefaultLocomotiveJson source, final DefaultLocomotive target) {
		target.setId(source.getId());
		target.setAddress(source.getAddress());
		target.setName(source.getName());
	}

	@Override
	public DefaultLocomotive convert(final DefaultLocomotiveJson source) {
		throw new RuntimeException("Not implemented yet!");
	}

}
