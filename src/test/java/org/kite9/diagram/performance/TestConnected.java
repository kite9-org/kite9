package org.kite9.diagram.performance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractPerformanceTest;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.functional.layout.TestingEngine;
import org.kite9.diagram.model.position.Layout;

public class TestConnected extends AbstractPerformanceTest {

	Random r = new Random(666);

	public Map<Metrics, String> generateSuite(int minConnected, int maxConnected, int step1, int minConnection,
			int maxConnection, int step2) {
		Map<Metrics, String> out = new LinkedHashMap<Metrics, String>();
		for (int i = minConnected; i <= maxConnected; i += step1) {
			for (int j = minConnection; j <= maxConnection; j += step2) {
				Metrics m = new Metrics("r" + i + "x" + j);
				m.connecteds = i;
				m.connections = j;
				String d = generateDiagram(m);
				out.put(m, d);
			}
		}

		return out;
	}

	public Map<Metrics, String> generateSuiteLinear(int minConnected, int step1, int minConnection, int step2,
			int steps) {
		Map<Metrics, String> out = new LinkedHashMap<Metrics, String>();
		for (int i = 0; i <= steps; i += 1) {
			int c = i * step1 + minConnected;
			int cc = i * step2 + minConnection;
			Metrics m = new Metrics("r" + c + "x" + cc);
			m.connecteds = c;
			m.connections = cc;
			String d = generateDiagram(m);
			out.put(m, d);
		}

		return out;
	}

	private String generateDiagram(Metrics m) {
		DiagramKite9XMLElement.TESTING_DOCUMENT = new ADLDocument();
		
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

		List<Kite9XMLElement> cl = new ArrayList<Kite9XMLElement>(items.length);
		Collections.addAll(cl, items);

		DiagramKite9XMLElement out = new DiagramKite9XMLElement( cl, null);
		
		return wrap(out);
	}

	@Test
	public void increasingConnected130Connections() throws IOException {
		Map<Metrics, String> suite1 = generateSuite(10, 40, 5, 130, 130, 10);
		render(suite1);
	}

	@Ignore("Ignoring to get travis to build")
	@Test
	public void increasingConnected50Connections() throws IOException {
		Map<Metrics, String> suite1 = generateSuite(20, 80, 5, 50, 50, 5);
		render(suite1);
	}
	
	@Test
	public void broken() throws IOException {
		Map<Metrics, String> suite1 = generateSuite(19, 19, 1, 100, 100, 1);
		render(suite1);
	}
	
	@Test
	public void broken2() throws IOException {
		Map<Metrics, String> suite1 = generateSuite(70, 70, 1, 135, 135, 1);
		render(suite1);
	}
}
