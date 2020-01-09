package de.wfb.model.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.node.Color;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.model.service.ModelService;

public class DefaultGraphColorStrategy implements GraphColorStrategy {

	private static final Logger logger = LogManager.getLogger(DefaultGraphColorStrategy.class);

	@Autowired
	private ModelService modelService;

	@Override
	public void execute() {

		modelService.resetGraphColors();
		final RailNode railNode = modelService.getArbitraryNode();

		if (railNode != null) {
			walkGraph(railNode.getGraphNodeOne(), Color.GREEN);
			walkGraph(railNode.getGraphNodeTwo(), Color.BLUE);
		}
	}

	private void walkGraph(final GraphNode graphNode, final Color color) {

		final List<GraphNode> workingList = new ArrayList<>();
		final List<GraphNode> visitedList = new ArrayList<>();
		workingList.add(graphNode);

		while (CollectionUtils.isNotEmpty(workingList)) {

			final GraphNode currentGraphNode = workingList.get(0);
			workingList.remove(currentGraphNode);
			visitedList.add(currentGraphNode);

			if (currentGraphNode.getColor() != Color.NONE) {

				final String msg = "GraphNode " + currentGraphNode.getId() + " has a color already! COLOR = "
						+ currentGraphNode.getColor().name();

				logger.warn(msg);

			} else {

				currentGraphNode.setColor(color);

				for (final GraphNode node : currentGraphNode.getChildren()) {

					if (visitedList.contains(node)) {
						continue;
					}
					workingList.add(node);
				}
			}
		}
	}

}
