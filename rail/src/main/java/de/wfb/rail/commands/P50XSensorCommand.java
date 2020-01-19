package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Request the contacts of a S88 feedback contact.
 */
public class P50XSensorCommand extends BaseEventCommand {

	private static Logger logger = LogManager.getLogger(P50XSensorCommand.class);

	/** 1-based not 0-based. The first contact has the ID 1 */
	private int s88FeedbackCommandId = 1;

	/**
	 * ctor
	 *
	 * @param s88FeedbackCommandId
	 */
	public P50XSensorCommand(final int s88FeedbackCommandId) {
		super();
		this.s88FeedbackCommandId = s88FeedbackCommandId;
	}

	@Override
	public int getResponseLength() {
		return 3;
	}

	@Override
	public void result(final ByteBuffer byteBuffer) {

		final byte[] array = byteBuffer.array();

		final String allDataAsHex = Hex.encodeHexString(array);
		logger.info("index = " + index + " byteBuffer.position(): " + byteBuffer.position() + " allDataAsHex = "
				+ allDataAsHex);

		for (int i = 0; i < byteBuffer.position(); i++) {

			final byte data = array[i];

			final String dataAsHex = Hex.encodeHexString(new byte[] { data });
			logger.info("data = " + dataAsHex);

			switch (index) {
			case 0:

				// 0 means ok
				s88ID = s88FeedbackCommandId;
				break;

			case 1:
				inputDescriptor1 = data;
				break;

			case 2:
				inputDescriptor2 = data;
				sendFeedbackBlockUpdateEvents(s88ID, inputDescriptor1, inputDescriptor2);
				break;
			}

			index++;
			if (index > 2) {
				index = 0;
			}
		}

	}

	@Override
	public byte[] getByteArray() {

		final byte[] byteArray = new byte[3];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0x98;
		byteArray[2] = (byte) s88FeedbackCommandId;

		return byteArray;
	}

	public int getS88FeedbackCommandId() {
		return s88FeedbackCommandId;
	}

	public void setS88FeedbackCommandId(final int s88FeedbackCommandId) {
		this.s88FeedbackCommandId = s88FeedbackCommandId;
	}

}
