package de.wfb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;

public class DefaultModel implements Model {

	private final int columns = 100;

	private final int rows = 100;

	private final Map<Integer, Node> idMap = new HashMap<>();

	private final Node[][] nodeGrid = new Node[rows][columns];

	private final List<DefaultLocomotive> locomotives = new ArrayList<DefaultLocomotive>();

	private Node selectedNode;

	@Override
	public Node getNode(final int x, final int y) {

		if (0 <= x && x < columns && 0 <= y && y < columns) {
			return nodeGrid[x][y];
		}

		return null;
	}

	@Override
	public void setNode(final int x, final int y, final Node node) {

		if (0 <= x && x < columns && 0 <= y && y < columns) {
			nodeGrid[x][y] = node;
		}
	}

	@Override
	public void removeNode(final int x, final int y) {

		final Node node = nodeGrid[x][y];
		if (node == null) {
			return;
		}
		if (idMap.containsKey(node.getId())) {
			idMap.remove(node.getId());
		}
		nodeGrid[x][y] = null;
	}

	@Override
	public void connectModel() {

		for (final Map.Entry<Integer, Node> entry : idMap.entrySet()) {
			entry.getValue().connect(this);
		}
	}

	@Override
	public List<GraphNode> getSwitchingNodes() {

		final List<GraphNode> switchingNodes = new ArrayList<>();

		for (final Map.Entry<Integer, Node> entry : idMap.entrySet()) {

			final Node node = entry.getValue();

			final GraphNode graphNodeOne = node.getGraphNodeOne();

			final List<GraphNode> children = graphNodeOne.getChildren();

			if (CollectionUtils.isEmpty(children)) {
				continue;
			}

			if (children.size() > 1) {
				switchingNodes.add(graphNodeOne);
			}
		}

		return switchingNodes;
	}

	@Override
	public List<RailNode> getAllRailNodes() {

		final List<RailNode> result = new ArrayList<>();

		for (final Map.Entry<Integer, Node> entry : idMap.entrySet()) {

			final Node node = entry.getValue();
			result.add((RailNode) node);
		}

		return result;
	}

	public int getColumns() {
		return columns;
	}

	public int getRows() {
		return rows;
	}

	@Override
	public Map<Integer, Node> getIdMap() {
		return idMap;
	}

	@Override
	public Node getSelectedNode() {
		return selectedNode;
	}

	@Override
	public void setSelectedNode(final Node selectedNode) {
		this.selectedNode = selectedNode;
	}

	public List<DefaultLocomotive> getLocomotives() {
		return locomotives;
	}

}
