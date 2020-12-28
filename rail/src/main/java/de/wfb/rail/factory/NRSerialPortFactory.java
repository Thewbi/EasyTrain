package de.wfb.rail.factory;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import gnu.io.NRSerialPort;
import gnu.io.SerialPort;

public class NRSerialPortFactory implements Factory<SerialPort> {

	public static final int BAUD_RATE = 19200;

	public static final int DATA_BITS = SerialPort.DATABITS_8;

	public static final int STOP_BITS = SerialPort.STOPBITS_2;

	public static final int PARITY = SerialPort.PARITY_NONE;

	private static final Logger logger = LogManager.getLogger(NRSerialPortFactory.class);

	@Override
	public SerialPort create(final Object... args) throws Exception {

		try {

			final Set<String> availableSerialPorts = NRSerialPort.getAvailableSerialPorts();

			if (CollectionUtils.isEmpty(availableSerialPorts)) {
				logger.error("No serial ports to connect to! Cannot connect to serial!");
				return null;
			}

			for (final String serialPortIdentifier : availableSerialPorts) {

				logger.info("Creating serial port via name: '" + serialPortIdentifier + "' ...");

				final NRSerialPort serialPort = new NRSerialPort(serialPortIdentifier, BAUD_RATE);
				serialPort.connect();
				serialPort.getSerialPortInstance().setSerialPortParams(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);

				logger.info("Creating serial port via name: '" + serialPortIdentifier + "' done.");

				return serialPort.getSerialPortInstance();
			}

		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

}
