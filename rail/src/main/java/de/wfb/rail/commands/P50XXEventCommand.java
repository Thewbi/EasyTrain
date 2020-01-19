package de.wfb.rail.commands;

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 0xC8 Command (XEvent)
 *
 * This command is send by WinDigipet every 100ms. This command will ask the
 * intellibox about events that happened. If anything happened, WinDigipet will
 * follow up with the XEvtSen command to ask for more detailed in formation.
 */
public class P50XXEventCommand implements Command {

	private static Logger logger = LogManager.getLogger(P50XXEventCommand.class);

	/**
	 * variable length response, -1 == PULL_MODE, read one more byte until 0 is
	 * returned
	 */
	private int responseLength = -1;

	private int byteIndex = -1;

	private boolean xStatusShouldBeCalled;

	@Override
	public int getResponseLength() {
		return responseLength;
	}

	@Override
	public void result(final ByteBuffer byteBuffer) {

		final byte[] array = byteBuffer.array();

		for (int i = 0; i < byteBuffer.position(); i++) {

			final byte data = array[i];

			logger.trace("data = " + Hex.encodeHexString(new byte[] { data }));

			byteIndex++;

			switch (byteIndex) {

			case 0:
				processEventFlags1(data);
				break;

			case 1:
				processEventFlags2(data);
				break;

			case 2:
				processEventFlags3(data);
				break;

			default:
				break;
			}

			// if the uppermost bit is set, there is another byte of data to read
			// if it is not set, the command's response is completely read.
			if ((data >> 7 & 1) <= 0) {
				responseLength = 0;
			}
		}
	}

	/**
	 * <pre>
	 * 1st	event flags to be interpreted as:
	 *
	 *  bit#   7     6     5     4     3     2     1     0
	 *   +-----+-----+-----+-----+-----+-----+-----+-----+
	 *   | Ev2 |  x  |Trnt |TRes |PwOff| Sen | IR  | Lok |
	 *   +-----+-----+-----+-----+-----+-----+-----+-----+
	 *
	 * where:
	 *	Ev2	set if also the 2nd byte of the XEvent
	 *		    reply shall be sent (i.e.: there is at least
	 *		    one event also in the 2nd or in the 3rd byte)
	 *	x	    (reserved for future use)
	 *	Trnt	there has been at least one non-PC Turnout cmd
	 *	TRes	there has been at least one non-PC attempt at
	 *		    changing the status of a 'reserved' Turnout
	 *	PwOff	there **has been** (not: is!) a Power Off
	 *	Sen	    there has been at least one sensor event
	 *		    (s88 or LocoNet)
	 *	IR	    there has been at least one infra-red event
	 *	Lok	    there has been at least one non-PC Lok cmd
	 * </pre>
	 *
	 * @param data
	 */
	private void processEventFlags1(final byte data) {

		logger.trace("processEventFlags1() data = " + data);

		final boolean lok = (data & 0x01 << 0) > 0;
		if (lok) {
			logger.trace("there has been at least one non-PC Lok cmd");
		}
		final boolean ir = (data & 0x01 << 1) > 0;
		if (ir) {
			logger.info("there has been at least one infra-red event");
		}
		final boolean sen = (data & 0x01 << 2) > 0;
		if (sen) {
			logger.info("there has been at least one sensor event (s88 or LocoNet)");
			xStatusShouldBeCalled = true;
		}
		final boolean pwoff = (data & 0x01 << 3) > 0;
		if (pwoff) {
			logger.info("there **has been** (not: is!) a Power Off");
		}
		final boolean tres = (data & 0x01 << 4) > 0;
		if (tres) {
			logger.info("there has been at least one non-PC attempt at changing the status of a 'reserved' Turnout");
		}
		final boolean trnt = (data & 0x01 << 5) > 0;
		if (trnt) {
			logger.info("there has been at least one non-PC Lok cmd");
		}
		final boolean x = (data & 0x01 << 6) > 0;
		if (x) {
			logger.info("(reserved for future use)");
		}
		final boolean ev2 = (data & 0x01 << 7) > 0;
		if (ev2) {
			logger.trace("set if also the 2nd byte of the XEvent reply shall be sent");
		}
	}

	/**
	 * <pre>
	 * 2nd	event flags to be interpreted as:
	 *
	 * bit#   7     6     5     4     3     2     1     0
	 *   +-----+-----+-----+-----+-----+-----+-----+-----+
	 *   | Ev3 | Sts | Hot |PTSh |RSSh |IntSh|LMSh |ExtSh|
	 *   +-----+-----+-----+-----+-----+-----+-----+-----+
	 *
	 * where:
	 *	Ev3	set if also the 3rd byte of the XEvent reply
	 *		shall be sent (i.e.: there is at least an
	 *		event also in the 3rd byte)
	 *	Sts	an XStatus cmd should be issued
	 *	Hot	Overheating condition detected
	 *	PTSh	while the PT relay was on (e.g., PT in 'PT only'
	 *		mode), a non-allowed electrical connection btw
	 *		the Programming Track and the rest of the layout
	 *		has been detected
	 *	RSSh	overload (short) on the DCC Booster C/D lines
	 *		or on the LocoNet (B connector) Rail Sync +/-
	 *		lines (or on the PT, if the PT relay was on)
	 *	IntSh	(Internal Short) short reported by the internal Booster
	 *	LMSh	(Lokmaus Short) overload (short) on the Lokmaus bus
	 *	ExtSh	(External Short) short reported by an external Booster
	 * </pre>
	 *
	 * @param data
	 */
	private void processEventFlags2(final byte data) {

		logger.trace("processEventFlags2() data = " + data);

		final boolean extsh = (data & 0x01 << 0) > 0;
		if (extsh) {
			logger.info("short reported by an external Booster");
		}
		final boolean lmsh = (data & 0x01 << 1) > 0;
		if (lmsh) {
			logger.info("overload (short) on the Lokmaus bus");
		}
		final boolean intsh = (data & 0x01 << 2) > 0;
		if (intsh) {
			logger.info("short reported by the internal Booster");
		}
		final boolean rssh = (data & 0x01 << 3) > 0;
		if (rssh) {
			logger.info("overload (short) on the DCC Booster C/D lines\n"
					+ "	 *		or on the LocoNet (B connector) Rail Sync +/-\n"
					+ "	 *		lines (or on the PT, if the PT relay was on)");
		}
		final boolean ptsh = (data & 0x01 << 4) > 0;
		if (ptsh) {
			logger.info("while the PT relay was on (e.g., PT in 'PT only'\n"
					+ "	 *		mode), a non-allowed electrical connection btw\n"
					+ "	 *		the Programming Track and the rest of the layout\n" + "	 *		has been detected");
		}
		final boolean hot = (data & 0x01 << 5) > 0;
		if (hot) {
			logger.info("Overheating condition detected");
		}
		final boolean sts = (data & 0x01 << 6) > 0;
		if (sts) {
			logger.trace("an XStatus cmd should be issued");
			xStatusShouldBeCalled = true;
		}
		final boolean ev3 = (data & 0x01 << 7) > 0;
		if (ev3) {
			logger.trace("set if also the 3rd byte of the XEvent reply shall be sent");
		}

	}

	/**
	 * <pre>
	 * 3rd	event flags to be interpreted as:
	 *
	 * bit#   7     6     5     4     3     2     1     0
	 *   +-----+-----+-----+-----+-----+-----+-----+-----+
	 *   | Ev4 |  x  |  x  |ExVlt|TkRel| Mem |RSOF |  PT |
	 *   +-----+-----+-----+-----+-----+-----+-----+-----+
	 *
	 * where:
	 *	Ev4	not currently used, reported as 0
	 *	x	not currently used
	 *	ExVlt	an external voltage source is present
	 *		(prior to turning on the layout). E.g.
	 *		an external transformer is in contact
	 *		with the rails.
	 *	TkRel	report Lok 'take' and 'release' events
	 *		from non-PC Lok controllers
	 *		(to be documented)
	 *	Mem	set if there has been at least one
	 *		'memory' event (to be documented - related
	 *		to the future IB 'memory' software expansion)
	 *	RSOF	set if an RS-232 rx overflow has been
	 *		detected (the PC probably does not
	 *		correctly handle the CTS line)
	 *	PT	a PT event is available.
	 *		N.B. This bit must be 'cleared' by sending
	 *		     the XPT_Event cmd!
	 *
	 * </pre>
	 *
	 * @param data
	 */
	private void processEventFlags3(final byte data) {

		logger.trace("processEventFlags3() data = " + data);

		final boolean pt = (data & 0x01 << 0) > 0;
		if (pt) {
			logger.info("a PT event is available.\n" + "	 *		N.B. This bit must be 'cleared' by sending\n"
					+ "	 *		     the XPT_Event cmd!");
		}
		final boolean rsof = (data & 0x01 << 1) > 0;
		if (rsof) {
			logger.info("set if an RS-232 rx overflow has been\n" + "	 *		detected (the PC probably does not\n"
					+ "	 *		correctly handle the CTS line)");
		}
		final boolean mem = (data & 0x01 << 2) > 0;
		if (mem) {
			logger.info(
					"set if there has been at least one\n" + "	 *		'memory' event (to be documented - related\n"
							+ "	 *		to the future IB 'memory' software expansion)");
		}
		final boolean tkrel = (data & 0x01 << 3) > 0;
		if (tkrel) {
			logger.info("report Lok 'take' and 'release' events\n" + "	 *		from non-PC Lok controllers\n"
					+ "	 *		(to be documented)");
		}
		final boolean exvlt = (data & 0x01 << 4) > 0;
		if (exvlt) {
			logger.info(
					"an external voltage source is present\n" + "	 *		(prior to turning on the layout). E.g.\n"
							+ "	 *		an external transformer is in contact\n" + "	 *		with the rails.");
		}
		final boolean x1 = (data & 0x01 << 5) > 0;
		if (x1) {
			logger.info("not currently used");
		}
		final boolean x2 = (data & 0x01 << 6) > 0;
		if (x2) {
			logger.info("not currently used");
		}
		final boolean ev4 = (data & 0x01 << 7) > 0;
		if (ev4) {
			logger.info("not currently used, reported as 0");
		}

	}

	@Override
	public byte[] getByteArray() {

		final byte[] byteArray = new byte[2];
		byteArray[0] = (byte) 0x58;
		byteArray[1] = (byte) 0xC8;

		return byteArray;
	}

	public boolean isxStatusShouldBeCalled() {
		return xStatusShouldBeCalled;
	}

}
