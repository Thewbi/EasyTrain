package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.node.Node;

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

//	private short turnoutId;

	private boolean thrown;

	private final Node node;

	/**
	 * ctor
	 *
	 * @param node
	 */
	public P50XXTrntStsCommand(final Node node) {
		this.node = node;
//		this.turnoutId = node.getProtocolTurnoutId().shortValue();
	}

//	public void execute(final OutputStream outputStream) {
//
//		logger.info(getClass().getSimpleName());
//
//		try {
//			// final String cmd = "78941000";
//			// final String cmd = "78946F00";
//			// final String cmd = "78949900";
//
////			final StringBuffer stringBuffer = new StringBuffer(4);
////			stringBuffer.append("7894");
//
//			// final byte[] byteArray =
//			// Hex.decodeHex(stringBuffer.toString().toCharArray());
//
//			// outputStream.write(byteArray, 0, byteArray.length);
//
//			final byte[] byteArray = new byte[4];
//			byteArray[0] = (byte) 0x78;
//			byteArray[1] = (byte) 0x94;
//
////			// little endian
////			byteArray[2] = (byte) (turnoutId & 0xff);
////			byteArray[3] = (byte) ((turnoutId >> 8) & 0xff);
//
//			short turnoutId = node.getProtocolTurnoutId().shortValue();
//
//			// big endian
//			byteArray[2] = (byte) ((turnoutId >> 8) & 0xFF);
//			byteArray[3] = (byte) (turnoutId & 0xFF);
//
//			outputStream.write(byteArray, 0, byteArray.length);
//
//		} catch (final IOException e) {
//			e.printStackTrace();
//		}
//	}

	@Override
	public int getResponseLength() {
		return 2;
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

		logger.info(getClass().getSimpleName() + " " + byteBuffer.toString());

		final byte byte0 = byteBuffer.get(0);

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

			return;
		}

		final byte byte1 = byteBuffer.get(1);

		final int byte1bit0 = (byte1 & 0x01 << 0);
		final int byte1bit1 = (byte1 & 0x01 << 1);
		final int byte1bit2 = (byte1 & 0x01 << 2);
		final int byte1bit3 = (byte1 & 0x01 << 3);

		final boolean reserved = byte1bit1 == 1;
		final boolean color = byte1bit2 == 1;

		final int configurationAsInt = byte1bit0 * 2 + byte1bit3;
		final TurnoutConfigurationEnum configuration = TurnoutConfigurationEnum.values()[configurationAsInt];

		final StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append("Reserved: ").append(reserved).append("\n");
		stringBuffer.append("Color: ").append(color ? "green (closed)" : "red (thrown)").append("\n");
		stringBuffer.append("Configuration: ").append(configuration.name()).append("\n");

		thrown = !color;
	}

//	public short getTurnoutId() {
//		return turnoutId;
//	}
//
//	public void setTurnoutId(final short turnoutId) {
//		this.turnoutId = turnoutId;
//	}

	@Override
	public byte[] getByteArray() {

		final byte[] byteArray = new byte[4];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0x94;

//		// little endian
//		byteArray[2] = (byte) (turnoutId & 0xff);
//		byteArray[3] = (byte) ((turnoutId >> 8) & 0xff);

		final short turnoutId = node.getProtocolTurnoutId().shortValue();

		// big endian
		byteArray[2] = (byte) ((turnoutId >> 8) & 0xFF);
		byteArray[3] = (byte) (turnoutId & 0xFF);

		return byteArray;
	}

	public boolean isThrown() {
		return thrown;
	}

}
