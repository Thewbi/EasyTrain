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

	private Map<Integer, Node> idMap = new HashMap<>();

	private Node[][] nodeGrid = new Node[rows][columns];

	private final List<DefaultLocomotive> locomotives = new ArrayList<DefaultLocomotive>();

	private Node selectedNode;

	private String currentModelFile;

	private String currentLocomotiveModelFile;

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

	@Override
	public List<DefaultLocomotive> getLocomotives() {
		return locomotives;
	}

	@Override
	public int retrieveNextLocomotiveId() {

		if (CollectionUtils.isEmpty(locomotives)) {
			return 0;
		}

		int id = 0;

		boolean done = false;
		while (!done) {

			done = true;
			if (findLocomotiveId(id)) {
				done = false;
				id++;
			}
		}

		return id;
	}

	private boolean findLocomotiveId(final int id) {

		for (final DefaultLocomotive locomotive : locomotives) {
			if (locomotive.getId() == id) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void clear() {
		idMap = new HashMap<>();
		nodeGrid = new Node[rows][columns];
		selectedNode = null;
	}

	@Override
	public GraphNode getGraphNodeById(final int id) {

		final List<RailNode> allRailNodes = getAllRailNodes();

		if (org.springframework.util.CollectionUtils.isEmpty(allRailNodes)) {
			return null;
		}

		for (final RailNode railNode : allRailNodes) {

			if (railNode.getGraphNodeOne().getId() == id) {
				return railNode.getGraphNodeOne();
			}
			if (railNode.getGraphNodeTwo().getId() == id) {
				return railNode.getGraphNodeTwo();
			}
		}

		return null;
	}

	@Override
	public String getCurrentModelFile() {
		return currentModelFile;
	}

	@Override
	public void setCurrentModelFile(final String currentModelFile) {
		this.currentModelFile = currentModelFile;
	}

	@Override
	public String getCurrentLocomotiveModelFile() {
		return currentLocomotiveModelFile;
	}

	@Override
	public void setCurrentLocomotiveModelFile(final String currentLocomotiveModelFile) {
		this.currentLocomotiveModelFile = currentLocomotiveModelFile;
	}

}
