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
	public void execute() {

		final byte[] byteArray = command.getByteArray();

		logger.trace("REQUEST: " + Hex.encodeHexString(byteArray));

		try {
			outputStream.write(byteArray, 0, byteArray.length);
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

	private void processVariableLengthResponse() throws IOException {

		int consumedLength = 0;

		int mode = -1;

		while (mode == -1) {

			final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
			final byte[] tempBuffer = new byte[1024];
			final int bytesRead = inputStream.read(tempBuffer);

			// logger.info("bytesRead: " + bytesRead);

			if (bytesRead == -1) {

				final String msg = "Cannot retrieve response!";
				logger.error(msg);
				throw new IOException(msg);
			}

			byteBuffer.put(tempBuffer, consumedLength, bytesRead);

			consumedLength += bytesRead;

			command.result(byteBuffer);
			mode = command.getResponseLength();
		}

	}

	private void processFixedLengthResponse(final int responseLength) throws IOException {

		final ByteBuffer byteBuffer = ByteBuffer.allocate(responseLength + 10);

		final byte[] tempBuffer = new byte[1024];
		int consumedLength = 0;

		// read bytes until the command says that enough bytes have been read
		do {

			final int bytesRead = inputStream.read(tempBuffer);

			logger.trace("bytesRead: " + bytesRead);

			if (bytesRead == -1) {

				final String msg = "Cannot retrieve response!";
				logger.error(msg);
				throw new IOException(msg);
			}

			byteBuffer.put(tempBuffer, consumedLength, bytesRead);

			consumedLength += bytesRead;

		} while (consumedLength < command.getResponseLength());

		logger.trace("RESPONSE: " + byteBuffer.toString());

		command.result(byteBuffer);
	}

}
