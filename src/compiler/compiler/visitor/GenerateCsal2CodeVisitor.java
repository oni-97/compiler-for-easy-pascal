package compiler.compiler.visitor;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import compiler.compiler.ast.ASTNode;

public class GenerateCsal2CodeVisitor {
	private PrintWriter pw;
	private int leftValue;
	private boolean isLocal;
	private boolean isArray;
	private boolean isCurrent;
	private boolean isOutput;
	private boolean isInput;
	private List<Integer> arrayRange;
	private int inoutMode; // 0:int, 1:char, 2:string
	private int trueCnt;
	private int bothCnt;
	private int loopCnt;
	private int elseCnt;
	private int memCnt;
	private int endifCnt;
	private int procCnt;
	private int fparamCnt;
	private int varType; // 0:int 1:char 2:boolean 3:array
	private int varSubType; // 0:int 1:char 2:boolean
	private int varArraySize;
	private List<String> charList;
	private List<ProcListComponent> procList;
	private List<VarListComponent> globalVarList;
	private List<VarListComponent> localVarList;
	private Deque<Integer> elseStack;
	private Deque<Integer> endIfStack;
	private Deque<Integer> loopStack;

	public GenerateCsal2CodeVisitor(PrintWriter pw) {
		this.pw = pw;
		this.inoutMode = -1;
		this.varType = -1;
		this.varSubType = -1;
		this.leftValue = 0;
		this.charList = new ArrayList<String>();
		this.bothCnt = 0;
		this.trueCnt = 0;
		this.loopCnt = 0;
		this.elseCnt = 0;
		this.memCnt = 0;
		this.endifCnt = 0;
		this.procCnt = 0;
		this.fparamCnt = 0;
		this.varArraySize = -1;
		this.globalVarList = new ArrayList<VarListComponent>();
		this.localVarList = new ArrayList<VarListComponent>();
		this.procList = new ArrayList<ProcListComponent>();
		this.arrayRange = new ArrayList<Integer>();
		this.elseStack = new ArrayDeque<Integer>();
		this.endIfStack = new ArrayDeque<Integer>();
		this.loopStack = new ArrayDeque<Integer>();
		this.isCurrent = false;
		this.isOutput = false;
		this.isInput = false;
	}

	public void visit(ASTNode node) {
		preGenCode(node);

		traceChildren(node);

		postGenCode(node);
	}

	private void traceChildren(ASTNode node) {
		List<ASTNode> children = node.getChildren();
		if (children != null) {
			if (node.getLabel().equals("ProcedureCall")) {
				int i = children.size() - 1;
				while (i >= 0) {
					children.get(i).accept(this);
					i--;
				}
			} else {
				int i = 0;
				while (i < children.size()) {
					children.get(i).accept(this);
					i++;
				}
			}
		}
	}

	private void preGenCode(ASTNode node) {
		switch (node.getLabel()) {

		case "Program":
			preGenCodeAtProgram();
			break;

		case "LeftValue":
			leftValue = 1;
			break;

		case "Subprogram":
			localVarList = new ArrayList<VarListComponent>();
			isLocal = true;
			break;

		case "ProcedureDeclarator":
			procList.add(new ProcListComponent(node.getSourceName(), procCnt, memCnt));
			genCode("PROC" + procCnt, "NOP", "");
			procCnt++;
			fparamCnt = 0;
			break;

		case "ArrayType":
			isArray = true;
			break;

		case "WhileStatement":
			preGenCodeAtWhileStatement();
			break;

		case "IndexedVariable":
			if (leftValue > 0) {
				leftValue++;
			}
			break;

		case "ElseStatement":
			preGenCodeAtElseStatement(node);
			break;

		case "ProcedureCall":
			if (procList.size() > 0 && procList.get(procList.size() - 1).getName().equals(node.getSourceName())) {
				isCurrent = true;
				preGenCodeAtProcedureCall(node);
			}
			break;

		case "OutputStatement":
			isOutput = true;
			break;

		case "InputStatement":
			isInput = true;
			break;
		}

	}

	private void preGenCodeAtProcedureCall(ASTNode node) {
		int fparamMemAddr = procList.get(procList.size() - 1).getFparamMemAddr();
		int fparamCnt = procList.get(procList.size() - 1).getFparamCnt();
		int i;
		for (i = 0; i < fparamCnt; i++) {
			genCode("LD", "GR2, =" + (fparamMemAddr + i));
			genCode("LD", "GR1, VAR, GR2");
			genCode("PUSH", "0, GR1");
		}
	}

	private void preGenCodeAtElseStatement(ASTNode node) {
		if (node.getChildren().size() > 0) {
			genCode("JUMP", "ENDIF" + endifCnt);
			endIfStack.push(endifCnt);
			endifCnt++;
		}
		genCode("ELSE" + elseStack.pop(), "NOP", "");
	}

	private void preGenCodeAtWhileStatement() {
		genCode("LOOP" + loopCnt, "NOP", "");
		loopStack.push(loopCnt);
		loopCnt++;
	}

	private void postGenCode(ASTNode node) {
		switch (node.getLabel()) {
		case "Program":
			postGenCodeAtProgram();
			break;

		case "OutputStatementComponent":
			postGenCodeAtOutputStatementComponent();
			break;

		case "OutputStatement":
			postGenCodeAtOutputStatement();
			isOutput = false;
			break;

		case "InputStatementComponent":
			postGenCodeAtInputStatementComponent();
			break;

		case "InputStatement":
			if (node.getChildren() == null) {
				postGenCodeAtInputStatement();
			}
			isInput = false;
			break;

		case "NumberLiteral":
			postGenCodeAtNumberLiteral(node);
			break;

		case "BinaryOperator":
			postGenCodeAtBinaryOperator(node);
			break;

		case "StringLiteral":
			postGenCodeAtStringLiteral(node);
			break;

		case "BooleanLiteral":
			postGenCodeAtBooleanLiteral(node);
			break;

		case "UnaryOperator":
			postGenCodeAtUnaryOperator(node);
			break;

		case "LeftValue":
			leftValue = 0;
			break;

		case "Subprogram":
			postGenCodeAtSubprogram();
			localVarList.clear();
			isLocal = false;
			break;

		case "VariableDeclarator":
			addVarToList(node);
			if (isArray) {
				arrayIndexCount();
				isArray = false;
				arrayRange.clear();
			}
			if (isLocal) {
				int fparamMemAddr = procList.get(procList.size() - 1).getFparamMemAddr();
				procList.get(procList.size() - 1).setFpramCnt(memCnt - fparamMemAddr);
			}
			break;

		case "FormalParameterDeclarator":
			addVarToList(node);
			procList.get(procList.size() - 1).incFpramCnt();
			postGenCodeAtFormalParameterDeclarator(node);
			break;

		case "SimpleVariable":
			if (leftValue == 1 || isInput) {
				postGenCodeAtSimpleLeftVariable(node);
				if (isInput) {
					inoutMode = getType(node.getSourceName());
				}
			} else {
				postGenCodeAtSimpleVariable(node);
				inoutMode = getType(node.getSourceName());
			}
			break;

		case "IndexedVariable":
			if (leftValue > 0) {
				leftValue--;
			}

			if (leftValue == 1 || isInput) {
				postGenCodeAtIndexedLeftVariable(node);
				inoutMode = getSubType(node.getSourceName());
			} else {
				postGenCodeAtIndexedVariable(node);
				inoutMode = getSubType(node.getSourceName());
			}
			break;

		case "Assignment":
			postGenCodeAtAssignment();
			break;

		case "StandardType":
			varType = getVarType(node.getSourceName());
			break;

		case "ArrayType":
			varSubType = varType;
			varType = 3;
			break;

		case "WhileExp":
			postGenCodeAtWhileExp();
			break;

		case "WhileStatement":
			postGenCodeAtWhileStatement();
			break;

		case "IfExp":
			postGenCodeAtIfExp();
			break;

		case "ElseStatement":
			if (node.getChildren().size() > 0) {
				postGenCodeAtElseStatement();
			}
			break;

		case "ProcedureCall":
			postGenCodeAtProcedureCall(node);
			break;

		case "ProcedureDeclarator":
			if (fparamCnt > 0) {
				postGenCodeAtProcedureDeclarator();
			}
			break;

		}

	}

	private void postGenCodeAtInputStatementComponent() {
		switch (inoutMode) {
		case 0:
			genCode("POP", "GR2");
			genCode("LAD", "GR1, VAR");
			genCode("ADDA", "GR2, GR1");
			genCode("CALL", "RDINT");
			break;

		case 1:
			genCode("POP", "GR2");
			genCode("LAD", "GR1, VAR");
			genCode("ADDA", "GR2, GR1");
			genCode("CALL", "RDCH");
			break;

		case 3:
			genCode("POP", "GR2");
			genCode("LAD", "GR1, " + varArraySize);
			genCode("CALL", "RDSTR");
			break;
		}
	}

	private void postGenCodeAtInputStatement() {
		genCode("CALL", "RDLN");
	}

	private void postGenCodeAtSubprogram() {
		genCode("RET", "");
	}

	private void postGenCodeAtProcedureDeclarator() {
		genCode("LD", "GR1, 0, GR8");
		genCode("ADDA", "GR8, =" + fparamCnt);
		genCode("ST", "GR1, 0, GR8");
	}

	private void postGenCodeAtFormalParameterDeclarator(ASTNode node) {
		if (fparamCnt == 0) {
			genCode("LD", "GR1, GR8");
		}
		fparamCnt++;

		genCode("ADDA", "GR1, =" + fparamCnt);
		genCode("LD", "GR2, 0, GR1");
		int memRelAddr = getMemRelAddr(node.getSourceName());
		genCode("LD", "GR3, =" + memRelAddr);
		genCode("ST", "GR2, VAR, GR3");
		genCode("SUBA", "GR1, =" + fparamCnt);
	}

	private void postGenCodeAtProcedureCall(ASTNode node) {
		int procNum = getProcNum(node.getSourceName());
		genCode("CALL", "PROC" + procNum);
		if (isCurrent) {
			isCurrent = false;
			int fparamMemAddr = procList.get(procList.size() - 1).getFparamMemAddr();
			int fparamCnt = procList.get(procList.size() - 1).getFparamCnt();
			int i;
			for (i = fparamCnt - 1; i >= 0; i--) {
				genCode("POP", "GR1");
				genCode("LD", "GR2, =" + (fparamMemAddr + i));
				genCode("ST", "GR1, VAR, GR2");
			}
		}

	}

	private int getProcNum(String name) {
		ProcListComponent com = searchProcList(name);
		if (com != null) {
			return com.getNumber();
		} else {
			return -1;
		}
	}

	private ProcListComponent searchProcList(String name) {
		int i;
		for (i = 0; i < procList.size(); i++) {
			if (procList.get(i).getName().equals(name)) {
				return procList.get(i);
			}
		}
		return null;
	}

	private void postGenCodeAtElseStatement() {
		genCode("ENDIF" + endIfStack.pop(), "NOP", "");
	}

	private void postGenCodeAtIfExp() {
		genCode("POP", "GR1");
		genCode("CPA", "GR1, =#FFFF");
		genCode("JZE", "ELSE" + elseCnt);
		elseStack.push(elseCnt);
		elseCnt++;
	}

	private void postGenCodeAtWhileStatement() {
		int value = loopStack.pop();
		genCode("JUMP", "LOOP" + value);
		genCode("ENDLP" + value, "NOP", "");
	}

	private void postGenCodeAtWhileExp() {
		int value = loopStack.pop();
		loopStack.push(value);
		genCode("POP", "GR1");
		genCode("CPL", "GR1, =#FFFF");
		genCode("JZE", "ENDLP" + value);
	}

	private void postGenCodeAtIndexedLeftVariable(ASTNode node) {
		genCode("POP", "GR2");
		int varMem = getMemRelAddr(node.getSourceName());
		int min = getArrayMin(node.getSourceName());
		genCode("ADDA", "GR2, =" + (varMem - min));
		genCode("PUSH", "0, GR2");
	}

	private int getArrayMin(String name) {
		int arrayMin;
		if (isLocal) {
			VarListComponent com = searchLoccalVarList(name);
			if (com != null) {
				arrayMin = com.getArrayMin();
				return arrayMin;
			}
		}

		VarListComponent com = searchGlobalVarList(name);
		if (com != null) {
			return com.getArrayMin();
		} else
			return -1; // もう少しましな値
	}

	private void postGenCodeAtIndexedVariable(ASTNode node) {
		genCode("POP", "GR2");
		int varMem = getMemRelAddr(node.getSourceName());
		int min = getArrayMin(node.getSourceName());
		genCode("ADDA", "GR2, =" + (varMem - min));
		genCode("LD", "GR1, VAR, GR2");
		genCode("PUSH", "0, GR1");
	}

	private int getSubType(String name) {
		if (isLocal) {
			VarListComponent com = searchLoccalVarList(name);
			if (com != null) {
				return com.getSubType();
			}
		}

		VarListComponent com = searchGlobalVarList(name);
		if (com != null) {
			return com.getSubType();
		} else
			return -1;
	}

	private int getType(String name) {
		if (isLocal) {
			VarListComponent com = searchLoccalVarList(name);
			if (com != null) {
				return com.getType();
			}
		}

		VarListComponent com = searchGlobalVarList(name);
		if (com != null) {
			return com.getType();
		} else
			return -1;
	}

	private int getVarType(String name) {
		switch (name) {
		case "integer":
			return 0;
		case "char":
			return 1;
		case "boolean":
			return 2;
		default:
			return -1;
		}
	}

	private void postGenCodeAtAssignment() {
		genCode("POP", "GR1");
		genCode("POP", "GR2");
		genCode("ST", "GR1, VAR, GR2");
	}

	private void postGenCodeAtSimpleLeftVariable(ASTNode node) {
		VarListComponent com = getVarListComponent(node.getSourceName());
		if (com.getType() == 3) {
			if (isInput) {
				int varMem = com.getMemRelAddr();
				int arraySize = com.getArraySize();
				varArraySize = arraySize;
				genCode("LAD", "GR1, VAR");
				genCode("ADDA", "GR1, =" + varMem);
				genCode("PUSH", "0, GR1");
			}
		} else {
			int varMem = getMemRelAddr(node.getSourceName());
			genCode("PUSH", "" + varMem);
		}
	}

	private void postGenCodeAtSimpleVariable(ASTNode node) {
		VarListComponent com = getVarListComponent(node.getSourceName());
		if (com.getType() == 3) {
			if (isOutput) {
				int varMem = com.getMemRelAddr();
				int arraySize = com.getArraySize();
				varArraySize = arraySize;
				genCode("LAD", "GR1, VAR");
				genCode("ADDA", "GR1, =" + varMem);
				genCode("PUSH", "0, GR1");
			}
		} else {
			int varMem = getMemRelAddr(node.getSourceName());
			genCode("LD", "GR2, =" + varMem);
			genCode("LD", "GR1, VAR, GR2");
			genCode("PUSH", "0, GR1");
		}
	}

	private VarListComponent getVarListComponent(String name) {
		if (isLocal) {
			VarListComponent com = searchLoccalVarList(name);
			if (com != null) {
				return com;
			}
		}

		VarListComponent com = searchGlobalVarList(name);
		if (com != null) {
			return com;
		}
		return null;
	}

	private int getMemRelAddr(String name) {
		// local処理
		int memRelAddr = -1;
		if (isLocal) {
			VarListComponent com = searchLoccalVarList(name);
			if (com != null) {
				memRelAddr = com.getMemRelAddr();
				return memRelAddr;
			}
		}

		VarListComponent com = searchGlobalVarList(name);
		if (com != null) {
			memRelAddr = com.getMemRelAddr();
		}
		return memRelAddr;

	}

	private VarListComponent searchLoccalVarList(String name) {
		int i;
		for (i = 0; i < localVarList.size(); i++) {
			if (localVarList.get(i).getName().equals(name)) {
				return localVarList.get(i);
			}
		}
		return null;
	}

	private VarListComponent searchGlobalVarList(String name) {
		int i;
		for (i = 0; i < globalVarList.size(); i++) {
			if (globalVarList.get(i).getName().equals(name)) {
				return globalVarList.get(i);
			}
		}
		return null;
	}

	private void arrayIndexCount() {
		int min = arrayRange.get(0);
		int max = arrayRange.get(1);
		int range = max - min;
		memCnt = memCnt + range;
	}

	private void addVarToList(ASTNode node) {
		VarListComponent com;
		if (varType == 3) {
			com = new VarListComponent(node.getSourceName(), memCnt, varType, varSubType, arrayRange.get(1),
					arrayRange.get(0));
		} else {
			com = new VarListComponent(node.getSourceName(), memCnt, varType);
		}

		if (isLocal) {
			localVarList.add(com);
			memCnt++;
		} else {
			globalVarList.add(com);
			memCnt++;
		}
	}

	private void postGenCodeAtUnaryOperator(ASTNode node) {
		switch (node.getSourceName()) {
		case "not":
			postGenCodeAtNotOp();
			break;

		case "-":
			if (isArray) {
				Integer value = arrayRange.get(arrayRange.size() - 1) * (-1);
				arrayRange.set(arrayRange.size() - 1, value);
			} else {
				postGenCodeAtMinusSignOp();
			}
			inoutMode = 0;
			break;
		}
	}

	private void postGenCodeAtMinusSignOp() {
		genCode("POP", "GR2");
		genCode("LD", "GR1, =0");
		genCode("SUBA", "GR1, GR2");
		genCode("PUSH", "0, GR1");
	}

	private void postGenCodeAtNotOp() {
		genCode("POP", "GR1");
		genCode("XOR", "GR1, =#FFFF");
		genCode("PUSH", "0, GR1");
	}

	private void postGenCodeAtBooleanLiteral(ASTNode node) {
		switch (node.getSourceName()) {
		case "true":
			genCode("PUSH", "#0000");
			break;

		case "false":
			genCode("PUSH", "#FFFF");
			break;
		}
	}

	private void postGenCodeAtBinaryOperator(ASTNode node) {
		genCode("POP", "GR2");
		genCode("POP", "GR1");

		switch (node.getSourceName()) {
		case "+":
			postGenCodeAtAddOp();
			inoutMode = 0;
			break;

		case "-":
			postGenCodeAtSubOp();
			inoutMode = 0;
			break;

		case "*":
			postGenCodeAtMulOp();
			inoutMode = 0;
			break;

		case "/":
		case "div":
			postGenCodeAtDivOp();
			inoutMode = 0;
			break;

		case "mod":
			postGenCodeAtModOp();
			inoutMode = 0;
			break;

		case "and":
			postGenCodeAtAndOp();
			break;

		case "or":
			postGenCodeAtOrOp();
			break;

		case "=":
			postGenCodeAtEqualOp();
			break;

		case "<>":
			postGenCodeAtNotEqualOp();
			break;

		case "<":
			postGenCodeAtLessOp();
			break;

		case "<=":
			postGenCodeAtLessEqualOp();
			break;

		case ">":
			postGenCodeAtGratOp();
			break;

		case ">=":
			postGenCodeAtGreatEqualOp();
			break;

		default:
		}
	}

	private void postGenCodeAtNotEqualOp() {
		genCode("CPA", "GR1, GR2");
		genCode("JNZ", "TRUE" + trueCnt);
		genCode("LD", "GR1, =#FFFF");
		genCode("JUMP", "BOTH" + bothCnt);
		genCode("TRUE" + trueCnt, "LD", "GR1, =#0000");
		genCode("BOTH" + bothCnt, "PUSH", "0, GR1");
		bothCnt++;
		trueCnt++;
	}

	private void postGenCodeAtGreatEqualOp() {
		genCode("CPA", "GR1, GR2");
		genCode("JMI", "TRUE" + trueCnt);
		genCode("LD", "GR1, =#0000");
		genCode("JUMP", "BOTH" + bothCnt);
		genCode("TRUE" + trueCnt, "LD", "GR1, =#FFFF");
		genCode("BOTH" + bothCnt, "PUSH", "0, GR1");
		bothCnt++;
		trueCnt++;
	}

	private void postGenCodeAtGratOp() {
		genCode("CPA", "GR1, GR2");
		genCode("JPL", "TRUE" + trueCnt);
		genCode("LD", "GR1, =#FFFF");
		genCode("JUMP", "BOTH" + bothCnt);
		genCode("TRUE" + trueCnt, "LD", "GR1, =#0000");
		genCode("BOTH" + bothCnt, "PUSH", "0, GR1");
		bothCnt++;
		trueCnt++;
	}

	private void postGenCodeAtEqualOp() {
		genCode("CPA", "GR1, GR2");
		genCode("JZE", "TRUE" + trueCnt);
		genCode("LD", "GR1, =#FFFF");
		genCode("JUMP", "BOTH" + bothCnt);
		genCode("TRUE" + trueCnt, "LD", "GR1, =#0000");
		genCode("BOTH" + bothCnt, "PUSH", "0, GR1");
		bothCnt++;
		trueCnt++;
	}

	private void postGenCodeAtLessEqualOp() {
		genCode("CPA", "GR1, GR2");
		genCode("JPL", "TRUE" + trueCnt);
		genCode("LD", "GR1, =#0000");
		genCode("JUMP", "BOTH" + bothCnt);
		genCode("TRUE" + trueCnt, "LD", "GR1, =#FFFF");
		genCode("BOTH" + bothCnt, "PUSH", "0, GR1");
		bothCnt++;
		trueCnt++;
	}

	private void postGenCodeAtLessOp() {
		genCode("CPA", "GR1, GR2");
		genCode("JMI", "TRUE" + trueCnt);
		genCode("LD", "GR1, =#FFFF");
		genCode("JUMP", "BOTH" + bothCnt);
		genCode("TRUE" + trueCnt, "LD", "GR1, =#0000");
		genCode("BOTH" + bothCnt, "PUSH", "0, GR1");
		bothCnt++;
		trueCnt++;
	}

	private void postGenCodeAtOrOp() {
		genCode("OR", "GR1, GR2");
		genCode("PUSH", "0, GR1");
	}

	private void postGenCodeAtAndOp() {
		genCode("AND", "GR1, GR2");
		genCode("PUSH", "0, GR1");
	}

	private void postGenCodeAtModOp() {
		genCode("CALL", "DIV");
		genCode("PUSH", "0, GR1");
	}

	private void postGenCodeAtDivOp() {
		genCode("CALL", "DIV");
		genCode("PUSH", "0, GR2");
	}

	private void postGenCodeAtMulOp() {
		genCode("CALL", "MULT");
		genCode("PUSH", "0, GR2");
	}

	private void postGenCodeAtSubOp() {
		genCode("SUBA", "GR1, GR2");
		genCode("PUSH", "0, GR1");
	}

	private void postGenCodeAtAddOp() {
		genCode("ADDA", "GR1, GR2");
		genCode("PUSH", "0, GR1");
	}

	private void postGenCodeAtNumberLiteral(ASTNode node) {
		if (isArray) {
			int value = Integer.parseInt(node.getSourceName());
			arrayRange.add(value);
		} else {
			genCode("PUSH", node.getSourceName());
		}
	}

	private void postGenCodeAtOutputStatement() {
		genCode("CALL", "WRTLN");
	}

	private void postGenCodeAtStringLiteral(ASTNode node) {
		int length = node.getSourceName().replace("'", "").replace("'", "").length();
		// str とchar の区別
		if (length == 1) {
			genCode("LD", "GR1, =" + node.getSourceName());
			genCode("PUSH", "0, GR1");
			inoutMode = 1;
		} else {
			genCode("LD", "GR1, =" + length);
			genCode("PUSH", "0, GR1");
			genCode("LAD", "GR2, CHAR" + charList.size());
			genCode("PUSH", "0, GR2");
			charList.add(node.getSourceName());
			inoutMode = 2;
		}

	}

	private void postGenCodeAtProgram() {
		genCode("RET");
		genCodeForProc();
		genCode("VAR", "DS", "" + memCnt);
		genCodeForCharMem();
		genCode("LIBBUF", "DS", "256");
		genCode("END");
	}

	private void genCodeForProc() {
		int i = 0;
		while (i < procList.size()) {
			List<String> codeList = procList.get(i).getCodeList();
			int j = 0;
			while (j < codeList.size()) {
				pw.println(codeList.get(j));
				pw.flush();
				j++;
			}
			i++;
		}
	}

	private void genCodeForCharMem() {
		int i = 0;
		while (i < charList.size()) {
			genCode("CHAR" + Integer.toString(i), "DC", charList.get(i));
			i++;
		}
	}

	private void postGenCodeAtOutputStatementComponent() {
		switch (inoutMode) {
		case 0:
			genCode("POP", "GR2");
			genCode("CALL", "WRTINT");
			break;

		case 1:
			genCode("POP", "GR2");
			genCode("CALL", "WRTCH");
			break;

		case 2:
			genCode("POP", "GR2");
			genCode("POP", "GR1");
			genCode("CALL", "WRTSTR");
			break;

		case 3:
			genCode("POP", "GR2");
			genCode("LAD", "GR1, " + varArraySize);
			genCode("CALL", "WRTSTR");
			break;
		}
	}

	private void preGenCodeAtProgram() {
		genCode("CASL", "START", "BEGIN");
		genCode("BEGIN", "LAD", "GR6, 0");
		genCode("LAD", "GR7, LIBBUF");
	}

	private void addProcCode(String code) {
		ProcListComponent cmp = procList.get(procList.size() - 1);
		cmp.getCodeList().add(code);
	}

	public void genCode(String instrCode) {
		if (isLocal) {
			String code = "\t" + instrCode + "\t";
			addProcCode(code);
		} else {
			pw.println("\t" + instrCode + "\t");
			pw.flush();
		}
	}

	public void genCode(String instrCode, String op) {
		if (isLocal) {
			String code = "\t" + instrCode + "\t" + op;
			addProcCode(code);
		} else {
			pw.println("\t" + instrCode + "\t" + op);
			pw.flush();
		}
	}

	public void genCode(String label, String instrCode, String op) {
		if (isLocal) {
			String code = label + "\t" + instrCode + "\t" + op;
			addProcCode(code);
		} else {
			pw.println(label + "\t" + instrCode + "\t" + op);
			pw.flush();
		}
	}

}