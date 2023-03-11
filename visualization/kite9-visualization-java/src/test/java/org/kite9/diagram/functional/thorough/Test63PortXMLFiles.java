package org.kite9.diagram.functional.thorough;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;

public class Test63PortXMLFiles extends AbstractLayoutFunctionalTest {

	@Test
	public void test_63_1_WeirdPortRouting() throws Exception {
		generate("weird_port_routing.xml");
	}


	@Override
	protected boolean checkMidConnections() {
		return false;
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}


}
