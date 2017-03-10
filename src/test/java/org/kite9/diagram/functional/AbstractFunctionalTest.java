package org.kite9.diagram.functional;

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
import org.kite9.diagram.visualization.batik.format.Kite9PNGTranscoder;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.AbstractStyleableXMLElement;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.serialization.XMLHelper;

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

	TranscoderOutput getTranscoderOutputPNG() throws IOException {
		File f = getOutputFile("-graph.png");
		TranscoderOutput out = new TranscoderOutput(new FileOutputStream(f));
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

}