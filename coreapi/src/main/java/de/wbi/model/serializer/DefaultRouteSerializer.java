package de.wbi.model.serializer;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.rail.converter.Converter;
import de.wfb.rail.service.Route;

public class DefaultRouteSerializer implements Converter<Route, String> {

	@Override
	public void convert(final Route source, final String target) {
		throw new RuntimeException("Not implemented yet!");
	}

	@Override
	public String convert(final Route route) {

		if (route == null) {
			return null;
		}

		final StringBuilder stringBuilder = new StringBuilder(4096);

		final Locomotive locomotive = route.getLocomotive();
		if (locomotive != null) {
			stringBuilder.append(locomotive.getId());
		}
		stringBuilder.append("|");

		int i = 0;
		for (final GraphNode graphNode : route.getGraphNodes()) {
			stringBuilder.append(graphNode.getId());

			if (i != route.getGraphNodes().size() - 1) {
				stringBuilder.append(",");
			}

			i++;
		}

		return stringBuilder.toString();

	}

}
