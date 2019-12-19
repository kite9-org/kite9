package org.kite9.diagram;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;

import org.junit.Test;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.batik.bridge.Kite9DiagramBridge;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.functional.TestingEngine.Checks;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.w3c.dom.Document;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonListener;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DOMDifferenceEngine;

import junit.framework.Assert;

public class AbstractDisplayFunctionalTest extends AbstractFunctionalTest {

	protected boolean checkXML() {
		return true;
	}

	protected void transcodeSVG(String s) throws Exception {
		try {
			super.transcodeSVG(s);
			
			Kite9XMLElement lastDiagram = Kite9DiagramBridge.lastDiagram;
			if (lastDiagram != null) {
				AbstractArrangementPipeline lastPipeline = Kite9DiagramBridge.lastPipeline;
				writeTemplateExpandedSVG(lastDiagram);
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
	
	private void writeTemplateExpandedSVG(Kite9XMLElement lastDiagram) throws IOException {
		ADLDocument d = lastDiagram.getOwnerDocument();
		File f = getOutputFile("-expanded.svg");
		String input2 = new XMLHelper().toXML(d);
		FileWriter fw = new FileWriter(f);
		fw.write(input2);
		fw.close();
	}

	protected Checks checks() {
		Checks out = new Checks();
		out.everythingStraight = false;
		out.checkNoHops = false;
		return out;
	}
	
	protected void renderDiagram(DiagramKite9XMLElement d) throws Exception {
		String xml = new XMLHelper().toXML(d.getOwnerDocument());
		renderDiagram(xml);
	}
	
	protected void renderDiagram(String xml) throws Exception {
		//transcodePNG(addSVGFurniture(xml));
		transcodeSVG(addSVGFurniture(xml));
	}

	public boolean checkIdenticalXML() throws Exception {
		File output = getOutputFile(".svg");
		Source in1;
		Source in2;
		try {
			InputStream is2 = getExpectedInputStream(".svg");
			
			// copy input file to output dir for ease of comparison
			File expectedOut = getOutputFile("-expected.svg");
			RepositoryHelp.streamCopy(is2, new FileOutputStream(expectedOut), true);
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
		        		if (comparison.getControlDetails().getXPath().endsWith("@k9-info")) {
		        			// ignore the info
		        			return;
		        		}
		        	}
		        	if (comparison.getType() == ComparisonType.TEXT_VALUE) {
		        		String c1 = comparison.getControlDetails().getValue().toString().trim();
		        		String c2 = comparison.getTestDetails().getValue().toString().trim();
		        		if (c1.equals(c2)) {
		        			return;
		        		}
		        		
		        	}
					if (!comparison.getControlDetails().getValue().toString().contains("file:")) {
						copyToErrors(output);	
						Assert.fail("found a difference: " + comparison);
					}
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
