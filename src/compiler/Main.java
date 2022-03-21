package compiler;

import java.nio.file.Files;
import java.nio.file.Paths;

import compiler.compiler.Compiler;
import compiler.lexer.Lexer;

public class Main {

	/*
	 * 
	 * 実行例：
	 * $ java Main lexer data/pas/in.pas tmp/out.ts 
	 * $ java Main compiler data/ts/in.ts tmp/out.cas 
	 * $ java Main all data/pas/in.pas tmp/out.ans
	 * 
	 */
	public static void main(final String[] args) {

		if (args.length < 2) {
			printUsage();
			return;
		}
		final String subcommand = args[0];
		final String in = args[1];

		String out = "";
		if (subcommand.matches("lexer|compiler|all")) {
			if (args.length < 3) {
				printUsage();
				return;
			}
			out = args[2];
		}

		if ("lexer".equals(subcommand)) {
			new Lexer().run(in, out);
		} else if ("compiler".equals(subcommand)) {
			new Compiler().run(in, out);
		} else if ("all".equals(subcommand)) {
			if (!Files.isDirectory(Paths.get(out))) {
				System.out.println("error: specify an output dir instead of file");
				return;
			}
			final String base = out + "/" + getBaseName(in);
			final String ts = base + ".ts";
			final String cas = base + ".cas";

			new Lexer().run(in, ts);
			new Compiler().run(ts, cas);
		} else {
			printUsage();
			return;
		}
	}

	private static void printUsage() {
		System.out.println("usage:");
		System.out.println("  lexer    in.pas out.ts");
		System.out.println("  compiler in.ts  out.cas");
		System.out.println("  all      in.pas tmp/");
	}

	private static String getBaseName(final String name) {
		final String f = Paths.get(name).getFileName().toString();
		final int n = f.lastIndexOf('.');
		if (n > 0) {
			return f.substring(0, n);
		}
		return f;
	}

}
