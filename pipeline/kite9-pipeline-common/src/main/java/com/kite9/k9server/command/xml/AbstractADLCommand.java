package com.kite9.k9server.command.xml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.dom.AbstractAttr;
import org.apache.batik.dom.AbstractElement;
import org.apache.commons.io.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonType;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.command.Command;
import com.kite9.k9server.command.CommandException;

/**
 * Contains a hash, which is used to make sure that the command is operating on a fresh version
 * of the data.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractADLCommand implements Command {
	
	public AbstractADLCommand() {
		super();
	}

	public static final Log LOG = LogFactory.getLog(Command.class);
	
	public void ensureNotNull(String field, Object value) throws CommandException {
		if (value == null) {
			throw new CommandException(HttpStatus.CONFLICT, this.getClass().getName()+" requires "+field+" to be set", this);
		}
	}
	
	protected Element findFragmentElement(ADLDocument doc, String fragmentId) {
		if (StringUtils.isEmpty(fragmentId)) {
			return doc.getDocumentElement();
		}
		
		int partIndex = fragmentId.indexOf("@");
		if (partIndex > -1 ) {
			LOG.info("Stripping part from "+fragmentId);
			fragmentId = fragmentId.substring(0, partIndex);
		}

		Element into = doc.getElementById(fragmentId);
		
		return into;
	}

	public Element getForeignElementCopy(ADLDocument currentDoc, URI baseUri, String uriStr, boolean deep, ADLDom context) {
		try {
			String id = uriStr.substring(uriStr.indexOf("#")+1);
			String location = uriStr.substring(0, uriStr.indexOf("#"));
			
			if (location.length() > 0) {
				// referencing a different doc.
				URI uri = baseUri.resolve(location);
				currentDoc = context.parseDocument(uri);
			} 
			
			Element template = currentDoc.getElementById(id);
			Element out = (Element) template.cloneNode(deep);	
			return out;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't get foreign element: "+uriStr);
		}
	}

	/**
	 * For some reason, in the clone node process, batik clears the parent element setting, and this doesn't get fixed.
	 */
	protected void ensureParentElements(Node parent, Node child) {
		if ((child instanceof Attr) && (((Attr)child).getOwnerElement() == null)) {
			((AbstractAttr)child).setOwnerElement((AbstractElement) parent);
		}
		
		if (child instanceof Element) {
			// this makes sure the document has it's id-map updated so you can
			// use getElementById in further commands.
			((Element) child).setAttributeNode(((Element) child).getAttributeNode("id"));
			
			NodeList children = child.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				ensureParentElements(child, children.item(i));
			}
			
			NamedNodeMap map = child.getAttributes();
			for (int i = 0; i < map.getLength(); i++) {
				ensureParentElements(child, map.item(i));
			}
		}
	}
	
	protected void replaceIds(Element insert) {
		 replaceIds(insert, ((ADLDocument) insert.getOwnerDocument()).createUniqueId());
	}

	protected void replaceIds(Element insert, String base) {
		if (insert.hasAttribute("id")) {
			insert.setAttribute("id", base); 
		}
		
		NodeList children = insert.getChildNodes();
		replaceIds(children, base+"-");
	}
	
	protected void replaceIds(NodeList children, String base) {
		int nextId = 0;
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n instanceof Element) {
				replaceIds((Element) n, base+nextId);
				nextId++;
			}
		}
	} 

	protected Element getSingleContentElement(ADLDocument d) throws CommandException {
		Element svg = d.getDocumentElement();
		if (svg instanceof SVGOMSVGElement) {
			int childElementCount = ((SVGOMSVGElement) svg).getChildElementCount();
			if (childElementCount != 1) {
				throw new CommandException(HttpStatus.CONFLICT, "Was expecting a single element within the svg document, but there are  "+childElementCount+" elements");
			}
			
			return ((SVGOMSVGElement) svg).getFirstElementChild();
			
		} else {
			throw new CommandException(HttpStatus.CONFLICT, "Was expecting SVG document: "+svg.getClass());
		}
	}
	
	protected Element decodeElement(String base64xml, ADLDom adl) {
		String xml = new String(Base64.getDecoder().decode(base64xml), Charsets.UTF_8);
		ADLDocument nDoc = (ADLDocument) adl.parseDocument(xml, null);
		Element n = getSingleContentElement(nDoc);
		return n;
	}
	

	protected Mismatch twoElementsAreIdentical(Element existing, Element with) {
		List<Comparison> out = new ArrayList<>();
		DiffBuilder
			.compare(Input.fromNode(existing).build())
			.withTest(Input.fromNode(with).build())
			.ignoreWhitespace()
			.ignoreComments()
			.withDifferenceListeners((a, b) -> 
			{
				// fix for when batik reformats the style and adds newlines.
				if (a.getType() == ComparisonType.ATTR_VALUE) {
					String s1 = (String) a.getTestDetails().getValue();
					String s2 = (String) a.getControlDetails().getValue();
					if (s1.replaceAll("\\s", "").equals(s2.replaceAll("\\s", ""))) {
						return;
					}
				}
				
				out.add(a);
				
			
			})
			.build();
		
		
		return out.size() == 0 ? null : () -> out.stream()
			.map(m -> m.toString())
			.reduce("", (a, b) -> a+ "\n" + b);
	}
	
}
