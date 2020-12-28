package de.wfb.model.locomotive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.node.Direction;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.service.Route;

public class DefaultLocomotive implements Locomotive {

	private static final Logger logger = LogManager.getLogger(DefaultLocomotive.class);

	private static final boolean FORWARD_DEFAULT_VALUE = true;

	private int id;

	/** the protocol address by which the locomotive can be controlled */
	private short address;

	private String name;

	private Direction edgeDirection = Direction.NONE;

	private RailNode railNode;

	private GraphNode graphNode;

	private Route route;

	private double speed;

	private boolean direction = FORWARD_DEFAULT_VALUE;

	private String imageFilename;

	private ProtocolFacade protocolFacade;

	private boolean stopped;

	public DefaultLocomotive() {
		super();
	}

	public DefaultLocomotive(final int id, final String name, final short address, final String imageFilename) {
		super();
		this.id = id;
		this.name = name;
		this.address = address;
		this.imageFilename = imageFilename;
	}

	@Override
	public void start(final double speed) {
		
		logger.info("Locomotive start() speed=" + speed + " stopped=" + stopped);
		
		if (stopped) {
			logger.warn("This locomotive is stopped and will not drive!");
			return;
		}

		// if the locomotive is going at the requested speed already, do not send a command
		if (Math.abs(this.speed - speed) < 0.1d) {
			return;
		}

		this.speed = speed;
		protocolFacade.throttleLocomotive(address, speed, direction);
	}

	@Override
	public void stop() {

		// if the locomotive is not running, do not send a command
		if (speed <= 0.0d) {
			return;
		}

		// if the speed is set to 0.0d and the reverse direction is used,
		// locomotives appruptly stop! This is unrealistic. The direction
		// has to be choosen as the direction the locomotive is currently going!
		// This causes the decoder to apply it's configured speed curved to bring
		// the locomotive to a smooth, realistic halt.
		speed = 0.0d;
		protocolFacade.throttleLocomotive(address, speed, direction);
	}

	@Override
	public void immediateStop() {

		// if the locomotive is not running, do not send a command
		if (speed <= 0.0d) {
			return;
		}

		// if the speed is set to 0.0d and the reverse direction is used,
		// locomotives appruptly stop! This is unrealistic. The direction
		// has to be choosen as the direction the locomotive is currently going!
		// This causes the decoder to apply it's configured speed curved to bring
		// the locomotive to a smooth, realistic halt.
		speed = 0.0d;
		protocolFacade.throttleLocomotive(address, speed, !direction);
		
		// mark the locomotive stopped
		stopped = true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DefaultLocomotive other = (DefaultLocomotive) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Locomotive name:'" + name + "' Direction: '" + getOrientation() + "'";
	}

	@Override
	public short getAddress() {
		return address;
	}

	@Override
	public void setAddress(final short address) {
		this.address = address;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(final int id) {
		this.id = id;
	}

	@Override
	public Direction getOrientation() {
		return edgeDirection;
	}
 
	@Override
	public void setOrientation(final Direction edgeDirection) {
		logger.info("OldOrientation = " + this.edgeDirection.name() + " NewOrientation = " + edgeDirection.name());
		this.edgeDirection = edgeDirection;
	}

	@Override
	public RailNode getRailNode() {
		return railNode;
	}

	@Override
	public void setRailNode(final RailNode railNode) {
		
		this.railNode = railNode;
	}

	@Override
	public Route getRoute() {
		return route;
	}

	@Override
	public void setRoute(final Route route) {
		this.route = route;
	}

	@Override
	public GraphNode getGraphNode() {
		return graphNode;
	}

	@Override
	public void setGraphNode(final GraphNode graphNode) {
		this.graphNode = graphNode;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public ProtocolFacade getProtocolFacade() {
		return protocolFacade;
	}

	public void setProtocolFacade(final ProtocolFacade protocolFacade) {
		this.protocolFacade = protocolFacade;
	}

	@Override
	public boolean isDirection() {
		return direction;
	}

	@Override
	public void setDirection(final boolean direction) {
		this.direction = direction;
	}

	@Override
	public String getImageFilename() {
		return imageFilename;
	}

	@Override
	public void setImageFilename(final String imageFilename) {
		this.imageFilename = imageFilename;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

}
