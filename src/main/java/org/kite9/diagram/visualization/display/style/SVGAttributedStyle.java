package org.kite9.diagram.visualization.display.style;

import java.util.Map;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.svggen.SVGDescriptor;
import org.kite9.diagram.visualization.display.style.io.SVGHelper;
import org.kite9.framework.logging.LogicException;
import org.w3c.dom.Element;

public class SVGAttributedStyle extends OverrideableAttributedStyle {
		
	CSSStylableElement styleElement;	
	SVGHelper helper;
	
	public SVGHelper getHelper() {
		return helper;
	}


	public void initSVG(SVGHelper h) {
		this.helper = h;

	}

	
	public void initSVG(SVGHelper h, SVGAttributedStyle existing) {
		if (existing == null) {
			initSVG(h);
			return;
		}
		this.helper = existing.helper;
	}

	public SVGAttributedStyle(SVGHelper h) {
		super();
		initSVG(h);
	}

	public SVGAttributedStyle(SVGHelper h, Map<String, String> elements) {
		super(elements);
		initSVG(h);
	}
	
	public SVGAttributedStyle(SVGHelper h, SVGAttributedStyle override) {
		this(h, override != null? override.getElements() : null);
		initSVG(h, override);
	}
	

	protected CSSStylableElement getStyleElement() {
		if (styleElement == null) {
			CSSStylableElement e = helper.createStyleableElement();
			StringBuilder style= new StringBuilder();
			for (Map.Entry<String, String> ent : attr.entrySet()) {
				style.append(ent.getKey());
				style.append(": ");
				style.append(ent.getValue());
				style.append(";");
			}
			
			e.setAttribute("style", style.toString());
			styleElement = e;
		}
		
		return styleElement;
	}
		
	
	protected void outputAttributes(SVGDescriptor d, String... names) {
		if (d==null) {
			return;
		}
		for (Object o : d.getAttributeMap(null).entrySet()) {
			@SuppressWarnings("unchecked")
			Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
			if ((names.length == 0) || (contains(names, entry.getKey()))) {
				attr.put(entry.getKey(), entry.getValue());
			}
		}
	}

	private boolean contains(String[] names, String key) {
		for (String string : names) {
			if (string.equals(key)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static <X extends SVGAttributedStyle> X override(String style, X in) {
		final X out;
		try {
			out = (X) in.clone();
			out.initSVG(in.helper);
		} catch (CloneNotSupportedException e) {
			throw new LogicException("Can't clone: "+in);
		}
		
		Element e = out.getStyleElement();
		
		// ok, going to create a sub-element now
		CSSStylableElement e2 = in.getHelper().createStyleableElement();
		e.appendChild(e2);
		e2.setAttribute("style", style);
		out.styleElement = e2;
		
		return out;
	}
}

