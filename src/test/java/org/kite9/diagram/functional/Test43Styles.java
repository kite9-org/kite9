package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.visualization.display.style.sheets.Designer2012Stylesheet;
import org.kite9.framework.common.HelpMethods;



public class Test43Styles extends AbstractFunctionalTest {

	
	@Override
	protected boolean checkImage() {
		return true;
	}

	@Test
	public void test_43_1_OverrideGlyphStrokeWidth() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		g1.setStyle("stroke-width: 8px");
		Diagram d= new Diagram(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	@Test
	public void test_43_2_OverrideGlyphFill() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		g1.setStyle("fill: red");
		Diagram d= new Diagram(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	@Test
	public void test_43_3_OverrideGlyphFillGradient() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		g1.setStyle("fill: \"270-#363525-#756365\"");
		Diagram d= new Diagram(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	@Test
	public void test_43_4_OverrideGlyphColor() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		g1.setStyle("stroke: rgb(200, 5, 5)");
		Diagram d= new Diagram(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	@Test
	public void test_43_5_OverrideGlyphFont() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		TextLine tl = new TextLine("Hello Biggie");
		tl.setStyle("font-size: 20px");
		g1.setLabel(tl);
		Diagram d= new Diagram(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	@Test
	public void test_43_6_OverrideGlyphStrokeDasharray() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		g1.setStyle("stroke-dasharray: \"-..\"");
		Diagram d= new Diagram(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_43_7_OverrideLinkStroke1() throws IOException {
		Glyph g1 = new Glyph("a", null, "a", null, null);
		Glyph g2 = new Glyph("b", null, "b", null, null);
		Diagram d= new Diagram(HelpMethods.listOf(g1, g2),  null);
		Link l2 = new Link(g1, g2, "DIAMOND", new TextLine("label 1"), "DIAMOND OPEN", new TextLine("label 2"), Direction.RIGHT);
		l2.setStyle("stroke-width: 1px; stroke-dasharray: '-..'; stroke: red");
		renderDiagram(d);
	}
	
	@Test
	public void test_43_8_OverrideTextBoxStyle() throws IOException {
		Glyph g1 = new Glyph("a", null, "a", null, null);
		Glyph g2 = new Glyph("b", null, "b", null, null);
		Diagram d= new Diagram(HelpMethods.listOf(g1, g2),  null);
		TextLine oneEnd = new TextLine("label 1 hhh hhh");
		//oneEnd.getText().setStyle("font-size: 20px;");
		oneEnd.setStyle("fill: \"270-#363525-#756365\"");
		TextLine otherEnd = new TextLine("label 1 hhh jhgjhg");
		otherEnd.setStyle("stroke-width: 8px");
		new Link(g1, g2, "DIAMOND", oneEnd, "DIAMOND OPEN", otherEnd, Direction.RIGHT);
	
		renderDiagram(d);
	}
	
	@Test
	public void test_43_9_Dasharrays() throws IOException {
		List<Contained> elems = new ArrayList<Contained>();
		for (String s : DasharrayValueManager.DASH_PATTERNS.keySet()) {
			Glyph g1 = new Glyph("a", null, "a", null, null);
			Glyph g2 = new Glyph("b", null, "b", null, null);
			Link l1 = new Link(g1, g2);
			l1.setStyle("stroke-dasharray: '"+s+"'");	
			elems.add(g1);
			elems.add(g2);
		}
		Diagram d= new Diagram(elems,  null);
		
		
		renderDiagram(d);
	}
	
	
	@Test
	public void test_43_10_OverrideLinkStroke2() throws IOException {
		Glyph g1 = new Glyph("a", null, "a", null, null);
		Glyph g2 = new Glyph("b", null, "b", null, null);
		Diagram d= new Diagram(HelpMethods.listOf(g1, g2),  null);
		Link l1 = new Link(g1, g2, "DIAMOND", new TextLine("label 1 hhh "), "DIAMOND OPEN", new TextLine("label 2 hh h"), Direction.RIGHT);
		l1.setStyle("stroke-width: 7px; stroke-dasharray: '--'");
		renderDiagram(d);
	}
}
