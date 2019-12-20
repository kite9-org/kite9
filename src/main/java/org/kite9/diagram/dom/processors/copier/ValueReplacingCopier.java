package org.kite9.diagram.dom.processors.copier;

import org.kite9.diagram.dom.processors.xpath.ValueReplacer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Performs value replacement, where the elements to be replaced contain 
 * placeholders in the form #{blah}. e.g. #{$x0} #{@name} etc.
 * 
 * @author robmoffat
 *
 */
public abstract class ValueReplacingCopier extends BasicCopier {
	
	protected ValueReplacer vr;

	public ValueReplacingCopier(Node destination, ValueReplacer vr) {
		super(destination);
		this.vr = vr;
	}

	@Override
	protected Element processTag(Element from) {
		Element out = super.processTag(from);
		performReplaceOnAttributes(out);
		return out;
	}

	private void performReplaceOnAttributes(Element n) {
		for (int j = 0; j < n.getAttributes().getLength(); j++) {
			Attr a = (Attr) n.getAttributes().item(j);
			if (canValueReplace(a)) {
				a.setValue(vr.performValueReplace(a.getValue(), n));
			}
		}
	}
	
	protected abstract boolean canValueReplace(Node n);

	protected Text processText(Text n) {
		Text out = super.processText(n);
		if (canValueReplace(out)) {
			out.setData(vr.performValueReplace(out.getData(), n));
		}
		return out;
	}

	
	
}
