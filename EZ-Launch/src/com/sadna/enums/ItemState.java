package com.sadna.enums;

public enum ItemState {
	NOT_ALLOWED("Disabled"),
	AUTO("Auto"),
	MUST("Always On");

	private String statusCode;
	private ItemState(String p){
		statusCode = p;
	}
	public static ItemState parse(String p){
		if (p.equalsIgnoreCase(NOT_ALLOWED.getStatusCode())) {
			return ItemState.NOT_ALLOWED;
		}
		if (p.equalsIgnoreCase(AUTO.getStatusCode())) {
			return ItemState.AUTO;
		}
		if (p.equalsIgnoreCase(MUST.getStatusCode())) {
			return ItemState.MUST;
		}
		return ItemState.AUTO;
	}
	public String getStatusCode(){
		return statusCode;
	}
	@Override
	public String toString(){
		return statusCode;
	}
}
