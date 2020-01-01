package de.wfb.model.converter;

import org.apache.commons.collections4.CollectionUtils;

import de.wfb.model.node.JsonNode;
import de.wfb.model.node.Node;
import de.wfb.rail.converter.Converter;

public class DefaultJsonNodeConverter implements Converter<Node, JsonNode> {

	@Override
	public void convert(final Node source, final JsonNode target) {

		target.setId(source.getId());
		target.setShapeType(source.getShapeType().name());
		target.setX(source.getX());
		target.setY(source.getY());

		if (CollectionUtils.isNotEmpty(source.getLeftList())) {

			for (final Node node : source.getLeftList()) {
				target.getLeftList().add(node.getId());
			}
		}

		if (CollectionUtils.isNotEmpty(source.getRightList())) {

			for (final Node node : source.getRightList()) {
				target.getRightList().add(node.getId());
			}
		}
	}

}
