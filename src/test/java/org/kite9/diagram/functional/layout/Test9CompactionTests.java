package org.kite9.diagram.functional.layout;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.functional.AbstractLayoutFunctionalTest;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.LinkEndStyle;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.logging.LogicException;

public class Test9CompactionTests extends AbstractLayoutFunctionalTest {


	@Test
	public void test_9_1_GlyphsLengthsAreMinimal1() throws Exception {
		Glyph one = new Glyph("", "some stupidly long named glyph", null, null);
		Glyph two = new Glyph("", "two", null, null);
		Glyph three = new Glyph("", "three", null, null);

		Arrow b = new Arrow("drinks");
		Arrow c = new Arrow("thinks");
		
		new Link(b, one, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(b, two);
		new Link(c, three);
		new Link(c, one, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList( one, b, two, c, three), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_9_2_ConstrainedBox2() throws Exception {
		Glyph tl = new Glyph("TL","", "TL", null, null);
		Glyph br = new Glyph("BR","", "BR", null, null);
		Glyph tr = new Glyph("TR","", "TR", null, null);
		Glyph bl = new Glyph("BL","", "BL", null, null);

		Arrow top = new Arrow("t", "top");
		Arrow left = new Arrow("l","left");
		Arrow right = new Arrow("r","right");
		Arrow bottom = new Arrow("b", "bottom");

		new Link(top, tl, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(top, tr, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(bottom, bl, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(bottom, br, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);

		new Link(left, tl, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(left, bl, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link( right, tr, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(right, br, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);

		DiagramXMLElement d = new DiagramXMLElement("D", createList(tl, tr, bl, br, top,
				left, bottom, right), null);

		renderDiagram(d);
	}
	
	@Test
	public void test_9_3_SeparateDirectedArrows2() throws Exception {
		Glyph one = new Glyph("", "One", null, null);
		Glyph two = new Glyph("", "Two", null, null);
		Glyph three = new Glyph("", "Three", null, null);
		Glyph four = new Glyph("", "Four", null, null);

		Arrow meets = new Arrow("meets");
		Arrow sees = new Arrow("sees");
		Arrow asks = new Arrow("asks");
		Arrow looks = new Arrow("looks");

		new Link(meets, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(sees, two, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link( asks, three, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(looks, four, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		DiagramXMLElement d = new DiagramXMLElement("D", createList( one, two, meets,
				sees, asks, looks, three, four), null);

		renderDiagram(d);
	}
	
	@Test
    public void test_9_4_HierarchicalContainers() throws Exception {
		XMLElement one = new Glyph("Stereo", "one", null, null);
		XMLElement two = new Glyph("Stereo", "two", null, null);
		XMLElement con1 = new Context("b1", createList(one), true, null, null);
		XMLElement con2 = new Context("b2", createList(two), true, null, null);
		XMLElement con3 = new Context("b3", createList(con1, con2), true, null, null);
			
		DiagramXMLElement d = new DiagramXMLElement("The Diagram",createList(con3), null);
		renderDiagram(d);
    }
	
	@Test
	@Ignore
    public void test_9_5_CompactionAlignmentRight() throws Exception {
		createDiagramInDirection(Direction.RIGHT);
	}
	
	@Test
	@Ignore
    public void test_9_6_CompactionAlignmentLeft() throws Exception {
		createDiagramInDirection(Direction.LEFT);
	}
	
	@Test
	@Ignore

    public void test_9_7_CompactionAlignmentDown() throws Exception {
		createDiagramInDirection(Direction.DOWN);
	}
	
	@Test
	@Ignore

    public void test_9_5_CompactionAlignmentUp() throws Exception {
		createDiagramInDirection(Direction.UP);
	}

	private void createDiagramInDirection(Direction d) throws Exception {
		Glyph one = new Glyph("one", "", "One", null, null);
		Glyph two = new Glyph("two", "", "Two", null, null);
		Glyph three = new Glyph("three", "", "Three", null, null);
		Glyph four = new Glyph("four", "", "Four", null, null);
	
		new Link(one, two, null, null, null, null,d);
		new Link(two, three, null, null, null, null, d);
		new Link(one, four, null, null, null, null, d);
		
		
		DiagramXMLElement d1 = new DiagramXMLElement("The Diagram",createList(one, two, three, four), null);
		DiagramXMLElement d2 = renderDiagram(d1);
		new DiagramElementVisitor().visit(d2.getDiagramElement(), new VisitorAction() {
			
			@Override
			public void visit(DiagramElement de) {
				if (de instanceof Connection) {
					RouteRenderingInformation rri = ((Connection)de).getRenderingInformation();
					Dimension2D size = rri.getSize();
					if ((size.getWidth() > 20) || (size.getHeight() > 20)) {
						throw new LogicException("Link too long: "+de);
					}
				}
			}
		});
	}
}
