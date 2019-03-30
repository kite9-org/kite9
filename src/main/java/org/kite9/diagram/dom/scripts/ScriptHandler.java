package org.kite9.diagram.dom.scripts;

import org.w3c.css.sac.SACMediaList;

public interface ScriptHandler {

	public void importScript(String uri, SACMediaList ml);
	
	public void setParam(String name, String value);

}
