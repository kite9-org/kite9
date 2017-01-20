package org.kite9.diagram.functional.display;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.kite9.diagram.functional.AbstractFunctionalTest;
import org.kite9.diagram.functional.NotAddressed;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.StackHelp;

public class AbstractDisplayFunctionalTest extends AbstractFunctionalTest {

	@Override
	protected boolean checkImage() {
		return true;
	}
	
	@Override
	protected boolean checkDiagramSize() {
		return false;
	}


	public DiagramXMLElement renderDiagram(String xml) throws IOException {
		TestingEngine te = new TestingEngine(getZipName(), false);
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		boolean addressed = m.getAnnotation(NotAddressed.class) == null;
		Class<?> theTest = m.getDeclaringClass();
		return te.renderDiagram(xml, theTest, m.getName(), true, checks(),  addressed);
	}
	
}
