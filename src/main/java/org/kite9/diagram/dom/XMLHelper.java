package org.kite9.diagram.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.util.SAXDocumentFactory;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.framework.common.Kite9XMLProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Utility methods for converting to and from XML in the expected format.
 * 
 * This copy exists here because we need it for testing.
 * 
 * This provides the following functionality:
 * <ul>
 * <li>Object-reference fixing so that we can omit parent/container references
 * in the xml</li>
 * <li>Use of kite9 namespace for xml generated</li>
 * <li>use of Kite9 id field in the xml, instead of Xstream generated ones.</li>
 * <li>Use of xsi:type to choose the subclass in the xml format (in accordance
 * with schema)</li>
 * 
 * @author robmoffat
 * 
 */
public class XMLHelper {

	public static final String XML_SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String KITE9_NAMESPACE = "http://www.kite9.org/schema/adl";
	public static final String DIAGRAM_ELEMENT = "diagram";
	public static final String CONTENTS_ELEMENT = "contents";

	
	public XMLHelper() {
	}

	public String toXML(Node dxe) {
		StringWriter sw= new StringWriter();
		toXML(dxe, sw);
		return sw.toString();
	}
	
	public void toXML(Node dxe, Writer w) {
		try  {
			 TransformerFactory transfac = TransformerFactory.newInstance();
			 Transformer trans = transfac.newTransformer();
			 trans.setOutputProperty(OutputKeys.INDENT, "yes");
			 trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			 trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			 trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			 Result result = new StreamResult(w);
			 trans.transform(new DOMSource(dxe), result);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't output xml: ",e);
		}
	}

	public ADLDocument fromXML(String s)  {
		return fromXML(new StringReader(s));
	}
	

	public ADLDocument fromXML(Reader s) {
		try {
			SAXDocumentFactory sdf = new SAXDocumentFactory(new ADLExtensibleDOMImplementation(), null);
			Document d = sdf.createDocument(null, s);
			return (ADLDocument) d;		
		} catch (IOException e) {
			throw new Kite9XMLProcessingException("Couldn't parse xml: ", e);
		}

	}

	public ADLDocument fromXML(InputStream s) {
		return fromXML(new InputStreamReader(s));
	}

}
