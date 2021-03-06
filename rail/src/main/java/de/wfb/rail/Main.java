package de.wfb.rail;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.commands.Command;
import de.wfb.rail.commands.P50XTurnoutCommand;
import de.wfb.rail.commands.P50XVersionCommand;
import de.wfb.rail.commands.P50XXNOPCommand;
import de.wfb.rail.factory.NRSerialPortFactory;
import de.wfb.rail.io.template.DefaultSerialTemplate;
import de.wfb.rail.io.template.SerialTemplate;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;

/**
 * MAC OS: Finding names of serial ports: Apple Menu > About this MAC > System
 * Report
 *
 * From: https://www.jmri.org/install/MacOSX.shtml
 *
 * <pre>
 * while : ;do clear;ls -lt /dev|head;i=$((i+1));echo $i;sleep 1;done
 * </pre>
 *
 * You want the cu - version, not the tty - version.
 *
 * <pre>
 * cd /dev
 * ls -la cu*
 * cu.usbserial - AO007Q6Q
 * </pre>
 *
 * http://rxtx.qbang.org/wiki/index.php/Using_RXTX
 *
 * https://stackoverflow.com/questions/2410384/managing-native-libraries-with-maven
 *
 * ERROR: On MAC: java.lang.UnsatisfiedLinkError: no rxtxSerial in
 * java.library.path thrown while loading gnu.io.RXTXCommDriver
 *
 * SOLUTION: Read http://rxtx.qbang.org/wiki/index.php/Installation_on_MacOS_X
 * You have to build RXTX.
 * <ol>
 *
 * <li>Download the source code for the latest version
 * (http://rxtx.qbang.org/wiki/index.php/Download). At the time of this writing
 * rxtx-2.1-7r2.zip.</li>
 *
 * <li>unzip the source</li>
 *
 * <li>
 *
 * <pre>
 * cd rxtx-2.1-7r2
 * sh ./configure
 * make all CC=/usr/bin/clang
 * </pre>
 *
 * </li>
 *
 * <li>Find the installation folder of your JDK
 *
 * <pre>
 * /Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home
 * </pre>
 *
 * </li>
 *
 * <li>Find the installation folder of your platform dependent JDK files: First,
 * find where your platform dependent jni_md.h file is located:
 *
 * <pre>
 * find / -name jni_md.h 2> /dev/null
 * </pre>
 *
 * Result on my machine:
 *
 * <pre>
 * /Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/include/darwin/jni_md.h
 * </pre>
 *
 * </li>
 *
 * <li>Edit the generated make file
 *
 * <pre>
 * JAVAINCLUDE = -I$(JAVAINCLUDEDIR) -I$(JAVAINCLUDEDIR_NATIVE)
 * JAVAINCLUDEDIR = /Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/include
 * JAVAINCLUDEDIR_NATIVE = /Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/include/darwin
 * </pre>
 *
 * </li>
 *
 *
 * <li>If everything has gone to plan you should have a directory corresponding
 * to the system's unix name, for example "powerpc-apple-darwin8.11.0", with the
 * native library 'librxtxSerial.jnilib' and in the current directory there
 * should be the java librar 'RXTXcomm.jar'. See the section on "installing
 * binaries" below, on where to put things.</li>
 *
 * <li>Copy the 'i686-apple-darwin18.5.0/librxtxSerial.jnilib' into the eclipse
 * Java project folder:
 *
 * <pre>
 * mkdir /Users/bischowg/Documents/workspace_p50x/rail/lib
 * cp /Users/bischowg/Downloads/rxtx-2.1-7r2/i686-apple-darwin18.5.0/librxtxSerial.jnilib /Users/bischowg/Documents/workspace_p50x/rail/lib/librxtxSerial.jnilib
 * cp /Users/bischowg/Downloads/rxtx-2.1-7r2/RXTXComm.jar /Users/bischowg/Documents/workspace_p50x/rail/lib/RXTXComm.jar
 * </pre>
 *
 * See:
 *
 * <pre>
 *
 * DOES NOT WORK:
 * You have to add the location of the folder that contains librxtxSerial.jnilib as Native library location:
 *
 *    1. Right-click project and choose Properties
 *    2. In Java Build Path, in the Libraries expand the RXTX JAR node
 *    3. Select the subnode Native library location: (None) and click Edit
 *    4. Enter the folder location that contains librxtxSerial.jnilib and click OK
 *
 * </pre>
 *
 *
 * </li>
 *
 * <li>You have to specify the folder using a command line parameter:
 *
 * <pre>
 * -Djava.library.path=/Users/bischowg/Documents/workspace_p50x/rail/lib
 * -Djava.library.path=/Users/bischowg/Documents/workspace_javafx/rail/lib
 * </pre>
 *
 * </li>
 *
 * </ol>
 *
 *
 *
 *
 * PROBLEM: gnu.io.PortInUseException: Unknown Application
 *
 * SOLUTION: https://playground.arduino.cc/Interfacing/Java/
 *
 * <pre>
 * sudo mkdir /var/lock
 * sudo dscl . -append /groups/_uucp GroupMembership bischowg
 * sudo chgrp uucp /var/lock
 * sudo chmod 775 /var/lock
 * sudo open /Users/bischowg/eclipse/jee-2018-09/Eclipse.app/
 * </pre>
 *
 *
 *
 *
 *
 * PROBLEM: check_group_uucp(): error testing lock file creation Error
 * details:Permission deniedcheck_lock_status: No permission to create lock
 * file. please see: How can I use Lock Files with rxtx? in INSTALL
 *
 * SOLUTION: Assign correct rights to the /var/lock folder see above
 *
 *
 *
 * PROBLEM: # # A fatal error has been detected by the Java Runtime Environment:
 * # # SIGSEGV (0xb) at pc=0x0000000117251248, pid=1126, tid=0x0000000000005a03
 * # # JRE version: Java(TM) SE Runtime Environment (8.0_191-b12) (build
 * 1.8.0_191-b12) # Java VM: Java HotSpot(TM) 64-Bit Server VM (25.191-b12 mixed
 * mode bsd-amd64 compressed oops) # Problematic frame: # C
 * [librxtxSerial.jnilib+0x5248] read_byte_array+0x38 # # Failed to write core
 * dump. Core dumps have been disabled. To enable core dumping, try "ulimit -c
 * unlimited" before starting Java again # # An error report file with more
 * information is saved as: #
 * /Users/bischowg/Documents/workspace_p50x/rail/hs_err_pid1126.log # # If you
 * would like to submit a bug report, please visit: #
 * http://bugreport.java.com/bugreport/crash.jsp # The crash happened outside
 * the Java Virtual Machine in native code. # See problematic frame for where to
 * report the bug. # Experimental: JNI_OnLoad called.
 *
 *
 * Debugging LOG4j2:
 *
 * -Dlog4j2.debug=true
 *
 * No Log4j 2 configuration file found.
 *
 *
 * RUNNING:
 *
 * Working version of librxtxSerial.jnilib
 * http://blog.iharder.net/2009/08/18/rxtx-java-6-and-librxtxserial-jnilib-on-intel-mac-os-x/
 *
 * Copy this to /Library/Java/Extensions or something???? OR specify a command
 * line parameter to the JVM???? What is the correct approach??? I forgot!!!!
 *
 * This worked:
 *
 * <pre>
 * mkdir /Library
 * mkdir /Library/Java
 * mkdir /Library/Java/Extensions
 *
 * sudo cp /Users/bischowg/Documents/workspace_javafx/rail/lib/librxtxSerial.jnilib /Library/Java/Extensions
 * </pre>
 *
 * <pre>
 * while : ;do clear;ls -lt /dev|head;i=$((i+1));echo $i;sleep 1;done
 * </pre>
 */
public class Main {

	public static final String SERIAL_PORT_IDENTIFIER = "COM4";

	// public static final String SERIAL_PORT_IDENTIFIER =
	// "/dev/cu.usbserial-AO007Q6Q";

	// public static final String SERIAL_PORT_IDENTIFIER = "/dev/cu.usbserial";

	// use the cu.usbserial version, not the tty.usbserial version
	// (https://www.jmri.org/install/MacOSX.shtml,
	// https://www.jmri.org/install/MacOSXRetro.html)
	// public static final String SERIAL_PORT_IDENTIFIER = "/dev/tty.usbserial";

	private static Logger logger = LogManager.getLogger(Main.class);

	public Main() {
		super();
	}

	public static void main(final String[] args) {

		logger.info("Starting the application!");

		SerialPort serialPort = null;

		try {

//			final DefaultSerialPortFactory serialPortFactory = new DefaultSerialPortFactory();
//			serialPort = serialPortFactory.create(SERIAL_PORT_IDENTIFIER);

			final NRSerialPortFactory serialPortFactory = new NRSerialPortFactory();
			serialPort = serialPortFactory.create();

			final InputStream inputStream = serialPort.getInputStream();
			final OutputStream outputStream = serialPort.getOutputStream();

			// nopCommand(inputStream, outputStream);
			// versionCommand(inputStream, outputStream);

			// in order to operate a turnout once (one change of direction)
			// two commands have to be sent!
			boolean straight = false;

			logger.info("turnoutCommandFirst ...");
			turnoutCommandFirst(inputStream, outputStream, straight);
			logger.info("turnoutCommandFirst done.");

			Thread.sleep(100);

			logger.info("turnoutCommandSecond ...");
			turnoutCommandSecond(inputStream, outputStream, straight);
			logger.info("turnoutCommandSecond done.");

			straight = true;

			logger.info("turnoutCommandFirst ...");
			turnoutCommandFirst(inputStream, outputStream, straight);
			logger.info("turnoutCommandFirst done.");

			Thread.sleep(100);

			logger.info("turnoutCommandSecond ...");
			turnoutCommandSecond(inputStream, outputStream, straight);
			logger.info("turnoutCommandSecond done.");

//			// Reads the response from the hardware and writes it to std out
//			final Runnable serialReader = new ConsoleOutputSerialReader(in);
//			final Thread serialReaderThread = new Thread(serialReader);
//			serialReaderThread.start();
//
//			// final CommandSerialWriter serialWriter = new CommandSerialWriter(out);
//
//			final Command p50XXNOPCommand = new P50XXNOPCommand();
//			serialWriter.getCommands().add(p50XXNOPCommand);
//
////			final Command p50XVersionCommand = new P50XVersionCommand();
////			serialWriter.getCommands().add(p50XVersionCommand);
////
////			final Command p50XTurnoutStatus = new P50XTurnoutStatusCommand();
////			serialWriter.getCommands().add(p50XTurnoutStatus);
//
//			final Command p50XTurnout = new P50XTurnoutCommand();
//			serialWriter.getCommands().add(p50XTurnout);
//
//			final Thread serialWriterThread = new Thread(serialWriter);
//			serialWriterThread.start();

		} catch (final NoSuchPortException e) {

			logger.error(e.getMessage(), e);
//			logger.error("Port '" + SERIAL_PORT_IDENTIFIER + "' does not exist! Cannot connect! Aborting application!");

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			if (serialPort != null) {
				serialPort.close();
				serialPort = null;
			}

		}

		logger.info("Application terminated!");
	}

	@SuppressWarnings("unused")
	private static void nopCommand(final InputStream inputStream, final OutputStream outputStream) {

		final Command command = new P50XXNOPCommand();
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	@SuppressWarnings("unused")
	private static void versionCommand(final InputStream inputStream, final OutputStream outputStream) {

		final Command command = new P50XVersionCommand();
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	private static void turnoutCommandFirst(final InputStream inputStream, final OutputStream outputStream,
			final boolean straight) {

		final Command command = new P50XTurnoutCommand((short) 153, straight, true);
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}

	private static void turnoutCommandSecond(final InputStream inputStream, final OutputStream outputStream,
			final boolean straight) {

		final Command command = new P50XTurnoutCommand((short) 153, straight, false);
		final SerialTemplate serialTemplate = new DefaultSerialTemplate(outputStream, inputStream, command);
		serialTemplate.execute();
	}
}
