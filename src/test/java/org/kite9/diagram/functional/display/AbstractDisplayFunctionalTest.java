package org.kite9.diagram.functional.display;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.junit.Test;
import org.kite9.diagram.functional.AbstractFunctionalTest;
import org.kite9.diagram.functional.NotAddressed;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.visualization.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonListener;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DOMDifferenceEngine;
import org.xmlunit.diff.DifferenceEngine;

import junit.framework.Assert;

public class AbstractDisplayFunctionalTest extends AbstractFunctionalTest {

	protected boolean checkXML() {
		return true;
	}

	protected void transcodePNG(String s) throws Exception {
		TranscoderOutput out = getTranscoderOutputPNG();
		TranscoderInput in = new TranscoderInput(new StringReader(s));
		Transcoder transcoder = new PNGTranscoder();
		transcoder.transcode(in, out);
	}
	
	protected void transcodeSVG(String s) throws Exception {
		TranscoderOutput out = getTranscoderOutputSVG();
		TranscoderInput in = new TranscoderInput(new StringReader(s));
		Transcoder transcoder = new Kite9SVGTranscoder();
		transcoder.transcode(in, out);
		
		if (checkXML()) {
			checkIdenticalXML();
		}
	}

	private TranscoderOutput getTranscoderOutputSVG() throws IOException {
		File f = getOutputFile("-graph.svg");
		TranscoderOutput out = new TranscoderOutput(new FileWriter(f));
		return out;
	}

	protected File getOutputFile(String ending) {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		File f = TestingHelp.prepareFileName(theTest, m.getName(), m.getName()+ending);
		return f;
	}
	
	private TranscoderOutput getTranscoderOutputPNG() throws IOException {
		File f = getOutputFile("-graph.png");
		TranscoderOutput out = new TranscoderOutput(new FileOutputStream(f));
		return out;
	}
	
	public boolean checkIdenticalXML() throws Exception {
		File output = getOutputFile("-graph.svg");
		InputStream is2 = getExpectedInputStream("-expected.svg");
		try {
			InputStream is1 = new FileInputStream(output);
			
			Source in1 = streamToDom(is1);
			Source in2 = streamToDom(is2);
			
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
	
	//protected void 

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

	public DiagramXMLElement renderDiagram(String xml) throws IOException {
		TestingEngine te = new TestingEngine(getZipName(), false);
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		boolean addressed = m.getAnnotation(NotAddressed.class) == null;
		Class<?> theTest = m.getDeclaringClass();
		return te.renderDiagram(xml, theTest, m.getName(), true, checks(),  addressed);
	}
	
}
