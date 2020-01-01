package de.wfb.model.node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TurnoutNode extends Node {

	private static final Logger logger = LogManager.getLogger(TurnoutNode.class);

	private boolean thrown;

	public boolean isThrown() {
		return thrown;
	}

	public void setThrown(final boolean thrown) {
		this.thrown = thrown;
	}

	public void toggle() {

		logger.info("toggle()");

		thrown = !thrown;
	}

}
