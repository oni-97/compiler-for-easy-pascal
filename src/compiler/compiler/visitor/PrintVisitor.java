package compiler.compiler.visitor;

import java.util.List;

import compiler.compiler.ast.ASTNode;

public class PrintVisitor {
	public void visit(ASTNode node, int t) {
		int i;
		for (i = 0; i < t; i++) {
			System.out.print("\t");
		}

		if (node.getSourceName() != null) {
			System.out.println("○" + node.getLabel() + " (" + node.getSourceName() + ")");
		} else {
			System.out.println("○" + node.getLabel());
		}

		t++;
		List<ASTNode> children = node.getChildren();
		if (children != null) {
			int j = 0;
			while (j < children.size()) {
				children.get(j).accept(this, t);
				j++;
			}
		}
	}
}