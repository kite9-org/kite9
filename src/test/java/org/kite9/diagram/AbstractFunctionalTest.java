package org.kite9.diagram;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.junit.Before;
import org.kite9.diagram.batik.format.Kite9PNGTranscoder;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.dom.XMLHelper;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.AbstractStyleableXMLElement;

public abstract class AbstractFunctionalTest extends HelpMethods {

	public AbstractFunctionalTest() {
		super();
	}

	@Before
	public void initTestDocument() {
		AbstractStyleableXMLElement.TESTING_DOCUMENT =  new ADLDocument();
	}
	
	@Before
	public void resetCounter() {
		AbstractStyleableXMLElement.resetCounter();
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
		File f = getOutputFile("-graph.png");
		TranscoderOutput out = new TranscoderOutput(new FileOutputStream(f));
		return out;
	}
	
	protected TranscoderOutput getTranscoderOutputSVG() throws IOException {
		File f = getOutputFile("-graph.svg");
		TranscoderOutput out = new TranscoderOutput(new FileWriter(f));
		return out;
	}

	protected TranscoderInput getTranscoderInput(String s) throws IOException {
		File f = getOutputFile("-input.svg");
		RepositoryHelp.streamCopy(new StringReader(s), new FileWriter(f), true);
		return new TranscoderInput(new StringReader(s));
	}

	protected abstract File getOutputFile(String ending);

	public String getDesignerStylesheetReference() {
		URL u = this.getClass().getResource("/stylesheets/designer.css");
		return "<stylesheet xmlns='"+XMLHelper.KITE9_NAMESPACE+"' href=\""+u.toString()+"\" xml:space=\"preserve \"/>";
	}

	protected String addSVGFurniture(String xml) {
		String prefix = "<svg:svg xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:svg='http://www.w3.org/2000/svg'>";
		String style = getDesignerStylesheetReference();
		String suffix = "</svg:svg>";
		xml = xml.replaceFirst("<\\?.*\\?>","");
		String full = prefix + style + xml + suffix;
		return full;
	}
}