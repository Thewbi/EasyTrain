package de.wfb.rail.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

/**
 * Establish a USB to Serial connection under macos
 *
 * <pre>
 * while : ;do clear;ls -lt /dev|head;i=$((i+1));echo $i;sleep 1;done
 * </pre>
 */
public class DefaultSerialPortFactory implements Factory<SerialPort> {

	private static final Logger logger = LogManager.getLogger(DefaultSerialPortFactory.class);

	public static final int BAUD_RATE = 19200;

	public static final int DATA_BITS = SerialPort.DATABITS_8;

	public static final int STOP_BITS = SerialPort.STOPBITS_2;

	public static final int PARITY = SerialPort.PARITY_NONE;

	/** the name of the application requesting the port */
	private static final String APPLICATION_NAME = "EasyTrain";

	@Override
	public SerialPort create(final Object... args) throws Exception {

		final String serialPortIdentifier = (String) args[0];

		logger.info("Creating serial port via name: '" + serialPortIdentifier + "'");

		try {
			return connect(serialPortIdentifier);
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	private SerialPort connect(final String portName) throws Exception {

		logger.info("connect() portName: '" + portName + "'");

		final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

		logger.info("PortIdentifier: " + portIdentifier);

		logger.info("Checking if port is currently owned ...");

		if (portIdentifier.isCurrentlyOwned()) {

			logger.error("Error: Port is currently in use");
			return null;
		}

		logger.info("Port is currently not owned!");

		logger.info("Trying to open the port ...");

		// number of milliseconds to wait for the port to open
		final int connectionTimeoutInMillis = 20;
		final CommPort commPort = portIdentifier.open(APPLICATION_NAME, connectionTimeoutInMillis);

		logger.info("CommPort: " + commPort);

		if (!(commPort instanceof SerialPort)) {
			logger.error("Error: Only serial ports are handled by this example.");
			return null;
		}

		logger.info("Trying to open the port done.");

		final SerialPort serialPort = (SerialPort) commPort;
		serialPort.setSerialPortParams(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);

		logger.info("connect() done.");

		return serialPort;
	}
}
