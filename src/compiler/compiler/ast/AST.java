package compiler.compiler.ast;

public class AST {
	private ASTNode root;

	public AST(ASTNode root) {
		this.root = root;
	}

	public ASTNode getRoot() {
		return this.root;
	}
}
