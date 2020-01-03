package de.wfb.model.converter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.node.JsonNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.TurnoutNode;
import de.wfb.rail.converter.Converter;

public class DefaultJsonNodeConverter implements Converter<Node, JsonNode> {

	private static final Logger logger = LogManager.getLogger(DefaultJsonNodeConverter.class);

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

		convertTurnoutNode(source, target);
	}

	private void convertTurnoutNode(final Node source, final JsonNode target) {

		final boolean isTurnoutNode = source instanceof TurnoutNode;
		if (!isTurnoutNode) {
			return;
		}

		final TurnoutNode turnoutNode = (TurnoutNode) source;

		target.setProtocolTurnoutId(turnoutNode.getProtocolTurnoutId());
	}

}
