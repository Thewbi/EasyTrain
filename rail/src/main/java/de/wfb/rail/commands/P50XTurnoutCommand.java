package de.wfb.rail.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.commands.Command;

/**
 * <PROTOCOL_SWITCH> 0x90 <LOW BYTE OF TURNOUT ADDRESS (A7..A0)> <HIGH BYTE OF
 * TURNOUT ADDRESS (A10..A8) plus 'color' and status bits>
 *
 * <pre>
 * 78 90 99 C0 78 90 99 80
 * </pre>
 *
 * <pre>
 * 78 90 99 40 78 90 99 00
 * </pre>
 */
public class P50XTurnoutCommand implements Command {

	private static Logger logger = LogManager.getLogger(P50XTurnoutCommand.class);

	private final short turnoutId;

	private boolean straight;

	private final boolean first;

	/**
	 * ctor
	 *
	 * @param turnoutId
	 * @param straight
	 */
	public P50XTurnoutCommand(final short turnoutId, final boolean straight, final boolean first) {
		this.turnoutId = turnoutId;
		this.straight = straight;
		this.first = first;
	}

	public void execute(final OutputStream outputStream) {

		final byte[] byteArray = new byte[4];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0x90;

		// little endian
		byteArray[2] = (byte) (turnoutId & 0xff);
		byteArray[3] = (byte) ((turnoutId >> 8) & 0xff);

//		// big endian
//		byteArray[2] = (byte) ((turnoutId >> 8) & 0xFF);
//		byteArray[3] = (byte) (turnoutId & 0xFF);

		if (first) {

			if (straight) {
				// 1100 0000
				byteArray[3] |= 0xC0;
			} else {
				// 0100 0000
				byteArray[3] |= 0x40;
			}

			try {
				outputStream.write(byteArray, 0, byteArray.length);
			} catch (final IOException e) {
				logger.error(e.getMessage(), e);
			}

		} else {

			if (straight) {
				// 1000 0000
				byteArray[3] |= 0x80;
			} else {
				// 0000 0000
				byteArray[3] |= 0x00;
			}

			try {
				outputStream.write(byteArray, 0, byteArray.length);
			} catch (final IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public int getResponseLength() {
		return 1;
	}

	public void result(final ByteBuffer byteBuffer) {

	}

	public boolean isStraight() {
		return straight;
	}

	public void setStraight(final boolean straight) {
		this.straight = straight;
	}

	public byte[] getByteArray() {

		final byte[] byteArray = new byte[4];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0x90;

		// little endian
		byteArray[2] = (byte) (turnoutId & 0xFF);
		byteArray[3] = (byte) ((turnoutId >> 8) & 0xFF);

//		// big endian
//		byteArray[2] = (byte) ((turnoutId >> 8) & 0xFF);
//		byteArray[3] = (byte) (turnoutId & 0xFF);

		if (first) {

			if (straight) {
				// 0xC0 = 1100 0000
				byteArray[3] |= 0xC0;
			} else {
				// 0x40 = 0100 0000
				byteArray[3] |= 0x40;
			}

		} else {

			if (straight) {
				// 0x80 = 1000 0000
				byteArray[3] |= 0x80;
			} else {
				// 0x00 = 0000 0000
				byteArray[3] |= 0x00;
			}

		}

		return byteArray;
	}

}
