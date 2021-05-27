package com.kite9.k9server.command.xml.replace;

import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;

public class ReplaceStyle extends AbstractReplaceCommand<String, String> {

	public String name;
	
	@Override
	protected String getFromContent(ADLDom context) {
		return from;
	}

	@Override
	protected String getToContent(ADLDom context) {
		return to;
	}

	@Override
	protected String getExistingContent(ADLDom in) {
		Element e = findFragmentElement(in.getDocument(), fragmentId);
		if (e instanceof StyledKite9XMLElement) {
			CSSStyleDeclaration sd = ((StyledKite9XMLElement) e).getStyle();
			return sd.getPropertyValue(name);
		} else {
			return "";
		}
	}

	@Override
	protected void doReplace(ADLDom in, String site, String toContent, String fromContent) {
		Element e = findFragmentElement(in.getDocument(), fragmentId);
		if (e instanceof StyledKite9XMLElement) {
			CSSStyleDeclaration sd = ((StyledKite9XMLElement) e).getStyle();
			if (toContent == null) {
				sd.removeProperty(name);
			} else {
				sd.setProperty(name, toContent, "");
			}
			
			if (sd.getLength() == 0) {
				((StyledKite9XMLElement) e).removeAttribute("style");
			}
		}
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
