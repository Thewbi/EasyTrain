package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <pre>
 * 0x4A = 74
0x60 = 0110 0000 = Force, Dir = 1 = forward
0x40 = 0100 0000 = Force, Dir = 0 = reverse

[CMD_START] [CMD_ID] [LOK_ID_HIGH_BYTE] [LOK_ID_LOW_BYTE] [SPEED] 	[FLAGS]

0x78        0x80     0x4A               0x00              0x00    	0x60

0x78        0x80     0x4A               0x00              0x1E = 30 0x40
 * </pre>
 */
public class P50XXLokCommand implements Command {

	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger(P50XXLokCommand.class);

	private final short locomotiveAddress;

	private final double throttleValue;

	private final boolean dirForward;

	public P50XXLokCommand(final short locomotiveAddress, final double throttleValue, final boolean dirForward) {

		this.locomotiveAddress = locomotiveAddress;
		this.throttleValue = throttleValue;
		this.dirForward = dirForward;
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

		final byte[] byteArray = new byte[6];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0x80;

//		// little endian
		byteArray[2] = (byte) (locomotiveAddress & 0xff);
		byteArray[3] = (byte) ((locomotiveAddress >> 8) & 0xff);

		final Double throttleValueAsDouble = Double.valueOf(throttleValue);

		byteArray[4] = throttleValueAsDouble.byteValue();

		byteArray[5] = 0x40;
		if (dirForward) {
			byteArray[5] |= 0x20;
		}

		return byteArray;
	}

}
