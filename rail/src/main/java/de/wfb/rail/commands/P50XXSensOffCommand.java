package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class P50XXSensOffCommand implements Command {

	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger(P50XXSensOffCommand.class);

	@Override
	public int getResponseLength() {
		return 1;
	}

	@Override
	public void result(final ByteBuffer byteBuffer) {

	}

	@Override
	public byte[] getByteArray() {

		final byte[] byteArray = new byte[2];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0x99;

		return byteArray;
	}

}
