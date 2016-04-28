package org.kite9.diagram.performance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.primitives.Contained;

public class TestConnected extends AbstractPerformanceTest {

	Random r = new Random(666);

	public Map<Metrics, Diagram> generateSuite(int minConnected, int maxConnected, int step1, int minConnection,
			int maxConnection, int step2) {
		Map<Metrics, Diagram> out = new LinkedHashMap<Metrics, Diagram>();
		for (int i = minConnected; i <= maxConnected; i += step1) {
			for (int j = minConnection; j <= maxConnection; j += step2) {
				Metrics m = new Metrics("r" + i + "x" + j);
				m.connecteds = i;
				m.connections = j;
				Diagram d = generateDiagram(m);
				out.put(m, d);
			}
		}

		return out;
	}

	public Map<Metrics, Diagram> generateSuiteLinear(int minConnected, int step1, int minConnection, int step2,
			int steps) {
		Map<Metrics, Diagram> out = new LinkedHashMap<Metrics, Diagram>();
		for (int i = 0; i <= steps; i += 1) {
			int c = i * step1 + minConnected;
			int cc = i * step2 + minConnection;
			Metrics m = new Metrics("r" + c + "x" + cc);
			m.connecteds = c;
			m.connections = cc;
			Diagram d = generateDiagram(m);
			out.put(m, d);
		}

		return out;
	}

	private Diagram generateDiagram(Metrics m) {
		r = new Random(m.toString().hashCode());
		Glyph[] items = new Glyph[m.connecteds];
		for (int i = 0; i < items.length; i++) {
			Glyph g = new Glyph("g" + i, "", "g" + i, null, null);
			items[i] = g;
		}

		int tc = 0;

		while (tc < m.connections) {
			int g1 = r.nextInt(items.length);
			int g2 = r.nextInt(items.length);

			if (g1 != g2) {
				Glyph g1g = items[g1];
				Glyph g2g = items[g2];
				//if (!g1g.isConnectedDirectlyTo(g2g)) {
				new Link(g1g, g2g);
				tc++;
			// }
			}
		}

		List<Contained> cl = new ArrayList<Contained>(items.length);
		Collections.addAll(cl, items);

		Diagram out = new Diagram(cl, null);
		return out;
	}

	@Test
	public void increasingConnected130Connections() throws IOException {
		Map<Metrics, Diagram> suite1 = generateSuite(10, 40, 5, 130, 130, 10);
		render(suite1);
	}

	@Test
	public void increasingConnected50Connections() throws IOException {
		Map<Metrics, Diagram> suite1 = generateSuite(20, 80, 5, 50, 50, 5);
		render(suite1);
	}
	
	@Test
	public void broken() throws IOException {
		Map<Metrics, Diagram> suite1 = generateSuite(19, 19, 1, 100, 100, 1);
		render(suite1);
	}
	
	@Test
	public void broken2() throws IOException {
		Map<Metrics, Diagram> suite1 = generateSuite(70, 70, 1, 135, 135, 1);
		render(suite1);
	}

	@Test
	public void broken3() throws IOException {
		Map<Metrics, Diagram> suite1 = generateSuite(10, 10, 10, 30, 30, 20);
		makeHorizontal(suite1);
		render(suite1);
	}

	@Test
	public void increasingConnected120ConnectionsHoriz() throws IOException {
		Map<Metrics, Diagram> suite1 = generateSuite(10, 40, 10, 30, 120, 20);
		makeHorizontal(suite1);
		render(suite1);
	}

	private void makeHorizontal(Map<Metrics, Diagram> suite1) {
		for (Diagram d : suite1.values()) {
			d.setLayoutDirection(Layout.HORIZONTAL);
		}
	}
}
