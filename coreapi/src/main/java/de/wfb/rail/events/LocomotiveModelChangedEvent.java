package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

import de.wfb.model.locomotive.Locomotive;

public class LocomotiveModelChangedEvent extends ApplicationEvent {

	private final Locomotive locomotive;

	private final OperationType operationType;

	public LocomotiveModelChangedEvent(final Object source, final Locomotive locomotive,
			final OperationType operationType) {
		super(source);
		this.locomotive = locomotive;
		this.operationType = operationType;
	}

	public Locomotive getLocomotive() {
		return locomotive;
	}

	public OperationType getOperationType() {
		return operationType;
	}

}
