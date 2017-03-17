package org.kite9.diagram.performance;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kite9.diagram.AbstractPerformanceTest;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.functional.layout.TestingEngine;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.DiagramXMLElement;
import org.kite9.framework.xml.XMLElement;

public class TestHierarchy extends AbstractPerformanceTest {


	public Map<Metrics, DiagramXMLElement> generateSuite(int minConnected, int maxConnected, int step1, int size, Direction dir) {
		Map<Metrics, DiagramXMLElement> out = new LinkedHashMap<Metrics, DiagramXMLElement>();
		for (int i = minConnected; i <= maxConnected; i += step1) {
			Metrics m = new Metrics("hi" + i+" s "+size);
			m.connecteds = i;
			DiagramXMLElement d = generateDiagram(m, size, dir);
			out.put(m, d);

		}

		return out;
	}

	private DiagramXMLElement generateDiagram(Metrics m, int size, Direction d) {
		DiagramXMLElement.TESTING_DOCUMENT = new ADLDocument();
		List<XMLElement> allGlyphs = new ArrayList<XMLElement>();
		Deque<XMLElement> needChildren = new ArrayDeque<XMLElement>();
		
		Glyph top = new Glyph("g"+allGlyphs.size(), null, "top", null, null);
		allGlyphs.add(top);
		needChildren.add(top);
		
		while (allGlyphs.size() < m.connecteds) {
			top = (Glyph) needChildren.removeFirst();
			createNewLevel(top, allGlyphs, m.connecteds, size, d, needChildren);
		}

		DiagramXMLElement out = new DiagramXMLElement(allGlyphs, null);
		TestingEngine.setDesignerStylesheetReference(out);
		return out;
	}

	private void createNewLevel(Glyph top, List<XMLElement> allGlyphs, int connecteds, int size, Direction d, Deque<XMLElement> needChildren) {
		List<Glyph> newOnes = new ArrayList<Glyph>();
		for (int i = 0; i < size; i++) {
			Glyph g = new Glyph("g"+allGlyphs.size(), null, "g"+allGlyphs.size(), null, null);
			newOnes.add(g);
			allGlyphs.add(g);
			new Link(top, g,null, null, null, null, d);
			needChildren.add(g);
		}
		
		needChildren.remove(top);
	}

	@Test
	public void downwardsBy3() throws IOException {
		Map<Metrics, DiagramXMLElement> suite1 = generateSuite(20, 50, 5, 3, Direction.DOWN);
		render(suite1);
	}
	
	@Test
	public void downwardsBy3_broken() throws IOException {
		Map<Metrics, DiagramXMLElement> suite1 = generateSuite(50, 50, 5, 3, Direction.DOWN);
		render(suite1);
	}
	
	@Test
	public void rightBy3() throws IOException {
		Map<Metrics, DiagramXMLElement> suite1 = generateSuite(20, 50, 5, 3, Direction.RIGHT);
		render(suite1);
	}
	
	@Test
	public void downwardsBy6() throws IOException {
		Map<Metrics, DiagramXMLElement> suite1 = generateSuite(20, 50, 5, 6, Direction.DOWN);
		render(suite1);
	}
	
	@Test
	public void rightBy6() throws IOException {
		Map<Metrics, DiagramXMLElement> suite1 = generateSuite(20, 50, 5, 6, Direction.RIGHT);
		render(suite1);
	}
	
}
