package org.kite9.diagram;

import org.junit.Test;
import org.kite9.diagram.adl.AbstractMutableXMLElement;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.StackHelp;
import org.kite9.diagram.common.StreamHelp;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.functional.TestingEngine.Checks;
import org.kite9.diagram.logging.LogicException;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.testing.DiagramChecker;
import org.kite9.diagram.testing.TestingHelp;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;
import org.kite9.diagram.visualization.pipeline.ArrangementPipeline;
import org.w3c.dom.*;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

public class AbstractLayoutFunctionalTest extends AbstractFunctionalTest {
	

	protected void renderDiagram(Element d) throws Exception {
		String xml = new XMLHelper().toXML(d.getOwnerDocument());
		renderDiagram(xml);
	}


	protected void renderDiagram(String xml) throws Exception {
		//transcodePNG(full);
		transcodeSVG(xml);
		copyTo(getOutputFile(".svg"), "svg-output");
		Diagram lastDiagram = Kite9SVGTranscoder.lastDiagram;
		AbstractArrangementPipeline lastPipeline = Kite9SVGTranscoder.lastPipeline;
		boolean addressed = isAddressed();
		new TestingEngine().testDiagram(lastDiagram, this.getClass(), getTestMethod(), checks(), addressed, lastPipeline);
	}

	private boolean isAddressed() {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		return !(m.isAnnotationPresent(NotAddressed.class));
	}

	protected File getOutputFile(String ending) {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		File f = TestingHelp.prepareFileName(theTest, m.getName(), m.getName()+ending);
		return f;
	}

	protected Checks checks() {
		Checks out = new Checks();
		out.checkEdgeDirections = checkEdgeDirections();
		out.checkOcclusion = checkOcclusion();
		out.checkLayout = checkLayout();
		out.checkNoContradictions = checkNoContradictions();
		out.checkNoHops = checkNoHops();
		out.everythingStraight = checkEverythingStraight();
		out.checkMidConnection = checkMidConnections();
		return out;
	}
	
	protected boolean checkMidConnections() {
		return true;
	}


	protected boolean checkOcclusion() {
		return true;
	}
	
	protected boolean checkDiagramSize() {
		return false;
	}
	
	protected boolean checkEdgeDirections() {
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
		
	protected DiagramElement getById(final String id, Element d) {
		Element x = d.getOwnerDocument().getElementById(id);
		DiagramElement de = Kite9SVGTranscoder.lastContext.getRegisteredDiagramElement(x);
		return de;
	}

	protected void mustTurn(Diagram d, Link l) {
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

	protected void mustContradict(Diagram diag, Link l) {
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
	
	public void generate(String name) throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/org/kite9/diagram/xml/"+name);
		InputStreamReader isr = new InputStreamReader(is);
		StringWriter sw = new StringWriter();
		StreamHelp.streamCopy(isr, sw, true);
		String s = sw.toString();
		XMLHelper xmlHelper = new XMLHelper();
		Document dxe = xmlHelper.fromXML(s);
		convertOldStructure(dxe.getDocumentElement());
		dxe.getDocumentElement().setAttribute("template", AbstractMutableXMLElement.TRANSFORM);
		
		// fix for old-style <allLinks> tag
		String theXML = xmlHelper.toXML(dxe);
		//Kite9Log.setLogging(false);
		transcodeSVG(theXML);
		boolean addressed = isAddressed();
		Diagram lastDiagram = Kite9SVGTranscoder.lastDiagram;
		AbstractArrangementPipeline lastPipeline = Kite9SVGTranscoder.lastPipeline;
		new TestingEngine().testDiagram(lastDiagram, this.getClass(), getTestMethod(), checks(), addressed, lastPipeline);
		
	}

	private void convertOldStructure(Element dxe) {
		if (dxe.getTagName().equals("allLinks")) {
			moveChildrenIntoParentAndDelete(dxe);
		} if (dxe.getTagName().equals("text")) {
			moveChildrenIntoParentAndDelete(dxe);
		} if (dxe.getTagName().equals("hints")) {
			dxe.getParentNode().removeChild(dxe);
		} if ((dxe.getTagName().equals("symbols")) && (((Element) dxe.getParentNode()).getTagName().equals("text-line"))) {
			dxe.getParentNode().removeChild(dxe);
		} else {
			NodeList nl = dxe.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n instanceof Element) {
					convertOldStructure((Element) n);
				}
			}
			convertAttributeToTag(dxe, "label");
			convertAttributeToTag(dxe, "stereotype");
		}
		
	}

	private void moveChildrenIntoParentAndDelete(Element dxe) {
		Element parent = (Element) dxe.getParentNode();
		Node n = dxe.getFirstChild();
		while (n != null) {
			Node n2 = n.getNextSibling();
			if ((n instanceof Element) || (n instanceof Text)) {
				parent.appendChild(n);
			}
			n = n2;
		}
		parent.removeChild(dxe);
	}

	private void convertAttributeToTag(Element dxe, String name) {
		if (dxe.getAttribute(name).length() > 0) {
			String label = dxe.getAttribute(name);
			Element e = dxe.getOwnerDocument().createElement(name);
			dxe.appendChild(e);
			e.setTextContent(label);
		}
	}
}
