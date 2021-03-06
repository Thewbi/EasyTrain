package de.wfb.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.node.DefaultRailNode;
import de.wfb.model.node.JsonNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import de.wfb.rail.converter.Converter;

public class DefaultJsonNodeConverter implements Converter<Node, JsonNode> {

	private static final Logger logger = LogManager.getLogger(DefaultJsonNodeConverter.class);

	@Override
	public void convert(final Node source, final JsonNode target) {

		logger.trace("convert!");

		target.setId(source.getId());
		target.setShapeType(source.getShapeType().name());
		target.setX(source.getX());
		target.setY(source.getY());
		target.setProtocolTurnoutId(source.getProtocolTurnoutId());

		target.setFeedbackBlockNumber(null);
		if (source.getFeedbackBlockNumber() != null && source.getFeedbackBlockNumber() > -1) {
			target.setFeedbackBlockNumber(source.getFeedbackBlockNumber());
		}

		target.setTraverse(source.getTraverse());
		target.setFlipped(source.isFlipped());

		// manual connections
		final DefaultRailNode defaultRailNode = (DefaultRailNode) source;
		final List<RailNode> manualConnections = defaultRailNode.getManualConnections();

		if (CollectionUtils.isNotEmpty(manualConnections)) {

			logger.trace("manualConnections.size(): " + manualConnections.size());
			logger.trace("ManualConnection found!");

			target.setManualConnections(new ArrayList<Integer>());

			for (final RailNode railNode : manualConnections) {

				target.getManualConnections().add(railNode.getId());
			}
		}
	}

	@Override
	public JsonNode convert(final Node source) {
		throw new RuntimeException("Not implemented yet!");
	}

}
