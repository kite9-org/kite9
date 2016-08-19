package org.kite9.diagram.functional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.junit.Before;
import org.kite9.diagram.adl.AbstractConnectedContained;
import org.kite9.diagram.adl.IdentifiableDiagramElement;
import org.kite9.diagram.style.DiagramElement;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.display.components.LinkDisplayer;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.AbstractXMLElement;
import org.kite9.diagram.xml.Diagram;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.serialization.XMLHelper;

public class AbstractFunctionalTest extends HelpMethods {
	
	public String getZipName() {
		return "/functional-test.zip";
	}
	
	public Diagram renderDiagram(Diagram d, TestingEngine te, boolean watermark) throws IOException {
		return te.renderDiagram(d, true, checkDiagramSize(), checkEdgeDirections(), checkNoHops(), checkEverythingStraight(), checkLayout(), checkNoContradictions(), checkImage());
	}

	public Diagram renderDiagramNoSerialize(Diagram d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName(), false);
		return renderDiagram(d, te, true);
	}

	public Diagram renderDiagramNoWM(Diagram d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		return renderDiagram(d, te, false);
	}
	
	public Diagram renderDiagram(Diagram d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		return renderDiagram(d, te, true);
	}

	public void renderDiagramPDF(Diagram d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		renderDiagram(d);
		te.renderDiagramPDF(d);
	}
	
	public void renderDiagramSVG(Diagram d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		renderDiagram(d);
		te.renderDiagramSVG(d);
	}
	
	public void renderDiagramADLAndSVG(Diagram d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		te.renderDiagramADLAndSVG(d);
	}
	
	public TestingEngine getTestingEngine() {
		return new TestingEngine(getZipName());
	}
	
	public void renderDiagramSizes(Diagram d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		te.renderDiagramSizes(d);
	}
	
	public void renderMap(Diagram d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		te.renderMap(d);
	}
	
	protected boolean checkDiagramSize() {
		return false;
	}
	
	protected boolean checkEdgeDirections() {
		LinkDisplayer.debug = true;
		return true;
	}
	
	protected boolean checkNoHops() {
		return true;
	}
	
	protected boolean checkEverythingStraight() {
		return true;
	}
	
	protected boolean checkLayout() {
		return true;
	}
	
	protected boolean checkNoContradictions() {
		return true;
	}
	
	
	protected boolean checkImage() {
		return false;
	}
	
	static boolean firstRun = true;
	
	
	@Before
	public void setLogging() {
		Kite9Log.setLogging(true);
		
		// if we are running more than one test, then there's no point in logging.
		if (firstRun) {
			firstRun = false;
		} else {
			Kite9Log.setLogging(false);
		}
	}
	
	@Before
	public void initTestDocument() {
		AbstractXMLElement.TESTING_DOCUMENT =  new ADLDocument();
	}

	

	public void generate(String name) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(name);
		InputStreamReader isr = new InputStreamReader(is);
		StringWriter sw = new StringWriter();
		RepositoryHelp.streamCopy(isr, sw, true);
		Object o = new XMLHelper().fromXML(sw.getBuffer().toString());
		Diagram d = (Diagram) o;
		final int[] i =  { 0 } ;
		relabel(d, i);
		renderDiagram(d);
		//renderDiagramSizes(d);
		
	}
	
	public Diagram generateNoRename(String name) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(name);
		InputStreamReader isr = new InputStreamReader(is);
		StringWriter sw = new StringWriter();
		RepositoryHelp.streamCopy(isr, sw, true);
		Object o = new XMLHelper().fromXML(sw.getBuffer().toString());
		Diagram d = (Diagram) o;
		return renderDiagram(d);
		//renderDiagramSizes(d);
	}

	public void relabel(DiagramElement de, int[] i) {
//		if (de instanceof IdentifiableDiagramElement) {
//			((IdentifiableDiagramElement) de).setID("id_"+i[0]);
//			i[0]++;
//		}
//		if (de instanceof Glyph) {
//			((Glyph) de).setLabel(new TextLine(((Glyph) de).getID()));
//		} else if (de instanceof Arrow) {
//			((Arrow) de).setLabel(new TextLine(((Arrow) de).getID()));
//		} else {
//			if (de instanceof Container) {
//				if (de instanceof Context) {
//					((Context) de).setBordered(true);
//					((Context) de).setLabel(new TextLine(((Context) de).getID()));
//				}
//				
//			}
//			
//			for (Contained d : ((Container)de).getContents()) {
//				relabel(d, i);
//			}
//		} 
	}
	
	public void generateSizes(String name) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(name);
		InputStreamReader isr = new InputStreamReader(is);
		StringWriter sw = new StringWriter();
		RepositoryHelp.streamCopy(isr, sw, true);
		Diagram d = (Diagram) new XMLHelper().fromXML(sw.getBuffer().toString());
		renderDiagramSizes(d);
	}
	
	public void generatePDF(String name) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(name);
		InputStreamReader isr = new InputStreamReader(is);
		StringWriter sw = new StringWriter();
		RepositoryHelp.streamCopy(isr, sw, true);
		Diagram d = (Diagram) new XMLHelper().fromXML(sw.getBuffer().toString());
		renderDiagramPDF(d);
		//renderDiagramSizes(d);
	}
	
	@Before
	public void resetCounter() {
		AbstractConnectedContained.resetCounter();
	}
	
	public DiagramElement getById(final String id, Diagram d) {
		DiagramElementVisitor vis = new DiagramElementVisitor();
		final DiagramElement[] found = { null };
		vis.visit(d, new VisitorAction() {
			
			@Override
			public void visit(DiagramElement de) {
				if (de instanceof IdentifiableDiagramElement) {
					if (((IdentifiableDiagramElement) de).getID().equals(id)) {
						found[0] = de;
					}
				}
			}
		});
		
		return found[0];
	}
}
