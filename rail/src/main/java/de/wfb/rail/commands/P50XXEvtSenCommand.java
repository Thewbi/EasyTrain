package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * https://tams-online.de/WebRoot/Store11/Shops/642f1858-c39b-4b7d-af86-f6a1feaca0e4/MediaGallery/15_Download/Sonstiges/interface.txt
 * https://www.opendcc.de/elektronik/opendcc/opendcc_doc_ib.html
 *
 * The thread EvtSenCommandThread sends the P50XXEventCommand periodically. When
 * the P50XXEventCommand determines that a change has occurred, this
 * P50XXEvtSenCommand command is executed to retrieve detailed information about
 * the event change.
 *
 * This command is executed in
 * de.wfb.rail.service.DefaultProtocolService.event()
 */
public class P50XXEvtSenCommand extends BaseEventCommand {

	private static Logger logger = LogManager.getLogger(P50XXEvtSenCommand.class);

	/**
	 * variable length response, -1 == PULL_MODE, read one more byte until 0 is
	 * returned
	 */
	private int responseLength = -1;

	@Override
	public int getResponseLength() {
		return responseLength;
	}

	@Override
	public void result(final ByteBuffer byteBuffer) {

		try {

			final byte[] array = byteBuffer.array();

			final String allDataAsHex = Hex.encodeHexString(array);
			logger.trace("index = " + index + " byteBuffer.position(): " + byteBuffer.position() + " allDataAsHex = "
					+ allDataAsHex);

			for (int i = 0; i < byteBuffer.position(); i++) {

				final byte data = array[i];

				final String dataAsHex = Hex.encodeHexString(new byte[] { data });
				logger.trace("data = " + dataAsHex);

				switch (index) {
				case 0:

					// end of data stream
					if (data == 0) {

						responseLength = 0;
						return;
					}
					s88ID = data;
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
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
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
