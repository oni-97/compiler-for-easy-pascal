package compiler.compiler.visitor;

import java.util.ArrayList;
import java.util.List;

public class ProcListComponent {
	private String name;
	private int number;
	private int fparamCnt;
	private int fparamMemAddr;
	private List<String> codeList;

	public ProcListComponent(String name, int number, int fparamMemAddr) {
		this.name = name;
		this.number = number;
		this.fparamCnt = 0;
		this.fparamMemAddr = fparamMemAddr;
		codeList = new ArrayList<String>();
	}

	public List<String> getCodeList() {
		return codeList;
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}

	public void addCode(String code) {
		codeList.add(code);
	}

	public void incFpramCnt() {
		this.fparamCnt++;
	}

	public void setFpramCnt(int num) {
		this.fparamCnt = num;
	}

	public int getFparamCnt() {
		return this.fparamCnt;
	}

	public int getFparamMemAddr() {
		return this.fparamMemAddr;
	}
}
