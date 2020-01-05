package de.wfb.rail.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import de.wfb.rail.io.template.DefaultSerialTemplate;
import de.wfb.rail.io.template.SerialTemplate;

public class P50XXEvtSenCommandTest {

	@Test
	public void requestResponseTest() {

		final DummyInputStream dummyInputStream = new DummyInputStream();
		final DummyOutputStream dummyOutputStream = new DummyOutputStream();

		final P50XXEvtSenCommand p50XXEvtSenCommand = new P50XXEvtSenCommand();

		final SerialTemplate serialTemplate = new DefaultSerialTemplate(dummyOutputStream, dummyInputStream,
				p50XXEvtSenCommand);
		serialTemplate.execute();
	}

	class DummyInputStream extends InputStream {

		private static final int BUFFER_LENGTH = 7 * 3 + 1;

		byte byteArray[] = new byte[BUFFER_LENGTH];

		private int currentIndex = 0;

		public DummyInputStream() {

			int idx = 0;

			// 01 FF E0
			byteArray[idx] = (byte) 0x01;
			idx++;

			byteArray[idx] = (byte) 0xFF;
			idx++;

			byteArray[idx] = (byte) 0xE0;
			idx++;

			// 02 80 00
			byteArray[idx] = (byte) 0x02;
			idx++;

			byteArray[idx] = (byte) 0x80;
			idx++;

			byteArray[idx] = (byte) 0x00;
			idx++;

			// 03 38 1E
			byteArray[idx] = (byte) 0x03;
			idx++;

			byteArray[idx] = (byte) 0x38;
			idx++;

			byteArray[idx] = (byte) 0x1E;
			idx++;

			// 04 7B FF
			byteArray[idx] = (byte) 0x04;
			idx++;

			byteArray[idx] = (byte) 0x7B;
			idx++;

			byteArray[idx] = (byte) 0xFF;
			idx++;

			// 05 00 02
			byteArray[idx] = (byte) 0x05;
			idx++;

			byteArray[idx] = (byte) 0x00;
			idx++;

			byteArray[idx] = (byte) 0x02;
			idx++;

			// 06 80 00
			byteArray[idx] = (byte) 0x06;
			idx++;

			byteArray[idx] = (byte) 0x80;
			idx++;

			byteArray[idx] = (byte) 0x00;
			idx++;

			// 07 02 03
			byteArray[idx] = (byte) 0x07;
			idx++;

			byteArray[idx] = (byte) 0x02;
			idx++;

			byteArray[idx] = (byte) 0x03;
			idx++;

			// 00
			byteArray[idx] = (byte) 0x00;
			idx++;
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
