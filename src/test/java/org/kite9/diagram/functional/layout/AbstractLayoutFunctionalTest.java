package org.kite9.diagram.functional.layout;

import java.io.File;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.functional.AbstractFunctionalTest;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.functional.TestingEngine.Checks;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.batik.bridge.Kite9DiagramBridge;
import org.kite9.diagram.visualization.display.components.ConnectionDisplayer;
import org.kite9.diagram.visualization.format.pos.DiagramChecker;
import org.kite9.diagram.visualization.pipeline.full.AbstractArrangementPipeline;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.serialization.XMLHelper;

public class AbstractLayoutFunctionalTest extends AbstractFunctionalTest {
	

	protected void renderDiagram(DiagramXMLElement d) throws Exception {
		String xml = new XMLHelper().toXML(d);
		String prefix = "<svg:svg xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:svg='http://www.w3.org/2000/svg'>";
		String style = getDesignerStylesheetReference();
		String suffix = "</svg:svg>";
		xml = xml.replaceFirst("<\\?.*\\?>\n","");
		transcodePNG(prefix + style + xml + suffix);
		DiagramXMLElement lastDiagram = Kite9DiagramBridge.lastDiagram;
		AbstractArrangementPipeline lastPipeline = Kite9DiagramBridge.lastPipeline;
		new TestingEngine().testDiagram(lastDiagram, this.getClass(), getTestMethod(), checks(), false, lastPipeline);
	}
	
	
	private String getTestMethod() {
		return StackHelp.getAnnotatedMethod(org.junit.Test.class).getName();
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
	

	
	protected DiagramElement getById(final String id, DiagramXMLElement d) {
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
