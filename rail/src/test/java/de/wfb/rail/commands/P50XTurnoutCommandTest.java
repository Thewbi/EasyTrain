package de.wfb.rail.commands;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import de.wfb.rail.commands.P50XTurnoutCommand;

public class P50XTurnoutCommandTest {

	@Test
	public void ByteArrayTest() {

		P50XTurnoutCommand command = new P50XTurnoutCommand((short) 153, true, true);
		Assert.assertEquals("789099c0", Hex.encodeHexString(command.getByteArray()));

		command = new P50XTurnoutCommand((short) 153, true, false);
		Assert.assertEquals("78909980", Hex.encodeHexString(command.getByteArray()));

		command = new P50XTurnoutCommand((short) 153, false, true);
		Assert.assertEquals("78909940", Hex.encodeHexString(command.getByteArray()));

		command = new P50XTurnoutCommand((short) 153, false, false);
		Assert.assertEquals("78909900", Hex.encodeHexString(command.getByteArray()));
	}

}
