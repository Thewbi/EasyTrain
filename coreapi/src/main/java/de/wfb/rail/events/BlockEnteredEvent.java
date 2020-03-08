package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

import de.wfb.model.locomotive.Locomotive;
import de.wfb.rail.service.Block;

public class BlockEnteredEvent extends ApplicationEvent {

	private final Block block;

	private final Locomotive locomotive;

	public BlockEnteredEvent(final Object source, final Block block, final Locomotive locomotive) {
		super(source);
		this.block = block;
		this.locomotive = locomotive;
	}

	public Block getBlock() {
		return block;
	}

	public Locomotive getLocomotive() {
		return locomotive;
	}

}
