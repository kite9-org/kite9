package org.kite9.diagram.article;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.builders.java.DiagramBuilder;
import org.kite9.diagram.visualization.display.java2d.style.sheets.Designer2012Stylesheet;
import org.kite9.diagram.visualization.display.java2d.style.sheets.OutlinerStylesheet;
import org.kite9.framework.alias.PropertyAliaser;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.model.ProjectModel;
import org.kite9.framework.model.ProjectModelImpl;
import org.kite9.java.examples.library.Book;

public class TestExamples extends AbstractJavaExamplesTest {

	
	
	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Override
	protected boolean checkDiagramSize() {
		return false;
	}

	/**
	 * @see http://www.kite9.com/content/class-hierarchy-npe
	 * @throws IOException
	 */
	@Test
	public void example_1_1_ClassHierarchy() throws IOException {
		renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_1_ClassHierarchy(), new Designer2012Stylesheet());
		renderDiagramNoWM(new org.kite9.java.examples.Examples().example_1_1_ClassHierarchy(), new Designer2012Stylesheet());
	}
	
	@Test
	public void example_1_3_FlowChart() throws IOException {
		DiagramBuilder db1 = new DiagramBuilder(new PropertyAliaser(), StackHelp.getAnnotatedMethod(Test.class), new ProjectModelImpl());
		renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_3_FlowChart(db1), new Designer2012Stylesheet());
		DiagramBuilder db2 = new DiagramBuilder(new PropertyAliaser(), StackHelp.getAnnotatedMethod(Test.class), new ProjectModelImpl());
		renderDiagramNoWM(new org.kite9.java.examples.Examples().example_1_3_FlowChart(db2), new Designer2012Stylesheet());
	}
	
	/**
	 * @see http://www.kite9.com/content/problem-building-nouns-example15usecases
	 */
	@Test
	public void example_1_5_UseCases() throws IOException {
		PropertyAliaser pa = new PropertyAliaser();
				
		ProjectModel pm = buildProjectModel(Book.class.getPackage().getName());
		
		DiagramBuilder db = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		// renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_5_UseCases(db), new Designer2012Stylesheet());
		renderDiagramNoWM(new org.kite9.java.examples.Examples().example_1_5_UseCases(db), new Designer2012Stylesheet());
	}
	
	

	@Test
	public void example_1_6_Packaging() throws IOException {
		PropertyAliaser pa = new PropertyAliaser();
		ProjectModel pm = buildProjectModel(Book.class.getPackage().getName());
		DiagramBuilder db1 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		DiagramBuilder db2 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_6_Packaging(db1), new Designer2012Stylesheet());
		renderDiagramNoWM(new org.kite9.java.examples.Examples().example_1_6_Packaging(db2), new Designer2012Stylesheet());
	}
	
	@Test
	public void example_1_7_ClassDependency() throws Exception {
		PropertyAliaser pa = new PropertyAliaser();
		ProjectModel pm = buildProjectModel(Book.class.getPackage().getName());
		DiagramBuilder db1 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		DiagramBuilder db2 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_7_ClassDependency(db1), new Designer2012Stylesheet());
		renderDiagramNoWM(new org.kite9.java.examples.Examples().example_1_7_ClassDependency(db2), new Designer2012Stylesheet());
	}
	
	@Test
	public void example_1_8_StateTransition() throws Exception {
		PropertyAliaser pa = new PropertyAliaser();
		ProjectModel pm = buildProjectModel(Book.class.getPackage().getName());
		DiagramBuilder db1 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		DiagramBuilder db2 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_8_StateTransition(db1), new Designer2012Stylesheet());
		renderDiagramNoWM(new org.kite9.java.examples.Examples().example_1_8_StateTransition(db2), new Designer2012Stylesheet());
	}
	
	@Test
	public void example_1_9_SequenceDiagramMethods() throws Exception {
		PropertyAliaser pa = new PropertyAliaser();
		ProjectModel pm = buildProjectModel(Book.class.getPackage().getName());
		DiagramBuilder db1 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		DiagramBuilder db2 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_9_SequenceDiagramMethods(db1), new OutlinerStylesheet());
		renderDiagramNoWM(new org.kite9.java.examples.Examples().example_1_9_SequenceDiagramMethods(db2), new Designer2012Stylesheet());
	
	}
	
	@Test
	public void example_1_10_SequenceDiagramClasses() throws Exception {
		PropertyAliaser pa = new PropertyAliaser();
		ProjectModel pm = buildProjectModel(Book.class.getPackage().getName());
		DiagramBuilder db1 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		DiagramBuilder db2 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_10_SequenceDiagramClasses(db1), new OutlinerStylesheet());
		renderDiagramNoWM(new org.kite9.java.examples.Examples().example_1_10_SequenceDiagramClasses(db2), new OutlinerStylesheet());
	}
	
	@Test
	public void example_1_11_MethodCallDiagramMethods() throws Exception {
		PropertyAliaser pa = new PropertyAliaser();
		ProjectModel pm = buildProjectModel(Book.class.getPackage().getName());
		DiagramBuilder db1 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		DiagramBuilder db2 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_11_MethodCallDiagramMethods(db1));
		renderDiagramNoWM(new org.kite9.java.examples.Examples().example_1_11_MethodCallDiagramMethods(db2), new Designer2012Stylesheet());
	}
	
	@Test
	public void example_1_12_MethodCallDiagramClasses() throws Exception {
		PropertyAliaser pa = new PropertyAliaser();
		ProjectModel pm = buildProjectModel(Book.class.getPackage().getName());
		DiagramBuilder db1 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		DiagramBuilder db2 = new DiagramBuilder(pa, StackHelp.getAnnotatedMethod(Test.class), pm);
		renderDiagramPDF(new org.kite9.java.examples.Examples().example_1_12_MethodCallDiagramClasses(db1), new Designer2012Stylesheet());
		renderDiagramNoWM(new org.kite9.java.examples.Examples().example_1_12_MethodCallDiagramClasses(db2), new Designer2012Stylesheet());
	}
	
	@Test
	public void example_1_13_ArchitectureDiagram() throws Exception {
		renderDiagramPDF(new TestJavaADLClasses().architecture(), new Designer2012Stylesheet());
		renderDiagramNoWM(new TestJavaADLClasses().architecture(), new Designer2012Stylesheet());
		
	}
}
