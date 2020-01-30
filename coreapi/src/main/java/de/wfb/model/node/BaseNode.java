package de.wfb.model.node;

import java.util.ArrayList;
import java.util.List;

import de.wfb.model.Model;
import de.wfb.rail.ui.ShapeType;

public abstract class BaseNode implements Node {

	private int id;

	private int x;

	private int y;

	private ShapeType shapeType;

	/**
	 * Is this rail going from left to right (Horizontal). This is important when
	 * automatically connecting nodes.
	 */
	private Boolean horizontal;

	private final List<Node> leftList = new ArrayList<>();

	private final List<Node> rightList = new ArrayList<>();

	/**
	 * I think some of the turnouts are hooked up incorrectly and the software has
	 * to reverse their state to account for the inverted cabeling so that the
	 * virtual layout shows the state that the turnout actually has on the real
	 * layout.
	 */
	private Boolean flipped;

	@Override
	public abstract void connect(final Model model);

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
		if (id != other.getId())
			return false;
		return true;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(final int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(final int y) {
		this.y = y;
	}

	@Override
	public ShapeType getShapeType() {
		return shapeType;
	}

	@Override
	public void setShapeType(final ShapeType shapeType) {
		this.shapeType = shapeType;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
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
	public Boolean isFlipped() {
		return flipped;
	}

	@Override
	public void setFlipped(final Boolean flipped) {
		this.flipped = flipped;
//		this.flipped = null;
//		if (flipped == true) {
//			this.flipped = flipped;
//		}
	}

}
