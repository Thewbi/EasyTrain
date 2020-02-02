package de.wfb.model.node;

import java.util.List;

public class JsonNode {

	private int id;

	private String shapeType;

	private int x;

	private int y;

	private Integer protocolTurnoutId;

	private List<Integer> manualConnections;

	private Integer feedbackBlockNumber;

	/** Null means traversable in all directions */
	private Direction traverse = null;

	private Boolean flipped;

	@Override
	public String toString() {
		return "JsonNode [id=" + id + ", shapeType=" + shapeType + ", x=" + x + ", y=" + y + ", protocolTurnoutId="
				+ protocolTurnoutId + ", manualConnections=" + manualConnections + ", feedbackBlockNumber="
				+ feedbackBlockNumber + ", traverse=" + traverse + ", flipped=" + flipped + "]";
	}

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

	public Integer getProtocolTurnoutId() {
		return protocolTurnoutId;
	}

	public void setProtocolTurnoutId(final Integer protocolTurnoutId) {
		this.protocolTurnoutId = protocolTurnoutId;
	}

	public Integer getFeedbackBlockNumber() {
		return feedbackBlockNumber;
	}

	public void setFeedbackBlockNumber(final Integer feedbackBlockNumber) {
		this.feedbackBlockNumber = feedbackBlockNumber;
	}

	public Direction getTraverse() {
		return traverse;
	}

	public void setTraverse(final Direction traverse) {
		this.traverse = traverse;
	}

	public Boolean isFlipped() {
		return flipped;
	}

	public void setFlipped(final Boolean flipped) {
		this.flipped = flipped;
	}

	public void setManualConnections(final List<Integer> manualConnections) {
		this.manualConnections = manualConnections;
	}

	public List<Integer> getManualConnections() {
		return manualConnections;
	}

}
