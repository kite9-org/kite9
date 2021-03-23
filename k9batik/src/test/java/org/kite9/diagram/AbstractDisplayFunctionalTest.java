package org.kite9.diagram;

import org.junit.Assert;
import org.junit.Test;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.StackHelp;
import org.kite9.diagram.common.StreamHelp;
import org.kite9.diagram.dom.XMLHelper;
import org.w3c.dom.Element;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.functional.TestingEngine.Checks;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.testing.TestingHelp;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;
import org.w3c.dom.*;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import java.io.*;
import java.lang.reflect.Method;

public class AbstractDisplayFunctionalTest extends AbstractFunctionalTest {

	protected boolean checkXML() {
		return true;
	}

	protected void transcodeSVG(String s) throws Exception {
		try {
			super.transcodeSVG(s);
			
			Diagram lastDiagram = Kite9SVGTranscoder.lastDiagram;
			if (lastDiagram != null) {
				AbstractArrangementPipeline lastPipeline = Kite9SVGTranscoder.lastPipeline;
				new TestingEngine().testDiagram(lastDiagram, this.getClass(), getTestMethod(), checks(), true, lastPipeline);
			}
			if (checkXML()) {
				checkIdenticalXML();
			}
		} finally {
			try {
				copyTo(getOutputFile(".svg"), "svg-output");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected Checks checks() {
		Checks out = new Checks();
		out.everythingStraight = false;
		out.checkNoHops = false;
		return out;
	}
	
	protected void renderDiagram(Element d) throws Exception {
		String xml = new XMLHelper().toXML(d.getOwnerDocument());
		renderDiagram(xml);
	}
	
	protected void renderDiagram(String xml) throws Exception {
		//transcodePNG(addSVGFurniture(xml));
		transcodeSVG(xml);
	}

	public boolean checkIdenticalXML() throws Exception {
		File output = getOutputFile(".svg");
		Source in1;
		Source in2;
		try {
			InputStream is2 = getExpectedInputStream(".svg");
			
			// copy input file to output dir for ease of comparison
			File expectedOut = getOutputFile("-expected.svg");
			StreamHelp.streamCopy(is2, new FileOutputStream(expectedOut), true);
			is2 = getExpectedInputStream(".svg");
			
			in2 = streamToDom(is2);
			
		} catch (Exception e1) {
			copyToErrors(output);
			Assert.fail("Couldn't perform comparison (no expected file): "+output+" "+e1.getMessage());
			return false;
		}
		
		try {
			InputStream is1 = new FileInputStream(output);
			in1 = streamToDom(is1);
			
			DOMDifferenceEngine diff = new DOMDifferenceEngine();
		
			
			diff.addDifferenceListener(new ComparisonListener() {
				
		        public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
		        	if (comparison.getType() == ComparisonType.ATTR_VALUE) {
		        		if (comparison.getControlDetails().getXPath().contains("@k9-")) {
		        			// ignore the info
		        			return;
		        		}

		        		if (comparison.getControlDetails().getXPath().endsWith("@template")) {
		        			// ignore template location
							return;
						}
		        		
		        		if (comparison.getControlDetails().getXPath().endsWith("@d")) {
		        			if ((((String) comparison.getControlDetails().getValue()).length() > 20) &&
		        					(((String) comparison.getTestDetails().getValue()).length() > 20)) {
								// in this case, we are likely looking at fonts being rendered into paths.
								// these never seem to be consistent between machines.
		        				return;
		        			}
		        		}
		        		
		        	}
		        	if (comparison.getType() == ComparisonType.TEXT_VALUE) {
		        		String c1 = comparison.getControlDetails().getValue().toString().trim();
		        		String c2 = comparison.getTestDetails().getValue().toString().trim();
		        		if (c1.equals(c2)) {
		        			return;
		        		}
		        		
		        	}
		        	if (comparison.getType() == ComparisonType.CHILD_NODELIST_LENGTH) {
		        		if (countNonWSChildren(comparison.getControlDetails().getTarget()) == 
		        			countNonWSChildren(comparison.getTestDetails().getTarget())) {
		        			return;
		        		}
		        		
		        	}
		        	
					if (!comparison.getControlDetails().getValue().toString().contains("file:")) {
						copyToErrors(output);	
						Assert.fail("found a difference: " + comparison);
					}
		        }

				private Object countNonWSChildren(Node target) {
					NodeList nl = target.getChildNodes();
					int count = 0;
					for (int i = 0; i < nl.getLength(); i++) {
						if ((nl.item(i) instanceof Text) && (((Text)nl.item(i)).getTextContent().trim().length()==0)) {
							// whitespace text, ignore
						} else {
							count++;
						}
					}
					
					return count;
				}
		    });
			
			diff.compare(in1, in2);
		} catch (NullPointerException e) {
			copyToErrors(output);
			Assert.fail("Missing diagram file: " + e.getMessage());
			return false;
		}
	
		return true;
	}

	protected InputStream getExpectedInputStream(String ending) throws FileNotFoundException {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		String name = m.getName()+ending;
		File f = new File(theTest.getResource("").getFile());
		File f2 = new File(f, name);
		
		InputStream is2 = new FileInputStream(f2);
		return is2;
	}
	
	protected File getOutputFile(String ending) {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		File f = TestingHelp.prepareFileName(theTest, m.getName(), m.getName()+ending);
		return f;
	}

	private Source streamToDom(InputStream is1) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		dbf.setIgnoringElementContentWhitespace(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document d = db.parse(is1);
		return Input.fromNode(d).build();
	}
}
