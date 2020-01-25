package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * XTrntSts (094h)
 *
 * <PROTOCOL_SWITCH> 0x94 <LOW BYTE OF TURNOUT ADDRESS (A7..A0)> <HIGH BYTE OF
 * TURNOUT ADDRESS (A10..A8)>
 *
 * <pre>
 * 78 94 10 00
 * </pre>
 */
public class P50XXTrntStsCommand implements Command {

	private static Logger logger = LogManager.getLogger(P50XXTrntStsCommand.class);

	private short turnoutId;

	private boolean thrown;

//	private final Node node;

	/**
	 * variable length response, -1 == PULL_MODE, read one more byte until 0 is
	 * returned
	 */
	private int responseLength = -1;

	private int status = 0;

	/**
	 * ctor
	 *
	 * @param node
	 */
	public P50XXTrntStsCommand(final short turnoutId) {
		this.turnoutId = turnoutId;
	}

	@Override
	public int getResponseLength() {
		return responseLength;
	}

	/**
	 * <pre>
	 * Reply (2 Byte):
	 *
	 * 1st	either 00h (cmd Ok, 1 byte shall follow) or error code
	 *
	 * 2nd
	 * 		bit #0: turnout configuration in bit #0: (check Bit #3 below, Bit #0 has to be combined with Bit #3)
	 *
	 * 		bit #1: turnout 'reserved' status in bit #1: 1 = reserved, 0 = free
	 *
	 * 		bit #2: turnout color in bit #2: 1 = green (closed), 0 = red (thrown)
	 *
	 * 		bit #3: turnout extended configuration in bit #3:
	 * 		Bit #0/3	turnout type
	 *	  		00		  Motorola
	 *	  		10		  DCC
	 *	  		01		  SX
	 *	  		11		  FMZ
	 *
	 * 		Other bits are reserved for future use.
	 *
	 * Error codes:
	 * XBADPRM (02h)	illegal parameter value
	 * XBADTNP (0Eh)	Error: illegal Turnout address for this protocol
	 * </pre>
	 */
	@Override
	public void result(final ByteBuffer byteBuffer) {

		logger.trace(getClass().getSimpleName() + " ByteBuffer Position: " + byteBuffer.position() + " data: "
				+ byteBuffer.toString());

		final byte[] array = byteBuffer.array();
		final String allDataAsHex = Hex.encodeHexString(array);

		logger.trace(allDataAsHex);

		for (int i = 0; i < byteBuffer.position(); i++) {

			if (status == 0) {

				final byte byte0 = byteBuffer.get(i);

				String errorDescription = "UNKNOWN ERROR!";

				if (byte0 != 0) {

					switch (byte0) {
					case (byte) 0x02:
						errorDescription = "illegal parameter value";
						break;

					case (byte) 0x0E:
						errorDescription = "Error: illegal Turnout address for this protocol";
						break;

					default:
						errorDescription = "UNKNOWN ERROR!";
					}

					logger.error("An error occured! Code: " + byteBuffer.get(0) + " Description: " + errorDescription);

					// command has read enough bytes, tell the template to stop
					responseLength = 0;

					return;
				}

				status++;

				// first byte did contain an OK code, so the command is waiting for the data
				// byte of the response
				// which contains the turnout status
//				index++;

			} else if (status == 1) {

				// if (byteBuffer.position() >= 2) {

				final byte byte1 = byteBuffer.get(i);

				logger.trace("byte1: " + byte1);

				final int byte1bit0 = ((byte1 >> 0) & 0x01);
				final int byte1bit1 = ((byte1 >> 1) & 0x01);
				final int byte1bit2 = ((byte1 >> 2) & 0x01);
				final int byte1bit3 = ((byte1 >> 3) & 0x01);

				logger.trace("byte1bit0: " + byte1bit0 + " byte1bit1: " + byte1bit1 + " byte1bit2: " + byte1bit2
						+ " byte1bit3: " + byte1bit3);

				final boolean reserved = byte1bit1 == 1;
				final boolean color = byte1bit2 == 1;
				thrown = !color;

				final int configurationAsInt = byte1bit0 * 2 + byte1bit3;
				final TurnoutConfigurationEnum configuration = TurnoutConfigurationEnum.values()[configurationAsInt];

				final StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append("\n");
				stringBuffer.append("ProtocolTurnoutID: ").append(turnoutId).append("\n");
				stringBuffer.append("Reserved: ").append(reserved).append("\n");
				stringBuffer.append("Color: ").append(color ? "green (closed)" : "red (thrown)").append("\n");
				stringBuffer.append("Thrown: ").append(thrown ? "true" : "false").append("\n");
				stringBuffer.append("Configuration: ").append(configuration.name()).append("\n");

				logger.trace(stringBuffer.toString());

				// all two bytes read
				responseLength = 0;

			} else {

				// want another byte
				responseLength = 1;

			}
		}
	}

	@Override
	public byte[] getByteArray() {

		logger.trace("getByteArray()");

		final byte[] byteArray = new byte[4];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0x94;

		logger.trace("Turnout ID: " + turnoutId);

		// little endian
		byteArray[2] = (byte) (turnoutId & 0xff);
		byteArray[3] = (byte) ((turnoutId >> 8) & 0xff);

		return byteArray;
	}

	public boolean isThrown() {
		return thrown;
	}

	public short getTurnoutId() {
		return turnoutId;
	}

	public void setTurnoutId(final short turnoutId) {
		this.turnoutId = turnoutId;
	}

}
