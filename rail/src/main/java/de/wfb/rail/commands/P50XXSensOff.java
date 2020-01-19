package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Causes the controller hardware (Intellibox) to flag S88 Feedback Modules as
 * changed if
 * <ul>
 * <li />they have at least a single bit set
 * </ul>
 *
 * The EvtSenCommandThread periodically sends the XEvent command. If one of the
 * S88 modules is flagged as changed, the result of the XEvent command will
 * contain a recommendation to send a XEvtSenCommand.
 *
 * The XEvtSenCommand contains a list of the new states of all changed S88
 * feedback modules.
 *
 * A S88 Feedback Module that has a 'changed' flag set, will show up in the list
 * of the controller states of a XEvtSenCommand.
 *
 * That means that XSensOff will cause the system to send a list of all S88
 * modules that have at least one of their contacts set to 1.
 *
 * You can initialize all your Feedback module contacts to 0, send the XSensOff
 * event, retrieve a list of the S88 modules via a XEvtSen command and update
 * the feedback module state with the result. By doing that, you have
 * synchronized the state of all feedback modules.
 */
public class P50XXSensOff extends BaseEventCommand {

	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger(P50XXSensOff.class);

	@Override
	public int getResponseLength() {

		// one byte error code.
		// value 0 means OK, no error, I think ????
		return 1;
	}

	@Override
	public void result(final ByteBuffer byteBuffer) {
		// nop
	}

	@Override
	public byte[] getByteArray() {

		final byte[] byteArray = new byte[2];
		byteArray[0] = (byte) 0x78;
		byteArray[1] = (byte) 0x99;

		return byteArray;
	}

}
