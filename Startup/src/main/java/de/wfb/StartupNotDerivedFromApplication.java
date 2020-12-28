package de.wfb;

/**
 * https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing
 *
 * This is required to create a self containd jar including JavaFX 13 that does
 * not print the error "Error: JavaFX runtime components are missing, and are
 * required to run this application" on the command line during start.
 */
public class StartupNotDerivedFromApplication {

	public static void main(final String[] args) {
		Startup.main(args);
	}

}
