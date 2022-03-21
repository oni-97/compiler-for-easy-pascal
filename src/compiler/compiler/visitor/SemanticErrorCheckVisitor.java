package compiler.compiler.visitor;

import java.util.ArrayList;
import java.util.List;

import compiler.compiler.ast.ASTNode;
import compiler.compiler.exception.SemanticException;

public class SemanticErrorCheckVisitor {
	private List<ID> globalIDList;
	private List<ID> procedureIDList;
	private List<ID> localIDList;
	private boolean isLocal;
	private String currentProcedure;
	private boolean minus;

	public SemanticErrorCheckVisitor() {
		this.globalIDList = new ArrayList<ID>();
		this.localIDList = new ArrayList<ID>();
		this.procedureIDList = new ArrayList<ID>();
		this.isLocal = false;
		this.minus = false;
	}

	public Type visit(ASTNode node) {
		preTypeCheck(node);

		List<Type> typeList = traceChildren(node);

		Type type = postTypeCheck(node, typeList);

		return type;
	}

	private void preTypeCheck(ASTNode node) {
		switch (node.getLabel()) {
		case "Subprogram":
			isLocal = true;
			break;

		case "ProcedureDeclarator":
			currentProcedure = node.getSourceName();
			break;

		case "UnaryOperator":
			setMinus(node);
			break;
		}
	}

	private void setMinus(ASTNode node) {
		if (node.getSourceName().equals("-")) {
			minus = true;
		}
	}

	private List<Type> traceChildren(ASTNode node) {
		List<Type> typeList = new ArrayList<Type>();

		List<ASTNode> children = node.getChildren();
		if (children != null) {
			int i = 0;
			while (i < children.size()) {
				Type t = children.get(i).accept(this);
				if (t != null)
					typeList.add(t);
				i++;
			}
		}
		return typeList;
	}

	public Type postTypeCheck(ASTNode node, List<Type> typeList) {
		Type type = null;
		switch (node.getLabel()) {
		case "VariableDeclarator":
			addVar(node, typeList);
			break;

		case "ProcedureDeclarator":
			addPro(node, typeList);
			break;

		case "FormalParameterDeclarator":
			type = addFPara(node, typeList);
			break;

		case "StandardType":
			type = new Type(node.getSourceName());
			break;

		case "ArrayType":
			type = arrayType(node, typeList);
			break;

		case "Subprogram":
			localIDList.clear();
			isLocal = false;
			break;

		case "NumberLiteral":
			type = numberLiteral(node);
			break;

		case "StringLiteral":
			type = stringiteral(node);
			break;

		case "BooleanLiteral":
			type = new Type("boolean");
			break;

		case "UnaryOperator":
			type = unaryOpCheck(node, typeList);
			minus = false;
			break;

		case "BinaryOperator":
			type = binaryOpCheck(node, typeList);
			break;

		case "Assignment":
			assignment(node, typeList);
			break;

		case "SimpleVariable":
			type = simpleVariable(node);
			break;

		case "IndexedVariable":
			type = indexedVariable(node, typeList);
			break;

		case "ProcedureCall":
			prosedureCall(node, typeList);
			break;

		case "IfStatement":
			isExpBoolean(node, typeList);
			break;

		case "WhileStatement":
			isExpBoolean(node, typeList);
			break;

		// add
		case "InputStatementComponent":
			inputOutputCheck(node, typeList);
			break;

		case "OutputStatementComponent":
			inputOutputCheck(node, typeList);
			break;

		case "LeftValue":
			type = typeList.get(0);
			break;

		case "IfExp":
			type = typeList.get(0);
			break;

		case "WhileExp":
			type = typeList.get(0);
			break;

		default:
		}

		return type;
	}

	private void inputOutputCheck(ASTNode node, List<Type> typeList) {
		if (typeList.size() > 0) {
			int i;
			for (i = 0; i < typeList.size(); i++) {
				Type type = typeList.get(i);
				String n = type.getType();
				if (n.equals("integer") || n.equals("char"))
					;
				else if (n.equals("array")) {
					String subType = type.getSubType();
					if (!subType.equals("char")) {
						semanticError(node);
					}
				} else {
					semanticError(node);
				}
			}
		}
	}

	private Type stringiteral(ASTNode node) {
		String str = node.getSourceName().replace("'", "").replace("'", "");
		Type type;
		if (str.length() == 1) {
			type = new Type("char");
		} else {
			type = new Type("array", "char");
		}
		return type;
	}

	private void isExpBoolean(ASTNode node, List<Type> typeList) {
		Type exp = typeList.get(0);
		if (!exp.getType().equals("boolean")) {
			semanticError(node);
		}
	}

	private void prosedureCall(ASTNode node, List<Type> typeList) {
		ID id = searchProcedureID(node);
		List<Type> fparams = id.getFParamType();

		if (fparams.size() != typeList.size()) {
			semanticError(node);
		} else {
			int i;
			for (i = 0; i < fparams.size(); i++) {
				String t1 = fparams.get(i).getType();
				String t2 = typeList.get(i).getType();
				if (!t1.equals(t2)) {
					semanticError(node);
				}
			}
		}
	}

	private Type binaryOpCheck(ASTNode node, List<Type> typeList) {
		Type type = null;
		switch (node.getSourceName()) {
		case "+":
		case "-":
		case "*":
		case "/":
		case "div":
		case "mod":
			type = arithOpCehck(node, typeList);
			break;

		case "and":
		case "or":
			type = logOpCheck(node, typeList);
			break;

		case "=":
		case "<>":
		case "<":
		case "<=":
		case ">":
		case ">=":
			type = relOpCheck(node, typeList);
			break;

		default:
		}
		return type;
	}

	private Type relOpCheck(ASTNode node, List<Type> typeList) {
		Type type1 = typeList.get(0);
		Type type2 = typeList.get(1);
		if (!type1.getType().equals(type2.getType())) {
			semanticError(node);
		}
		return new Type("boolean");
	}

	private Type logOpCheck(ASTNode node, List<Type> typeList) {
		Type type1 = typeList.get(0);
		Type type2 = typeList.get(1);

		if (!type1.getType().equals("boolean")) {
			semanticError(node);
		}
		if (!type2.getType().equals("boolean")) {
			semanticError(node);
		}

		return new Type("boolean");
	}

	private Type arithOpCehck(ASTNode node, List<Type> typeList) {
		Type type1 = typeList.get(0);
		Type type2 = typeList.get(1);

		if (!type1.getType().equals("integer")) {
			semanticError(node);
		}
		if (!type2.getType().equals("integer")) {
			semanticError(node);
		}

		return new Type("integer");
	}

	private void assignment(ASTNode node, List<Type> typeList) {
		Type type1 = typeList.get(0);
		Type type2 = typeList.get(1);

		if (type1.getType().equals("array") || type2.getType().equals("array")) {
			semanticError(node);
		}

		if (!type1.getType().equals(type2.getType())) {
			semanticError(node);
		}
	}

	private Type indexedVariable(ASTNode node, List<Type> typeList) {
		ID id = searchVariableID(node);
		Type idType = id.getType();
		if (!idType.getType().equals("array")) {
			semanticError(node);
		}

		Type indexType = typeList.get(0);
		if (!indexType.getType().equals("integer")) {
			semanticError(node);
		}

		Type type = new Type(idType.getSubType());
		return type;
	}

	private Type simpleVariable(ASTNode node) {
		ID id = searchVariableID(node);
		return id.getType();
	}

	public ID searchVariableID(ASTNode node) {
		String name = node.getSourceName();
		if (isLocal) {
			int i;
			for (i = 0; i < localIDList.size(); i++) {
				ID id = localIDList.get(i);
				if (id.getName().equals(name)) {
					return id;
				}
			}
		}

		int i;
		for (i = 0; i < globalIDList.size(); i++) {
			ID id = globalIDList.get(i);
			if (id.getName().equals(name)) {
				return id;
			}
		}

		semanticError(node);
		return null;
	}

	public ID searchProcedureID(ASTNode node) {
		String name = node.getSourceName();
		int i;
		for (i = 0; i < procedureIDList.size(); i++) {
			ID id = procedureIDList.get(i);
			if (id.getName().equals(name)) {
				return id;
			}
		}

		semanticError(node);
		return null;
	}

	private Type unaryOpCheck(ASTNode node, List<Type> typeList) {
		Type type = typeList.get(0);
		String typeName = type.getType();

		switch (node.getSourceName()) {
		case "+":
		case "-":
			if (!typeName.equals("integer"))
				semanticError(node);
			break;

		case "not":
			if (!typeName.equals("boolean"))
				semanticError(node);
			break;

		default:
		}
		return type;
	}

	private Type numberLiteral(ASTNode node) {
		String intValue = node.getSourceName();
		int value = Integer.parseInt(intValue);
		Type type = new Type("integer", value);
		if (minus) {
			if (32768 < value) {
				semanticError(node);
			}
		} else {
			if (32767 < value) {
				semanticError(node);
			}
		}
		return type;
	}

	private Type arrayType(ASTNode node, List<Type> typeList) {
		int min = typeList.get(0).getIntValue();
		int max = typeList.get(1).getIntValue();
		String subType = typeList.get(2).getType();
		if (min > max) {
			semanticError(node);
		}
		Type type = new Type("array", subType);
		return type;
	}

	private Type addFPara(ASTNode node, List<Type> typeList) {
		Type type = typeList.get(0);
		String name = node.getSourceName();
		int scope = 1;

		if (currentProcedure.equals(name)) {
			semanticError(node);
		}
		checkLocalIDList(node);

		ID id = new ID(name, type, scope);
		localIDList.add(id);

		return type;
	}

	private void addPro(ASTNode node, List<Type> typeList) {
		checkGlobalIDList(node);
		checkProcedureIDList(node);

		Type type = null;
		String name = node.getSourceName();
		List<Type> fParamType = typeList;
		int scope = 0;

		ID id = new ID(name, type, fParamType, scope);
		procedureIDList.add(id);
	}

	private void addVar(ASTNode node, List<Type> typeList) {
		int scope;
		if (isLocal) {
			checkCurrentProcedure(node);
			checkLocalIDList(node);
			scope = 1;
		} else {
			checkGlobalIDList(node);
			scope = 0;
		}

		Type type = typeList.get(0);
		String name = node.getSourceName();

		ID id = new ID(name, type, scope);

		if (isLocal)
			localIDList.add(id);
		else
			globalIDList.add(id);
	}

	public void checkLocalIDList(ASTNode node) {
		String name = node.getSourceName();
		int i;
		for (i = 0; i < localIDList.size(); i++) {
			if (localIDList.get(i).getName().equals(name)) {
				semanticError(node);
			}
		}
	}

	public void checkGlobalIDList(ASTNode node) {
		String name = node.getSourceName();
		int i;
		for (i = 0; i < globalIDList.size(); i++) {
			if (globalIDList.get(i).getName().equals(name)) {
				semanticError(node);
			}
		}
	}

	public void checkProcedureIDList(ASTNode node) {
		checkLocalIDList(node);
		String name = node.getSourceName();
		int i;
		for (i = 0; i < procedureIDList.size(); i++) {
			if (procedureIDList.get(i).getName().equals(name)) {
				semanticError(node);
			}
		}
	}

	public void checkCurrentProcedure(ASTNode node) {
		String name = node.getSourceName();
		ID currentProcedure = procedureIDList.get(procedureIDList.size() - 1);
		if (currentProcedure.getName().equals(name)) {
			semanticError(node);
		}
	}

	private void semanticError(ASTNode node) {
		throw new SemanticException("Semantic error: line " + node.getLinenumber());
	}
}
