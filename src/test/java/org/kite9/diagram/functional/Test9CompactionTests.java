package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.XMLElement;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.framework.logging.LogicException;

public class Test9CompactionTests extends AbstractFunctionalTest {


	@Test
	public void test_9_1_GlyphsLengthsAreMinimal1() throws IOException {
		Glyph one = new Glyph("", "some stupidly long named glyph", null, null);
		Glyph two = new Glyph("", "two", null, null);
		Glyph three = new Glyph("", "three", null, null);

		Arrow b = new Arrow("drinks");
		Arrow c = new Arrow("thinks");
		
		new Link(b, one, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(b, two);
		new Link(c, three);
		new Link(c, one, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		
		Diagram d = new Diagram("The Diagram", createList((Contained) one, b, two, c, three), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_9_2_ConstrainedBox2() throws IOException {
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

		Diagram d = new Diagram("D", createList((Contained) tl, tr, bl, br, top,
				left, bottom, right), null);

		renderDiagram(d);
	}
	
	@Test
	public void test_9_3_SeparateDirectedArrows2() throws IOException {
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
		Diagram d = new Diagram("D", createList((Contained) one, two, meets,
				sees, asks, looks, three, four), null);

		renderDiagram(d);
	}
	
	@Test
    public void test_9_4_HierarchicalContainers() throws IOException {
		XMLElement one = new Glyph("Stereo", "one", null, null);
		XMLElement two = new Glyph("Stereo", "two", null, null);
		XMLElement con1 = new Context("b1", createList(one), true, null, null);
		XMLElement con2 = new Context("b2", createList(two), true, null, null);
		XMLElement con3 = new Context("b3", createList(con1, con2), true, null, null);
			
		Diagram d = new Diagram("The Diagram",createList(con3), null);
		renderDiagram(d);
    }
	
	@Test
	@Ignore
    public void test_9_5_CompactionAlignmentRight() throws IOException {
		createDiagramInDirection(Direction.RIGHT);
	}
	
	@Test
	@Ignore
    public void test_9_6_CompactionAlignmentLeft() throws IOException {
		createDiagramInDirection(Direction.LEFT);
	}
	
	@Test
	@Ignore

    public void test_9_7_CompactionAlignmentDown() throws IOException {
		createDiagramInDirection(Direction.DOWN);
	}
	
	@Test
	@Ignore

    public void test_9_5_CompactionAlignmentUp() throws IOException {
		createDiagramInDirection(Direction.UP);
	}

	private void createDiagramInDirection(Direction d) throws IOException {
		Glyph one = new Glyph("one", "", "One", null, null);
		Glyph two = new Glyph("two", "", "Two", null, null);
		Glyph three = new Glyph("three", "", "Three", null, null);
		Glyph four = new Glyph("four", "", "Four", null, null);
	
		new Link(one, two, null, null, null, null,d);
		new Link(two, three, null, null, null, null, d);
		new Link(one, four, null, null, null, null, d);
		
		
		Diagram d1 = new Diagram("The Diagram",createList((Contained) one, two, three, four), null);
		Diagram d2 = renderDiagram(d1);
		new DiagramElementVisitor().visit(d2, new VisitorAction() {
			
			@Override
			public void visit(DiagramElement de) {
				if (de instanceof Link) {
					RouteRenderingInformation rri = ((Link)de).getRenderingInformation();
					Dimension2D size = rri.getSize();
					if ((size.getWidth() > 20) || (size.getHeight() > 20)) {
						throw new LogicException("Link too long: "+de);
					}
				}
			}
		});
	}
}
