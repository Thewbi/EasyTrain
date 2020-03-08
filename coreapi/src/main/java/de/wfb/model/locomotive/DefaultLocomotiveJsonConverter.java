package de.wfb.model.locomotive;

import de.wfb.rail.converter.Converter;

public class DefaultLocomotiveJsonConverter implements Converter<Locomotive, DefaultLocomotiveJson> {

	@Override
	public void convert(final Locomotive source, final DefaultLocomotiveJson target) {
		target.setId(source.getId());
		target.setAddress(source.getAddress());
		target.setName(source.getName());
		target.setImageFilename(source.getImageFilename());
	}

	@Override
	public DefaultLocomotiveJson convert(final Locomotive source) {
		throw new RuntimeException("Not implemented yet!");
	}

}
