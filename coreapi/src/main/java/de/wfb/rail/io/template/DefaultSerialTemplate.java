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

		// send request
		// command.execute(outputStream);

		final byte[] byteArray = command.getByteArray();

		logger.info("REQUEST: " + Hex.encodeHexString(byteArray));

		try {
			outputStream.write(byteArray, 0, byteArray.length);
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// retrieve response
		// final byte[] buffer = new byte[1024];

		final ByteBuffer byteBuffer = ByteBuffer.allocate(command.getResponseLength() + 10);

		final byte[] tempBuffer = new byte[1024];
		int consumedLength = 0;

		try {

			// eat bytes until the command says that enough bytes have been eaten
			do {

				final int bytesRead = this.inputStream.read(tempBuffer);

				logger.info("bytesRead: " + bytesRead);

				if (bytesRead == -1) {
					logger.error("Cannot retrieve response!");
					return;
				}

				byteBuffer.put(tempBuffer, consumedLength, bytesRead);

				consumedLength += bytesRead;

			} while (consumedLength < command.getResponseLength());

		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}

		logger.info("RESPONSE: " + byteBuffer.toString());

		command.result(byteBuffer);
	}

}
