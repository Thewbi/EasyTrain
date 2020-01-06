package de.wfb.model;

import java.util.HashMap;
import java.util.Map;

import de.wfb.model.node.Node;

public class DefaultModel implements Model {

	private final int columns = 100;

	private final int rows = 100;

	private final Map<Integer, Node> idMap = new HashMap<>();

	private final Node[][] viewModel = new Node[rows][columns];

	private Node selectedNode;

	@Override
	public Node getNode(final int x, final int y) {
		if (0 <= x && x < columns && 0 <= y && y < columns) {
			return viewModel[x][y];
		}
		return null;
	}

	@Override
	public void setNode(final int x, final int y, final Node node) {
		if (0 <= x && x < columns && 0 <= y && y < columns) {
			viewModel[x][y] = node;
		}
	}

	@Override
	public void removeNode(final int x, final int y) {
		final Node node = viewModel[x][y];
		if (node == null) {
			return;
		}
		if (idMap.containsKey(node.getId())) {
			idMap.remove(node.getId());
		}
		viewModel[x][y] = null;
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

}
