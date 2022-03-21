package compiler.compiler.cas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class CaslSubroutine {
	private static final String LIBCAS = "data/cas/lib.cas";

	public static void appendLibcas(final String fileName) {
		if (Files.notExists(Paths.get(fileName))) {
			System.err.println("[CaslSimulator] input file \"" + fileName + "\" does not exist.");
			return;
		}
		try {
			final List<String> libcas = Files.readAllLines(Paths.get(LIBCAS));
			Files.write(Paths.get(fileName), libcas, StandardOpenOption.APPEND);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
