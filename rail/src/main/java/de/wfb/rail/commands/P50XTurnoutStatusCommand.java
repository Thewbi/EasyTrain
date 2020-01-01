package de.wfb.rail.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.commands.Command;

/**
 * XTrntSts (094h)
 *
 * <PROTOCOL_SWITCH> 0x94 <LOW BYTE OF TURNOUT ADDRESS (A7..A0)> <HIGH BYTE OF
 * TURNOUT ADDRESS (A10..A8)>
 *
 * <pre>
 * 78 94 10 00
 * </pre>
 */
public class P50XTurnoutStatusCommand implements Command {

	private static Logger logger = LogManager.getLogger(P50XTurnoutStatusCommand.class);

	private short turnoutId;

	public void execute(final OutputStream outputStream) {

		logger.info(getClass().getSimpleName());

		try {
			// final String cmd = "78941000";
			// final String cmd = "78946F00";
			// final String cmd = "78949900";

//			final StringBuffer stringBuffer = new StringBuffer(4);
//			stringBuffer.append("7894");

			// final byte[] byteArray =
			// Hex.decodeHex(stringBuffer.toString().toCharArray());

			// outputStream.write(byteArray, 0, byteArray.length);

			final byte[] byteArray = new byte[4];
			byteArray[0] = (byte) 0x78;
			byteArray[1] = (byte) 0x94;

//			// little endian
//			byteArray[2] = (byte) (turnoutId & 0xff);
//			byteArray[3] = (byte) ((turnoutId >> 8) & 0xff);

			// big endian
			byteArray[2] = (byte) ((turnoutId >> 8) & 0xFF);
			byteArray[3] = (byte) (turnoutId & 0xFF);

			outputStream.write(byteArray, 0, byteArray.length);

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public int getResponseLength() {
		return 2;
	}

	public void result(final ByteBuffer byteBuffer) {
		logger.info(getClass().getSimpleName() + " " + byteBuffer.toString());
	}

	public short getTurnoutId() {
		return turnoutId;
	}

	public void setTurnoutId(final short turnoutId) {
		this.turnoutId = turnoutId;
	}

	public byte[] getByteArray() {

		final byte[] byteArray = new byte[4];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0x94;

//		// little endian
//		byteArray[2] = (byte) (turnoutId & 0xff);
//		byteArray[3] = (byte) ((turnoutId >> 8) & 0xff);

		// big endian
		byteArray[2] = (byte) ((turnoutId >> 8) & 0xFF);
		byteArray[3] = (byte) (turnoutId & 0xFF);

		return byteArray;
	}

}
