package org.kite9.diagram;

import org.apache.batik.dom.GenericDocument;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kite9.diagram.adl.AbstractMutableXMLElement;
import org.kite9.diagram.batik.format.Kite9PNGTranscoder;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.HelpMethods;
import org.kite9.diagram.common.StackHelp;
import org.kite9.diagram.common.StreamHelp;
import org.kite9.diagram.dom.ADLExtensibleDOMImplementation;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9Log.Destination;
import org.kite9.diagram.logging.Kite9LogImpl;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFunctionalTest extends HelpMethods {

	public AbstractFunctionalTest() {
		super();
	}

	@BeforeClass
	public static void setLoggingFactory() {
		Kite9Log.Companion.setFactory(l -> new Kite9LogImpl(l));
	}

	@BeforeClass
	public static void setupTestDOMImplementation() {
		AbstractMutableXMLElement.DOM_IMPLEMENTATION = new ADLExtensibleDOMImplementation();
	}

	@Before
	public void setupTestXMLDocument() {
		AbstractMutableXMLElement.TRANSFORM = AbstractFunctionalTest.class.getResource("/stylesheets/tester.xslt").getFile();
		AbstractMutableXMLElement.TESTING_DOCUMENT = AbstractMutableXMLElement.newDocument();
	}

	@Before
	public void setLogging() {
//		Kite9LogImpl.setLogging(Destination.STREAM);
		if ("off".equals(System.getProperty("kite9.logging"))) {
			Kite9LogImpl.setLogging(Destination.OFF);
		} else {
			// if we are running more than one test, then there's no point in logging.
			if (firstRun) {
				firstRun = false;
			} else {
				Kite9LogImpl.setLogging(Destination.OFF);
			}
		}
	}

	@Before
	public void initTestDocument() {
		AbstractMutableXMLElement.TESTING_DOCUMENT = new GenericDocument(null, AbstractMutableXMLElement.DOM_IMPLEMENTATION);
		AbstractMutableXMLElement.nextId = 0;
	}

	protected void transcodePNG(String s) throws Exception {
		TranscoderOutput out = getTranscoderOutputPNG();
		TranscoderInput in = getTranscoderInput(s);
		Transcoder transcoder = new Kite9PNGTranscoder();
		transcoder.transcode(in, out);
	}
	
	
	
	protected void transcodeSVG(String s) throws Exception {
		TranscoderOutput out = getTranscoderOutputSVG();
		TranscoderInput in = getTranscoderInput(s);
		Transcoder transcoder = new Kite9SVGTranscoder();
		transcoder.transcode(in, out);
	}

	protected TranscoderOutput getTranscoderOutputPNG() throws IOException {
		File f = getOutputFile(".png");
		TranscoderOutput out = new TranscoderOutput(new FileOutputStream(f));
		return out;
	}
	
	protected TranscoderOutput getTranscoderOutputSVG() throws IOException {
		File f = getOutputFile(".svg");
		TranscoderOutput out = new TranscoderOutput(new FileWriter(f));
		out.setURI(f.getAbsolutePath());
		return out;
	}

	protected TranscoderInput getTranscoderInput(String s) throws IOException {
		File f = getOutputFile("-input.adl");
		StreamHelp.streamCopy(new StringReader(s), new FileWriter(f), true);
		TranscoderInput out = new TranscoderInput(new StringReader(s));
		out.setURI(getInputURI(f));
		return out;
	}

	protected abstract File getOutputFile(String ending);
	
	protected String getInputURI(File f) {
		String name = f.getName();
		String packageName = this.getClass().getPackage().getName();
		String root = "src/test/resources";
		return root+"/"+packageName.replace('.', '/') + "/" + name;
	}

	
	protected String getTestMethod() {
		return StackHelp.getAnnotatedMethod(org.junit.Test.class).getName();
	}
	
	static boolean firstRun = true;
	

	

	protected void copyToErrors(File output) {
		copyTo(output, "errors");
	}
	
	
	protected void copyTo(File output, String dir) {
		try {
			File parent = output.getParentFile().getParentFile().getParentFile();
			File errors = new File(parent, dir);
			errors.mkdir();
			String name = output.getName();
			File newFile = new File(errors, name);
			StreamHelp.streamCopy(new FileInputStream(output), new FileOutputStream(newFile), true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}