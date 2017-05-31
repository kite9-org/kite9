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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Handles copying of XML from one document to another, and the CSS template directive.
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

					copyIntoDocument(in, e, resource);
				} catch (Exception e) {
					throw new Kite9ProcessingException("Couldn't resolve template: " + uri, e);
				}
			}
		}
	}

	public void copyIntoDocument(Kite9XMLElement in, Element template, String resource) {
		// copy all children of e into the new document
		ValueReplacer vr = new ParentElementValueReplacer(in);
		
		NodeList children = template.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);

			if (n instanceof Element) {
				Node copy = ((Element) n).cloneNode(true);
				ADLDocument thisDoc = in.getOwnerDocument();
				thisDoc.adoptNode(copy);

				if (in.getChildXMLElementCount() == 0) {
					in.appendChild(copy);
				} else {
					Kite9XMLElement first = in.iterator().next();
					in.insertBefore(copy, first);
				}

				// ensure xml:base is set so references work in the copied
				// content
				((Element) copy).setAttributeNS(XMLConstants.XML_NAMESPACE_URI, XMLConstants.XML_BASE_ATTRIBUTE, resource);
				
				performReplace(copy, vr);
			}
		}
	}
	
	public static void insertCopyBefore(Node before, Element contentsOf) {
		Element into = (Element) before.getParentNode();
		ADLDocument thisDoc = (ADLDocument) into.getOwnerDocument();
		NodeList toCopy = contentsOf.getChildNodes();
		for (int i = toCopy.getLength()-1; i >=0; i--) {
			Node e = toCopy.item(i);
			Node copy = e.cloneNode(true);
			thisDoc.adoptNode(copy);
			into.insertBefore(copy, before);
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
			data = data.replaceAll("\\{\\{contents\\}\\}", vr.getText());
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
	
	

}
