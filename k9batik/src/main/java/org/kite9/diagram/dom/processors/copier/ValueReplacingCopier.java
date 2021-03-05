package org.kite9.diagram.dom.processors.copier;

import org.kite9.diagram.dom.processors.xpath.PatternValueReplacer;
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
	
	protected PatternValueReplacer vr;

	public ValueReplacingCopier(Node destination, boolean copyTop, PatternValueReplacer vr) {
		super(destination, copyTop);
		this.vr = vr;
	}

	@Override
	protected void processAttributes(Element n, Element context) {
		super.processAttributes(n, context);
		for (int j = 0; j < n.getAttributes().getLength(); j++) {
			Attr a = (Attr) n.getAttributes().item(j);
			if (canValueReplace(a)) {
				String oldValue = a.getValue();
				String newValue = vr.performValueReplace(oldValue, context);

				if (!oldValue.equals(newValue)) {
					updateAttribute(n, a, newValue);
				}
				
			}
		}
	}
	
	protected void updateAttribute(Element n, Attr a, String newValue) {
		a.setValue(newValue);
	}

	protected abstract boolean canValueReplace(Node n);

//	protected Text processText(Text n) {
//		Text out = super.processText(n);
//		if (canValueReplace(out)) {
//			String newData = vr.performValueReplace(out.getData(), n);
//			out.setData(newData);
//		}
//		return out;
//	}

	
	
}
