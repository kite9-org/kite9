package org.kite9.diagram.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.kite9.diagram.AbstractPerformanceTest;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.model.position.Layout;

public class TestContainers extends AbstractPerformanceTest {


	public Map<Metrics, String> generateSuite(int minConnected, int maxConnected, int step1, int minConnection,
			int maxConnection, int step2, int containers, Layout l) {
		Map<Metrics, String> out = new HashMap<Metrics, String>();
		for (int i = minConnected; i <= maxConnected; i += step1) {
			for (int j = minConnection; j <= maxConnection; j += step2) {
				Metrics m = new Metrics("c+" + containers + "r" + i + "x" + j);
				System.out.println("Generating Metrics: " + m);
				m.connecteds = i;
				m.connections = j;
				String d = generateDiagram(m, containers, l);
				out.put(m, d);
			}
		}

		return out;
	}

	private String generateDiagram(Metrics m, int containers, Layout l) {
		DiagramKite9XMLElement.TESTING_DOCUMENT = new ADLDocument();
		Random r = new Random(m.name.hashCode());

		System.out.println("Generating diagram for " + m);

		Glyph[] items = new Glyph[m.connecteds];

		for (int i = 0; i < items.length; i++) {
			Glyph g = new Glyph("g" + i, "", "g" + i, null, null);
			items[i] = g;
		}

		Context[] contexts = new Context[containers];

		for (int i = 0; i < contexts.length; i++) {
			contexts[i] = new Context("c" + i, new ArrayList<Kite9XMLElement>(), true, new TextLine("Context " + i), l);
		}

		for (int i = 0; i < items.length; i++) {
			int c = r.nextInt(containers);
			contexts[c].appendChild(items[i]);
			items[i].setID(items[i].getID()+"("+contexts[c].getID()+")");
		}

		int tc = 0;
		
		List<Kite9XMLElement> cl = new ArrayList<Kite9XMLElement>(items.length);
		Collections.addAll(cl, contexts);
		Set<String> connections = new HashSet<>();

		while (tc < m.connections) {
			int g1 = r.nextInt(items.length + contexts.length);
			int g2 = r.nextInt(items.length + contexts.length);

			if (g1 != g2) {
				String conId = Math.min(g1, g2)+"-"+Math.max(g1, g2);
				Kite9XMLElement g1g = (g1 < items.length) ? items[g1] : contexts[g1 - items.length];
				Kite9XMLElement g2g = (g2 < items.length) ? items[g2] : contexts[g2 - items.length];
				if (!connections.contains(conId) && checkContainment(g1g, g2g) && checkContainment(g2g, g1g)) {
					new Link(g1g, g2g);
					connections.add(conId);
					tc++;
				}
			}
		}
		
		DiagramKite9XMLElement out = new DiagramKite9XMLElement(cl, null);

	
		return wrap(out);
	}

	private boolean checkContainment(Kite9XMLElement i, Kite9XMLElement c) {
		if (c instanceof Context) {
			for (Kite9XMLElement e : c) {
				if (e == i) {
					return false;
				}
			}
		}
		
		return true;
	}

	/**
	 * @see http://www.kite9.com/content/self-link
	 * @throws Exception
	 */
	@Test
	public void increasingConnections3Containers() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(10, 10, 1, 5, 20, 5, 3, null);
		render(suite1);
	}

	@Test
	public void increasingConnections3ContainersHorizontal() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(10, 10, 1, 5, 20, 5, 3, Layout.HORIZONTAL);
		render(suite1);
	}

	@Test
	public void increasingConnections6Containers() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(10, 20, 1, 5, 30, 5, 6, null);
		render(suite1);
	}
	
	@Test
	public void increasingConnections8Containers() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(10, 20, 1, 5, 30, 5, 8, null);
		render(suite1);
	}
	
	@Test
	public void brokenEdgesOutAndIn() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(17, 17, 1, 5, 5, 5 , 6, null);
		render(suite1);
	}

}
