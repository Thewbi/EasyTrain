package de.wfb.rail.commands;

import java.io.DataInputStream;
import java.io.IOException;

import org.junit.Test;

import de.wfb.rail.Main;
import de.wfb.rail.factory.DefaultSerialPortFactory;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NRSerialPort;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class DefaultSerialPortFactoryTest {

	@Test
	public void testConnectNeuronRobotics() throws IOException {

		System.out.println("A");

		final NRSerialPort serial = new NRSerialPort(Main.SERIAL_PORT_IDENTIFIER, DefaultSerialPortFactory.BAUD_RATE);
		serial.connect();

		System.out.println("B");

		final DataInputStream ins = new DataInputStream(serial.getInputStream());

		System.out.println("C");

		// read the first 10000 bytes a byte at a time
		for (int i = 0; i < 10000; i++) {

			System.out.println("D");

			final int b = ins.read();
			if (b == -1) {
				System.out.println("got EOF - going to keep trying");
				continue;
			}
		}

		System.out.println("E");

		serial.disconnect();
	}

	@Test
	public void testConnect() throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException {

		CommPort commPort = null;

		try {
			System.out.println("Getting identifier " + Main.SERIAL_PORT_IDENTIFIER + " ...");
			final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(Main.SERIAL_PORT_IDENTIFIER);
			System.out.println("Getting identifier " + Main.SERIAL_PORT_IDENTIFIER + " done.");

			System.out.println("isCurrentlyOwned() " + portIdentifier.isCurrentlyOwned());
			System.out.println(portIdentifier.getCurrentOwner());

			System.out.println("CommPort open ...");
			final int connectionTimeoutInMillis = 1;
			commPort = portIdentifier.open("APPLICATION_NAME", connectionTimeoutInMillis);
			System.out.println("CommPort open done: " + commPort);

			System.out.println("CommPort: " + commPort);

			if (!(commPort instanceof SerialPort)) {
				System.out.println("Error: Only serial ports are handled by this example.");
				return;
			}

			System.out.println("Trying to open the port done.");

			final SerialPort serialPort = (SerialPort) commPort;
			serialPort.setSerialPortParams(DefaultSerialPortFactory.BAUD_RATE, DefaultSerialPortFactory.DATA_BITS,
					DefaultSerialPortFactory.STOP_BITS, DefaultSerialPortFactory.PARITY);

			System.out.println("connect() done.");

		} catch (final Exception e) {
			e.printStackTrace();
		} finally {

			if (commPort != null) {
				commPort.close();
				commPort = null;
			}
		}
	}

}
