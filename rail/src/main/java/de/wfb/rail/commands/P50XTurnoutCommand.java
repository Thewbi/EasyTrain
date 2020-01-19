package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger(P50XTurnoutCommand.class);

	private final short turnoutId;

	private final boolean thrown;

	private final boolean first;

	/**
	 * ctor
	 *
	 * @param turnoutId
	 * @param straight
	 */
	public P50XTurnoutCommand(final short turnoutId, final boolean thrown, final boolean first) {

		this.turnoutId = turnoutId;
		this.thrown = thrown;
		this.first = first;
	}

	@Override
	public int getResponseLength() {
		return 1;
	}

	@Override
	public void result(final ByteBuffer byteBuffer) {

	}

	@Override
	public byte[] getByteArray() {

		final byte[] byteArray = new byte[4];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0x90;

		// little endian
		byteArray[2] = (byte) (turnoutId & 0xFF);
		byteArray[3] = (byte) ((turnoutId >> 8) & 0xFF);

		if (first) {

			if (thrown) {

				// 0x40 = 0100 0000
				byteArray[3] |= 0x40;

			} else {

				// 0xC0 = 1100 0000
				byteArray[3] |= 0xC0;

			}

		} else {

			if (thrown) {

				// 0x00 = 0000 0000
				byteArray[3] |= 0x00;

			} else {

				// 0x80 = 1000 0000
				byteArray[3] |= 0x80;

			}
		}

		return byteArray;
	}

}
