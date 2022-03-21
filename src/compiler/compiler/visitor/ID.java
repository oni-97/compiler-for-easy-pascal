package compiler.compiler.visitor;

import java.util.List;

public class ID {
	private String name;
	private Type type;
	private List<Type> fParamType;
	private int scope; //global:0, local:1

	public ID(String name, Type type, int scope) {
		this.name = name;
		this.type = type;
		this.scope = scope;
	}

	public ID(String name, Type type, List<Type> fParamType, int scope) {
		this.name = name;
		this.type = type;
		this.fParamType = fParamType;
		this.scope = scope;
	}

	public String getName() {
		return this.name;
	}

	public Type getType() {
		return this.type;
	}

	public List<Type> getFParamType() {
		return this.fParamType;
	}

	public int getScope() {
		return this.scope;
	}
}
