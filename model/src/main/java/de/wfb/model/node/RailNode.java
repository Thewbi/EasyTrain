package de.wfb.model.node;

import de.wfb.model.Model;
import de.wfb.rail.ui.ShapeType;

public interface RailNode {

	int getX();

	void setX(int x);

	int getY();

	void setY(int y);

	Edge getNorthEdge();

	void setNorthEdge(Edge edge);

	Edge getEastEdge();

	void setEastEdge(Edge edge);

	Edge getSouthEdge();

	void setSouthEdge(Edge edge);

	Edge getWestEdge();

	void setWestEdge(Edge edge);

	Edge getEdge(EdgeDirection edgeDirection);

	void setEdge(EdgeDirection edgeDirection, Edge edge);

	ShapeType getShapeType();

	void setShapeType(ShapeType shapeType);

	void setGraphNodeOne(GraphNode graphNodeOut);

	GraphNode getGraphNodeOne();

	void setGraphNodeTwo(GraphNode graphNodeOut);

	GraphNode getGraphNodeTwo();

	void connect(Model model);

}
