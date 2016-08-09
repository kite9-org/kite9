package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.Context;
import org.kite9.diagram.xml.Diagram;
import org.kite9.diagram.xml.Glyph;
import org.kite9.diagram.xml.Link;
import org.kite9.diagram.xml.LinkEndStyle;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.logging.Kite9Log;


public class Test25NLinkedContainers extends AbstractFunctionalTest {

	
	
	public void testGrid(int glyphs, int containers, Layout withinContainer, Layout overallL, Direction d) throws IOException {
		Random r = new Random(101);
		List<XMLElement> contents = new ArrayList<XMLElement>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", glyphs, containers, contents, withinContainer);
		
		for (int i = 0; i < glyphs; i++) {
			for (int j = 0; j < containers-1; j++) {
				new Link(out[j][i],out[j+1][i], null, null, LinkEndStyle.ARROW, null, d);
			}
		}
		
		for (XMLElement c : contents) {
			Collections.shuffle(((Context)c).getContents(), r);
		}
		
		Context overall = new Context("co", contents, true, null, overallL);
		List<XMLElement> out2 = new ArrayList<XMLElement>();
		out2.add(overall);
		
		renderDiagram(new Diagram(out2, null));
	}
	
	@Test
	public void test_25_1_6Glyphs3LinkedContainersHVNull() throws IOException {
		testGrid(6, 3, Layout.HORIZONTAL, Layout.VERTICAL, null);
	}
	

	@Test
	public void test_25_2_6Glyphs3LinkedContainersVHNull() throws IOException {
		testGrid(6, 3, Layout.VERTICAL, Layout.HORIZONTAL, null);
	}
	
	@Test
	public void test_25_3_3Glyphs3LinkedContainersHVDown() throws IOException {
		testGrid(3, 3, Layout.HORIZONTAL, Layout.VERTICAL, Direction.DOWN);
	}

	@Test
	public void test_25_4_6Glyphs3LinkedContainersVHRight() throws IOException {
		testGrid(6, 3, Layout.VERTICAL, Layout.HORIZONTAL, Direction.RIGHT);
	}
	
	@Test
	public void test_25_5_TwoFourLinkedContainersV() throws IOException {
		testGrid(2, 3, Layout.HORIZONTAL, null, null);		
	}
	
	@Test
	public void test_25_6_TwoSevenLinkedContainers() throws IOException {
		testGrid(2, 15, Layout.HORIZONTAL, null, null);		
	}
	
	@Test
	public void test_25_7_10Glyphs3LinkedContainersHVNull() throws IOException {
		testGrid(10, 3, Layout.HORIZONTAL, Layout.VERTICAL, null);
	}
	
	@Test
	public void test_25_8_6Glyphs3LinkedContainersNUllNUllRight() throws IOException {
		testGrid(6, 3, null, null, Direction.RIGHT);
	}
	
	@Test
	public void test_25_9_TwoFourLinkedContainersH() throws IOException {
		testGrid(2, 3, Layout.VERTICAL, null, null);		
	}
	
	@Test
	public void test_25_10_30Glyphs3LinkedContainersPartialRightHV() throws IOException {
		Kite9Log.setLogging(false);
		Random r = new Random(101);
		int n = 30;
		List<XMLElement> contents = new ArrayList<XMLElement>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", n, 3, contents, Layout.HORIZONTAL);
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 2; j++) {
				new Link(out[j][i],out[j+1][i], null, null, LinkEndStyle.ARROW, null, null);
			}
		}
		
		((Context)contents.get(0)).setLayoutDirection(Layout.RIGHT);
		
		for (XMLElement c : contents) {
			Collections.shuffle(((Context)c).getContents(), r);
		}
		
		Context overall = new Context("co", contents, true, null, Layout.VERTICAL);
		List<XMLElement> out2 = new ArrayList<XMLElement>();
		out2.add(overall);
		
		renderDiagram(new Diagram(out2, null));
	}
}
