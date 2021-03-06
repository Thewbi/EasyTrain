package de.wfb.rail.writer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes System.in into the output stream
 */
public class ConsoleInputSerialWriter implements Runnable {

	private final OutputStream out;

	public ConsoleInputSerialWriter(final OutputStream out) {
		this.out = out;
	}

	public void run() {
		try {
			int c = 0;
			while ((c = System.in.read()) > -1) {
				out.write(c);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}