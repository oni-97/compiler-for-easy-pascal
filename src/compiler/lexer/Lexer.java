package compiler.lexer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lexer {

	/**
	 * 仕様:
	 * 第一引数で指定されたpasファイルを読み込み，トークン列に分割する．
	 * トークン列は第二引数で指定されたtsファイルに書き出す．
	 * 正常に処理が終了した場合は標準出力に"OK"を，
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了する．
	 *
	 * @param inputFileName 入力pasファイル名
	 * @param outputFileName 出力tsファイル名
	 * ex: new Lexer().run("data/pas/in.pas", "data/ts/out.ts");
	 */

	public static final int SAND = 0;
	public static final int SARRAY = 1;
	public static final int SBEGIN = 2;
	public static final int SBOOLEAN = 3;
	public static final int SCHAR = 4;
	public static final int SDIVD = 5;
	public static final int SDO = 6;
	public static final int SELSE = 7;
	public static final int SEND = 8;
	public static final int SFALSE = 9;
	public static final int SIF = 10;
	public static final int SINTEGER = 11;
	public static final int SMOD = 12;
	public static final int SNOT = 13;
	public static final int SOF = 14;
	public static final int SOR = 15;
	public static final int SPROCEDURE = 16;
	public static final int SPROGRAM = 17;
	public static final int SREADLN = 18;
	public static final int STHEN = 19;
	public static final int STRUE = 20;
	public static final int SVAR = 21;
	public static final int SWHILE = 22;
	public static final int SWRITELN = 23;
	public static final int SEQUAL = 24;
	public static final int SNOTEQUAL = 25;
	public static final int SLESS = 26;
	public static final int SLESSEQUAL = 27;
	public static final int SGREATEQUAL = 28;
	public static final int SGREAT = 29;
	public static final int SPLUS = 30;
	public static final int SMINUS = 31;
	public static final int SSTAR = 32;
	public static final int SLPAREN = 33;
	public static final int SRPAREN = 34;
	public static final int SLBRACKET = 35;
	public static final int SRBRACKET = 36;
	public static final int SSEMICOLON = 37;
	public static final int SCOLON = 38;
	public static final int SRANGE = 39;
	public static final int SASSIGN = 40;
	public static final int SCOMMA = 41;
	public static final int SDOT = 42;
	public static final int SIDENTIFIER = 43;
	public static final int SCONSTANT = 44;
	public static final int SSTRING = 45;

	public static final int SSPACE = 50;
	public static final int STAB = 51;
	public static final int SLBRACE = 52;
	public static final int SRBRACE = 53;
	public static final int SNULL = 54;
	public static final int SQUOTE = 55;
	public static final int SALPHA = 56;
	public static final int SDIGIT = 57;

	public static final int SUNDEFINED = 100;

	public static final char asciiTable[] = {
			SNULL, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, STAB,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SSPACE, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SQUOTE,
			SLPAREN, SRPAREN, SSTAR, SPLUS, SCOMMA,
			SMINUS, SDOT, SDIVD, SDIGIT, SDIGIT,
			SDIGIT, SDIGIT, SDIGIT, SDIGIT, SDIGIT,
			SDIGIT, SDIGIT, SDIGIT, SCOLON, SSEMICOLON,
			SLESS, SEQUAL, SGREAT, SUNDEFINED, SUNDEFINED,
			SALPHA, SALPHA, SALPHA, SALPHA, SALPHA,
			SALPHA, SALPHA, SALPHA, SALPHA, SALPHA,
			SALPHA, SALPHA, SALPHA, SALPHA, SALPHA,
			SALPHA, SALPHA, SALPHA, SALPHA, SALPHA,
			SALPHA, SALPHA, SALPHA, SALPHA, SALPHA,
			SALPHA, SLBRACKET, SUNDEFINED, SRBRACKET, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SALPHA, SALPHA, SALPHA,
			SALPHA, SALPHA, SALPHA, SALPHA, SALPHA,
			SALPHA, SALPHA, SALPHA, SALPHA, SALPHA,
			SALPHA, SALPHA, SALPHA, SALPHA, SALPHA,
			SALPHA, SALPHA, SALPHA, SALPHA, SALPHA,
			SALPHA, SALPHA, SALPHA, SLBRACE, SUNDEFINED,
			SRBRACE, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED, SUNDEFINED,
			SUNDEFINED,
	};

	public static final Token reservedWordArray[] = {
			new Token("and", "SAND", SAND), new Token("array", "SARRAY", SARRAY),
			new Token("begin", "SBEGIN", SBEGIN), new Token("boolean", "SBOOLEAN", SBOOLEAN),
			new Token("char", "SCHAR", SCHAR), new Token("div", "SDIVD", SDIVD),
			new Token("do", "SDO", SDO), new Token("else", "SELSE", SELSE),
			new Token("end", "SEND", SEND), new Token("false", "SFALSE", SFALSE),
			new Token("if", "SIF", SIF), new Token("integer", "SINTEGER", SINTEGER),
			new Token("mod", "SMOD", SMOD), new Token("not", "SNOT", SNOT),
			new Token("of", "SOF", SOF), new Token("or", "SOR", SOR),
			new Token("procedure", "SPROCEDURE", SPROCEDURE), new Token("program", "SPROGRAM", SPROGRAM),
			new Token("readln", "SREADLN", SREADLN), new Token("then", "STHEN", STHEN),
			new Token("true", "STRUE", STRUE), new Token("var", "SVAR", SVAR),
			new Token("while", "SWHILE", SWHILE), new Token("writeln", "SWRITELN", SWRITELN)
	};

	private Token whichReservedWord(String word) {
		int i = 0;
		while (i < reservedWordArray.length) {
			if (word.equals(reservedWordArray[i].GetSourceName()))
				return reservedWordArray[i];
			else
				i++;
		}
		return new Token(word, "SIDENTIFIER", SIDENTIFIER);
	}

	public void run(final String inputFileName, final String outputFileName) {
		try {
			final List<String> textList = Files.readAllLines(Paths.get(inputFileName));
			File outputFile = new File(outputFileName);
			FileWriter fileWriter = new FileWriter(outputFile);

			int lineNumber = 1;
			boolean flag = false;
			int errlineNumber = 0;

			while (lineNumber <= textList.size()) {
				int beginPoint = 0, endPoint = 0;
				String text = textList.get(lineNumber - 1) + "\0";

				while (asciiTable[text.charAt(beginPoint)] != SNULL) {

					while (asciiTable[text.charAt(beginPoint)] == SSPACE
							|| asciiTable[text.charAt(beginPoint)] == STAB)
						beginPoint++;

					if (asciiTable[text.charAt(beginPoint)] == SLBRACE || flag) {
						if (!flag) {
							flag = !flag;
							beginPoint++;
							errlineNumber = lineNumber;
						}
						while (asciiTable[text.charAt(beginPoint)] != SRBRACE
								&& asciiTable[text.charAt(beginPoint)] != SNULL)
							beginPoint++;

						if (asciiTable[text.charAt(beginPoint)] == SRBRACE) {
							beginPoint++;
							flag = false;
						}

						continue;
					}

					Token token = null;

					endPoint = beginPoint + 1;

					switch (asciiTable[text.charAt(beginPoint)]) {

					case SALPHA:
						while (asciiTable[text.charAt(endPoint)] == SALPHA
								|| asciiTable[text.charAt(endPoint)] == SDIGIT)
							endPoint++;

						token = whichReservedWord(text.substring(beginPoint, endPoint));
						break;

					case SDIGIT:
						while (asciiTable[text.charAt(endPoint)] == SDIGIT)
							endPoint++;

						token = new Token(text.substring(beginPoint, endPoint), "SCONSTANT", SCONSTANT);
						break;

					case SPLUS:
						token = new Token(text.substring(beginPoint, endPoint), "SPLUS", SPLUS);
						break;

					case SMINUS:
						token = new Token(text.substring(beginPoint, endPoint), "SMINUS", SMINUS);
						break;

					case SSTAR:
						token = new Token(text.substring(beginPoint, endPoint), "SSTAR", SSTAR);
						break;

					case SDIVD:
						token = new Token(text.substring(beginPoint, endPoint), "SDIVD", SDIVD);
						break;

					case SEQUAL:
						token = new Token(text.substring(beginPoint, endPoint), "SEQUAL", SEQUAL);
						break;

					case SLESS:
						if (asciiTable[text.charAt(endPoint)] == SGREAT) {
							endPoint++;
							token = new Token(text.substring(beginPoint, endPoint), "SNOTEQUAL", SNOTEQUAL);
						} else if (asciiTable[text.charAt(endPoint)] == SEQUAL) {
							endPoint++;
							token = new Token(text.substring(beginPoint, endPoint), "SLESSEQUAL", SLESSEQUAL);
						} else {
							token = new Token(text.substring(beginPoint, endPoint), "SLESS", SLESS);
						}
						break;

					case SGREAT:
						if (asciiTable[text.charAt(endPoint)] == SEQUAL) {
							endPoint++;
							token = new Token(text.substring(beginPoint, endPoint), "SGREATEQUAL", SGREATEQUAL);
						} else {
							token = new Token(text.substring(beginPoint, endPoint), "SGREAT", SGREAT);
						}
						break;

					case SLPAREN:
						token = new Token(text.substring(beginPoint, endPoint), "SLPAREN", SLPAREN);
						break;

					case SRPAREN:
						token = new Token(text.substring(beginPoint, endPoint), "SRPAREN", SRPAREN);
						break;

					case SLBRACKET:
						token = new Token(text.substring(beginPoint, endPoint), "SLBRACKET", SLBRACKET);
						break;

					case SRBRACKET:
						token = new Token(text.substring(beginPoint, endPoint), "SRBRACKET", SRBRACKET);
						break;

					case SCOLON:
						if (asciiTable[text.charAt(endPoint)] == SEQUAL) {
							endPoint++;
							token = new Token(text.substring(beginPoint, endPoint), "SASSIGN", SASSIGN);
						} else {
							token = new Token(text.substring(beginPoint, endPoint), "SCOLON", SCOLON);
						}
						break;

					case SSEMICOLON:
						token = new Token(text.substring(beginPoint, endPoint), "SSEMICOLON", SSEMICOLON);
						break;

					case SDOT:
						if (asciiTable[text.charAt(endPoint)] == SDOT) {
							endPoint++;
							token = new Token(text.substring(beginPoint, endPoint), "SRANGE", SRANGE);
						} else {
							token = new Token(text.substring(beginPoint, endPoint), "SDOT", SDOT);
						}
						break;

					case SCOMMA:
						token = new Token(text.substring(beginPoint, endPoint), "SCOMMA", SCOMMA);
						break;

					case SQUOTE:
						while (asciiTable[text.charAt(endPoint)] != SQUOTE) {
							if (asciiTable[text.charAt(endPoint)] == SNULL) {
								System.err.println(" ' is not closed: line " + lineNumber);
								fileWriter.close();
								return;
							} else {
								endPoint++;
							}
						}
						endPoint++;
						token = new Token(text.substring(beginPoint, endPoint), "SSTRING", SSTRING);
						break;

					case SNULL:
						break;

					default:
						System.err.println("undefined token that begin with \"" + text.charAt(beginPoint)
								+ "\" is included: line " + lineNumber);
						fileWriter.close();
						return;

					}
					fileWriter.write(token.GetSourceName() + "\t" + token.GetTokenName() + "\t" + token.GetId() + "\t"
							+ lineNumber + "\n");
					beginPoint = endPoint;
				}

				lineNumber++;
			}
			fileWriter.close();
			if (flag) {
				System.err.println(" { is not closed: line " + errlineNumber);
				return;
			}
			System.out.println("OK");
		} catch (final IOException e) {
			System.err.println("File not found");
		}
	}
}

class Token {
	Token(String sourceName, String tokenName, int id) {
		this.sourceName = sourceName;
		this.tokenName = tokenName;
		this.id = id;
	}

	private String sourceName;
	private String tokenName;
	private int id;

	public String GetSourceName() {
		return this.sourceName;
	}

	public String GetTokenName() {
		return this.tokenName;
	}

	public int GetId() {
		return this.id;
	}
}
