package com.kite9.server.update;

import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.command.CommandContext;
import org.kite9.diagram.model.style.DiagramElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * This is now a one-shot class: use and discard.  Will fix any references that occur twice.
 * @author robmoffat
 *
 */
public class ADLReferenceHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(ADLReferenceHandler.class);
	
	private final Set<String> existingIDs = new HashSet<String>();
	private final Map<String, List<Element>> duplicates = new HashMap<>();
	private final Document doc;
	private final String url;
	private final CommandContext ctx;
	
	public ADLReferenceHandler(ADLDom adl, CommandContext ctx) {
		this.doc = adl.getDocument();
		this.url = adl.getUri().toString();
		this.ctx = ctx;
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
				if (isType(node, DiagramElementType.LINK)) {
					if (firstLink == null)  {
						firstLink = node;
					}
				} else if (firstLink != null) {
					// links should be the last content in any container
					container.insertBefore(node, firstLink);
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
				String newId = ctx.uniqueId(doc);
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
					List<Element> dups = duplicates.getOrDefault(id, new ArrayList<>());
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
	
		if (isType(n, DiagramElementType.LINK_END)) {
			Node link = n.getParentNode();
			String ref = ((Element)n).getAttribute("reference");

			if (!findReferenceOrParentReference(ref)) {
				link.getParentNode().removeChild(link);
			}

			if (!isType(link, DiagramElementType.LINK)) {
				link.removeChild(n);
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

	protected boolean findReferenceOrParentReference(String id) {
		boolean found = existingIDs.contains(id);
		int childOffset = id.indexOf("@");
		if ((!found) && (childOffset > 0)) {
			id = id.substring(0, childOffset);
			found = existingIDs.contains(id);
		}
		
		return found;
	}

	/**
	 * Shocking hack for now - we need to use Schemas.
	 */
	protected boolean isType(Node n, DiagramElementType t) {
		if (n instanceof Element) {
			Element e = (Element) n;
			switch (t) {
				case LINK:
					return e.getTagName().equals("link");
				case LINK_END:
					return e.getTagName().equals("from") || e.getTagName().equals("to");
				default:
					throw new UnsupportedOperationException("Can't figure out type from xml");
			}
		}

		return false;
	}

	private List<Node> copyToList(NodeList nl) {
		List<Node> out = new ArrayList<>(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) {
			out.add(nl.item(i));
		}
		return out;
	} 


}
