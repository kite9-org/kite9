package org.kite9.diagram.dom.processors.xpath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathResult;

/**
 * Provides a context from which xpath expressions can be resolved.
 * 
 * @author robmoffat
 *
 */
public abstract class ValueReplacer {
	
	public abstract String getReplacementStringValue(String in, Node at);	
	
	public abstract XPathResult getReplacementXML(String in, short type, Node at);
	
	public abstract Node getLocation();
	
	public String performValueReplace(String input, Node at) {
		Pattern p = Pattern.compile("\\#\\{(.*?)\\}");
		
		Matcher m = p.matcher(input);
		StringBuilder out = new StringBuilder();
		int place = 0;
		while (m.find()) {
			out.append(input.substring(place, m.start()));
			
			String in = m.group(1).toLowerCase();
			String replacement = getReplacementStringValue(in, at);
			
			if ((replacement == null) || (replacement.trim().length() == 0)) {
				//
				out.append(""); // decide how to do this better.
				//throw new Kite9XMLProcessingException("Couldn't determine value of '"+input+"' from "+at, at);
			}
			
			out.append(replacement.trim());
			
			place = m.end();
		}
		
		out.append(input.substring(place));
		return out.toString();
	}
	
}