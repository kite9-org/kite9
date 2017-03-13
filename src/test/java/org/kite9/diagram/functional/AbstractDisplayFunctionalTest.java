package org.kite9.diagram.functional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.junit.Test;
import org.kite9.diagram.visualization.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.kite9.framework.serialization.XMLHelper;
import org.w3c.dom.Document;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonListener;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DOMDifferenceEngine;

import junit.framework.Assert;

public class AbstractDisplayFunctionalTest extends AbstractFunctionalTest {

	protected boolean checkXML() {
		return true;
	}

	protected void transcodeSVG(String s) throws Exception {
		TranscoderOutput out = getTranscoderOutputSVG();
		TranscoderInput in = getTranscoderInput(s);
		Transcoder transcoder = new Kite9SVGTranscoder();
		transcoder.transcode(in, out);
		
		if (checkXML()) {
			checkIdenticalXML();
		}
	}
	
	protected void renderDiagram(DiagramXMLElement d) throws Exception {
		String xml = new XMLHelper().toXML(d);
		renderDiagram(xml);
	}
	
	protected void renderDiagram(String xml) throws Exception {
		transcodeSVG(addSVGFurniture(xml));
	}

	private TranscoderOutput getTranscoderOutputSVG() throws IOException {
		File f = getOutputFile("-graph.svg");
		TranscoderOutput out = new TranscoderOutput(new FileWriter(f));
		return out;
	}

	public boolean checkIdenticalXML() throws Exception {
		File output = getOutputFile("-graph.svg");
		Source in1;
		Source in2;
		try {
			InputStream is2 = getExpectedInputStream("-graph.svg");
			
			// copy input file to output dir for ease of comparison
			File expectedOut = getOutputFile("-expected.svg");
			RepositoryHelp.streamCopy(is2, new FileOutputStream(expectedOut), true);
			is2 = getExpectedInputStream("-graph.svg");
			
			in2 = streamToDom(is2);
			
		} catch (Exception e1) {
			Assert.fail("Couldn't perform comparison (no expected file): "+e1.getMessage());
			return false;
		}
		
		try {
			InputStream is1 = new FileInputStream(output);
			in1 = streamToDom(is1);
			
			DOMDifferenceEngine diff = new DOMDifferenceEngine();
			
			
			diff.addDifferenceListener(new ComparisonListener() {
				
		        public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
		            Assert.fail("found a difference: " + comparison);
		            
		        }
		    });
			
			diff.compare(in1, in2);
		} catch (NullPointerException e) {
			Assert.fail("Missing diagram file: " + e.getMessage());
			return false;
		}
	
		return true;
	}

	protected InputStream getExpectedInputStream(String ending) {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		InputStream is2 = theTest.getResourceAsStream(m.getName()+ending);
		return is2;
	}
	
	protected File getOutputFile(String ending) {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		File f = TestingHelp.prepareFileName(theTest, "", m.getName()+ending);
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
