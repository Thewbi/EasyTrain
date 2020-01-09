package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.rail.events.FeedbackBlockState;
import de.wfb.rail.events.FeedbackBlockUpdateEvent;

/**
 * https://tams-online.de/WebRoot/Store11/Shops/642f1858-c39b-4b7d-af86-f6a1feaca0e4/MediaGallery/15_Download/Sonstiges/interface.txt
 * https://www.opendcc.de/elektronik/opendcc/opendcc_doc_ib.html
 */
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

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public int getResponseLength() {
		return responseLength;
	}

	@Override
	public void result(final ByteBuffer byteBuffer) {

		final byte[] array = byteBuffer.array();

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
				retrieveConnectionIds(s88ID, inputDescriptor1, inputDescriptor2);
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
	private void retrieveConnectionIds(final int s88id, final byte inputDescriptor1, final byte inputDescriptor2) {

		final FeedbackBlockUpdateEvent feedbackBlockUpdateEvent = new FeedbackBlockUpdateEvent(this);

		final int base = 16 * (s88id - 1);

		int currentOffset = 1;

		// ausgaenge 1 - 8 am modul (laut
		// https://tams-online.de/WebRoot/Store11/Shops/642f1858-c39b-4b7d-af86-f6a1feaca0e4/MediaGallery/15_Download/Sonstiges/interface.txt)
		for (byte i = 7; i >= 0; i--) {

			final int value = ((inputDescriptor1 >> i) & 1);
			final int offset = base + currentOffset;

			logger.info("Contact " + offset + (value == 0 ? ") UNUSED" : ") USED"));

			feedbackBlockUpdateEvent.getFeedbackBlockState()[offset - 1] = value == 0 ? FeedbackBlockState.FREE
					: FeedbackBlockState.BLOCKED;

			currentOffset++;
		}

		// ausgaenge 9 - 16 am modul (laut
		// https://tams-online.de/WebRoot/Store11/Shops/642f1858-c39b-4b7d-af86-f6a1feaca0e4/MediaGallery/15_Download/Sonstiges/interface.txt)
		for (byte i = 7; i >= 0; i--) {

			final int value = ((inputDescriptor2 >> i) & 1);
			final int offset = base + currentOffset;

			logger.info("Contact " + offset + (value == 0 ? ") UNUSED" : ") USED"));

			feedbackBlockUpdateEvent.getFeedbackBlockState()[offset - 1] = value == 0 ? FeedbackBlockState.FREE
					: FeedbackBlockState.BLOCKED;

			currentOffset++;
		}

		// send event
		applicationEventPublisher.publishEvent(feedbackBlockUpdateEvent);
	}

	@Override
	public byte[] getByteArray() {

		final byte[] byteArray = new byte[2];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0xCB;

		return byteArray;
	}

	public ApplicationEventPublisher getApplicationEventPublisher() {
		return applicationEventPublisher;
	}

	public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

}
