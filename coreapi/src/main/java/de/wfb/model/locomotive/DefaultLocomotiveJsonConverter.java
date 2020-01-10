package de.wfb.model.locomotive;

import de.wfb.rail.converter.Converter;

public class DefaultLocomotiveJsonConverter implements Converter<DefaultLocomotive, DefaultLocomotiveJson> {

	@Override
	public void convert(final DefaultLocomotive source, final DefaultLocomotiveJson target) {

		target.setAddress(source.getAddress());
		target.setName(source.getName());
	}

}
