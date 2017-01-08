package org.kite9.diagram.functional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.junit.Before;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.display.components.ConnectionDisplayer;
import org.kite9.diagram.visualization.format.pos.DiagramChecker;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.AbstractXMLElement;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.StylesheetReference;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.serialization.XMLHelper;

public class AbstractFunctionalTest extends HelpMethods {
	
	public String getZipName() {
		return "/functional-test.zip";
	}
	
	public DiagramXMLElement renderDiagram(DiagramXMLElement d, TestingEngine te, boolean watermark) throws IOException {
		return te.renderDiagram(d, true, checkDiagramSize(), checkEdgeDirections(), checkNoHops(), checkEverythingStraight(), checkLayout(), checkNoContradictions(), checkImage());
	}

	public DiagramXMLElement renderDiagramNoSerialize(DiagramXMLElement d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName(), false);
		return renderDiagram(d, te, true);
	}

	public DiagramXMLElement renderDiagramNoWM(DiagramXMLElement d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		return renderDiagram(d, te, false);
	}
	
	public DiagramXMLElement renderDiagram(DiagramXMLElement d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		return renderDiagram(d, te, true);
	}

	public void renderDiagramPDF(DiagramXMLElement d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		renderDiagram(d);
		te.renderDiagramPDF(d);
	}
	
	public void renderDiagramSVG(DiagramXMLElement d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		renderDiagram(d);
		te.renderDiagramSVG(d);
	}
	
	public void renderDiagramADLAndSVG(DiagramXMLElement d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		te.renderDiagramADLAndSVG(d);
	}
	
	public TestingEngine getTestingEngine() {
		return new TestingEngine(getZipName());
	}
	
	public void renderDiagramSizes(DiagramXMLElement d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		te.renderDiagramSizes(d);
	}
	
	public void renderMap(DiagramXMLElement d) throws IOException {
		TestingEngine te = new TestingEngine(getZipName());
		te.renderMap(d);
	}
	
	protected boolean checkDiagramSize() {
		return false;
	}
	
	protected boolean checkEdgeDirections() {
		ConnectionDisplayer.debug = true;
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
		DiagramXMLElement d = (DiagramXMLElement) o;
		
		StylesheetReference sr = d.getStylesheetReference();
		if (sr == null) {
			TestingEngine.setDesignerStylesheetReference(d);
		}
		
		final int[] i =  { 0 } ;
		relabel(d.getDiagramElement(), i);
		renderDiagram(d);
		//renderDiagramSizes(d);
		
	}
	
	public DiagramXMLElement generateNoRename(String name) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(name);
		InputStreamReader isr = new InputStreamReader(is);
		StringWriter sw = new StringWriter();
		RepositoryHelp.streamCopy(isr, sw, true);
		Object o = new XMLHelper().fromXML(sw.getBuffer().toString());
		DiagramXMLElement d = (DiagramXMLElement) o;
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
		DiagramXMLElement d = (DiagramXMLElement) new XMLHelper().fromXML(sw.getBuffer().toString());
		renderDiagramSizes(d);
	}
	
	public void generatePDF(String name) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(name);
		InputStreamReader isr = new InputStreamReader(is);
		StringWriter sw = new StringWriter();
		RepositoryHelp.streamCopy(isr, sw, true);
		DiagramXMLElement d = (DiagramXMLElement) new XMLHelper().fromXML(sw.getBuffer().toString());
		renderDiagramPDF(d);
		//renderDiagramSizes(d);
	}
	
	@Before
	public void resetCounter() {
		AbstractXMLElement.resetCounter();
	}
	
	public DiagramElement getById(final String id, DiagramXMLElement d) {
		DiagramElementVisitor vis = new DiagramElementVisitor();
		final DiagramElement[] found = { null };
		vis.visit(d.getDiagramElement(), new VisitorAction() {
			
			@Override
			public void visit(DiagramElement de) {
				if (de.getID().equals(id)) {
						found[0] = de;
				}
			}
		});
		
		return found[0];
	}

	protected void mustTurn(DiagramXMLElement d, Link l) {
		DiagramChecker.checkConnnectionElements(d, new DiagramChecker.ConnectionAction() {
	
			@Override
			public void action(RouteRenderingInformation rri, Object d, Connection c) {
				if (l == c) {
					if (d != DiagramChecker.MULTIPLE_DIRECTIONS) {
						throw new LogicException("Should be turning");
					}
				}
			}
		});
	}

	protected void mustContradict(DiagramXMLElement diag, Link l) {
		DiagramChecker.checkConnnectionElements(diag, new DiagramChecker.ConnectionAction() {
			
			@Override
			public void action(RouteRenderingInformation rri, Object d, Connection c) {
				if (c == l) {
					if (d != DiagramChecker.SET_CONTRADICTING) {
						throw new LogicException("Should be contradicting");
					}
				}
			}
		});
	}
}
