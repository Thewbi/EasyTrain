package de.wfb.model.node;

import java.util.ArrayList;
import java.util.List;

import de.wfb.rail.ui.ShapeType;

public class Node {

	private int id;

	private int x;

	private int y;

	private ShapeType shapeType;

	private final List<Node> leftList = new ArrayList<>();

	private final List<Node> rightList = new ArrayList<>();

	public int getX() {
		return x;
	}

	public void setX(final int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(final int y) {
		this.y = y;
	}

	public ShapeType getShapeType() {
		return shapeType;
	}

	public void setShapeType(final ShapeType shapeType) {
		this.shapeType = shapeType;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public List<Node> getLeftList() {
		return leftList;
	}

	public List<Node> getRightList() {
		return rightList;
	}

}
