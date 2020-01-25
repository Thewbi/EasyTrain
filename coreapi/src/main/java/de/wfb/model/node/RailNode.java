package de.wfb.model.node;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.Model;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.service.Block;
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

	Edge getEdge(Direction edgeDirection);

	void setEdge(Direction edgeDirection, Edge edge);

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

	@Override
	boolean isHighlighted();

	@Override
	void setHighlighted(boolean highlighted);

	List<RailNode> getManualConnections();

	@Override
	int getFeedbackBlockNumber();

	@Override
	void connect(Model model);

	void manualConnectTo(RailNode railNode);

	void switchToGraphNode(ApplicationEventPublisher applicationEventPublisher, ProtocolFacade protocolFacade,
			Model model, GraphNode nextGraphNode);

	@Override
	boolean isReserved();

	@Override
	void setReserved(boolean reserved);

	@Override
	int getReservedLocomotiveId();

	@Override
	void setReservedLocomotiveId(int reservedLocomotiveId);

	Block getBlock();

	void setBlock(Block block);

	boolean isReservedExcluding(int id);

	Edge[] getEdges();

}
