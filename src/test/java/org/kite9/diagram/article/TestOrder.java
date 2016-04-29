package org.kite9.diagram.article;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.builders.java.DiagramBuilder;
import org.kite9.diagram.visualization.display.style.sheets.CGWhiteStylesheet;
import org.kite9.framework.alias.PropertyAliaser;
import org.kite9.framework.model.ProjectModel;
import org.kite9.java.examples.orders.Order;


public class TestOrder extends AbstractJavaExamplesTest {

	@Test
	public void testOrder() throws IOException, SecurityException, NoSuchMethodException {
		PropertyAliaser pa = new PropertyAliaser();
		
		ProjectModel pm = buildProjectModel(Order.class.getPackage().getName());
		
		DiagramBuilder db = new DiagramBuilder(pa, Order.class.getDeclaredMethod("orderEntityRelationshipDiagramb", DiagramBuilder.class), pm);
		// renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_5_UseCases(db), new CGWhiteStylesheet());
		renderDiagramNoWM(Order.orderEntityRelationshipDiagramb(db), new CGWhiteStylesheet());

	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}
	
	
}
