package org.kite9.diagram.dom.css;

import org.w3c.css.sac.CSSParseException;

public class ContextualCSSParseException extends CSSParseException {

	private final String context;
	
	public ContextualCSSParseException(String message, String uri, String content, int line, int column) {
		super(message, uri, line,column);
		this.context = content;
	}

	/**
	 * Returns the complete document being read by the CSS parser, to aid in debugging.
	 */
	public String getContext() {
		String[] lines = context.split("\\r?\\n");
		
		StringBuilder out = new StringBuilder();
		outputLine(lines, out, lines.length-3);
		outputLine(lines, out, lines.length-2);
		outputLine(lines, out, lines.length-1);
		return out.toString();
	}

	private void outputLine(String[] lines, StringBuilder out, int ln) {
		if ((lines.length>ln) && (ln >=0)) {
			out.append(paddedLineNumber(ln+1));
			out.append(":");
			out.append(lines[ln]);
			out.append("\n");
		} 
	}
	
	private String paddedLineNumber(int ln) {
		return String.format("%1$" + 5 + "s", ln);
	}
}
