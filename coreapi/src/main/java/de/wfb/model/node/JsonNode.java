package de.wfb.model.node;

import java.util.ArrayList;
import java.util.List;

public class JsonNode {

	private int id;

	private String shapeType;

	private int x;

	private int y;

	private Integer protocolTurnoutId;

	private final List<Integer> manualConnections = new ArrayList<>();

	private int feedbackBlockNumber;

	/** Null means traversable in all directions */
	private Direction traverse = null;

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

	public List<Integer> getManualConnections() {
		return manualConnections;
	}

	public int getFeedbackBlockNumber() {
		return feedbackBlockNumber;
	}

	public void setFeedbackBlockNumber(final int feedbackBlockNumber) {
		this.feedbackBlockNumber = feedbackBlockNumber;
	}

	public Direction getTraverse() {
		return traverse;
	}

	public void setTraverse(final Direction traverse) {
		this.traverse = traverse;
	}

}
