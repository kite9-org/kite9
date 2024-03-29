package com.kite9.server.domain;

public abstract class Document extends Content {

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

	protected String getExtension(String name) {
		return name.substring(name.lastIndexOf(".")+1);
	}
	
	@Override
	public String getIcon() {
		if (hasIcon(getTitle())) {
			return "/public/templates/admin/icons/"+getExtension(getTitle())+".svg";
		} else {
			return "/public/templates/admin/icons/unknown.svg";
		}
	}
}
