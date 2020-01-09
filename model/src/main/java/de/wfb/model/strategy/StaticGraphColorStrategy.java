package de.wfb.model.strategy;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.node.Color;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.ModelService;

/**
 * Paints every node green
 */
public class StaticGraphColorStrategy implements GraphColorStrategy {

	@Autowired
	private ModelService modelService;

	@Override
	public void execute() {

		modelService.resetGraphColors();

		final List<RailNode> allRailNodes = modelService.getAllRailNodes();
		if (CollectionUtils.isEmpty(allRailNodes)) {
			return;
		}

		for (final RailNode railNode : allRailNodes) {

			final GraphNode graphNodeOne = railNode.getGraphNodeOne();
			if (graphNodeOne != null) {
				graphNodeOne.setColor(Color.GREEN);
			}

			final GraphNode graphNodeTwo = railNode.getGraphNodeTwo();
			if (graphNodeTwo != null) {
				graphNodeTwo.setColor(Color.GREEN);
			}
		}

	}

}
