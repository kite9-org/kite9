package com.kite9.k9server;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.jupiter.api.Assertions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonListener;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;

public class XMLCompare {

	public static void compareXML(String a, String b) {
		DiffBuilder
			.compare(stringToDom(a))
			.withTest(stringToDom(b))
			.ignoreWhitespace()
			.withDifferenceListeners(new ComparisonListener() {

				public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
					String item = comparison.getControlDetails().getValue().toString();
					if ((!item.contains("file:") && (!item.contains("http:"))) 
							&& (comparison.getType() != ComparisonType.XML_ENCODING)) {
						
						if (comparison.getType() == ComparisonType.TEXT_VALUE) {
							// could be the adl markup
							Node owner = comparison.getTestDetails().getTarget().getParentNode();
							if (owner instanceof Element) {
								Element eo = (Element) owner;
								if (eo.getAttribute("id").equals("adl:markup")) {
									return;
								}
							}
						} 
						
						if (comparison.getControlDetails().getXPath().endsWith("@d")) {
		        			if ((((String) comparison.getControlDetails().getValue()).length() > 20) &&
		        					(((String) comparison.getTestDetails().getValue()).length() > 20)) {
								// in this case, we are likely looking at fonts being rendered into paths.
								// these never seem to be consistent between machines.
		        				return;
		        			}
		        		}
		        		
						
						Assertions.fail("found a difference: " + comparison);
						
					}
				}
			}).build().hasDifferences();
	}

	public static Source stringToDom(String a) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			dbf.setIgnoringElementContentWhitespace(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document d = db.parse(new ReaderInputStream(new StringReader(a)));
			return Input.fromNode(d).build();
		} catch (Exception e) {
			throw new RuntimeException("Couldn't create DOM Source", e);
		}
	}
}
