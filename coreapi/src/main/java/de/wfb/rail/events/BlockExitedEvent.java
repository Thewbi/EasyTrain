package de.wfb.rail.events;

import org.springframework.context.ApplicationEvent;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.rail.service.Block;

public class BlockExitedEvent extends ApplicationEvent {

	private final Block block;

	private final DefaultLocomotive locomotive;

	public BlockExitedEvent(final Object source, final Block block, final DefaultLocomotive locomotive) {
		super(source);
		this.block = block;
		this.locomotive = locomotive;
	}

	public Block getBlock() {
		return block;
	}

	public DefaultLocomotive getLocomotive() {
		return locomotive;
	}

}
