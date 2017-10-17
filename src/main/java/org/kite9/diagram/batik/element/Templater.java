package org.kite9.diagram.batik.element;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.util.XMLConstants;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.Kite9XMLElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Handles copying of XML from one document to another, and the CSS 'template' directive.
 * 
 * @author robmoffat
 *
 */
public class Templater {
	
	public interface ValueReplacer {
		
		public String getReplacementValue(String prefix, String attr);	
		
		public String getText();
		
	}
	
	public static class ParentElementValueReplacer implements ValueReplacer {
		
		Element e;
		String text;
		
		public ParentElementValueReplacer(Element parent) {
			super();
			this.e = parent;
			this.text = e.getTextContent();
		}

		/**
		 * Handles replacement of {@someattribute} within the SVG.
		 */
		public String getReplacementValue(String prefix, String attr) {
			Element parent = this.e;
			if ("@".equals(prefix)) {
				while (parent != null) {
					if (parent.hasAttribute(attr)) {
						return parent.getAttribute(attr);
					} 
					parent=(Element) parent.getParentNode();
				}
			} 
			
			return "{"+prefix+attr+"}";	// couldn't be replaced - leave original
		}

		@Override
		public String getText() {
			return text;
		}
	}
	
	
	
	private DocumentLoader loader;

	public Templater(DocumentLoader loader) {
		this.loader = loader;
	}

	/**
	 * This needs to copy the template XML source into the destination.
	 */
	public void handleTemplateElement(Kite9XMLElement in, DiagramElement o) {
		if (o instanceof AbstractXMLDiagramElement) {
			AbstractXMLDiagramElement out = (AbstractXMLDiagramElement) o;
			Value template = out.getCSSStyleProperty(CSSConstants.TEMPLATE);
			if (template != ValueConstants.NONE_VALUE) {
				String uri = template.getStringValue();

				try {
					// identify the fragment referenced in the other document
					// and
					// load it
					URI u = new URI(uri);
					String fragment = u.getFragment();
					String resource = u.getScheme() + ":" + u.getSchemeSpecificPart();
					ADLDocument templateDoc = loadReferencedDocument(resource);
					Element e = templateDoc.getElementById(fragment);

					ValueReplacer vr = new ParentElementValueReplacer(in);
					transcribeContent(in, e, resource, vr, true);
				} catch (Exception e) {
					throw new Kite9ProcessingException("Couldn't resolve template: " + uri, e);
				}
			}
		}
	}

	/**
	 * Copies from source into destination, for duplicating XML.
	 * @param dest
	 * @param source
	 * @param resourceBase  Add this if you want to include an xml:base on each copied element (to preserve references to defs)
	 * @param vr Add this if you want to do value replacement in the source XML.
	 * @param removeExistingText Add this to clear out text nodes in dest before copying
	 */
	public void transcribeContent(Element dest, Element source, String resourceBase, ValueReplacer vr, boolean removeExistingText) {
		if (removeExistingText) {
			removeTextNodes(source);
		}

		NodeList children = source.getChildNodes();
		// copying in reverse order (just simpler, and maintains existing content)
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node n = children.item(i);

			transcribeNode(dest, n, vr, resourceBase, false);
		}
	}
	
	public Node transcribeNode(Document dest, Node source, boolean removePrefix) {
		Node copy = source.cloneNode(true);
		
		if (removePrefix) {
			removePrefixes(copy);
		}
		
		dest.adoptNode(copy);
		return copy;
	}

	public void transcribeNode(Element dest, Node source, ValueReplacer vr, String resourceBase, boolean removePrefix) {
		Node copy = transcribeNode(dest.getOwnerDocument(), source, removePrefix);
		
		if (dest.getChildNodes().getLength() == 0) {
			dest.appendChild(copy);
		} else {
			Node first = dest.getChildNodes().item(0);
			dest.insertBefore(copy, first);
		}

		if ((source instanceof Element) && (resourceBase != null)) {
			// ensure xml:base is set so references work (e.g. to <defs> for
			// gradients or whatever)
			// in the copied content
			((Element) copy).setAttributeNS(XMLConstants.XML_NAMESPACE_URI, XMLConstants.XML_BASE_ATTRIBUTE, resourceBase);
		}

		performReplace(copy, vr);
	}

	private void removePrefixes(Node copy) {
		if (copy instanceof Element) {
			((Element)copy).setPrefix("");
		} 
		
		for (int i = 0; i < copy.getChildNodes().getLength(); i++) {
			removePrefixes(copy.getChildNodes().item(i));
		}
		
	}

	private void removeTextNodes(Element in) {
		// remove any text nodes, as this will be expanded out where needed by the ValueReplacer
		NodeList existing = in.getChildNodes();
		for (int i = 0; i < existing.getLength(); i++) {
			Node n = existing.item(i);
			if (n instanceof Text) {
				in.removeChild(n);
			}
		}
	}
	
	private ADLDocument loadReferencedDocument(String resource) throws IOException {
		return (ADLDocument) loader.loadDocument(resource);
	}
	

	/**
	 * Replaces parameters in the SVG contents of the diagram element, prior to being 
	 * turned into `GraphicsNode`s .  
	 */
	public void performReplace(Node n, ValueReplacer vr) {
		if (vr == null)
			return;
		
		if (n instanceof Element) {
			performReplace(n.getChildNodes(), vr);
			for (int j = 0; j < n.getAttributes().getLength(); j++) {
				Attr a = (Attr) n.getAttributes().item(j);
				a.setValue(performValueReplace(a.getValue(), vr));
			}
		} else if (n instanceof Text) {
			Text text = (Text) n;
			String data = text.getData();
			data = performValueReplace(data, vr);
			data = Pattern.compile("\\{\\{contents\\}\\}").matcher(data).quoteReplacement(vr.getText());
			//data = data.replaceAll("\\{\\{contents\\}\\}", vr.getText());
			if (!data.equals(text.getData())) {
				text.replaceData(0, text.getLength(), data);
			}
			
			
		}
	}

	public void performReplace(NodeList nodeList, ValueReplacer vr) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			performReplace(n, vr);
		}
	}

	protected String performValueReplace(String input, ValueReplacer vr) {
		Pattern p = Pattern.compile("\\{([xXyY@])([a-zA-Z0-9]+)}");
		
		Matcher m = p.matcher(input);
		StringBuilder out = new StringBuilder();
		int place = 0;
		while (m.find()) {
			out.append(input.substring(place, m.start()));
			
			String prefix = m.group(1).toLowerCase();
			String indexStr = m.group(2);
			String replacement = vr.getReplacementValue(prefix, indexStr);
			
			if (replacement != null) {
				out.append(replacement);
			}
			
			place = m.end();
		}
		
		out.append(input.substring(place));
		return out.toString();
	}


	/**
	 * Used for copying to the output XML file.
	 */
	public void transcode(Node from, Node to) {
		 NodeList nl = from.getChildNodes();
		 for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			Node copy;
			if (n instanceof Kite9XMLElement) {
				copy = ((Kite9XMLElement) n).output(to.getOwnerDocument(), this);
			} else {
				copy = n.cloneNode(false);
				removePrefixes(copy);
				to.getOwnerDocument().adoptNode(copy);
				transcode(n, copy);
			}
			
			to.appendChild(copy);
		}
	}


}
