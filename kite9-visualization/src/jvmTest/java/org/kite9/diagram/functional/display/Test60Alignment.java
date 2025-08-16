package org.kite9.diagram.functional.display;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;

public class Test60Alignment extends AbstractDisplayFunctionalTest {

	Glyph g1, g2, g3;
	Context c4, c5;

	public DiagramKite9XMLElement setUpDiagram1(Layout c4Layout, Layout c5Layout, VerticalAlignment gva,
			VerticalAlignment c4Align, HorizontalAlignment gha, Layout diagramLayout) {
		g1 = new Glyph("one", "", "one", null, null);
		g2 = new Glyph("two", "", "two ", null, null);
		g3 = new Glyph("three", "", "three ", null, null);
		c4 = new Context("c4", listOf(g1), true, null, null);
		c5 = new Context("c5", listOf(g2, g3), true, null, null);
		g1.setAttribute("style", "--kite9-vertical-align: " + gva + "; --kite9-horizontal-align: " + gha);
		g2.setAttribute("style", "--kite9-vertical-align: " + gva + "; --kite9-horizontal-align: " + gha);
		g3.setAttribute("style", "--kite9-vertical-align: " + gva + "; --kite9-horizontal-align: " + gha);
		c4.setAttribute("style", "--kite9-min-height: 80px; --kite9-sizing: minimize; --kite9-vertical-align: "
				+ c4Align + "; --kite9-layout: " + c4Layout);
		c5.setAttribute("style", "--kite9-min-height: 200px; --kite9-layout: " + c5Layout);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("diagram" + diagramLayout.name(), Arrays.asList(c4, c5),
				diagramLayout, null);
		return d;
	}

	public DiagramKite9XMLElement setUpDiagram2(Layout cLayout) {
		g1 = new Glyph("one", "", "one", null, null);
		g2 = new Glyph("two", "", "two ", null, null);
		g3 = new Glyph("three", "", "three ", null, null);
		c5 = new Context("c5", listOf(g1, g2, g3), true, null, null);
		g1.setAttribute("style", "--kite9-vertical-align: " + VerticalAlignment.TOP + "; --kite9-horizontal-align: "
				+ HorizontalAlignment.LEFT);
		g2.setAttribute("style", "--kite9-vertical-align: " + VerticalAlignment.CENTER + "; --kite9-horizontal-align: "
				+ HorizontalAlignment.CENTER);
		g3.setAttribute("style", "--kite9-vertical-align: " + VerticalAlignment.BOTTOM + "; --kite9-horizontal-align: "
				+ HorizontalAlignment.RIGHT);
		c5.setAttribute("style", "--kite9-min-size: 200px 200px; --kite9-layout: " + cLayout);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("diagram", Arrays.asList(c5), cLayout, null);
		return d;
	}

	public DiagramKite9XMLElement setUpDiagram3(Layout cLayout, boolean link, boolean outLink, boolean addAlignments) {
		g1 = new Glyph("one", "", "one", null, null);
		g2 = new Glyph("two", "", "two ", null, null);
		g3 = new Glyph("three", "", "three ", null, null);
		c5 = new Context("c5", listOf(g1, g2, g3), true, null, null);
		c4 = new Context("c4", null, true, null, null);

		if (addAlignments) {
			g1.setAttribute("style", "--kite9-vertical-align: " + VerticalAlignment.CENTER
					+ "; --kite9-horizontal-align: " + HorizontalAlignment.CENTER);
			g2.setAttribute("style", "--kite9-vertical-align: " + VerticalAlignment.CENTER
					+ "; --kite9-horizontal-align: " + HorizontalAlignment.CENTER);
			g3.setAttribute("style", "--kite9-vertical-align: " + VerticalAlignment.CENTER
					+ "; --kite9-horizontal-align: " + HorizontalAlignment.CENTER);
		}
		c5.setAttribute("style",
				"--kite9-min-size: 400px 200px; " + (cLayout != null ? "--kite9-layout: " + cLayout : ""));

		if (link) {
			new Link(g1, g3);
		}

		if (outLink) {
			new Link(g2, c4);
		}

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("diagram", Arrays.asList(c5, c4));
		return d;
	}

	@Test
	public void test_60_1_ContentsAlignedTop() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram1(Layout.DOWN, Layout.DOWN, VerticalAlignment.TOP,
				VerticalAlignment.CENTER, HorizontalAlignment.CENTER, Layout.RIGHT);
		renderDiagram(d);
	}

	@Test
	public void test_60_2_ContentsAlignedRight() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram1(Layout.DOWN, Layout.DOWN, VerticalAlignment.CENTER,
				VerticalAlignment.CENTER, HorizontalAlignment.RIGHT, Layout.RIGHT);
		renderDiagram(d);
	}

	@Test
	public void test_60_3_ContentsAlignedDown() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram1(Layout.DOWN, Layout.DOWN, VerticalAlignment.BOTTOM,
				VerticalAlignment.CENTER, HorizontalAlignment.LEFT, Layout.RIGHT);
		renderDiagram(d);
	}

	@Test
	public void test_60_4_ContentsAlignedDifferentlyDown() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram2(Layout.DOWN);
		renderDiagram(d);
	}

	/*
	 * Result of these tests is unspecified, because it depends on the order you
	 * process the elements.
	 * 
	 * @Test
	 * public void test_60_5_ContentsAlignedDifferentlyUp() throws Exception {
	 * DiagramElement d = setUpDiagram2(Layout.UP);
	 * renderDiagram(d);
	 * }
	 * 
	 * @Test
	 * public void test_60_6_ContentsAlignedDifferentlyLeft() throws Exception {
	 * DiagramElement d = setUpDiagram2(Layout.LEFT);
	 * renderDiagram(d);
	 * }
	 */

	@Test
	public void test_60_7_ContentsAlignedDifferentlyRight() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram2(Layout.RIGHT);
		renderDiagram(d);
	}

	@Test
	public void test_60_8_ContentsAlignedCentreDown() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram3(Layout.DOWN, false, false, true);
		renderDiagram(d);
	}

	@Test
	public void test_60_9_ContentsAlignedCentreLeft() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram3(Layout.LEFT, false, false, true);
		renderDiagram(d);
	}

	@Test
	public void test_60_10_ContentsAlignedCentreDown() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram3(Layout.DOWN, true, false, true);
		renderDiagram(d);
	}

	@Test
	public void test_60_11_ContentsAlignedCentreLeft() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram3(Layout.LEFT, true, false, true);
		renderDiagram(d);
	}

	@Test
	public void test_60_12_ContentsAlignedOutlink1() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram3(Layout.LEFT, false, true, true);
		renderDiagram(d);
	}

	@Test
	public void test_60_13_ContentsAlignedOutlink2() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram3(Layout.DOWN, true, true, true);
		renderDiagram(d);
	}

	@Test
	public void test_60_14_ContentsAlignedOutlink3() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram3(Layout.UP, true, true, true);
		renderDiagram(d);
	}

	@Test
	public void test_60_15_ContentsUnAlignedOutlink1() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram3(Layout.DOWN, true, true, false);
		renderDiagram(d);
	}

	@Test
	public void test_60_16_ContentsUnAlignedOutlink2() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram3(Layout.RIGHT, true, true, false);
		renderDiagram(d);
	}

	@Test
	public void test_60_17_ContentsUnAlignedOutlink3() throws Exception {
		DiagramKite9XMLElement d = setUpDiagram3(null, true, true, false);
		renderDiagram(d);
	}
}
