package org.kite9.diagram.performance;

import org.junit.jupiter.api.Test;
import org.kite9.diagram.AbstractPerformanceTest;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.model.position.Direction;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.*;

public class TestHierarchy extends AbstractPerformanceTest {

	public Map<Metrics, String> generateSuite(int minConnected, int maxConnected, int step1, int size, Direction dir) {
		Map<Metrics, String> out = new LinkedHashMap<Metrics, String>();
		for (int i = minConnected; i <= maxConnected; i += step1) {
			Metrics m = new Metrics("hi" + i + "s" + size);
			m.connecteds = i;
			String d = generateDiagram(m, size, dir);
			out.put(m, d);

		}

		return out;
	}

	private String generateDiagram(Metrics m, int size, Direction d) {
		DiagramKite9XMLElement.TESTING_DOCUMENT = DiagramKite9XMLElement.newDocument();
		List<Element> allGlyphs = new ArrayList<Element>();
		Deque<Element> needChildren = new ArrayDeque<Element>();

		Glyph top = new Glyph("g" + allGlyphs.size(), null, "top", null, null);
		allGlyphs.add(top);
		needChildren.add(top);

		while (allGlyphs.size() < m.connecteds) {
			top = (Glyph) needChildren.removeFirst();
			createNewLevel(top, allGlyphs, m.connecteds, size, d, needChildren);
		}

		DiagramKite9XMLElement out = new DiagramKite9XMLElement(allGlyphs, null);
		return wrap(out);
	}

	private void createNewLevel(Glyph top, List<Element> allGlyphs, int connecteds, int size, Direction d,
			Deque<Element> needChildren) {
		List<Glyph> newOnes = new ArrayList<Glyph>();
		for (int i = 0; i < size; i++) {
			Glyph g = new Glyph("g" + allGlyphs.size(), null, "g" + allGlyphs.size(), null, null);
			newOnes.add(g);
			allGlyphs.add(g);
			new Link(top, g, null, null, null, null, d);
			needChildren.add(g);
		}

		needChildren.remove(top);
	}

	@Test
	public void downwardsBy3() throws IOException {
		Map<Metrics, String> suite1 = generateSuite(20, 50, 5, 3, Direction.DOWN);
		render(suite1);
	}

	@Test
	public void downwardsBy3_broken() throws IOException {
		Map<Metrics, String> suite1 = generateSuite(50, 50, 5, 3, Direction.DOWN);
		render(suite1);
	}

	@Test
	public void rightBy3() throws IOException {
		Map<Metrics, String> suite1 = generateSuite(20, 50, 5, 3, Direction.RIGHT);
		render(suite1);
	}

	@Test
	public void downwardsBy6() throws IOException {
		Map<Metrics, String> suite1 = generateSuite(20, 50, 5, 6, Direction.DOWN);
		render(suite1);
	}

	@Test
	public void rightBy6() throws IOException {
		Map<Metrics, String> suite1 = generateSuite(20, 50, 5, 6, Direction.RIGHT);
		render(suite1);
	}

}
