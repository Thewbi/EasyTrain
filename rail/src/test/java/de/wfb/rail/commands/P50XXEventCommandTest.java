package de.wfb.rail.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import de.wfb.rail.io.template.DefaultSerialTemplate;
import de.wfb.rail.io.template.SerialTemplate;

public class P50XXEventCommandTest {

	@Test
	public void requestResponseTest() {

		final DummyInputStream dummyInputStream = new DummyInputStream();
		final DummyOutputStream dummyOutputStream = new DummyOutputStream();

		final P50XXEventCommand p50XXEventCommand = new P50XXEventCommand();

		final SerialTemplate serialTemplate = new DefaultSerialTemplate(dummyOutputStream, dummyInputStream,
				p50XXEventCommand);
		serialTemplate.execute();

	}

	class DummyInputStream extends InputStream {

		private static final int BUFFER_LENGTH = 2;

		byte byteArray[] = new byte[BUFFER_LENGTH];

		private int currentIndex = 0;

		public DummyInputStream() {

			byteArray[0] = (byte) 0x80;
			byteArray[1] = (byte) 0x45;
		}

		@Override
		public int read() throws IOException {

			if (currentIndex == BUFFER_LENGTH) {
				currentIndex = 0;

				return -1;
			}

			final byte result = byteArray[currentIndex];
			currentIndex++;

			// https://stackoverflow.com/questions/7401550/how-to-convert-int-to-unsigned-byte-and-back
			// convert byte value to unsigned int
			return result & 0xFF;
		}
	}

	class DummyOutputStream extends OutputStream {

		@Override
		public void write(final int b) throws IOException {
			// nop
		}
	}
}
