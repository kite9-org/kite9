package org.kite9.diagram.functional.thorough;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.GraphConstructionTools;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.ContradictingLink;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.batik.bridge.Kite9DiagramBridge;
import org.kite9.diagram.functional.layout.Test36LayoutChoices;
import org.kite9.diagram.functional.layout.TestingEngine.ElementsMissingException;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.xml.DiagramKite9XMLElement;
import org.kite9.framework.xml.LinkEndStyle;
import org.kite9.framework.xml.LinkLineStyle;
import org.kite9.framework.xml.Kite9XMLElement;
import org.w3c.dom.Element;

import junit.framework.Assert;

public class Test33Contradictions extends AbstractLayoutFunctionalTest {

	@Test
	public void test_33_1_ContradictionHoriz() throws Exception {

		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1, g2), true, null, Layout.RIGHT);

		Link l = new ContradictingLink(g1, g2, null, null, null, null, Direction.LEFT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1), null);
		renderDiagram(d);

		assertContradicting(l);
	}

	private void assertContradicting(Link l) {
		Assert.assertTrue(isContradicting(l));
	}

	private boolean isContradicting(Kite9XMLElement l) {
		DiagramElement diagramElement = getOriginalElement(l);
		return ((RouteRenderingInformation) diagramElement.getRenderingInformation()).isContradicting();
	}

	private DiagramElement getOriginalElement(Kite9XMLElement l) {
		String id = l.getID();
		DiagramKite9XMLElement lastDiagram = Kite9DiagramBridge.lastDiagram;
		Element e = lastDiagram.getOwnerDocument().getElementById(id);
		Kite9XMLElement xe = (Kite9XMLElement) e;
		DiagramElement diagramElement = xe.getDiagramElement();
		return diagramElement;
	}

	@Test
	public void test_33_2_ContradictionHoriz2() throws Exception {

		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1, g2), true, null, Layout.RIGHT);
		Link l = new ContradictingLink(g1, g2, null, null, null, null, Direction.DOWN);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1), null);


		renderDiagram(d);
		assertContradicting(l);
	}

	@Test
	public void test_33_3_ContradictionVert() throws Exception {

		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1, g2), true, null, Layout.HORIZONTAL);
		Link l = new ContradictingLink(g1, g2, null, null, null, null, Direction.DOWN);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1), null);

		renderDiagram(d);
		assertContradicting(l);
	}

	@Test
	public void test_33_4_ContradictionVert2() throws Exception {

		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1, g2), true, null, Layout.VERTICAL);
		Link l = new ContradictingLink(g1, g2, null, null, null, null, Direction.RIGHT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1), null);

		renderDiagram(d);
		assertContradicting(l);
	}

	@Test
	public void test_33_5_ContradictionLoop() throws Exception {

		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);
		Glyph g3 = new Glyph("3", null, "3", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1, g2, g3), true, null, null);

		Link l1 = new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		Link l2 = new ContradictingLink(g2, g3, null, null, null, null, Direction.RIGHT);
		Link l3 = new ContradictingLink(g3, g1, null, null, null, null, Direction.RIGHT);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1), null);

		renderDiagram(d);
		int count = 0;
		count = count + (isContradicting(l1) ? 1 : 0);
		count = count + (isContradicting(l2) ? 1 : 0);
		count = count + (isContradicting(l3) ? 1 : 0);

		Assert.assertTrue(count > 0);

	}

	@Test
	public void test_33_6_ContradictionLoop2() throws Exception {

		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);
		Glyph g3 = new Glyph("3", null, "3", null, null);
		Glyph g4 = new Glyph("4", null, "4", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1, g2, g3, g4), true, null, Layout.RIGHT);

		Link l = new ContradictingLink(g1, g3, null, null, null, null, Direction.DOWN);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1), null);

		renderDiagram(d);

		assertContradicting(l);

	}

	@Test
	public void test_33_7_ContradictionHoriz3() throws Exception {

		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);
		Glyph g3 = new Glyph("3", null, "3", null, null);
		Glyph g4 = new Glyph("4", null, "4", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1, g2, g3, g4), true, null, Layout.RIGHT);
		Link l1 = new Link(g1, g3, null, null, null, null, Direction.RIGHT);
		Link l2 = new Link(g1, g4, null, null, null, null, Direction.RIGHT);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1), null);

		renderDiagram(d);
		Assert.assertFalse(isContradicting(l1));
		Assert.assertFalse(isContradicting(l2));

	}

	@Test
	@Ignore
	public void test_33_8_HierarchyContradiction() throws Exception {
		try {
			generate("hierarchy_contradiction.xml");
		} catch (ElementsMissingException e) {
			Assert.assertEquals(1, e.getCountOfMissingElements());
			return;
		}

		throw new LogicException("Was expecting missing attr");
	}

	@Test
	public void test_33_9_HierarchyContradiction2() throws Exception {
		try {
			generate("hierarchy_contradiction2.xml");
		} catch (ElementsMissingException e) {
			Assert.assertEquals(1, e.getCountOfMissingElements());
			return;
		}

		throw new LogicException("Was expecting missing attr");

	}

	@Test
	public void test_33_10_EdgeContradictsContainer1() throws Exception {
		Glyph g1 = new Glyph("0", null, "0", null, null);
		Glyph g2 = new Glyph("1", null, "1", null, null);
		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1), true, null, Layout.DOWN);
		Link l1 = new ContradictingLink(g1, g2, null, null, null, null, Direction.RIGHT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1, g2), Layout.DOWN, null);
		renderDiagram(d);
		assertContradicting(l1);
	}

	@Test
	public void test_33_11_EdgeContradictsContainer2() throws Exception {
		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);
		Glyph g3 = new Glyph("3", null, "3", null, null);
		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1), true, null, Layout.DOWN);
		Link l1 = new ContradictingLink(g1, g2, null, null, null, null, Direction.RIGHT);
		Link l2 = new ContradictingLink(g1, g3, null, null, null, null, Direction.UP);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1, g2, g3), Layout.VERTICAL, null);

		renderDiagram(d);
		Assert.assertTrue(isContradicting(l1));
		Assert.assertTrue(!isContradicting(l2));
	}

	@Test
	public void test_33_12_ContradictionHoriz4() throws Exception {

		Glyph g0 = new Glyph("0", null, "0", null, null);
		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);
		Glyph g3 = new Glyph("3", null, "3", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g0, g1, g2, g3), true, null,
				Layout.HORIZONTAL);

		Link l1 = new ContradictingLink(g0, g2, null, null, null, null, Direction.DOWN);
		Link l2 = new ContradictingLink(g0, g3, null, null, null, null, Direction.DOWN);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1), null);

		renderDiagram(d);
		assertContradicting(l1);
		assertContradicting(l2);
		
	}

	@Test
	public void test_33_13_ContradictingRectangularContext1() throws Exception {
		Glyph o2 = new Glyph("o2", null, "o2", null, null);
		Glyph i1 = new Glyph("i1", null, "i1", null, null);
		Glyph i2 = new Glyph("i2", null, "i2", null, null);
		Glyph i3 = new Glyph("i3", null, "i3", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) i1, i2, i3), true, null, null);
		Link l2 = new Link(o2, i3, null, null, null, null, Direction.DOWN);
		Link l3 = new Link(i1, o2, null, null, null, null, Direction.RIGHT);
		Link l4 = new Link(i1, i2, null, null, null, null, Direction.DOWN);
		Link l5 = new Link(i2, i3, null, null, null, null, Direction.RIGHT);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1, o2), null);


		renderDiagram(d);
		
		int contCount = 0;
		contCount = contCount + (isContradicting(l2) ? 1 :0);
		contCount = contCount + (isContradicting(l3) ? 1 :0);
		contCount = contCount + (isContradicting(l4) ? 1 :0);
		contCount = contCount + (isContradicting(l5) ? 1 :0);
		
		Assert.assertTrue(contCount <=2);
	}

	@Test
	public void test_33_14_ContextInternalContradiction() throws Exception {
		Glyph o1 = new Glyph("o1", null, "o1", null, null);
		Glyph o2 = new Glyph("o2", null, "o2", null, null);
		Glyph i1 = new Glyph("i1", null, "i1", null, null);
		Glyph i2 = new Glyph("i2", null, "i2", null, null);
		Glyph i3 = new Glyph("i3", null, "i3", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) o1, o2), true, null, null);
		Link l2 = new Link(o2, i2, null, null, null, null, Direction.UP);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1, i1, i2, i3), Layout.DOWN, null);


		renderDiagram(d);

		// Assert.assertTrue(l2.getRenderingInformation().isContradicting());
		// Assert.assertTrue(l3.getRenderingInformation().isContradicting());
		// Assert.assertFalse(l4.getRenderingInformation().isContradicting());
		// Assert.assertFalse(l5.getRenderingInformation().isContradicting());
	}

	@Override
	protected boolean checkDiagramSize() {
		return false;
	}

	@Test
	public void test_33_15_TooManyContradictions() throws Exception {
		generate("too_many_contradictions.xml");
	}

	@Test
	public void test_33_16_UseCaseIssue() throws Exception {
		List<Kite9XMLElement> contents = new ArrayList<Kite9XMLElement>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 4, 4, contents, null);

		Context top = new Context("top", listOf(contents.get(0), contents.get(1)), true, null, Layout.HORIZONTAL);
		Context bottom = new Context("bottom", listOf(contents.get(2), contents.get(3)), true, null, Layout.HORIZONTAL);
		List<Kite9XMLElement> out2 = new ArrayList<Kite9XMLElement>();
		out2.add(top);
		out2.add(bottom);

		new Link(out[0][0], out[0][1], null, null, null, null, Direction.DOWN);
		new Link(out[0][0], out[0][2], null, null, null, null, Direction.DOWN);
		new Link(out[0][1], out[2][0], null, null, null, null, Direction.RIGHT);
		new Link(out[0][1], out[2][1], null, null, null, null, Direction.RIGHT);
		new Link(out[0][1], out[2][2], null, null, null, null, Direction.RIGHT);
		new Link(out[0][2], out[2][2], null, null, null, null, Direction.RIGHT);

		new Link(out[2][2], out[2][3], null, null, null, null, Direction.RIGHT);
		new Link(out[2][1], out[2][3], null, null, null, null, Direction.RIGHT);
		new Link(out[2][0], out[2][3], null, null, null, null, Direction.RIGHT);

		new Link(out[2][0], out[3][0], null, null, null, null, Direction.RIGHT);
		new Link(out[2][1], out[3][1], null, null, null, null, Direction.RIGHT);
		new Link(out[2][2], out[3][1], null, null, null, null, Direction.RIGHT);

		new Link(out[3][0], out[3][2], null, null, null, null, Direction.RIGHT);
		new Link(out[3][1], out[3][2], null, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement("bob", out2, Layout.HORIZONTAL, null));

	}

	/**
	 * Groupwise planarizer doesn't allow you to bend the container, so this should throw an exception
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_33_17_ArrowBendingContainer() throws Exception {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);

		Kite9XMLElement con1 = new Context("b1", createList((Kite9XMLElement) one, two), true, null, null);

		Arrow a = new Arrow("links", "links");

		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(a, two, null, null, LinkEndStyle.ARROW, null, Direction.UP);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con1, a), null);
		renderDiagram(d);
	}

	/**
	 * Containers can't be bent, so this will bend the directed link instead.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_33_18_ArrowBendingContainer() throws Exception {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);

		Kite9XMLElement con1 = new Context("b1", createList((Kite9XMLElement) one, two), true, null, null);

		Arrow a = new Arrow("links", "links");

		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(a, two, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con1, a), null);
		renderDiagram(d);
	}

	@Test
	public void test_33_19_IllegalRoute1() throws Exception {
		List<Kite9XMLElement> contents = new ArrayList<Kite9XMLElement>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 4, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Kite9XMLElement> out2 = new ArrayList<Kite9XMLElement>();
		out2.add(overall);

		Link a = new ContradictingLink(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		Link b = new ContradictingLink(out[0][1], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		Link c = new ContradictingLink(out[0][2], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		Link d = new Link(out[0][3], out[0][0], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement(out2, null));
	}

	@Test
	public void test_33_20_IllegalRoute2() throws Exception {
		List<Kite9XMLElement> contents = new ArrayList<Kite9XMLElement>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 4, 1, contents, Layout.VERTICAL);

		Context overall = new Context("co", contents, true, null, null);
		List<Kite9XMLElement> out2 = new ArrayList<Kite9XMLElement>();
		out2.add(overall);

		Link a = new ContradictingLink(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		Link b = new Link(out[0][1], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		Link c = new ContradictingLink(out[0][2], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		Link d = new ContradictingLink(out[0][3], out[0][0], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}

	@Test
	public void test_33_21_BadRequest() throws Exception {
		generate("badrequest.xml");
	}

	@Test
	public void test_33_22_BretsError() throws Exception {
		generate("brets_error.xml");
	}

	@Test
	public void test_33_23_BentContainer() throws Exception {
		generate("bent_container.xml");
	}

	@Test
	public void test_33_24_NestedDirectedHorizontalContradiction() throws Exception {
		// this is an impossible merge, because g4 and g3 can't exist side-by-side
		renderDiagram(Test36LayoutChoices.doNestedDirected(Layout.RIGHT, Layout.HORIZONTAL, Layout.VERTICAL,
				Direction.UP));
	}

	@Test
	public void test_33_25_NestedDirectedVertical() throws Exception {
		renderDiagram(Test36LayoutChoices.doNestedDirected(Layout.UP, Layout.VERTICAL, Layout.HORIZONTAL,
				Direction.RIGHT));
	}

	@Test
	@Ignore // too many attr to work
	public void test_33_27_30KElements() throws Exception {
		generate("30k.xml");
	}

	@Test
	public void test_33_28_ADLHierarchyStrict() throws Exception {
		Kite9Log.setLogging(false);
		generate("ContradictingADLClassHierarchy.xml");
	}

	@Test
	public void test_33_29_IllegalConnection() throws Exception {
		try {
			Glyph one = new Glyph("one", "", "one", null, null);
			Glyph two = new Glyph("two", "", "two", null, null);

			Kite9XMLElement con1 = new Context("b1", createList((Kite9XMLElement) one, two), true, null, null);

			Arrow a = new Arrow("links", "links");

			new Link(one, con1);
			new Link(a, two, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);

			DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con1, a), null);
			renderDiagram(d);
		} catch (ElementsMissingException e) {
			Assert.assertEquals(1, e.getCountOfMissingElements());
			return;
		}

		throw new LogicException("Was expecting missing attr");

	}

	@Test
	public void test_33_30_ADLHierarchy() throws Exception {
		generate("ADLClassHierarchy.xml");
	}
	
	@Test
	public void test_33_31_ADLHierarchyNoBorder() throws Exception {
		generate("ADLClassHierarchyNoBorder.xml");
	}
	
	@Test
	public void test_33_32_ADLHierarchyStrictNoBorder() throws Exception {
		generate("ContradictingADLClassHierarchyNoBorder.xml");
	}
	
//	@Ignore("Broken in sprint 7")
	@Test
	public void test_33_33_LayoutDirection() throws Exception {
		generate("layout_direction.xml");
	}
	
	@Test
	public void test_33_34_CouldntEstablishDirection() throws Exception {
		generate("couldnt_establish_direction.xml");
	}
	
	@Test
	public void test_33_35_RankedContradictionLoop() throws Exception {

		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);
		Glyph g3 = new Glyph("3", null, "3", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1, g2, g3), true, null, null);

		Link l1 = new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		Link l2 = new ContradictingLink(g2, g3, null, null, null, null, Direction.RIGHT);
		Link l3 = new ContradictingLink(g3, g1, null, null, null, null, Direction.RIGHT);

		l1.setRank(5);
		l2.setRank(10);
		l3.setRank(15);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1), null);
		
		renderDiagram(d);
		assertContradicting(l1);

	}
	
	@Test
	public void test_33_36_ContradictingLastLink() throws Exception {
		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);
		Glyph g3 = new Glyph("3", null, "3", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1, g2), true, null, null);


		Link l1 = new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		Link l2 = new Link(g2, g3, null, null, null, null, Direction.RIGHT);
		Link l3 = new ContradictingLink(g1, g3, null, null, null, null, Direction.UP);

		l1.setRank(5);
		l2.setRank(10);
		l3.setRank(15);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1, g3), null);

		renderDiagram(d);
		assertContradicting(l1);
	}
	
	
	@Test
	public void test_33_38_AssertInvisibleContradictionsNotRendered() throws Exception {

		Glyph g1 = new Glyph("1", null, "1", null, null);
		Glyph g2 = new Glyph("2", null, "2", null, null);

		Context c1 = new Context("c1", HelpMethods.createList((Kite9XMLElement) g1, g2), true, null, Layout.RIGHT);


		Link l = new Link(g1, g2, null, null, null, null, Direction.LEFT);
		l.setShapeName(LinkLineStyle.INVISIBLE);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d1", HelpMethods.listOf(c1), null);
		renderDiagram(d);

		Assert.assertFalse("rendered, shouldn't be", getOriginalElement(l).getRenderingInformation().isRendered());

	}

	@Override
	protected boolean checkEdgeDirections() {
		return true;
	}

	@Override
	protected boolean checkNoHops() {
		return false;
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Override
	protected boolean checkNoContradictions() {
		return false;
	}
	
	@Override
	protected boolean checkMidConnections() {
		return false;
	}
}
