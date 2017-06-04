package org.kite9.diagram;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.batik.bridge.Kite9DiagramBridge;
import org.kite9.diagram.functional.layout.TestingEngine;
import org.kite9.diagram.functional.layout.TestingEngine.Checks;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.model.visitors.DiagramChecker;
import org.kite9.diagram.model.visitors.DiagramElementVisitor;
import org.kite9.diagram.model.visitors.VisitorAction;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.kite9.framework.dom.XMLHelper;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.DiagramKite9XMLElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class AbstractLayoutFunctionalTest extends AbstractFunctionalTest {
	

	protected DiagramKite9XMLElement renderDiagram(DiagramKite9XMLElement d) throws Exception {
		String xml = new XMLHelper().toXML(d.getOwnerDocument());
		return renderDiagram(xml);
	}


	protected DiagramKite9XMLElement renderDiagram(String xml) throws Exception {
		String full = addSVGFurniture(xml);
		transcodePNG(full);
		transcodeSVG(full);
		DiagramKite9XMLElement lastDiagram = Kite9DiagramBridge.lastDiagram;
		AbstractArrangementPipeline lastPipeline = Kite9DiagramBridge.lastPipeline;
		boolean addressed = isAddressed();
		new TestingEngine().testDiagram(lastDiagram, this.getClass(), getTestMethod(), checks(), addressed, lastPipeline);
		return lastDiagram;
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
		out.checkLabelOcclusion = checkLabelOcclusion();
		out.checkLayout = checkLayout();
		out.checkNoContradictions = checkNoContradictions();
		out.checkNoHops = checkNoHops();
		out.everythingStraight = checkEverythingStraight();
		return out;
	}
	
	protected boolean checkLabelOcclusion() {
		return false;
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
		
	protected DiagramElement getById(final String id, DiagramKite9XMLElement d) {
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

	protected void mustTurn(DiagramKite9XMLElement d, Link l) {
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

	protected void mustContradict(DiagramKite9XMLElement diag, Link l) {
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
		RepositoryHelp.streamCopy(isr, sw, true);
		String s = sw.toString();
		s = addSVGFurniture(s);
		XMLHelper xmlHelper = new XMLHelper();
		ADLDocument dxe = xmlHelper.fromXML(s);
		convertOldStructure(dxe.getRootElement());
		
		// fix for old-style <allLinks> tag
		String theXML = xmlHelper.toXML(dxe);
		//Kite9Log.setLogging(false);
		transcodePNG(theXML);
		transcodeSVG(theXML);
		boolean addressed = isAddressed();
		DiagramKite9XMLElement lastDiagram = Kite9DiagramBridge.lastDiagram;
		AbstractArrangementPipeline lastPipeline = Kite9DiagramBridge.lastPipeline;
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
