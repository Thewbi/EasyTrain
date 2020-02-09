package de.wfb.model.locomotive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.node.Direction;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.rail.facade.ProtocolFacade;
import de.wfb.rail.service.Route;

public class DefaultLocomotive {

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

	private ProtocolFacade protocolFacade;

	public DefaultLocomotive() {
		super();
	}

	public DefaultLocomotive(final int id, final String name, final short address) {
		super();
		this.id = id;
		this.name = name;
		this.address = address;
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
		return "Locomotive " + name + "";
	}

	public short getAddress() {
		return address;
	}

	public void setAddress(final short address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public Direction getOrientation() {
		return edgeDirection;
	}

	public void setOrientation(final Direction edgeDirection) {
		logger.trace("OldOrientation = " + this.edgeDirection.name() + " NewOrientation = " + edgeDirection.name());
		this.edgeDirection = edgeDirection;
	}

	public RailNode getRailNode() {
		return railNode;
	}

	public void setRailNode(final RailNode railNode) {
		this.railNode = railNode;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(final Route route) {
		this.route = route;
	}

	public GraphNode getGraphNode() {
		return graphNode;
	}

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

	public void start(final double speed) {

		if (Math.abs(this.speed - speed) < 0.1d) {
			return;
		}

		this.speed = speed;
		protocolFacade.throttleLocomotive(address, speed, direction);
	}

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

	public boolean isDirection() {
		return direction;
	}

	public void setDirection(final boolean direction) {
		this.direction = direction;
	}

}
