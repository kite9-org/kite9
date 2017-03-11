package org.kite9.diagram.performance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.functional.layout.TestingEngine;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.XMLElement;

public class TestDirectedMatrix extends AbstractPerformanceTest {


	public Map<Metrics, DiagramXMLElement> generateSuite(int minConnected, int maxConnected, int step1, int size) {
		Map<Metrics, DiagramXMLElement> out = new HashMap<Metrics, DiagramXMLElement>();
		for (int i = minConnected; i <= maxConnected; i += step1) {
			Metrics m = new Metrics("d-r" + i+" s "+size);
			m.connecteds = i;
			DiagramXMLElement d = generateDiagram(m, size);
			out.put(m, d);

		}

		return out;
	}

	private DiagramXMLElement generateDiagram(Metrics m, int size) {
		DiagramXMLElement.TESTING_DOCUMENT = new ADLDocument();
		Random r = new Random(666);

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

			Glyph g = new Glyph("x" + x+"y"+y, "", "x " + x+"y "+y, null, null);
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

		List<XMLElement> cl = new ArrayList<XMLElement>(items.length);
		Collections.addAll(cl, items);
		m.connections = connections;
		
		DiagramXMLElement out = new DiagramXMLElement(cl, null);
		TestingEngine.setDesignerStylesheetReference(out);
		return out;
	}

	@Test
	public void size12IncreasingConnecteds() throws IOException {
		Map<Metrics, DiagramXMLElement> suite1 = generateSuite(30, 90, 10, 12);
		render(suite1);
	}

	
	@Test
	public void size6IncreasingConnecteds() throws IOException {
		Map<Metrics, DiagramXMLElement> suite1 = generateSuite(15, 5, 5, 6);
		render(suite1);
	}
	

}
