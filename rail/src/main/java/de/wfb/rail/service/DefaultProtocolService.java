package de.wfb.rail.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.Model;
import de.wfb.model.node.Node;
import de.wfb.rail.commands.Command;
import de.wfb.rail.commands.P50XTurnoutCommand;
import de.wfb.rail.commands.P50XVersionCommand;
import de.wfb.rail.commands.P50XXEventCommand;
import de.wfb.rail.commands.P50XXEvtSenCommand;
import de.wfb.rail.commands.P50XXLokCommand;
import de.wfb.rail.commands.P50XXNOPCommand;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.io.template.DefaultSerialTemplate;
import de.wfb.rail.io.template.SerialTemplate;
import de.wfb.rail.ui.ShapeType;
import gnu.io.SerialPort;

public class DefaultProtocolService implements ProtocolService {

	// private static final String SERIAL_PORT_IDENTIFIER = "COM3";
	private static final String SERIAL_PORT_IDENTIFIER = "/dev/cu.usbserial-AO007Q6Q";
	// private static final String SERIAL_PORT_IDENTIFIER = "/dev/cu.usbserial";

	private static final Logger logger = LogManager.getLogger(DefaultProtocolService.class);

	private SerialPort serialPort;

	private InputStream inputStream;

	private OutputStream outputStream;

	private final Lock lock = new ReentrantLock(true);

	@Autowired
	private Model model;

	@Autowired
	private Factory<SerialPort> serialPortFactory;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void nodeClicked(final int x, final int y) {

		logger.trace("nodeClicked x = " + x + " y = " + y);

		final Node node = model.getNode(x, y);

		if (node == null) {

			logger.info("nodeClicked node is null");

			// all nodes that are selected have to be unused

			return;
		}

		logger.trace("nodeClicked " + node.getId() + " (" + node.getX() + ", " + node.getY() + ")");
//		logger.info(node);

		// switch turnouts
		if (ShapeType.isTurnout(node.getShapeType())) {

			turnTurnout(node);
		}
	}

	// private void turnTurnout(final TurnoutNode turnoutNode) {
	private void turnTurnout(final Node node) {

		lock.lock();

		try {

			if (node.getProtocolTurnoutId() == null || node.getProtocolTurnoutId() <= 0) {
				logger.info("The turnout has no valid turnoutId! Cannot switch the turnout via the Intellibox!");

				return;
			}

			if (!isConnected()) {
				logger.info("Not connected! Aborting operation!");

				return;
			}

			// in order to operate a turnout once (one change of direction)
			// two commands have to be sent!
			turnoutCommandFirst(inputStream, outputStream, node.getProtocolTurnoutId(), node.isThrown());
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
			turnoutCommandSecond(inputStream, outputStream, node.getProtocolTurnoutId(), node.isThrown());

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			lock.unlock();

		}
	}

	@Override
	public void event() {

		lock.lock();

		try {

			if (!isConnected()) {

				logger.trace("Not connected! Aborting operation!");

				return;
			}

			// check the event status
			final P50XXEventCommand eventCommand = eventCommand(inputStream, outputStream);

			logger.trace("eventCommand.isxStatusShouldBeCalled() = " + eventCommand.isxStatusShouldBeCalled());

			// if the event status says to check the S88 contacts, check those contacts
			if (eventCommand.isxStatusShouldBeCalled()) {

				final P50XXEvtSenCommand eventSenseCommand = eventSenseCommand(inputStream, outputStream);
				logger.trace(eventSenseCommand);
			}

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			lock.unlock();

		}
	}

	@Override
	public void throttleLocomotive(final short locomotiveAddress, final double throttleValue,
			final boolean dirForward) {

		lock.lock();

		try {

			if (!isConnected()) {

				logger.info("Not connected! Aborting operation!");

				return;
			}
			throttleCommand(inputStream, outputStream, locomotiveAddress, throttleValue, dirForward);

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			lock.unlock();

		}
	}

	@SuppressWarnings("unused")
	private void nopCommand(final InputStream inputStream, final OutputStream outputStream) {

		final Command command = new P50XXNOPCommand();
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	@SuppressWarnings("unused")
	private void versionCommand(final InputStream inputStream, final OutputStream outputStream) {

		final Command command = new P50XVersionCommand();
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	private void turnoutCommandFirst(final InputStream inputStream, final OutputStream outputStream,
			final int protocolTurnoutId, final boolean straight) {

		final Command command = new P50XTurnoutCommand((short) protocolTurnoutId, straight, true);
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	private void turnoutCommandSecond(final InputStream inputStream, final OutputStream outputStream,
			final int protocolTurnoutId, final boolean straight) {

		final Command command = new P50XTurnoutCommand((short) protocolTurnoutId, straight, false);
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	private void throttleCommand(final InputStream inputStream, final OutputStream outputStream,
			final short locomotiveAddress, final double throttleValue, final boolean dirForward) {

		final Command command = new P50XXLokCommand(locomotiveAddress, throttleValue, dirForward);
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	private P50XXEventCommand eventCommand(final InputStream inputStream, final OutputStream outputStream) {

		final P50XXEventCommand command = new P50XXEventCommand();
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();

		return command;
	}

	private P50XXEvtSenCommand eventSenseCommand(final InputStream inputStream, final OutputStream outputStream) {

		final P50XXEvtSenCommand command = new P50XXEvtSenCommand();

		// this command will send out a FeedbackBlockUpdateEvent per S88 module state
		// update. To send events, it gets a reference to a ApplicationEventPublisher
		command.setApplicationEventPublisher(applicationEventPublisher);

		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();

		return command;
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
