package de.wfb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.lang3.StringUtils;

public class Main {

	public static void main(final String[] args) throws FileNotFoundException {

		// final File fout = new
//		final File fout = new File("/Users/bischowg/Documents/workspace_javafx/Startup/persistence⁩/model_fix.json");
		final File fout = new File("/Users/bischowg/Documents/model_fix.json");
		final FileOutputStream fos = new FileOutputStream(fout);

		BufferedReader reader;
		final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
		try {

			reader = new BufferedReader(
					new FileReader("/Users/bischowg/Documents/workspace_javafx/Startup/persistence/model.json"));

			String line = reader.readLine();
			String oldLine = null;

			while (line != null) {

				if (oldLine == null || !StringUtils.equalsIgnoreCase(line, oldLine)) {
					bufferedWriter.write(line);
					bufferedWriter.write("\n");
				}

				oldLine = line;

				// System.out.println(line);
				// read next line
				line = reader.readLine();
			}
			reader.close();

			bufferedWriter.flush();
			bufferedWriter.close();
			fos.flush();
			fos.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
