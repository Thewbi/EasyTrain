package de.wfb.model.node;

import java.util.ArrayList;
import java.util.List;

import de.wfb.rail.ui.ShapeType;

public class Node {

	private int id;

	protected int x;

	protected int y;

	protected ShapeType shapeType;

	/**
	 * Is this rail going from left to right (Horizontal). This is important when
	 * automatically connecting nodes.
	 */
	private Boolean horizontal;

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

	public Boolean isHorizontal() {
		return horizontal;
	}

	public void setHorizontal(final Boolean horizontal) {
		this.horizontal = horizontal;
	}

	@Override
	public String toString() {
		return "Node( id:" + id + " " + x + ", " + y + ", " + shapeType + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Node other = (Node) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
