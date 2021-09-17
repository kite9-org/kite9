package org.kite9.diagram.functional.layout;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.LinkBody;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.w3c.dom.Element;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.testing.DiagramElementVisitor;
import org.kite9.diagram.testing.VisitorAction;

public class Test9CompactionTests extends AbstractLayoutFunctionalTest {


	@Test
	public void test_9_1_GlyphsLengthsAreMinimal1() throws Exception {
		Glyph one = new Glyph("", "some stupidly long named glyph", null, null);
		Glyph two = new Glyph("", "two", null, null);
		Glyph three = new Glyph("", "three", null, null);

		LinkBody b = new LinkBody("drinks");
		LinkBody c = new LinkBody("thinks");
		
		new Link(b, one, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(b, two);
		new Link(c, three);
		new Link(c, one, null, null, LinkEndStyle.ARROW, null, Direction.UP);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList( one, b, two, c, three), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_9_2_ConstrainedBox2() throws Exception {
		Glyph tl = new Glyph("TL","", "TL", null, null);
		Glyph br = new Glyph("BR","", "BR", null, null);
		Glyph tr = new Glyph("TR","", "TR", null, null);
		Glyph bl = new Glyph("BL","", "BL", null, null);

		LinkBody top = new LinkBody("t", "top");
		LinkBody left = new LinkBody("l","left");
		LinkBody right = new LinkBody("r","right");
		LinkBody bottom = new LinkBody("b", "bottom");

		new Link(top, tl, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(top, tr, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(bottom, bl, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(bottom, br, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);

		new Link(left, tl, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(left, bl, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link( right, tr, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(right, br, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(tl, tr, bl, br, top,
				left, bottom, right), null);

		renderDiagram(d);
	}
	
	@Test
	public void test_9_3_SeparateDirectedArrows2() throws Exception {
		Glyph one = new Glyph("", "One", null, null);
		Glyph two = new Glyph("", "Two", null, null);
		Glyph three = new Glyph("", "Three", null, null);
		Glyph four = new Glyph("", "Four", null, null);

		LinkBody meets = new LinkBody("meets");
		LinkBody sees = new LinkBody("sees");
		LinkBody asks = new LinkBody("asks");
		LinkBody looks = new LinkBody("looks");

		new Link(meets, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(sees, two, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link( asks, three, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(looks, four, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList( one, two, meets,
				sees, asks, looks, three, four), null);

		renderDiagram(d);
	}
	
	@Test
    public void test_9_4_HierarchicalContainers() throws Exception {
		Element one = new Glyph("Stereo", "one", null, null);
		Element two = new Glyph("Stereo", "two", null, null);
		Element con1 = new Context("b1", createList(one), true, null, null);
		Element con2 = new Context("b2", createList(two), true, null, null);
		Element con3 = new Context("b3", createList(con1, con2), true, null, null);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram",createList(con3), null);
		renderDiagram(d);
    }
	
	@Test
	@Ignore("This worked by chance before - CenteringAligner doesn't work properly without layout.")
    public void test_9_5_CompactionAlignmentRight() throws Exception {
		createDiagramInDirection(Direction.RIGHT);
	}
	
	@Test
    public void test_9_6_CompactionAlignmentLeft() throws Exception {
		createDiagramInDirection(Direction.LEFT);
	}
	
	@Test
    public void test_9_7_CompactionAlignmentDown() throws Exception {
		createDiagramInDirection(Direction.DOWN);
	}
	
	@Test
    public void test_9_8_CompactionAlignmentUp() throws Exception {
		createDiagramInDirection(Direction.UP);
	}
	

	@Test
    public void test_9_9_CheckHorizontalEdgeShortening() throws Exception {
		Glyph one = new Glyph("", "One", null, null);
		Glyph two = new Glyph("", "Two", null, null);
		Glyph three = new Glyph("", "Three", null, null);
		Glyph four = new Glyph("", "Four - this one has a really long label to make compaction harder", null, null);

		new Link(one, two, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(two, three, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(three, four, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new TurnLink(one, three);
		new TurnLink(one, three);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram",createList(one, two, three, four), null);
		renderDiagram(d);

	}

	private void createDiagramInDirection(Direction d) throws Exception {
		Glyph one = new Glyph("one", "", "One", null, null);
		Glyph two = new Glyph("two", "", "Two", null, null);
		Glyph three = new Glyph("three", "", "Three", null, null);
		Glyph four = new Glyph("four", "", "Four", null, null);
	
		new Link(one, two, null, null, null, null,d);
		new Link(two, three, null, null, null, null, d);
		new Link(one, four, null, null, null, null, d);
		
		
		Element d1 = new DiagramKite9XMLElement("The Diagram",createList(one, two, three, four), null);
		renderDiagram(d1);

		new DiagramElementVisitor().visit(Kite9SVGTranscoder.lastDiagram, new VisitorAction() {
			
			@Override
			public void visit(DiagramElement de) {
				if (de instanceof Connection) {
					RouteRenderingInformation rri = ((Connection)de).getRenderingInformation();
					Dimension2D size = rri.getSize();
					if ((size.getW() > 80) || (size.getH() > 80)) {
						throw new TestingEngine.LayoutErrorException("Link too long: "+de);
					}
				}
			}
		});
	}
}
