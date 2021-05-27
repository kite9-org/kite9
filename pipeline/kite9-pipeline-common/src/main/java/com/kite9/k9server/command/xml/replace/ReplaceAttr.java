package com.kite9.k9server.command.xml.replace;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;

public class ReplaceAttr extends AbstractReplaceCommand<String, String> {

	public String name;
	
	@Override
	protected String getFromContent(ADLDom in) {
		return from;
	}

	@Override
	protected String getToContent(ADLDom in) {
		return to;
	}

	@Override
	protected String getExistingContent(ADLDom in) {
		Element on = findFragmentElement(in.getDocument(), fragmentId);
		return on.getAttribute(name);
	}

	@Override
	protected void doReplace(ADLDom existing, String site, String value, String old) {
		ADLDocument doc = existing.getDocument();
		Element e = findFragmentElement(doc, fragmentId);

		if (value == null) {
			e.removeAttribute(name);
		} else {
			e.setAttribute(name, value);
		}
	
		
		LOG.info("Processed replace attribute of "+fragmentId+" "+name);
	}

	@Override
	protected void checkProperties() {
		ensureNotNull("name", name);
		super.checkProperties();
	}

	@Override
	protected Mismatch same(String existing, String with) {
		if (StringUtils.isEmpty(existing) && StringUtils.isEmpty(with)) {
			return null;
		}
		
		return super.same(existing, with);
	}
	
	
	
	
}
