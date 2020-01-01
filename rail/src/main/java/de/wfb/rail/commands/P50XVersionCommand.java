package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.commands.Command;

/**
 * XVer (0A0h) - length = 1 byte - show versions
 *
 * 78 A0
 *
 * <pre>
 * 24	10:53:09.248	0.00004966	RailTCB32.exe	IRP_MJ_WRITE     COM6	SUCCESS	Length: 2, Data: 78 A0
 * 25	10:53:09.273	0.00000828	RailTCB32.exe	IRP_MJ_READ      COM6	SUCCESS	Length: 8, Data: 02 50 15 02 50 15 01 15
 * 26	10:53:09.293	0.00000728	RailTCB32.exe	IRP_MJ_READ      COM6	SUCCESS	Length: 8, Data: 01 10 01 14 05 10 00 02
 * 27	10:53:09.303	0.00000728	RailTCB32.exe	IRP_MJ_READ      COM6	SUCCESS	Length: 3, Data: 61 08 00
 * </pre>
 */
public class P50XVersionCommand implements Command {

	private static Logger logger = LogManager.getLogger(P50XVersionCommand.class);

//	public void execute(final OutputStream outputStream) {
//
//		logger.info("Version");
//
//		try {
//			final byte[] byteArray = Hex.decodeHex("78A0".toCharArray());
//			outputStream.write(byteArray, 0, byteArray.length);
//		} catch (final IOException e) {
//			e.printStackTrace();
//		} catch (final DecoderException e) {
//			e.printStackTrace();
//		}
//
//	}

	public int getResponseLength() {
		return 19;
	}

	public void result(final ByteBuffer byteBuffer) {
		// TODO Auto-generated method stub

	}

	public byte[] getByteArray() {

		final byte[] byteArray = new byte[2];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0xA0;

		return byteArray;
	}

}
