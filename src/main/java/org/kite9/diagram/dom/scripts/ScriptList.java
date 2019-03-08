package org.kite9.diagram.dom.scripts;

import java.util.LinkedHashSet;

public class ScriptList extends LinkedHashSet<String> {
	
	public String formatImportList() {
		return stream().map(i -> "import '"+i+"'\n").reduce("", String::concat);
	}
}
