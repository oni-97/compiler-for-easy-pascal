package compiler.compiler.ast;

import java.util.List;

import compiler.compiler.visitor.GenerateCsal2CodeVisitor;
import compiler.compiler.visitor.PrintVisitor;
import compiler.compiler.visitor.SemanticErrorCheckVisitor;
import compiler.compiler.visitor.Type;

public class ASTNode {
	private String label;
	private String sourceName;
	private String linenumber;
	private List<ASTNode> children;

	public ASTNode(String label, List<ASTNode> children) {
		this.label = label;
		this.sourceName = null;
		this.linenumber = null;
		this.children = children;
	}

	public ASTNode(ASTNode node, List<ASTNode> children) {
		this.label = node.getLabel();
		this.sourceName = node.getSourceName();
		this.linenumber = node.getLinenumber();
		this.children = children;
	}

	public ASTNode(String label, String sourceName, String linenumber, List<ASTNode> children) {
		this.label = label;
		this.sourceName = sourceName;
		this.linenumber = linenumber;
		this.children = children;
	}

	public ASTNode clone() {
		ASTNode n = new ASTNode(this.getLabel(), this.getSourceName(), this.getLinenumber(), this.getChildren());
		return n;
	}

	public String getLabel() {
		return label;
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getLinenumber() {
		return linenumber;
	}

	public List<ASTNode> getChildren() {
		return children;
	}

	public void setChildren(List<ASTNode> children) {
		this.children = children;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLinenumber(String linenumber) {
		this.linenumber = linenumber;
	}

	public Type accept(SemanticErrorCheckVisitor v) {
		return v.visit(this);
	}

	public void accept(GenerateCsal2CodeVisitor v) {
		v.visit(this);
	}

	public void accept(PrintVisitor v, int t) {
		v.visit(this, t);
	}
}