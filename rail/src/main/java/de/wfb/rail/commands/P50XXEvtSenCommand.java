package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class P50XXEvtSenCommand implements Command {

	private static Logger logger = LogManager.getLogger(P50XXEvtSenCommand.class);

	/**
	 * variable length response, -1 == PULL_MODE, read one more byte until 0 is
	 * returned
	 */
	private int responseLength = -1;

	private int index = 0;

	private int s88ID;

	private byte inputDescriptor1;

	private byte inputDescriptor2;

	@Override
	public int getResponseLength() {
		return responseLength;
	}

	@Override
	public void result(final ByteBuffer byteBuffer) {

//		logger.trace(Hex.encodeHexString(byteBuffer));

		final byte[] array = byteBuffer.array();

		for (int i = 0; i < byteBuffer.position(); i++) {

			final byte data = array[i];
			final String dataAsHex = Hex.encodeHexString(new byte[] { data });

//			logger.trace("data = " + dataAsHex);

			switch (index) {
			case 0:

				// end of data stream
				if (data == 0) {
					responseLength = 0;
					return;
				}
				s88ID = data;
//				logger.info("S88 ID = 0x" + dataAsHex);
				break;

			case 1:
				inputDescriptor1 = data;
//				logger.info("S88 data 1 = 0x" + dataAsHex);
				break;

			case 2:
				inputDescriptor2 = data;
				retrieveConnectionId(s88ID, inputDescriptor1, inputDescriptor2);
//				logger.info("S88 data 2 = 0x" + dataAsHex);
				break;
			}

			index++;
			if (index > 2) {
				index = 0;
			}
		}
	}

	/**
	 * Outputs all contacts that are signaled in use by this state change event!
	 *
	 * @param s88id
	 * @param inputDescriptor1
	 * @param inputDescriptor2
	 */
	private void retrieveConnectionId(final int s88id, final byte inputDescriptor1, final byte inputDescriptor2) {

		if (inputDescriptor1 == 0 && inputDescriptor2 == 0) {

			logger.info("No contact used!");

			return;
		}

		final int base = 16 * (s88id - 1);

		int currentOffset = 1;

		for (byte i = 7; i >= 0; i--) {

			if (((inputDescriptor2 >> i) & 1) > 0) {

				logger.info("Contact used: " + (base + currentOffset));
			}

			currentOffset++;
		}

		for (byte i = 7; i >= 0; i--) {

			if (((inputDescriptor1 >> i) & 1) > 0) {

				logger.info("Contact used: " + (base + currentOffset));
			}

			currentOffset++;
		}

	}

	@Override
	public byte[] getByteArray() {

		final byte[] byteArray = new byte[2];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0xCB;

		return byteArray;
	}

}
