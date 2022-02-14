package org.kite9.diagram.dom;

import org.apache.batik.dom.util.SAXDocumentFactory;
import org.kite9.diagram.batik.format.ConsolidatedErrorHandler;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Utility methods for handling XML, DOM and XSLT.
 * 
 * @author robmoffat
 * 
 */
public class XMLHelper {

	private final String transformerFactoryClassName;
	private transient TransformerFactory transFact;
	private ConsolidatedErrorHandler eh;
	
	public XMLHelper() {
		this("", null);
	}

	public XMLHelper(String transFact, ConsolidatedErrorHandler eh) {
		this.transformerFactoryClassName = transFact;
		this.eh = eh;
	}

	public String toXML(Node dxe, boolean omitDeclaration) {
		StringWriter sw= new StringWriter();
		toXML(dxe, sw, omitDeclaration);
		return sw.toString();
	}
	
	public void toXML(Node dxe, Writer w, boolean omitDeclaration) {
		try  {
			Transformer trans = newTransformer(omitDeclaration);
			Result result = new StreamResult(w);
			trans.transform(new DOMSource(dxe), result);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't output xml: ",e);
		}
	}

	public Document fromXML(String s)  {
		return fromXML(new StringReader(s));
	}
	

	public Document fromXML(Reader s) {
		try {
			SAXDocumentFactory sdf = new SAXDocumentFactory(new ADLExtensibleDOMImplementation(), null);
			if (eh != null) {
				sdf.setErrorHandler(eh);
			}
			Document d = sdf.createDocument(null, s);
			return  d;
		} catch (IOException e) {
			throw new Kite9XMLProcessingException("Couldn't parse xml: ", e);
		}

	}

	public void duplicate(Node n, boolean omitDeclaration, Result sr) {
		try {
			Transformer transformer = newTransformer(omitDeclaration);
			transformer.transform(new DOMSource(n), sr);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't serialize XML:", e, null, null);
		}
	}

	public Transformer newTransformer(boolean omitDeclaration) throws Exception {
		return newTransformer(null, omitDeclaration);
	}

	public Transformer newTransformer(Source source, boolean omitDeclaration) throws Exception {
		TransformerFactory transfac = getTransformerFactory();
		Transformer trans = (source != null) ? transfac.newTransformer(source) : transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitDeclaration  ? "yes" : "no");
		return trans;
	}

	public TransformerFactory getTransformerFactory() throws Exception {
		if (transFact == null) {
			if ((transformerFactoryClassName != null) && (transformerFactoryClassName.trim().length() > 0)) {
				Class<?> tfClass = Class.forName(transformerFactoryClassName);
				transFact = (TransformerFactory) tfClass.getConstructor().newInstance();
			} else {
				transFact = TransformerFactory.newInstance();
			}

			if (eh != null) {
				transFact.setErrorListener(this.eh);
			}
		}

		return transFact;
	}

	public ConsolidatedErrorHandler getErrorHandler() {
		return eh;
	}

}
