package de.wfb.rail.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.io.SerialWriter;

/**
 * Writes data received from the input stream to system.out
 */
public class ConsoleOutputSerialReader implements SerialWriter, Runnable {

	private static Logger logger = LogManager.getLogger(ConsoleOutputSerialReader.class);

	private final InputStream in;

	public ConsoleOutputSerialReader(final InputStream in) {
		this.in = in;
	}

	public void run() {

		final byte[] buffer = new byte[1024];
		int len = -1;
		try {
			while ((len = this.in.read(buffer)) > -1) {

//				final String response = new String(buffer, 0, len);
//				System.out.print(response);
//
				final byte[] stringBuffer = new byte[len];

				final String response = Hex.encodeHexString(Arrays.copyOf(buffer, len));

				logger.info("Response: '" + response + "'");

			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}