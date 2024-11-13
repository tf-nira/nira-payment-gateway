package io.mosip.gateway.payment.constants;


public enum NiraProcess {
	REPLACE("LOST"),
	UPDATE("UPDATE");
	
	private final String process;
	
	private NiraProcess(String process) {
		this.process = process;
	}
	
	public String getProcess() {
		return process;
	}
}
