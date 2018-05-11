package org.kite9.framework.dom.elements;

import org.apache.batik.dom.AbstractElement;
import org.kite9.framework.dom.XMLHelper;
import org.w3c.dom.Node;

/**
 * Marker element used by the templater.
 * 
 * @author robmoffat
 *
 */
public class ContentsElement extends AbstractElement {

	private boolean readOnly = false;
	
	public ContentsElement(ADLDocument doc) {
		this.setOwnerDocument(doc);
	}

	@Override
	public String getNodeName() {
		return XMLHelper.CONTENTS_ELEMENT;
	}

	@Override
	public boolean isReadonly() {
		return readOnly;
	}

	@Override
	public void setReadonly(boolean v) {
		this.readOnly = v;
	}

	@Override
	protected Node newNode() {
		return new ContentsElement(null);
	}

}
