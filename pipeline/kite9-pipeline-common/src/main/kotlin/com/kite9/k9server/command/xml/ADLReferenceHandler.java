package com.kite9.k9server.command.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.ReferencingKite9XMLElement;
import org.kite9.diagram.model.style.DiagramElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;

/**
 * This is now a one-shot class: use and discard.  Will fix any references that occur twice.
 * @author robmoffat
 *
 */
public class ADLReferenceHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(ADLReferenceHandler.class);
	
	private Set<String> existingIDs = new HashSet<String>();
	private Map<String, List<Element>> duplicates = new HashMap<>();
	private ADLDocument doc;
	private String url;
	
	public ADLReferenceHandler(ADLDom adl) {
		this.doc = adl.getDocument();
		adl.ensureCssEngine(doc);
		this.url = adl.getUri().toString();
	}
	
	public void ensureConsistency() {
		auditIDs(doc);
		renameDuplicates();
		checkReferences(doc);
		ensureLinksAreLast(doc);
	}
	
	protected void ensureLinksAreLast(Node container) {
		if (container instanceof Element) {
			NodeList nl = container.getChildNodes();
			List<Node> copy = copyToList(nl);
			Node firstLink = null;
			for (Node node : copy) {
				if (node instanceof ReferencingKite9XMLElement) {
					DiagramElementType det = ((ReferencingKite9XMLElement) node).getType();
					
					if (det == DiagramElementType.LINK) {
						if (firstLink == null)  {
							firstLink = node;
						}
					} else if (firstLink != null) {
						// links should be the last content in any container
						container.insertBefore(node, firstLink);
					}
				}
				
				ensureLinksAreLast(node);
			}
		} else if (container instanceof Document) {
			ensureLinksAreLast(((Document) container).getDocumentElement());
		}
	}

	protected void renameDuplicates() {
		for (String oldId : duplicates.keySet()) {
			for (Element e : duplicates.get(oldId)) {
				String newId = doc.createUniqueId();
				LOG.info("Renaming duplicate of {} to {} in {}", oldId, newId, url);	
				e.setAttribute("id", newId);
			}
		}
	}

	protected void auditIDs(Node n) {
		if (n instanceof Element) {
			if (((Element) n).hasAttribute("id")) {
				String id  = ((Element) n).getAttribute("id");
				if (existingIDs.contains(id)) {
					List<Element> dups = duplicates.getOrDefault(id, new ArrayList<Element>());
					dups.add((Element) n);
					duplicates.put(id, dups);
					LOG.info("Discovered duplicate for {} in {}", id, url);
				} else {
					existingIDs.add(id);
				}
			}
			
			NodeList nl = n.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				auditIDs(nl.item(i));
			}
		} else if (n instanceof Document) {
			auditIDs(((Document) n).getDocumentElement());
		}
	}

	/**
	 * Removes any link references that are broken, and orphaned terminators
	 */
	protected void checkReferences(Node n) {
	
		if (isType(n, DiagramElementType.LINK)) {
			
			String fromId = ((ReferencingKite9XMLElement) n).getIDReference(CSSConstants.LINK_FROM_XPATH);
			String toId = ((ReferencingKite9XMLElement) n).getIDReference(CSSConstants.LINK_TO_XPATH);
			
			if (!findReferenceOrParentReference(n, fromId)) {
				n.getParentNode().removeChild(n);
			} else if (!findReferenceOrParentReference(n, toId)) {
				n.getParentNode().removeChild(n);
			}
		}
		
		if (isType(n, DiagramElementType.LINK_END)) {
			if (!isType(n.getParentNode(), DiagramElementType.LINK)) {
				n.getParentNode().removeChild(n);
			}
		}
		
		
		if (n instanceof Element) {
			NodeList nl = n.getChildNodes();
			List<Node> copy = copyToList(nl);
			for (Node c : copy) {
				checkReferences(c);
			}
		}
		
		if (n instanceof Document) {
			checkReferences(((Document) n).getDocumentElement());
		}
	}

	protected boolean findReferenceOrParentReference(Node n, String id) {
		boolean found = existingIDs.contains(id);
		int childOffset = id.indexOf("@");
		if ((found == false) && (childOffset > 0)) {
			id = id.substring(0, childOffset);
			found = existingIDs.contains(id);
		}
		
		return found;
	}

	protected boolean isType(Node n, DiagramElementType t) {
		if (n instanceof ReferencingKite9XMLElement) {
			return ((ReferencingKite9XMLElement) n).getType() == t;
		} else {
			return false;
		}
	}

	private List<Node> copyToList(NodeList nl) {
		List<Node> out = new ArrayList<>(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) {
			out.add(nl.item(i));
		}
		return out;
	} 


}
