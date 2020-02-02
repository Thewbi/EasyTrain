package de.wfb.model.node;

import de.wfb.model.Model;
import de.wfb.rail.ui.ShapeType;

public interface Node {

	int getId();

	void setId(int id);

	int getX();

	void setX(int x);

	int getY();

	void setY(int y);

	ShapeType getShapeType();

	void setShapeType(ShapeType shapeType);

	boolean isThrown();

	void setThrown(boolean thrown);

	Integer getProtocolTurnoutId();

	void setProtocolTurnoutId(Integer protocolTurnoutId);

	Integer getFeedbackBlockNumber();

	void setFeedbackBlockNumber(Integer feedbackBlockNumber);

	boolean isFeedbackBlockUsed();

	void setFeedbackBlockUsed(boolean feedbackBlockUsed);

	boolean isSelected();

	void setSelected(boolean selected);

	boolean isHighlighted();

	void setHighlighted(boolean highlighted);

	void toggleTurnout();

	void connect(Model model);

	void disconnect(Model model);

	GraphNode getGraphNodeOne();

	GraphNode getGraphNodeTwo();

	boolean isReserved();

	void setReserved(boolean reserved);

	int getReservedLocomotiveId();

	void setReservedLocomotiveId(int reservedLocomotiveId);

	Direction getTraverse();

	void setTraverse(Direction traverse);

	Boolean isFlipped();

	void setFlipped(Boolean flipped);

}
