package de.wfb.rail.io.template;

import java.nio.ByteBuffer;

import de.wfb.rail.commands.Command;

public class PullModeCommand implements Command {

	int bytesRead = 0;

	int responseLength = -1;

	@Override
	public int getResponseLength() {

		// pull mode unless three bytes are read
		if (bytesRead >= 10) {
			responseLength = 0;
		}

		return responseLength;
	}

	@Override
	public void result(final ByteBuffer byteBuffer) {

		bytesRead += byteBuffer.position();

		// bytesRead++;

	}

	@Override
	public byte[] getByteArray() {

		final byte[] byteArray = new byte[2];
		byteArray[0] = (byte) 0x58;
		byteArray[1] = (byte) 0xC8;

		return byteArray;
	}

}
