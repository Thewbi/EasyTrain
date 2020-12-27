package de.wfb.rail.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.io.NRSerialPort;
import gnu.io.SerialPort;

public class NRSerialPortFactory implements Factory<SerialPort> {
	
	public static final int BAUD_RATE = 19200;

	public static final int DATA_BITS = SerialPort.DATABITS_8;

	public static final int STOP_BITS = SerialPort.STOPBITS_2;

	public static final int PARITY = SerialPort.PARITY_NONE;

	private static final Logger logger = LogManager.getLogger(NRSerialPortFactory.class);

	@Override
	public SerialPort create(Object... args) throws Exception {

		final String serialPortIdentifier = (String) args[0];

		logger.info("Creating serial port via name: '" + serialPortIdentifier + "'");
		
		try {

		for (String s : NRSerialPort.getAvailableSerialPorts()) {
			System.out.println("Availible port: " + s);
		}
		
		int baudRate = BAUD_RATE;
		NRSerialPort serialPort = new NRSerialPort(serialPortIdentifier, baudRate);
		
//		serialPort.setSerialPortParams(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
		serialPort.connect();
		serialPort.getSerialPortInstance().setSerialPortParams(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
		
		return serialPort.getSerialPortInstance();
		
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
