package compiler.compiler.visitor;

public class Type {
	private String type;
	private String subType;
	private int indexMin;
	private int indexMax;
	private int intValue;

	public Type(String type) {
		this.type = type;
	}

	public Type(String type, int intValue) {
		this.type = type;
		this.intValue = intValue;
	}

	public Type(String type, String subType) {
		this.type = type;
		this.subType = subType;
	}

	public String getType() {
		return this.type;
	}

	public String getSubType() {
		return this.subType;
	}

	public int getIndexMin() {
		return this.indexMin;
	}

	public int getIndexMax() {
		return this.indexMax;
	}

	public int getIntValue() {
		return this.intValue;
	}

	public int getArraySize() {
		return indexMax - indexMin + 1;
	}
}
