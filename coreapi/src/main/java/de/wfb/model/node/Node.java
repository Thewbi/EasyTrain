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

	void toggleTurnout();

	void connect(Model model);

	void disconnect(Model model);

	GraphNode getGraphNodeOne();

	GraphNode getGraphNodeTwo();

}
