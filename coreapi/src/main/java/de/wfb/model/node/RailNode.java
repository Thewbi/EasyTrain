package de.wfb.model.node;

import java.util.List;

import de.wfb.model.Model;
import de.wfb.rail.ui.ShapeType;

public interface RailNode extends Node {

	@Override
	int getId();

	@Override
	void setId(int id);

	@Override
	int getX();

	@Override
	void setX(int x);

	@Override
	int getY();

	@Override
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

	@Override
	ShapeType getShapeType();

	@Override
	void setShapeType(ShapeType shapeType);

	void setGraphNodeOne(GraphNode graphNodeOut);

	@Override
	GraphNode getGraphNodeOne();

	void setGraphNodeTwo(GraphNode graphNodeOut);

	@Override
	GraphNode getGraphNodeTwo();

	@Override
	Integer getProtocolTurnoutId();

	@Override
	void setProtocolTurnoutId(Integer protocolTurnoutId);

	List<RailNode> getManualConnections();

	@Override
	void connect(Model model);

	void connectTo(RailNode railNode);

}