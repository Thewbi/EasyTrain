package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import de.wfb.rail.commands.Command;

/**
 * XNOP (C4) - WITHOUT 78 !!!!!!
 *
 * C4
 */
public class P50XXNOPCommand implements Command {

//	public void execute(final OutputStream outputStream) {
//		try {
//			final byte[] byteArray = Hex.decodeHex("C4".toCharArray());
//			outputStream.write(byteArray, 0, byteArray.length);
//		} catch (final IOException e) {
//			e.printStackTrace();
//		} catch (final DecoderException e) {
//			e.printStackTrace();
//		}
//	}

	public int getResponseLength() {
		return 2;
	}

	public void result(final ByteBuffer byteBuffer) {
		// TODO Auto-generated method stub

	}

	public byte[] getByteArray() {

		final byte[] byteArray = new byte[1];
		byteArray[0] = (byte) 0xC4;

		return byteArray;
	}

}
