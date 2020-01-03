package de.wfb.rail.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.Model;
import de.wfb.model.node.Node;
import de.wfb.model.node.TurnoutNode;
import de.wfb.rail.commands.Command;
import de.wfb.rail.commands.P50XTurnoutCommand;
import de.wfb.rail.commands.P50XVersionCommand;
import de.wfb.rail.commands.P50XXNOPCommand;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.io.template.DefaultSerialTemplate;
import de.wfb.rail.io.template.SerialTemplate;
import gnu.io.SerialPort;

public class DefaultProtocolService implements ProtocolService {

	// private static final String SERIAL_PORT_IDENTIFIER = "COM3";
	private static final String SERIAL_PORT_IDENTIFIER = "/dev/cu.usbserial-AO007Q6Q";
	// private static final String SERIAL_PORT_IDENTIFIER = "/dev/cu.usbserial";

	private static final Logger logger = LogManager.getLogger(DefaultProtocolService.class);

	private SerialPort serialPort;

	private InputStream inputStream;

	private OutputStream outputStream;

	@Autowired
	private Model model;

	@Autowired
	private Factory<SerialPort> serialPortFactory;

	@Override
	public void nodeClicked(final int x, final int y) {

		logger.info("nodeClicked x = " + x + " y = " + y);

		final Node node = model.getNode(x, y);

		if (node == null) {
			logger.info("nodeClicked node is null");
			return;
		}

		logger.info("nodeClicked node id = " + node.getId() + " node = " + node.getClass().getSimpleName());

		// switch turnouts
		if (node instanceof TurnoutNode) {

			final TurnoutNode turnoutNode = (TurnoutNode) node;

			turnTurnout(turnoutNode);
		}
	}

	private void turnTurnout(final TurnoutNode turnoutNode) {

		if (turnoutNode.getProtocolTurnoutId() <= 0) {
			logger.info("The turnout has no valid turnoutId! Cannot switch the turnout!");
			return;
		}

		if (!isConnected()) {
			logger.info("Not connected! Aborting operation!");
			return;
		}

		// in order to operate a turnout once (one change of direction)
		// two commands have to be sent!
		turnoutCommandFirst(inputStream, outputStream, turnoutNode.getProtocolTurnoutId(), turnoutNode.isThrown());
		try {
			Thread.sleep(100);
		} catch (final InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		turnoutCommandSecond(inputStream, outputStream, turnoutNode.getProtocolTurnoutId(), turnoutNode.isThrown());

	}

	private static void nopCommand(final InputStream inputStream, final OutputStream outputStream) {

		final Command command = new P50XXNOPCommand();
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	private static void versionCommand(final InputStream inputStream, final OutputStream outputStream) {

		final Command command = new P50XVersionCommand();
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	private static void turnoutCommandFirst(final InputStream inputStream, final OutputStream outputStream,
			final int protocolTurnoutId, final boolean straight) {

		final Command command = new P50XTurnoutCommand((short) protocolTurnoutId, straight, true);
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	private static void turnoutCommandSecond(final InputStream inputStream, final OutputStream outputStream,
			final int protocolTurnoutId, final boolean straight) {

		final Command command = new P50XTurnoutCommand((short) protocolTurnoutId, straight, false);
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	public boolean isConnected() {
		return serialPort != null;
	}

	@Override
	public void connect() throws Exception {

		logger.info("connect()");

		if (isConnected()) {
			return;
		}

		logger.info("Creating port ... " + serialPortFactory);
		serialPort = serialPortFactory.create(SERIAL_PORT_IDENTIFIER);
		logger.info("Creating port done.");

		if (serialPort != null) {
			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();
		}
	}

	@Override
	public void disconnect() {

		if (!isConnected()) {
			return;
		}

		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (final IOException e) {
				logger.error(e.getMessage(), e);
			}
			inputStream = null;
		}

		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (final IOException e) {
				logger.error(e.getMessage(), e);
			}
			outputStream = null;
		}

		serialPort.close();
		serialPort = null;
	}
}
