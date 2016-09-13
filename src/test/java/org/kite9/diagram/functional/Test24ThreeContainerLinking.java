package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.Map.Entry;

import org.junit.Test;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;

public class Test24ThreeContainerLinking extends AbstractFunctionalTest {

    @Test
    public void test_24_1_NoOrdering() throws IOException {
    	for (Entry<Object, Object> e : System.getProperties().entrySet()) {
			System.out.println(e.getKey()+"="+e.getValue());
		}
    	testContainers(null, null, null, null, null, null);
    }

    public void testContainers(Layout c1, Layout c2, Layout c3, Direction l1, Direction l2, Layout c4) throws IOException {
    	Glyph one = new Glyph("a", "Stereo", "a", null, null);
    	Glyph two = new Glyph("b", "Stereo", "b", null, null);
    	Glyph three = new Glyph("c", "Stereo", "c", null, null);
    	
    	Context con1 = new Context("b1", createList(one, two,three), true, null, c1);
    	
    	Glyph four = new Glyph("d", "Stereo", "d", null, null);
    	Glyph five = new Glyph("e", "Stereo", "e", null, null);
    	Glyph six = new Glyph("f", "Stereo", "f", null, null);
    	
    	Context con2 = new Context("b2", createList(four, five, six), true, null, c2);
        
    	Glyph seven = new Glyph("g", "Stereo", "g", null, null);
    	Glyph eight = new Glyph("h", "Stereo", "h", null, null);
    	Glyph nine = new Glyph("i", "Stereo", "i", null, null);
    	
    	Context con3 = new Context("b3", createList( seven, eight,  nine), true, null, c3);

       	
       	new TurnLink(one, eight, null, null, null, null, l1);
    	new TurnLink(two, nine, null, null, null, null, l2);
    	
    
    	Context con4 = new Context("b4", createList(con1, con2, con3), true, null, c4);
    
    	DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con4), null);
    	renderDiagram(d);
    }

    @Test
    public void test_24_2_LinkedHorizontally() throws IOException {
    	testContainers(Layout.RIGHT, Layout.RIGHT, Layout.RIGHT, null, null, null);
    }

    @Test
    public void test_24_3_LinkedDifferentDirection() throws IOException {
    	testContainers(Layout.LEFT, Layout.LEFT, Layout.UP, null, null, null);
    }
    
    @Test
    public void test_24_4_DirectedArrows() throws IOException {
    	testContainers(null, null, null, Direction.UP, Direction.UP, null);
    }
    
    @Test
    public void test_24_5_AllDirected() throws IOException {
    	testContainers(Layout.RIGHT, Layout.RIGHT, Layout.RIGHT, Direction.UP, Direction.UP, null);
    }
    
    @Test
    public void test_24_6_HorizontalDirection() throws IOException {
    	testContainers(Layout.HORIZONTAL, Layout.HORIZONTAL, Layout.HORIZONTAL, null, null, null);
    }
    
    @Test
    public void test_24_7_VerticalDirection() throws IOException {
    	testContainers(Layout.VERTICAL, Layout.VERTICAL, Layout.VERTICAL, null, null, null);
    }
    
    @Test
    public void test_24_8_MixedPlanarDirection() throws IOException {
    	testContainers(Layout.VERTICAL, Layout.HORIZONTAL, Layout.HORIZONTAL, null, null, null);
    }
    
    @Test
    public void test_24_9_HorizontalDirection() throws IOException {
    	testContainers(Layout.RIGHT, Layout.RIGHT, Layout.RIGHT, null, null, Layout.DOWN);
    }
    
    @Test
    public void test_24_10_VerticalDirection() throws IOException {
    	testContainers(Layout.UP, Layout.UP, Layout.UP, null, null, Layout.RIGHT);
    }
    
    @Test
    public void test_24_11_SameDirection() throws IOException {
    	testContainers(Layout.RIGHT, Layout.RIGHT, Layout.RIGHT, null, null, Layout.RIGHT);
    }
    
    @Test
    public void test_24_12_LinkedDifferentReverseDirection() throws IOException {
    	testContainers(Layout.RIGHT, Layout.RIGHT, Layout.DOWN, null, null, null);
    }
    
    
}
