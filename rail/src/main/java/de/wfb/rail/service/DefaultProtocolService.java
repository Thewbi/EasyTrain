package de.wfb.rail.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.Model;
import de.wfb.model.node.Node;
import de.wfb.rail.commands.Command;
import de.wfb.rail.commands.P50XSensorCommand;
import de.wfb.rail.commands.P50XTurnoutCommand;
import de.wfb.rail.commands.P50XVersionCommand;
import de.wfb.rail.commands.P50XXEventCommand;
import de.wfb.rail.commands.P50XXEvtSenCommand;
import de.wfb.rail.commands.P50XXLokCommand;
import de.wfb.rail.commands.P50XXNOPCommand;
import de.wfb.rail.commands.P50XXSensOffCommand;
import de.wfb.rail.commands.P50XXTrntStsCommand;
import de.wfb.rail.events.FeedbackBlockUpdateEvent;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.io.template.DefaultSerialTemplate;
import de.wfb.rail.io.template.SerialTemplate;
import de.wfb.rail.ui.ShapeType;
import gnu.io.SerialPort;

public class DefaultProtocolService implements ProtocolService {

	 private static final String SERIAL_PORT_IDENTIFIER = "COM5";
	 
	//private static final String SERIAL_PORT_IDENTIFIER = "/dev/cu.usbserial-AO007Q6Q";
	 //private static final String SERIAL_PORT_IDENTIFIER = "/dev/cu.usbserial";

	private static final Logger logger = LogManager.getLogger(DefaultProtocolService.class);

	private SerialPort serialPort;

	private InputStream inputStream;

	private OutputStream outputStream;

//	private final ReentrantLock lock = new ReentrantLock(true);

	@Autowired
	private Model model;

	@Autowired
	private Factory<SerialPort> serialPortFactory;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public synchronized void nodeClicked(final int x, final int y) {

		logger.trace("nodeClicked x = " + x + " y = " + y);

		final Node node = model.getNode(x, y);
		if (node == null) {
			logger.trace("nodeClicked node is null");
			return;
		}

		logger.trace("nodeClicked " + node.getId() + " (" + node.getX() + ", " + node.getY() + ")");

		// switch turnouts
		if (ShapeType.isTurnout(node.getShapeType())) {

			turnTurnout(node);
		}
	}

	@Override
	public synchronized void turnTurnout(final Node node) {

		logger.info("turnTurnout()");

		if (ShapeType.isNotTurnout(node.getShapeType())) {

			logger.info("Not a turnout shape!");
			return;
		}

		try {

			lockLock(false);

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

//			//  flip flag in turnout nodes!
//			asdf
//			if (node.getProtocolTurnoutId() == 12 || node.getProtocolTurnoutId() == 80) {
//
//				// in order to operate a turnout once (one change of direction)
//				// two commands have to be sent!
//				turnoutCommandFirst(inputStream, outputStream, node.getProtocolTurnoutId(), !node.isThrown());
//				try {
//					Thread.sleep(100);
//				} catch (final InterruptedException e) {
//					logger.error(e.getMessage(), e);
//				}
//				turnoutCommandSecond(inputStream, outputStream, node.getProtocolTurnoutId(), !node.isThrown());
//
//			} else {
//
//				// in order to operate a turnout once (one change of direction)
//				// two commands have to be sent!
//				turnoutCommandFirst(inputStream, outputStream, node.getProtocolTurnoutId(), node.isThrown());
//				try {
//					Thread.sleep(100);
//				} catch (final InterruptedException e) {
//					logger.error(e.getMessage(), e);
//				}
//				turnoutCommandSecond(inputStream, outputStream, node.getProtocolTurnoutId(), node.isThrown());
//			}

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			logger.info("turnTurnout unlockLock ...");
			unlockLock();
			logger.info("turnTurnout unlockLock done.");

		}
	}

	@Override
	public synchronized boolean turnoutStatus(final short protocolId) {

		logger.trace("turnoutStatus()");

		try {

			lockLock(false);

			if (!isConnected()) {

				logger.trace("Not connected! Aborting operation!");

				return false;
			}

			// check the turnout status
			logger.trace("executing turnoutStatusCommand ...");
			final P50XXTrntStsCommand command = turnoutStatusCommand(inputStream, outputStream, protocolId);
			logger.trace("executing turnoutStatusCommand done. Thrown: " + command.isThrown());

			return command.isThrown();

//			//  flip flag in turnout nodes!
//			asdf
//			if ((protocolId == 12) || (protocolId == 80)) {
//				return !command.isThrown();
//			} else {
//				return command.isThrown();
//			}

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			logger.trace("turnoutStatus unlockLock ...");
			unlockLock();
			logger.trace("turnoutStatus unlockLock done.");

		}

		return false;
	}

	@Override
	public synchronized boolean event() {

		logger.trace("E - event()");

//		logger.info("E - Lock HoldCount: " + lock.getHoldCount() + " QueueLength: " + lock.getQueueLength()
//				+ " hasQueuedThreads: " + lock.hasQueuedThreads());

//		logger.trace("E - Aquiring lock ...");
		try {

			logger.trace("Event lock...");
			lockLock(true);
			logger.trace("Event locked done.");

//			final boolean tryLockResult = lock.tryLock(10, TimeUnit.SECONDS);
//
//			if (tryLockResult) {
//				logger.trace("E - Aquiring Lock done.");
//			} else {
//				logger.info("E - Aquiring Lock failed!");
//				return;
//			}

			logger.trace("E - Checking Connection!");
			if (!isConnected()) {

				logger.trace("E - Not connected! Aborting operation!");

				return false;
			}

			// check the event status
			logger.trace("Event command ...");
			final P50XXEventCommand eventCommand = eventCommand(inputStream, outputStream);
			logger.trace("Event command done.");

			logger.trace("E - eventCommand.isxStatusShouldBeCalled() = " + eventCommand.isxStatusShouldBeCalled());

			// yolo
			// if the event status says to check the S88 contacts, check those contacts
			if (eventCommand.isxStatusShouldBeCalled()) {

				return true;

				// yolo
//				logger.info("Event Sense command ...");
//				final P50XXEvtSenCommand eventSenseCommand = eventSenseCommand(inputStream, outputStream);
//				logger.info("Event Sense command done.");
			}

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			logger.trace("Event unlockLock ...");
			unlockLock();
			logger.trace("Event unlockLock done.");

		}

		return false;
	}

	@Override
	public synchronized void throttleLocomotive(final short locomotiveAddress, final double throttleValue,
			final boolean dirForward) {

		try {

			lockLock(true);

			if (!isConnected()) {

				logger.trace("Not connected! Aborting operation!");

				return;
			}
			throttleCommand(inputStream, outputStream, locomotiveAddress, throttleValue, dirForward);

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			logger.info("throttleLocomotive unlockLock ...");
			unlockLock();
			logger.info("throttleLocomotive unlockLock done.");
		}
	}

	@Override
	public synchronized void sense(final int feedbackContactId) {

		try {

			logger.trace("Sense lock...");
			lockLock(false);
			logger.info("Sense locked done.");

			if (!isConnected()) {

				logger.trace("Not connected! Aborting operation!");

				return;
			}
			sensorCommand(inputStream, outputStream, feedbackContactId);

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			logger.info("Sense unlockLock ...");
			unlockLock();
			logger.info("Sense unlockLock done.");
		}
	}

	@Override
	public synchronized void xSensOff() {

		try {

			lockLock(false);

			if (!isConnected()) {

				logger.trace("Not connected! Aborting operation!");
				return;
			}

			logger.info("xSensOffCommand ...");
			xSensOffCommand(inputStream, outputStream);
			logger.info("xSensOffCommand done.");

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			unlockLock();
		}
	}

	private void lockLock(final boolean debug) {

//		if (debug) {
//			logger.info("Lock BEFORE IsHeldByCurrentThread: " + lock.isHeldByCurrentThread() + " HoldCount: "
//					+ lock.getHoldCount() + " QueueLength: " + lock.getQueueLength() + " hasQueuedThreads: "
//					+ lock.hasQueuedThreads());
//
//			logger.info("Aquiring lock ...");
//		}
//
//		if (!lock.isHeldByCurrentThread()) {
//			lock.lock();
//		}
//
//		if (debug) {
//			logger.info("Aquiring Lock done.");
//
//			logger.info("Lock AFTER IsHeldByCurrentThread: " + lock.isHeldByCurrentThread() + " HoldCount: "
//					+ lock.getHoldCount() + " QueueLength: " + lock.getQueueLength() + " hasQueuedThreads: "
//					+ lock.hasQueuedThreads());
//		}
	}

	private void unlockLock() {

//		try {
//
//			logger.trace("Unlock BEFORE HoldCount: " + lock.getHoldCount() + " QueueLength: " + lock.getQueueLength()
//					+ " hasQueuedThreads: " + lock.hasQueuedThreads());
//
//			logger.trace("Releasing lock ...");
//			lock.unlock();
//			logger.trace("Releasing lock done.");
//
//			if (lock.getHoldCount() > 0) {
//
//				logger.info("Unlock AFTER HoldCount: " + lock.getHoldCount() + " QueueLength: " + lock.getQueueLength()
//						+ " hasQueuedThreads: " + lock.hasQueuedThreads());
//			}
//		} catch (final Exception e) {
//			logger.error(e.getMessage(), e);
//		}
	}

	@SuppressWarnings("unused")
	private void nopCommand(final InputStream inputStream, final OutputStream outputStream) {

		synchronized (this) {
			final Command command = new P50XXNOPCommand();
			final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
			serialTemplate.execute();
		}
	}

	@SuppressWarnings("unused")
	private void versionCommand(final InputStream inputStream, final OutputStream outputStream) {

		synchronized (this) {
			final Command command = new P50XVersionCommand();
			final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
			serialTemplate.execute();
		}
	}

	private void turnoutCommandFirst(final InputStream inputStream, final OutputStream outputStream,
			final int protocolTurnoutId, final boolean thrown) {

		synchronized (this) {
			final Command command = new P50XTurnoutCommand((short) protocolTurnoutId, thrown, true);
			final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
			serialTemplate.execute();
		}
	}

	private void turnoutCommandSecond(final InputStream inputStream, final OutputStream outputStream,
			final int protocolTurnoutId, final boolean thrown) {

		synchronized (this) {
			final Command command = new P50XTurnoutCommand((short) protocolTurnoutId, thrown, false);
			final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
			serialTemplate.execute();
		}
	}

	private void throttleCommand(final InputStream inputStream, final OutputStream outputStream,
			final short locomotiveAddress, final double throttleValue, final boolean dirForward) {

		synchronized (this) {
			final Command command = new P50XXLokCommand(locomotiveAddress, throttleValue, dirForward);
			final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
			serialTemplate.execute();
		}
	}

	private P50XXEventCommand eventCommand(final InputStream inputStream, final OutputStream outputStream) {

		synchronized (this) {
			final P50XXEventCommand command = new P50XXEventCommand();
			final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
			serialTemplate.execute();

			return command;
		}
	}

	private List<FeedbackBlockUpdateEvent> eventSenseCommand(final InputStream inputStream,
			final OutputStream outputStream) {

		synchronized (this) {
			try {

				logger.info("EventSense Command ...");

				final P50XXEvtSenCommand command = new P50XXEvtSenCommand();

				// this command will send out a FeedbackBlockUpdateEvent per S88 module state
				// update. To send events, it gets a reference to a ApplicationEventPublisher
				command.setApplicationEventPublisher(applicationEventPublisher);

				final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
				serialTemplate.execute();

				logger.info("EventSense done.");

				return command.getFeedbackBlockUpdates();
			} catch (final Exception e) {
				logger.error(e.getMessage(), e);
			}

			return null;
		}
	}

	private P50XSensorCommand sensorCommand(final InputStream inputStream, final OutputStream outputStream,
			final int feedbackBlockId) {

		synchronized (this) {

			final P50XSensorCommand command = new P50XSensorCommand(feedbackBlockId);
			final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
			serialTemplate.execute();

			return command;
		}
	}

	private P50XXSensOffCommand xSensOffCommand(final InputStream inputStream, final OutputStream outputStream) {

		synchronized (this) {
			final P50XXSensOffCommand command = new P50XXSensOffCommand();
			final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
			serialTemplate.execute();

			return command;
		}
	}

	private P50XXTrntStsCommand turnoutStatusCommand(final InputStream inputStream, final OutputStream outputStream,
			final short protocolId) {

		synchronized (this) {
			logger.trace("turnoutStatusCommand()");

			final P50XXTrntStsCommand command = new P50XXTrntStsCommand(protocolId);
			final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
			serialTemplate.execute();

			return command;
		}
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

	@Override
	public List<FeedbackBlockUpdateEvent> eventSenseCommand() {
		lockLock(true);
		try {
			return eventSenseCommand(inputStream, outputStream);
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			unlockLock();
		}

		return new ArrayList<>();

	}

}
