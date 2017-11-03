package org.kite9.diagram.batik.templater;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.Kite9XMLElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Performs value replacement, where the elements to be replaced contain 
 * placeholders in the form {blah}. e.g. {x0} {@name} etc.
 * 
 * @author robmoffat
 *
 */
public class ValueReplacingProcessor implements XMLProcessor {
	
	public interface ValueReplacer {
		
		public String getReplacementValue(String prefix, String attr);	
		
	}

	private ValueReplacer valueReplacer;
	
	public ValueReplacingProcessor(ValueReplacer vr) {
		this.valueReplacer = vr;
	}



	@Override
	public void processContents(Node from) {
		if (from instanceof Element) {
			performReplaceOnAttributes(from, valueReplacer);
			performReplace(((Element)from).getChildNodes(), valueReplacer);
		} else {
			throw new Kite9ProcessingException("Was expecting an element");
		}
	}

	/**
	 * Replaces parameters in the SVG contents of the diagram element, prior to being 
	 * turned into `GraphicsNode`s .  
	 */
	public void performReplace(Node n, ValueReplacer vr) {
		if (vr == null)
			return;

		if (n instanceof Element) {
			performReplaceOnAttributes(n, vr);

			if (n instanceof Kite9XMLElement) {
				// we don't do sub-elements - they're someone else's problem
				return;	
			} else {
				performReplace(n.getChildNodes(), vr);
			}
		}

		
	}



	private void performReplaceOnAttributes(Node n, ValueReplacer vr) {
		for (int j = 0; j < n.getAttributes().getLength(); j++) {
			Attr a = (Attr) n.getAttributes().item(j);
			a.setValue(performValueReplace(a.getValue(), vr));
		}
	}

	public void performReplace(NodeList nodeList, ValueReplacer vr) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			performReplace(n, vr);
		}
	}

	protected String performValueReplace(String input, ValueReplacer vr) {
		Pattern p = Pattern.compile("\\{([xXyY@])([a-zA-Z0-9]+)}");
		
		Matcher m = p.matcher(input);
		StringBuilder out = new StringBuilder();
		int place = 0;
		while (m.find()) {
			out.append(input.substring(place, m.start()));
			
			String prefix = m.group(1).toLowerCase();
			String indexStr = m.group(2);
			String replacement = vr.getReplacementValue(prefix, indexStr);
			
			if (replacement != null) {
				out.append(replacement);
			}
			
			place = m.end();
		}
		
		out.append(input.substring(place));
		return out.toString();
	}
	
}
