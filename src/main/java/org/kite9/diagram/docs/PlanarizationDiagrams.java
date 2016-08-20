package org.kite9.diagram.docs;

import org.kite9.diagram.builders.java.DiagramBuilder;
import org.kite9.diagram.builders.wizards.classdiagram.ClassDiagramWizard;
import org.kite9.diagram.builders.wizards.objectgraph.ObjectDependencyWizard;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarizer;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.Kite9Item;

public class PlanarizationDiagrams {

	@Kite9Item
	public DiagramXMLElement planarizationView(DiagramBuilder db) {
		ClassDiagramWizard erw = new ClassDiagramWizard(db);
		erw.show(db.withAnnotatedClasses());
		db.withKeyText("Planarization Diagram", "This diagram shows how the main attr of a planarization come together");
		return db.getDiagram();
	}
	
	@Kite9Item 
	public DiagramXMLElement planarizationDependencyGraph(final DiagramBuilder db) {
		ObjectDependencyWizard odw = new ObjectDependencyWizard(db, null);
		odw.setShowMethodReturnValues(true);
		odw.setValueFilter(db.and(
				db.onlyInModel(Planarization.class.getPackage()), 
				db.not(db.only(Tools.class))));
		odw.show(new MGTPlanarizer());
		return db.getDiagram();
		
	}
	
}

