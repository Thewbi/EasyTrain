package de.wfb.rail.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class DefaultSerialPortFactory implements Factory<SerialPort> {

	private static final Logger logger = LogManager.getLogger(DefaultSerialPortFactory.class);

	private static final int BAUD_RATE = 19200;

	private static final int DATA_BITS = SerialPort.DATABITS_8;

	private static final int STOP_BITS = SerialPort.STOPBITS_2;

	private static final int PARITY = SerialPort.PARITY_NONE;

	/** the name of the application requesting the port */
	private static final String APPLICATION_NAME = "EasyTrain";

	@Override
	public SerialPort create(final Object... args) throws Exception {

		final String serialPortIdentifier = (String) args[0];

		logger.info("Creating serial port via name: '" + serialPortIdentifier + "'");

		return connect(serialPortIdentifier);
	}

	private SerialPort connect(final String portName) throws Exception {

		logger.info("connect portName: " + portName);

		final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

		logger.info("PortIdentifier: " + portIdentifier);

		if (portIdentifier.isCurrentlyOwned()) {

			logger.error("Error: Port is currently in use");
			return null;
		}

		// number of milliseconds to wait for the port to open
		final int connectionTimeoutInMillis = 2000;

		final CommPort commPort = portIdentifier.open(APPLICATION_NAME, connectionTimeoutInMillis);

		logger.info("CommPort: " + commPort);

		if (!(commPort instanceof SerialPort)) {
			logger.error("Error: Only serial ports are handled by this example.");
			return null;
		}

		final SerialPort serialPort = (SerialPort) commPort;
		serialPort.setSerialPortParams(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);

		return serialPort;
	}
}
