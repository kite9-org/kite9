package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.Collections;
import java.util.Map.Entry;

import org.junit.Test;
import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.Context;
import org.kite9.diagram.xml.Diagram;
import org.kite9.diagram.xml.Glyph;
import org.kite9.framework.common.HelpMethods;

public class Test17TwoContainerLinking extends AbstractFunctionalTest {

    @Test
    public void test_17_1_NoOrdering() throws IOException {
    	for (Entry<Object, Object> e : System.getProperties().entrySet()) {
			System.out.println(e.getKey()+"="+e.getValue());
		}
    	testContainers(null, null, null, null, null);
    }

    public void testContainers(Layout c1, Layout c2, Direction l1, Direction l2, Layout c3) throws IOException {
    	Glyph one = new Glyph("a", "Stereo", "a", null, null);
    	Glyph two = new Glyph("b", "Stereo", "b", null, null);
    	Glyph three = new Glyph("c", "Stereo", "c", null, null);
    	
    	Context con1 = new Context("b1", createList((Contained) one, (Contained) two, (Contained) three), true, null, c1);
    	
    	Glyph four = new Glyph("d", "Stereo", "d", null, null);
    	Glyph five = new Glyph("e", "Stereo", "e", null, null);
    	Glyph six = new Glyph("f", "Stereo", "f", null, null);
    	
    	new TurnLink(one, four, null, null, null, null, l1);
    	new TurnLink(two, five, null, null, null, null, l2);
    	
    	Context con2 = new Context("b2", createList((Contained) four, (Contained) five, (Contained) six), true, null, c2);
    
    	Context con3 = new Context("b3", createList(con1, con2), true, null, c3);
    
    	Diagram d = new Diagram("The Diagram", createList(con3), null);
    	renderDiagram(d);
    }

    @Test
    public void test_17_2_LinkedHorizontally() throws IOException {
    	testContainers(Layout.RIGHT, Layout.RIGHT, null, null, null);
    }

    @Test
    public void test_17_3_LinkedDifferentDirection() throws IOException {
    	testContainers(Layout.LEFT, Layout.UP, null, null, null);
    }
    
    @Test
    public void test_17_4_DirectedArrows() throws IOException {
    	testContainers(null, null, Direction.UP, Direction.UP, null);
    }
    
    @Test
    public void test_17_5_AllDirected() throws IOException {
    	testContainers(Layout.RIGHT, Layout.RIGHT, Direction.UP, Direction.UP, null);
    }
    
    @Test
    public void test_17_6_HorizontalDirection() throws IOException {
    	testContainers(Layout.HORIZONTAL, Layout.HORIZONTAL, null, null, null);
    }
    
    @Test
    public void test_17_7_VerticalDirection() throws IOException {
    	testContainers(Layout.VERTICAL, Layout.VERTICAL, null, null, null);
    }
    
    @Test
    public void test_17_8_MixedPlanarDirection() throws IOException {
    	testContainers(Layout.VERTICAL, Layout.HORIZONTAL, null, null, null);
    }
    
    @Test
    public void test_17_9_HorizontalDirection() throws IOException {
    	testContainers(Layout.RIGHT, Layout.RIGHT, null, null, Layout.DOWN);
    }
    
    @Test
    public void test_17_10_VerticalDirection() throws IOException {
    	testContainers(Layout.UP, Layout.UP, null, null, Layout.RIGHT);
    }
    
    @Test
    public void test_17_11_SameDirection() throws IOException {
    	testContainers(Layout.RIGHT, Layout.RIGHT, null, null, Layout.RIGHT);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void test_17_12_LinkedDifferentDirection() throws IOException {
    	Context i1 = new Context("i1", Collections.EMPTY_LIST, true, null, Layout.DOWN);
    	Context i2 = new Context("i2", Collections.EMPTY_LIST, true, null, Layout.DOWN);
    	Context i3 = new Context("i3", Collections.EMPTY_LIST, true, null, Layout.DOWN);
    	Context i4 = new Context("i4", Collections.EMPTY_LIST, true, null, Layout.DOWN);
    	
    	Context outer = new Context("outer", HelpMethods.listOf(i1, i2, i3, i4), true, null, Layout.RIGHT);
    	Diagram d= new Diagram(HelpMethods.listOf(outer), null);
    	renderDiagram(d);
    }
    

}
