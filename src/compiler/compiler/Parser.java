package compiler.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import compiler.compiler.ast.ASTNode;
import compiler.compiler.exception.SyntaxException;

public class Parser {
	private Tokens tokens;
	static final List<String> firstOfStatement = Arrays.asList("SIF", "SWHILE", "SIDENTIFIER", "SREADLN", "SWRITELN",
			"SBEGIN");
	public static final List<String> firstOfRelationalOperator = Arrays.asList("SEQUAL", "SNOTEQUAL", "SLESS",
			"SLESSEQUAL", "SGREAT", "SGREATEQUAL");
	public static final List<String> firstOfPlusOrMinusSign = Arrays.asList("SPLUS", "SMINUS");
	public static final List<String> firstOfBasicStatement = Arrays.asList("SIDENTIFIER", "SREADLN", "SWRITELN",
			"SBEGIN");
	public static final List<String> firstOfAdditiveOperator = Arrays.asList("SPLUS", "SMINUS", "SOR");
	public static final List<String> firstOfMultiplicativeOperator = Arrays.asList("SSTAR", "SDIVD", "SMOD", "SAND");
	public static final List<String> firstOfConstant = Arrays.asList("SCONSTANT", "SSTRING", "SFALSE", "STRUE");

	public Parser(Tokens tokens) {
		this.tokens = tokens;
	}

	private void syntaxError() {
		throw new SyntaxException("Syntax error: line " + tokens.getLineNumber());
	}

	public ASTNode run() {
		return program();
	}

	private ASTNode program() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		if (!tokens.getNext().equals("SPROGRAM")) {
			syntaxError();
		}

		ASTNode n1 = programName();
		children.add(n1);

		if (!tokens.getNext().equals("SSEMICOLON")) {
			syntaxError();
		}

		ASTNode n2 = block();
		if (n2 != null)
			children.add(n2);

		ASTNode n3 = complexStatement();
		children.add(n3);

		if (!tokens.getNext().equals("SDOT")) {
			syntaxError();
		}

		ASTNode n = new ASTNode("Program", children);
		return n;
	}

	public ASTNode programName() {
		ASTNode n = name();
		n.setLabel("ProgramName");
		return n;
	}

	public ASTNode block() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n1 = variableDeclaration();
		if (n1 != null) {
			children.add(n1);
		}

		ASTNode n2 = subprogramDeclarationGroup();
		if (n2 != null)
			children.add(n2);

		ASTNode n;
		if (children.size() == 0) {
			n = null;
		} else if (children.size() == 1) {
			n = children.get(0);
		} else {
			n = new ASTNode("Block", children);
		}
		return n;
	}

	public ASTNode variableDeclaration() {
		ASTNode n;
		if (tokens.lookAheadOne().equals("SVAR")) {
			if (!tokens.getNext().equals("SVAR")) {
				syntaxError();
			}

			n = rowOfVariableDeclaration();

		} else {
			n = null;
		}

		return n;
	}

	public ASTNode rowOfVariableDeclaration() {
		List<ASTNode> children = new ArrayList<ASTNode>();
		do {
			List<ASTNode> children1 = new ArrayList<ASTNode>();

			ASTNode n1 = rowOfVariableName();

			if (!tokens.getNext().equals("SCOLON")) {
				syntaxError();
			}

			ASTNode n2 = type();
			children1.add(n2);

			if (!tokens.getNext().equals("SSEMICOLON")) {
				syntaxError();
			}

			ASTNode n3;
			if (n1.getLabel().equals("VariableNames")) {
				int i;
				for (i = 0; i < n1.getChildren().size(); i++) {
					ASTNode t = n1.getChildren().get(i);
					t.setLabel("VariableDeclarator");
					t.setChildren(children1);
					children.add(t);
				}
			} else {
				n3 = n1;
				n3.setLabel("VariableDeclarator");
				n3.setChildren(children1);
				children.add(n3);
			}
		} while (tokens.lookAheadOne().equals("SIDENTIFIER"));

		ASTNode n;
		if (children.size() == 1) {
			n = children.get(0);
		} else {
			n = new ASTNode("VariableDeclaration", children);
		}
		return n;
	}

	public ASTNode rowOfVariableName() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n1 = variableName();
		children.add(n1);

		while (tokens.lookAheadOne().equals("SCOMMA")) {
			if (!tokens.getNext().equals("SCOMMA")) {
				syntaxError();
			}

			ASTNode n2 = variableName();
			children.add(n2);
		}

		ASTNode n;
		if (children.size() == 1) {
			n = children.get(0);
		} else {
			n = new ASTNode("VariableNames", children);
		}
		return n;
	}

	public ASTNode variableName() {
		ASTNode n = name();
		n.setLabel("VariableName");
		return n;

	}

	public ASTNode type() {
		ASTNode n;
		switch (tokens.lookAheadOne()) {
		case "SINTEGER":
		case "SCHAR":
		case "SBOOLEAN":
			n = standardType();
			break;
		case "SARRAY":
			n = arrayType();
			break;
		default:
			n = null;
			syntaxError();
		}
		return n;
	}

	public ASTNode standardType() {
		ASTNode n;
		switch (tokens.getNext()) {
		case "SINTEGER":
			n = new ASTNode("StandardType", tokens.getSourceName(), tokens.getLineNumber(), null);
			break;
		case "SCHAR":
			n = new ASTNode("StandardType", tokens.getSourceName(), tokens.getLineNumber(), null);
			break;
		case "SBOOLEAN":
			n = new ASTNode("StandardType", tokens.getSourceName(), tokens.getLineNumber(), null);
			break;
		default:
			n = null;
			syntaxError();
		}
		return n;
	}

	public ASTNode arrayType() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		if (!tokens.getNext().equals("SARRAY")) {
			syntaxError();
		}
		ASTNode n = new ASTNode("ArrayType", tokens.getSourceName(), tokens.getLineNumber(), null);

		if (!tokens.getNext().equals("SLBRACKET")) {
			syntaxError();
		}

		ASTNode n1 = minimumValueOfIndex();
		children.add(n1);

		if (!tokens.getNext().equals("SRANGE")) {
			syntaxError();
		}

		ASTNode n2 = maxValueOfIndex();
		children.add(n2);

		if (!tokens.getNext().equals("SRBRACKET")) {
			syntaxError();
		}

		if (!tokens.getNext().equals("SOF")) {
			syntaxError();
		}

		ASTNode n3 = standardType();
		children.add(n3);

		n.setChildren(children);
		return n;
	}

	private ASTNode minimumValueOfIndex() {
		ASTNode n = integer();
		return n;
	}

	private ASTNode maxValueOfIndex() {
		ASTNode n = integer();
		return n;
	}

	private ASTNode integer() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n = null;
		if (firstOfPlusOrMinusSign.contains(tokens.lookAheadOne())) {
			n = plusOrMinusSign();
		}

		ASTNode n1 = constant();
		children.add(n1);

		if (n == null) {
			n = children.get(0);
		} else {
			n.setChildren(children);
		}
		return n;
	}

	private ASTNode plusOrMinusSign() {
		ASTNode n = null;
		switch (tokens.getNext()) {
		case "SPLUS":
			n = new ASTNode("UnaryOperator", tokens.getSourceName(), tokens.getLineNumber(), null);
			break;
		case "SMINUS":
			n = new ASTNode("UnaryOperator", tokens.getSourceName(), tokens.getLineNumber(), null);
			break;
		default:
			syntaxError();
		}
		return n;
	}

	public ASTNode subprogramDeclarationGroup() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		while (tokens.lookAheadOne().equals("SPROCEDURE")) {
			ASTNode n1 = subprogramDeclaration();
			children.add(n1);

			if (!tokens.getNext().equals("SSEMICOLON")) {
				syntaxError();
			}
		}

		ASTNode n;
		if (children.size() == 0) {
			n = null;
		} else if (children.size() == 1) {
			n = children.get(0);
		} else {
			n = new ASTNode("Subprograms", children);
		}
		return n;
	}

	private ASTNode subprogramDeclaration() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n1 = headOfSubprogram();
		children.add(n1);

		ASTNode n2 = variableDeclaration();
		if (n2 != null) {
			children.add(n2);

		}

		ASTNode n3 = complexStatement();
		children.add(n3);

		ASTNode n = new ASTNode("Subprogram", children);
		return n;
	}

	private ASTNode headOfSubprogram() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		if (!tokens.getNext().equals("SPROCEDURE")) {
			syntaxError();
		}

		ASTNode n = procedureName();
		n.setLabel("ProcedureDeclarator");

		ASTNode n2 = formalParameter();
		if (n2 != null) {
			if (n2.getLabel().equals("FormalParameters")) {
				int i;
				for (i = 0; i < n2.getChildren().size(); i++) {
					ASTNode t = n2.getChildren().get(i);
					children.add(t);
				}
			} else {
				children.add(n2);
			}
		}

		if (!tokens.getNext().equals("SSEMICOLON")) {
			syntaxError();
		}

		if (children.size() > 0) {
			n.setChildren(children);
		}
		return n;
	}

	private ASTNode procedureName() {
		ASTNode n = name();
		n.setLabel("ProcedureName");
		return n;
	}

	private ASTNode formalParameter() {
		ASTNode n;

		if (tokens.lookAheadOne().equals("SLPAREN")) {
			if (!tokens.getNext().equals("SLPAREN")) {
				syntaxError();
			}

			n = rowOfFormalParameter();

			if (!tokens.getNext().equals("SRPAREN")) {
				syntaxError();
			}
		} else {
			n = null;
		}

		return n;
	}

	private ASTNode rowOfFormalParameter() {
		List<ASTNode> children = new ArrayList<ASTNode>();
		List<ASTNode> children1 = new ArrayList<ASTNode>();

		ASTNode n1 = rowOfFormalParameterName();

		if (!tokens.getNext().equals("SCOLON")) {
			syntaxError();
		}

		ASTNode n2 = standardType();
		children1.add(n2);

		ASTNode n3;
		if (n1.getLabel().equals("FormalParameterNames")) {
			int i;
			for (i = 0; i < n1.getChildren().size(); i++) {
				ASTNode t = n1.getChildren().get(i);
				t.setLabel("FormalParameterDeclarator");
				t.setChildren(children1);
				children.add(t);
			}
		} else {
			n3 = n1;
			n3.setLabel("FormalParameterDeclarator");
			n3.setChildren(children1);
			children.add(n3);
		}

		while (tokens.lookAheadOne().equals("SSEMICOLON")) {
			List<ASTNode> children2 = new ArrayList<ASTNode>();

			if (!tokens.getNext().equals("SSEMICOLON")) {
				syntaxError();
			}

			ASTNode n4 = rowOfFormalParameterName();

			if (!tokens.getNext().equals("SCOLON")) {
				syntaxError();
			}

			ASTNode n5 = standardType();
			children2.add(n5);

			ASTNode n6;
			if (n4.getLabel().equals("FormalParameterNames")) {
				int i;
				for (i = 0; i < n4.getChildren().size(); i++) {
					ASTNode t = n4.getChildren().get(i);
					t.setLabel("FormalParameterDeclarator");
					t.setChildren(children2);
					children.add(t);
				}
			} else {
				n6 = n1;
				n6.setLabel("FormalParameterDeclarator");
				n6.setChildren(children2);
				children.add(n6);
			}
		}

		ASTNode n;
		if (children.size() == 1) {
			n = children.get(0);
		} else {
			n = new ASTNode("FormalParameters", children);
		}
		return n;
	}

	private ASTNode rowOfFormalParameterName() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n1 = formalParameterName();
		children.add(n1);

		while (tokens.lookAheadOne().equals("SCOMMA")) {
			if (!tokens.getNext().equals("SCOMMA")) {
				syntaxError();
			}

			ASTNode n2 = formalParameterName();
			children.add(n2);
		}

		ASTNode n;
		if (children.size() == 1) {
			n = children.get(0);
		} else {
			n = new ASTNode("FormalParameterNames", children);
		}
		return n;
	}

	private ASTNode formalParameterName() {
		ASTNode n = name();
		return n;
	}

	public ASTNode complexStatement() {
		if (!tokens.getNext().equals("SBEGIN")) {
			syntaxError();
		}

		ASTNode n = rowOfStatement();

		if (!tokens.getNext().equals("SEND")) {
			syntaxError();
		}

		return n;
	}

	private ASTNode rowOfStatement() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		do {
			ASTNode n1 = statement();
			children.add(n1);

			if (!tokens.getNext().equals("SSEMICOLON")) {
				syntaxError();
			}
		} while (firstOfStatement.contains(tokens.lookAheadOne()));

		ASTNode n;
		if (children.size() == 1) {
			n = children.get(0);
		} else {
			n = new ASTNode("Statements", children);
		}
		return n;
	}

	private ASTNode statement() {
		List<ASTNode> children = new ArrayList<ASTNode>();
		List<ASTNode> children1 = new ArrayList<ASTNode>();
		ASTNode n;

		// 変更
		switch (tokens.lookAheadOne()) {
		case "SIF":

			if (!tokens.getNext().equals("SIF")) {
				syntaxError();
			}
			String linenumber1 = tokens.getLineNumber();

			ASTNode n1 = expression();
			children1.add(n1);
			ASTNode n4 = new ASTNode("IfExp", children1);
			children.add(n4);

			if (!tokens.getNext().equals("STHEN")) {
				syntaxError();
			}

			List<ASTNode> children3 = new ArrayList<ASTNode>();
			ASTNode n2 = complexStatement();
			children3.add(n2);
			ASTNode n6 = new ASTNode("ThenStatement", children3);
			children.add(n6);

			ASTNode n5 = new ASTNode("ElseStatement", new ArrayList<ASTNode>());
			children.add(n5);
			if (tokens.lookAheadOne().equals("SELSE")) {
				if (!tokens.getNext().equals("SELSE")) {
					syntaxError();
				}
				List<ASTNode> children2 = new ArrayList<ASTNode>();
				ASTNode n3 = complexStatement();
				children2.add(n3);
				n5.setChildren(children2);
			}

			n = new ASTNode("IfStatement", children);
			n.setLinenumber(linenumber1);

			break;

		case "SWHILE":
			if (!tokens.getNext().equals("SWHILE")) {
				syntaxError();
			}
			String linenumber2 = tokens.getLineNumber();

			ASTNode m1 = expression();
			children1.add(m1);
			ASTNode m4 = new ASTNode("WhileExp", children1);
			children.add(m4);

			if (!tokens.getNext().equals("SDO")) {
				syntaxError();
			}

			ASTNode m2 = complexStatement();
			children.add(m2);

			n = new ASTNode("WhileStatement", children);
			n.setLinenumber(linenumber2);

			break;

		default:
			if (firstOfBasicStatement.contains(tokens.lookAheadOne())) {
				n = basicStatement();
			} else {
				n = null;
				syntaxError();
			}
		}

		return n;
	}

	public ASTNode basicStatement() {
		ASTNode n;

		switch (tokens.lookAheadOne()) {
		case "SIDENTIFIER":
			n = assignmentOrProcedureCallStatement();
			break;

		case "SREADLN":
		case "SWRITELN":
			n = inputOutputStatement();
			break;

		case "SBEGIN":
			n = complexStatement();
			break;

		default:
			n = null;
			syntaxError();
		}

		return n;
	}

	private ASTNode assignmentOrProcedureCallStatement() {
		ASTNode n;
		switch (tokens.lookAheadTwo()) {
		case "SLBRACKET":
		case "SASSIGN":
			n = assignmentStatement();
			break;

		default:
			n = procedureCallStatement();
		}
		return n;
	}

	private ASTNode assignmentStatement() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n1 = leftSide();
		children.add(n1);

		if (!tokens.getNext().equals("SASSIGN")) {
			syntaxError();
		}
		ASTNode n = new ASTNode("Assignment", tokens.getSourceName(), tokens.getLineNumber(), null);

		ASTNode n2 = expression();
		children.add(n2);

		n.setChildren(children);
		return n;
	}

	public ASTNode leftSide() {
		List<ASTNode> children = new ArrayList<ASTNode>();
		ASTNode n1 = variable();
		children.add(n1);
		ASTNode n = new ASTNode("LeftValue", children);
		return n;
	}

	public ASTNode procedureCallStatement() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n = procedureName();
		n.setLabel("ProcedureCall");

		if (tokens.lookAheadOne().equals("SLPAREN")) {
			if (!tokens.getNext().equals("SLPAREN")) {
				syntaxError();
			}

			ASTNode n1 = rowOfExpression();
			if (n1.getLabel().equals("Expressions")) {
				int i;
				for (i = 0; i < n1.getChildren().size(); i++) {
					children.add(n1.getChildren().get(i));
				}
			} else {
				children.add(n1);
			}

			if (!tokens.getNext().equals("SRPAREN")) {
				syntaxError();
			}
		}

		if (children.size() > 0) {
			n.setChildren(children);
		}
		return n;
	}

	public ASTNode variable() {
		ASTNode n;
		if (tokens.lookAheadTwo().equals("SLBRACKET")) {
			n = indexedVariable();
		} else {
			n = simpleVariable();
		}
		return n;
	}

	public ASTNode simpleVariable() {
		ASTNode n = variableName();
		n.setLabel("SimpleVariable");
		return n;
	}

	public ASTNode indexedVariable() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n = variableName();
		n.setLabel("IndexedVariable");

		if (!tokens.getNext().equals("SLBRACKET")) {
			syntaxError();
		}

		ASTNode n2 = index();
		children.add(n2);

		if (!tokens.getNext().equals("SRBRACKET")) {
			syntaxError();
		}

		n.setChildren(children);
		return n;
	}

	private ASTNode index() {
		ASTNode n = expression();
		return n;
	}

	private ASTNode inputOutputStatement() {
		List<ASTNode> children = new ArrayList<ASTNode>();
		ASTNode n1, n = null;

		switch (tokens.getNext()) {
		case "SREADLN":
			n = new ASTNode("InputStatement", tokens.getSourceName(), tokens.getLineNumber(), null);

			if (tokens.lookAheadOne().equals("SLPAREN")) {
				if (!tokens.getNext().equals("SLPAREN")) {
					syntaxError();
				}

				n1 = rowOfVariable();
				if (n1.getLabel().equals("Variables")) {
					int i;
					for (i = 0; i < n1.getChildren().size(); i++) {
						List<ASTNode> children1 = new ArrayList<ASTNode>();
						children1.add(n1.getChildren().get(i));
						ASTNode n3 = new ASTNode("InputStatementComponent", children1);
						children.add(n3);
					}
				} else {
					List<ASTNode> children1 = new ArrayList<ASTNode>();
					children1.add(n1);
					ASTNode n3 = new ASTNode("InputStatementComponent", children1);
					children.add(n3);
				}

				if (!tokens.getNext().equals("SRPAREN")) {
					syntaxError();
				}
			}

			if (children.size() > 0) {
				n.setChildren(children);
			}

			break;

		case "SWRITELN":
			n = new ASTNode("OutputStatement", tokens.getSourceName(), tokens.getLineNumber(), null);

			if (tokens.lookAheadOne().equals("SLPAREN")) {
				if (!tokens.getNext().equals("SLPAREN")) {
					syntaxError();
				}

				n1 = rowOfExpression();
				if (n1.getLabel().equals("Expressions")) {
					int i;
					for (i = 0; i < n1.getChildren().size(); i++) {
						List<ASTNode> children1 = new ArrayList<ASTNode>();
						children1.add(n1.getChildren().get(i));
						ASTNode n3 = new ASTNode("OutputStatementComponent", children1);
						children.add(n3);
					}
				} else {
					List<ASTNode> children1 = new ArrayList<ASTNode>();
					children1.add(n1);
					ASTNode n3 = new ASTNode("OutputStatementComponent", children1);
					children.add(n3);
				}

				if (!tokens.getNext().equals("SRPAREN")) {
					syntaxError();
				}
			}

			if (children.size() > 0) {
				n.setChildren(children);
			}

			break;

		default:
			syntaxError();
		}

		return n;
	}

	private ASTNode rowOfExpression() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n1 = expression();
		children.add(n1);

		while (tokens.lookAheadOne().equals("SCOMMA")) {
			if (!tokens.getNext().equals("SCOMMA")) {
				syntaxError();
			}

			ASTNode n2 = expression();
			children.add(n2);
		}

		ASTNode n;
		if (children.size() == 1) {
			n = children.get(0);
		} else {
			n = new ASTNode("Expressions", children);
		}
		return n;
	}

	private ASTNode expression() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n1 = simpleExpression();
		children.add(n1);

		ASTNode n = null;
		if (firstOfRelationalOperator.contains(tokens.lookAheadOne())) {
			n = relationalOperator();

			ASTNode n2 = simpleExpression();
			children.add(n2);
		}

		if (n == null) {
			n = children.get(0);
		} else {
			n.setChildren(children);
		}
		return n;
	}

	private ASTNode simpleExpression() {
		List<ASTNode> children1 = new ArrayList<ASTNode>();

		ASTNode n = null;
		if (firstOfPlusOrMinusSign.contains(tokens.lookAheadOne())) {
			n = plusOrMinusSign();
		}

		ASTNode n1 = term();
		children1.add(n1);

		if (n == null) {
			n = children1.get(0);
		} else {
			n.setChildren(children1);
		}

		while (firstOfAdditiveOperator.contains(tokens.lookAheadOne())) {
			List<ASTNode> children2 = new ArrayList<ASTNode>();
			ASTNode n2 = n.clone();
			children2.add(n2);

			n = additiveOperator();

			ASTNode n4 = term();
			children2.add(n4);

			n.setChildren(children2);
		}

		return n;
	}

	private ASTNode term() {
		ASTNode n = factor();
		while (firstOfMultiplicativeOperator.contains(tokens.lookAheadOne())) {
			List<ASTNode> children = new ArrayList<ASTNode>();
			ASTNode n1 = n.clone();
			children.add(n1);

			n = multiplicativeOperator();

			ASTNode n2 = factor();
			children.add(n2);

			n.setChildren(children);
		}

		return n;
	}

	private ASTNode factor() {
		ASTNode n = null;
		switch (tokens.lookAheadOne()) {
		case "SIDENTIFIER":
			n = variable();
			break;
		case "SLPAREN":
			if (!tokens.getNext().equals("SLPAREN")) {
				syntaxError();
			}

			n = expression();

			if (!tokens.getNext().equals("SRPAREN")) {
				syntaxError();
			}

			break;

		case "SNOT":
			List<ASTNode> children = new ArrayList<ASTNode>();
			if (!tokens.getNext().equals("SNOT")) {
				syntaxError();
			}
			n = new ASTNode("UnaryOperator", tokens.getSourceName(), tokens.getLineNumber(), null);

			ASTNode n1 = factor();
			children.add(n1);

			n.setChildren(children);

			break;

		default:
			if (firstOfConstant.contains(tokens.lookAheadOne())) {
				n = constant();
			} else {
				syntaxError();
			}
		}
		return n;
	}

	private ASTNode relationalOperator() {
		if (!firstOfRelationalOperator.contains(tokens.getNext())) {
			syntaxError();
		}

		ASTNode n = new ASTNode("BinaryOperator", tokens.getSourceName(), tokens.getLineNumber(), null);
		return n;
	}

	private ASTNode additiveOperator() {
		if (!firstOfAdditiveOperator.contains(tokens.getNext())) {
			syntaxError();
		}

		ASTNode n = new ASTNode("BinaryOperator", tokens.getSourceName(), tokens.getLineNumber(), null);
		return n;
	}

	private ASTNode multiplicativeOperator() {
		if (!firstOfMultiplicativeOperator.contains(tokens.getNext())) {
			syntaxError();
		}

		ASTNode n = new ASTNode("BinaryOperator", tokens.getSourceName(), tokens.getLineNumber(), null);
		return n;
	}

	private ASTNode rowOfVariable() {
		List<ASTNode> children = new ArrayList<ASTNode>();

		ASTNode n1 = variable();
		children.add(n1);

		while (tokens.lookAheadOne().equals("SCOMMA")) {
			if (!tokens.getNext().equals("SCOMMA")) {
				syntaxError();
			}

			ASTNode n2 = variable();
			children.add(n2);
		}

		ASTNode n;
		if (children.size() == 1) {
			n = children.get(0);
		} else {
			n = new ASTNode("Variables", children);
		}
		return n;
	}

	private ASTNode constant() {
		ASTNode n = null;
		switch (tokens.getNext()) {
		case "SCONSTANT":
			n = new ASTNode("NumberLiteral", tokens.getSourceName(), tokens.getLineNumber(), null);
			break;
		case "SSTRING":
			n = new ASTNode("StringLiteral", tokens.getSourceName(), tokens.getLineNumber(), null);
			break;
		case "SFALSE":
			n = new ASTNode("BooleanLiteral", tokens.getSourceName(), tokens.getLineNumber(), null);
			break;
		case "STRUE":
			n = new ASTNode("BooleanLiteral", tokens.getSourceName(), tokens.getLineNumber(), null);
			break;

		default:
			syntaxError();
		}
		return n;
	}

	public ASTNode name() {
		if (!tokens.getNext().equals("SIDENTIFIER")) {
			syntaxError();
		}
		ASTNode n = new ASTNode("Identifire", tokens.getSourceName(), tokens.getLineNumber(), null);
		return n;
	}
}
