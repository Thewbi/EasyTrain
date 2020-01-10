package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

import de.wfb.model.locomotive.DefaultLocomotive;

public class LocomotiveModelChangedEvent extends ApplicationEvent {

	private final DefaultLocomotive locomotive;

	private final OperationType operationType;

	public LocomotiveModelChangedEvent(final Object source, final DefaultLocomotive locomotive,
			final OperationType operationType) {
		super(source);
		this.locomotive = locomotive;
		this.operationType = operationType;
	}

	public DefaultLocomotive getLocomotive() {
		return locomotive;
	}

	public OperationType getOperationType() {
		return operationType;
	}

}
