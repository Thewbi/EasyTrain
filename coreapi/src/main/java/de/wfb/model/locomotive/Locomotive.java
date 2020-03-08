package de.wfb.model.locomotive;

import de.wfb.model.node.Direction;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.rail.service.Route;

public interface Locomotive {

	void start(double throttleValue);

	void stop();

	void immediateStop();

	void setDirection(boolean dirForward);

	boolean isDirection();

	int getId();

	void setId(int id);

	short getAddress();

	void setAddress(short address);

	String getImageFilename();

	void setImageFilename(String imageFilename);

	Route getRoute();

	void setRoute(Route route);

	void setGraphNode(GraphNode startGraphNode);

	GraphNode getGraphNode();

	RailNode getRailNode();

	void setRailNode(RailNode railNode);

	Direction getOrientation();

	void setOrientation(Direction edgeDirection);

	String getName();

	void setName(String name);

}
