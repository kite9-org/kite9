package org.kite9.diagram.dom.css;

import java.util.List;

import org.w3c.css.sac.SACMediaList;

public interface ScriptHandler {

	public void importScript(String uri, SACMediaList ml);
	
	public void setParam(String name, String value);
	
	public void addParams(String name, List<String> additionalValues);

}