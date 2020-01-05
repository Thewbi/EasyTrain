package de.wfb.rail.io.template;

import java.io.IOException;
import java.io.InputStream;

public class DummyInputStream extends InputStream {

	private static final int BUFFER_LENGTH = 5;

	byte byteArray[] = new byte[BUFFER_LENGTH];

	private int currentIndex = 0;

	public DummyInputStream() {

		byteArray[0] = (byte) 0x01;
		byteArray[1] = (byte) 0x02;
		byteArray[2] = (byte) 0x03;
		byteArray[3] = (byte) 0x04;
		byteArray[4] = (byte) 0x05;
	}

	@Override
	public int read() throws IOException {

		if (currentIndex == BUFFER_LENGTH) {
			currentIndex = 0;

			return -1;
		}

		final byte result = byteArray[currentIndex];
		currentIndex++;

		return result;
	}

}
