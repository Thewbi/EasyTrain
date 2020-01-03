package de.wfb.model.node;

import java.util.ArrayList;
import java.util.List;

public class JsonNode {

	private int id;

	private String shapeType;

	private int x;

	private int y;

	private final List<Integer> leftList = new ArrayList<>();

	private final List<Integer> rightList = new ArrayList<>();

	private Integer protocolTurnoutId;

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getShapeType() {
		return shapeType;
	}

	public void setShapeType(final String shapeType) {
		this.shapeType = shapeType;
	}

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

	public List<Integer> getLeftList() {
		return leftList;
	}

	public List<Integer> getRightList() {
		return rightList;
	}

	public Integer getProtocolTurnoutId() {
		return protocolTurnoutId;
	}

	public void setProtocolTurnoutId(final Integer protocolTurnoutId) {
		this.protocolTurnoutId = protocolTurnoutId;
	}

}
