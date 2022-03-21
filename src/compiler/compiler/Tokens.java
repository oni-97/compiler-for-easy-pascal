package compiler.compiler;

import java.util.List;

public class Tokens {
	private List<String> tokenList;
	private int ip;
	private String token;

	Tokens(List<String> tokenList) {
		this.tokenList = tokenList;
		this.ip = -1;
		this.tokenList.add("\0");
		this.token = null;
	}

	public String getSourceName() {
		String[] tokenInfo = token.split("\t");
		return tokenInfo[0];
	}

	public String getLineNumber() {
		String[] tokenInfo = token.split("\t");
		return tokenInfo[3];
	}

	public String getToken() {
		return token;
	}

	public String getTokenSrc() {
		String[] tokenInfo = token.split("\t");
		return tokenInfo[0];
	}

	public String lookAheadOne() {
		ip++;
		String aheadToken = tokenList.get(ip);
		String[] tokenInfo = aheadToken.split("\t");
		ip--;
		return tokenInfo[1];
	}

	public String lookAheadTwo() {
		ip++;
		ip++;
		String aheadToken = tokenList.get(ip);
		String[] tokenInfo = aheadToken.split("\t");
		ip--;
		ip--;
		return tokenInfo[1];
	}

	public String getNext() {
		ip++;
		token = tokenList.get(ip);
		String[] tokenInfo = token.split("\t");
		return tokenInfo[1];
	}
}
