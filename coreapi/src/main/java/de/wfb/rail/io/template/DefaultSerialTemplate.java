package de.wfb.rail.io.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.commands.Command;

public class DefaultSerialTemplate implements SerialTemplate {

	private static Logger logger = LogManager.getLogger(DefaultSerialTemplate.class);

	private final OutputStream outputStream;

	private final InputStream inputStream;

	private final Command command;

	/**
	 * ctor
	 *
	 * @param outputStream
	 * @param command
	 */
	public DefaultSerialTemplate(final OutputStream outputStream, final InputStream inputStream,
			final Command command) {

		this.outputStream = outputStream;
		this.inputStream = inputStream;
		this.command = command;
	}

	@Override
	public synchronized void execute() {

		if (outputStream == null) {
			return;
		}

		final byte[] byteArray = command.getByteArray();

		logger.trace("REQUEST: " + Hex.encodeHexString(byteArray));

		try {
			logger.info("writing ...");
			outputStream.write(byteArray, 0, byteArray.length);
			logger.info("writing done.");
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}

		try {
			final int responseLength = command.getResponseLength();

			if (responseLength > 0) {

				processFixedLengthResponse(responseLength);

			} else {

				processVariableLengthResponse();
			}
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void processFixedLengthResponse(final int responseLength) throws IOException {

		logger.trace("processFixedLengthResponse(): " + responseLength);

		final ByteBuffer byteBuffer = ByteBuffer.allocate(responseLength + 10);

		final byte[] tempBuffer = new byte[1024];
		int consumedLength = 0;

		int loopBreaker = 40;

		// read bytes until the command says that enough bytes have been read
		do {

			loopBreaker--;
			if (loopBreaker < 0) {

				logger.info("LoopBreaker ...");
				break;
			}

			final int available = inputStream.available();
			logger.trace("reading ... available: " + available);

			if (available <= 0) {
				try {
					Thread.sleep(200);
				} catch (final InterruptedException e) {
					logger.info(e.getMessage(), e);
				}

				continue;
			}

			final int bytesRead = inputStream.read(tempBuffer);
			logger.trace("bytesRead: " + bytesRead);

			final String allDataAsHex = Hex.encodeHexString(tempBuffer);
			logger.trace("allDataAsHex: " + allDataAsHex);

			if (bytesRead == -1) {

				final String msg = "Cannot retrieve response!";
				logger.error(msg);
				throw new IOException(msg);
			}

			byteBuffer.put(tempBuffer, 0, bytesRead);

			consumedLength += bytesRead;

		} while (consumedLength < command.getResponseLength());

		logger.trace("RESPONSE: " + byteBuffer.toString());

		command.result(byteBuffer);
	}

	private void processVariableLengthResponse() throws IOException {

		logger.trace("processVariableLengthResponse() ...");

		@SuppressWarnings("unused")
		int consumedLength = 0;

		int mode = -1;

		while (mode == -1) {

			final byte[] tempBuffer = new byte[1024];

			// read into the buffer
			final int bytesRead = inputStream.read(tempBuffer);

			logger.trace("bytesRead: " + bytesRead + " mode = " + mode);

			if (bytesRead == -1) {

				final String msg = "Cannot retrieve response!";
				logger.error(msg);
				throw new IOException(msg);
			}

			final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
			byteBuffer.put(tempBuffer, 0, bytesRead);

			consumedLength += bytesRead;

			// put the data read so far into the command
			command.result(byteBuffer);

			// ask the command to read more or to stop reading
			mode = command.getResponseLength();
		}

	}

}
