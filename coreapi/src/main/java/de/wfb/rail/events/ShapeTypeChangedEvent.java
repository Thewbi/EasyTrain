package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

import de.wfb.rail.ui.ShapeType;

public class ShapeTypeChangedEvent extends ApplicationEvent {

	private final ShapeType shapeType;

	public ShapeTypeChangedEvent(final Object source, final ShapeType shapeType) {
		super(source);
		this.shapeType = shapeType;
	}

	public ShapeType getShapeType() {
		return shapeType;
	}

}
