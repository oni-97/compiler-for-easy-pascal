package compiler.compiler;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import compiler.compiler.ast.AST;
import compiler.compiler.cas.CaslSubroutine;
import compiler.compiler.exception.SemanticException;
import compiler.compiler.exception.SyntaxException;
import compiler.compiler.visitor.GenerateCsal2CodeVisitor;
import compiler.compiler.visitor.SemanticErrorCheckVisitor;

public class Compiler {
	
	/**
	 * 仕様: 第一引数で指定されたtsファイルを読み込み，CASL IIプログラムにコンパイルする．
	 * コンパイル結果のCASLIIプログラムは第二引数で指定されたcasファイルに書き出す．
	 * 構文的・意味的に正しい場合は標準出力に"OK"を出力する．
	 * 構文的もしくは意味的なエラーを発見した場合は標準エラーにエラーメッセージを出力する．
	 *   構文的に正しくない場合は"Syntax error: line"という文字列とともに，最初のエラーを見つけた行の番号を標準エラーに出力する （例: "Syntax error: line 1"）．
	 *   意味的に正しくない場合は"Semantic error: line"という文字列とともに，最初のエラーを見つけた行の番号を標準エラーに出力する （例: "Semantic error: line 6"）．
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了する．
	 * 入力ファイル内に複数のエラーが含まれる場合は，最初に見つけたエラーのみを出力する．
	 *
	 * @param inputFileName  入力tsファイル名
	 * @param outputFileName 出力casファイル名
	 * ex: new Compiler().run("data/ts/in.ts", "tmp/out.cas");
	 */
	public void run(final String inputFileName, final String outputFileName) {
		List<String> textList = null;
		try {
			textList = Files.readAllLines(Paths.get(inputFileName));
		} catch (final IOException e) {
			fileError();
			return;
		}

		Tokens tokens = new Tokens(textList);

		AST ast;
		try {
			ast = new AST(new Parser(tokens).run());
		} catch (SyntaxException e) {
			System.err.println(e.getMessage());
			return;
		}

		// Parser
		try {
			ast.getRoot().accept(new SemanticErrorCheckVisitor());
		} catch (SemanticException e) {
			System.err.println(e.getMessage());
			return;
		}

		// Compiler
		try {
			PrintWriter pw = new PrintWriter(outputFileName);
			ast.getRoot().accept(new GenerateCsal2CodeVisitor(pw));
			pw.close();
			CaslSubroutine.appendLibcas(outputFileName);
		} catch (final IOException e) {
			System.out.println(e);
			return;
		}

	}

	private void fileError() {
		System.err.println("File not found");
	}

}
