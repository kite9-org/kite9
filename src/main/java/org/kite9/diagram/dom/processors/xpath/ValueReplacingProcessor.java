package org.kite9.diagram.dom.processors.xpath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.xpath.XPathResult;

/**
 * Performs value replacement, where the elements to be replaced contain 
 * placeholders in the form #{blah}. e.g. #{$x0} #{@name} etc.
 * 
 * @author robmoffat
 *
 */
public class ValueReplacingProcessor extends AbstractProcessor {
	
	public interface ValueReplacer {
		
		public String getReplacementStringValue(String in, Node position);	
		
		public XPathResult getReplacementXML(String in, short type, Node position);
		
	}

	protected ValueReplacer valueReplacer;
	
	public ValueReplacingProcessor(ValueReplacer vr) {
		this.valueReplacer = vr;
	}

	@Override
	protected void processElement(Element from) {
		performReplaceOnAttributes(from);
		super.processElement(from);
	}

	private void performReplaceOnAttributes(Element n) {
		for (int j = 0; j < n.getAttributes().getLength(); j++) {
			Attr a = (Attr) n.getAttributes().item(j);
			a.setValue(performValueReplace(a.getValue(), n));
		}
	}
	
	protected void processText(Text n) {
		n.setData(performValueReplace(n.getData(), n));
		super.processText(n);
	}

	protected String performValueReplace(String input, Node at) {
		Pattern p = Pattern.compile("\\#\\{(.*?)\\}");
		
		Matcher m = p.matcher(input);
		StringBuilder out = new StringBuilder();
		int place = 0;
		while (m.find()) {
			out.append(input.substring(place, m.start()));
			
			String in = m.group(1).toLowerCase();
			String replacement = valueReplacer.getReplacementStringValue(in, at);
			
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
