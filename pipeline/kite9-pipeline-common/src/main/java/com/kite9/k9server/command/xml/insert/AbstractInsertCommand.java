package com.kite9.k9server.command.xml.insert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.command.xml.AbstractADLCommand;

public abstract class AbstractInsertCommand extends AbstractADLCommand {

	public String fragmentId;
	public String beforeId;
	public List<String> containedIds = Collections.emptyList();
	public String newId;	

	protected Mismatch doDelete(ADLDom in) {
		Element oldState = getContents(in);
		Element toDelete = findFragmentElement(in.getDocument(), oldState.getAttribute("id"));
		Element parent = (Element) toDelete.getParentNode();
		
		Mismatch m = same(toDelete, oldState);
		
		if (m != null){
			return m;
		}
		
		String parentId = parent.getAttribute("id");
		if (!parentId.equals(fragmentId)) {
			return () -> "Parent id "+parentId+" doesn't match "+fragmentId;
		}
		
		NodeList children = toDelete.getChildNodes();
		int i = 0;
		while (i < children.getLength()) {
			Node c = children.item(i);
			if ((c instanceof Element) && (containedIds.contains(((Element) c).getAttribute("id")))) {
				parent.insertBefore(c, toDelete);
			} else {
				i++;
			}
		}
		
		toDelete.getParentNode().removeChild(toDelete);
		LOG.info("Processed delete to "+fragmentId);
		return null;
	}

	protected Mismatch same(Element expected, Element actual) {
		expected = copyWithoutContainedIds(expected);
		actual = copyWithoutContainedIds(actual);
		return twoElementsAreIdentical(expected, actual);
	}

	protected Element copyWithoutContainedIds(Element expected) {
		Element out = (Element) expected.cloneNode(true);
		int i = 0;
		while (i < out.getChildNodes().getLength()) {
			Node n = out.getChildNodes().item(i);
			if ((n instanceof Element) && (containedIds.contains(((Element)n).getAttribute("id")))) {
				out.removeChild(n);
			} else {
				i++;
			}
		}
		return out;
	}

	protected Mismatch doInsert(ADLDom in) {
		Element destination = getDestination(in);
		Element before = getBefore(in);
		Element contents = getContents(in);
		
		String containsId = contents.getAttribute("id");
		
		if (alreadyExists(in, containsId)) {
			return () -> "Already contains "+containsId;
		}
		
		if (destination==null) {
			return () -> "Destination no longer exists";
		}
		
		in.getDocument().adoptNode(contents);
		if (before != null) {
			destination.insertBefore(contents, before);
		} else {
			destination.appendChild(contents);
		}
		ensureParentElements(destination, contents);
		
		// now make sure content is set
		NodeList children = destination.getChildNodes();
		List<Element> toMove = new ArrayList<Element>();
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if ((c instanceof Element) && (containedIds.contains(((Element) c).getAttribute("id")))) {
				toMove.add((Element) c);
			}
		}
		
		for (Element element : toMove) {
			contents.appendChild(element);
		}
		
		LOG.info("Processed insert into "+fragmentId);		
		return null;
	}

	private boolean alreadyExists(ADLDom in, String newId) {
		return in.getDocument().getElementById(newId) != null;
	}

	protected Element getBefore(ADLDom in) {
		if (beforeId != null) {
			return findFragmentElement(in.getDocument(), beforeId);			
		} else {
			return null;
		}
	}

	protected Element getDestination(ADLDom in) {
		return findFragmentElement(in.getDocument(), fragmentId);
	}

	protected abstract Element getContents(ADLDom in);
	
	protected void checkProperties() {
		ensureNotNull("fragmentId", fragmentId);
		ensureNotNull("newId", fragmentId);
	}


	
	
	
}
