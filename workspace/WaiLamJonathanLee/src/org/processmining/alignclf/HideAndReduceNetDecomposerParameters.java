package org.processmining.alignclf;

public class HideAndReduceNetDecomposerParameters {
	public String netFilePath;
	public String outFilePath;
	public String decompositionFilePath;
	
	public HideAndReduceNetDecomposerParameters(String netFilePath, String decompositionFilePath, String outFilePath) {
		this.netFilePath = netFilePath;
		this.outFilePath = outFilePath;
		this.decompositionFilePath = decompositionFilePath;
	}
}
