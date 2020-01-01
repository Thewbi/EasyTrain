package de.wfb.rail.writer;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.commands.Command;

public class CommandSerialWriter implements Runnable {

	private static Logger logger = LogManager.getLogger(CommandSerialWriter.class);

	private final OutputStream out;

	private final List<Command> commands = new ArrayList<Command>();

	public List<Command> getCommands() {
		return commands;
	}

	public CommandSerialWriter(final OutputStream out) {
		this.out = out;
	}

	public void run() {

		logger.info("run");

		if (CollectionUtils.isEmpty(commands)) {
			return;
		}

		for (final Command command : commands) {

			logger.info("Executing: " + command.getClass());

			ExecuteCommand(command, out);

			try {
				logger.info("sleeping ...");
				Thread.sleep(5000);
			} catch (final InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void ExecuteCommand(final Command command, final OutputStream outputStream) {
		// command.execute(outputStream);
	}

}
