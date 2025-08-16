package org.kite9.diagram.performance;

import org.junit.jupiter.api.Test;
import org.kite9.diagram.AbstractPerformanceTest;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.model.position.Direction;
import org.w3c.dom.Element;

import java.util.*;

public class TestDirectedUndirectedMatrix extends AbstractPerformanceTest {

	public Map<Metrics, String> generateSuite(int minConnected, int maxConnected, int step1, int size) {
		Map<Metrics, String> out = new LinkedHashMap<Metrics, String>();
		for (int i = minConnected; i <= maxConnected; i += step1) {
			Metrics m = new Metrics("d+r" + i + "s" + size);
			m.connecteds = i;
			String d = generateDiagram(m, size);
			out.put(m, d);

		}

		return out;
	}

	private String generateDiagram(Metrics m, int size) {
		DiagramKite9XMLElement.TESTING_DOCUMENT = DiagramKite9XMLElement.newDocument();
		Random r = new Random(m.hashCode());

		Glyph[][] space = new Glyph[size][];
		for (int i = 0; i < space.length; i++) {
			space[i] = new Glyph[size];
		}

		Glyph[] items = new Glyph[m.connecteds];
		for (int i = 0; i < items.length; i++) {
			int x, y;
			do {
				x = r.nextInt(size);
				y = r.nextInt(size);
			} while (space[y][x] != null);

			Glyph g = new Glyph("x" + x + "y" + y, "", "x " + x + "y " + y, null, null);
			items[i] = g;
			space[y][x] = items[i];
		}

		int connections = 0;

		// join horiz
		for (int y = 0; y < size; y++) {
			Glyph current = null;
			for (int x = 0; x < size; x++) {
				if (space[y][x] != null) {
					if (current != null) {
						new Link(current, space[y][x], null, null, null, null, Direction.RIGHT);
						connections++;
					}
					current = space[y][x];
				}
			}
		}

		// join vert
		for (int x = 0; x < size; x++) {
			Glyph current = null;
			for (int y = 0; y < size; y++) {
				if (space[y][x] != null) {
					if (current != null) {
						new Link(current, space[y][x], null, null, null, null, Direction.DOWN);
						connections++;
					}
					current = space[y][x];
				}
			}
		}

		// random connections
		for (int i = 0; i < m.connecteds; i++) {
			Glyph from = items[r.nextInt(items.length)];
			Glyph to = items[r.nextInt(items.length)];
			if (from != to) {
				Link l = new Link(from, to);
				l.setAttribute("class", "dotted");
			}

		}

		List<Element> cl = new ArrayList<Element>(items.length);
		Collections.addAll(cl, items);
		m.connections = connections;

		DiagramKite9XMLElement out = new DiagramKite9XMLElement(cl, null);
		return wrap(out);
	}

	@Test
	public void increasingConnectedSize16() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(20, 50, 10, 16);
		render(suite1);
	}

	@Test
	public void increasingConnectedSize9() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(10, 40, 5, 9);
		render(suite1);
	}

	@Test
	public void brokenNudge() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(15, 15, 9, 9);
		render(suite1);
	}

	@Test
	public void directedMergeBroken() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(10, 10, 9, 9);
		render(suite1);
	}

	@Test
	public void unnecessaryContradictions() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(20, 50, 10, 16);
		render(suite1);
	}

}
