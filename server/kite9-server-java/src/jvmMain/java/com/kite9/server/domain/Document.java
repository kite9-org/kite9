package com.kite9.server.domain;

public abstract class Document extends RestEntity {

	@Override
	public String getCommands() {
		return "open";
	}

	@Override
	public String getType() {
		return "document";
	}

	protected boolean hasIcon(String name) {
		int li = name.lastIndexOf(".");
		if (li ==-1) {
			return false;
		}
		
		String suffix = name.substring(li+1);
		switch (suffix) {
		case "adl":
		case "js":
		case "css":
		case "svg":
		case "png":
		case "xml":
		case "html":
			return true;
		default:
			return false;
		}
	}
}
