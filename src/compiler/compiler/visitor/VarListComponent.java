package compiler.compiler.visitor;

public class VarListComponent {
	private String name;
	private int memRelAddr;
	private int type; //0:int 1:char 2:boolean 3:array
	private int subType; //0:int 1:char 2:boolean
	private int arrayMax;
	private int arrayMin;

	VarListComponent(String name, int memRelAddr, int type) {
		this.name = name;
		this.memRelAddr = memRelAddr;
		this.type = type;
	}

	VarListComponent(String name, int memRelAddr, int type,
			int subType, int arrayMax, int arrayMin) {
		this.name = name;
		this.memRelAddr = memRelAddr;
		this.type = type;
		this.subType = subType;
		this.arrayMax = arrayMax;
		this.arrayMin = arrayMin;
	}

	public String getName() {
		return this.name;
	}

	public int getMemRelAddr() {
		return this.memRelAddr;
	}

	public int getType() {
		return this.type;
	}

	public int getSubType() {
		return this.subType;
	}

	public int getArrayMin() {
		return this.arrayMin;
	}

	public int getArrayMax() {
		return this.arrayMax;
	}

	public int getArraySize() {
		return this.arrayMax - this.arrayMin + 1;
	}
}
