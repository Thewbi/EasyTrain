package de.wfb.rail.commands;

import java.nio.ByteBuffer;

/**
 * XNOP (C4) - WITHOUT 78 !!!!!!
 *
 * C4
 */
public class P50XXNOPCommand implements Command {

	@Override
	public int getResponseLength() {
		return 2;
	}

	@Override
	public void result(final ByteBuffer byteBuffer) {

	}

	@Override
	public byte[] getByteArray() {

		final byte[] byteArray = new byte[1];
		byteArray[0] = (byte) 0xC4;

		return byteArray;
	}

}
