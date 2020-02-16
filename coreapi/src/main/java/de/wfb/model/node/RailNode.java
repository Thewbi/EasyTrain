package de.wfb.model.node;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.Model;
import de.wfb.model.facade.ModelFacade;
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

	void setGraphNodeOne(GraphNode graphNode);

	@Override
	GraphNode getGraphNodeOne();

	void setGraphNodeTwo(GraphNode graphNode);

	@Override
	GraphNode getGraphNodeTwo();

	void setGraphNodeThree(GraphNode graphNode);

	@Override
	GraphNode getGraphNodeThree();

	void setGraphNodeFour(GraphNode graphNode);

	@Override
	GraphNode getGraphNodeFour();

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
	Integer getFeedbackBlockNumber();

	@Override
	void connect(Model model);

	void manualConnectTo(RailNode railNode);

	void switchToGraphNode(ApplicationEventPublisher applicationEventPublisher, ProtocolFacade protocolFacade,
			Model model, final GraphNode currentGraphNode, GraphNode nextGraphNode);

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

	/**
	 * If a RailNode is only passable in a specific direction, set the blocked flag
	 * on the responsible graph node.
	 */
	void updateBlockedGraphNode();

	void free();

	void setModelFacade(ModelFacade modelFacade);

	List<RailNode> getTurnoutGroup();

}
