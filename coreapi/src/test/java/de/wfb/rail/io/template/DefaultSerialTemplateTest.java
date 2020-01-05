package de.wfb.rail.io.template;

import org.junit.Test;

public class DefaultSerialTemplateTest {

	/**
	 * This test validates, that a command that initiates pull mode to consume its
	 * variable length response is handled correctly.
	 */
	@Test
	public void PullModeTest() {

		final DummyOutputStream dummyOutputStream = new DummyOutputStream();
		final DummyInputStream dummyInputStream = new DummyInputStream();

		final PullModeCommand pullModeCommand = new PullModeCommand();

		final DefaultSerialTemplate defaultSerialTemplate = new DefaultSerialTemplate(dummyOutputStream,
				dummyInputStream, pullModeCommand);

		defaultSerialTemplate.execute();

	}

}
