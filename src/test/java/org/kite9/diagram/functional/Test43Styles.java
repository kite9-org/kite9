package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.XMLElement;
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
		DiagramXMLElement d= new DiagramXMLElement(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	@Test
	public void test_43_2_OverrideGlyphFill() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		g1.setStyle("fill: red");
		DiagramXMLElement d= new DiagramXMLElement(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	@NotAddressed("Need to use SVG-first rendering for this to work")
	@Test
	public void test_43_3_OverrideGlyphFillGradient() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		g1.setStyle("background-image: linear-gradient(red, orange);");
		DiagramXMLElement d= new DiagramXMLElement(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	@Test
	public void test_43_4_OverrideGlyphColor() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		g1.setStyle("stroke: rgb(200, 5, 5)");
		DiagramXMLElement d= new DiagramXMLElement(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	@Test
	public void test_43_5_OverrideGlyphFont() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		TextLine tl = new TextLine("Hello Biggie");
		tl.setStyle("font-size: 20px");
		g1.setLabel(tl);
		DiagramXMLElement d= new DiagramXMLElement(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	@Test
	public void test_43_6_OverrideGlyphStrokeDasharray() throws IOException {
		Glyph g1 = new Glyph("Stereo", "label", null, null);
		g1.setStyle("stroke-dasharray: 5");
		DiagramXMLElement d= new DiagramXMLElement(HelpMethods.listOf(g1),  null);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_43_7_OverrideLinkStroke1() throws IOException {
		Glyph g1 = new Glyph("a", null, "a", null, null);
		Glyph g2 = new Glyph("b", null, "b", null, null);
		DiagramXMLElement d= new DiagramXMLElement(HelpMethods.listOf(g1, g2),  null);
		Link l2 = new Link(g1, g2, "DIAMOND", new TextLine("label 1"), "DIAMOND OPEN", new TextLine("label 2"), Direction.RIGHT);
		l2.setStyle("stroke-width: 1px; stroke-dasharray: '-..'; stroke: red");
		renderDiagram(d);
	}
	
	@Test
	public void test_43_8_OverrideTextBoxStyle() throws IOException {
		Glyph g1 = new Glyph("a", null, "a", null, null);
		Glyph g2 = new Glyph("b", null, "b", null, null);
		DiagramXMLElement d= new DiagramXMLElement(HelpMethods.listOf(g1, g2),  null);
		TextLine oneEnd = new TextLine("label 1 hhh hhh");
		//oneEnd.getText().setStyle("font-size: 20px;");
		oneEnd.setStyle("fill: \"270-#363525-#756365\"");
		TextLine otherEnd = new TextLine("label 1 hhh jhgjhg");
		otherEnd.setStyle("stroke-width: 8px");
		new Link(g1, g2, "DIAMOND", oneEnd, "DIAMOND OPEN", otherEnd, Direction.RIGHT);
	
		renderDiagram(d);
	}
		
	@Test
	public void test_43_10_OverrideLinkStroke2() throws IOException {
		Glyph g1 = new Glyph("a", null, "a", null, null);
		Glyph g2 = new Glyph("b", null, "b", null, null);
		DiagramXMLElement d= new DiagramXMLElement(HelpMethods.listOf(g1, g2),  null);
		Link l1 = new Link(g1, g2, "DIAMOND", new TextLine("label 1 hhh "), "DIAMOND OPEN", new TextLine("label 2 hh h"), Direction.RIGHT);
		l1.setStyle("stroke-width: 7px; stroke-dasharray: '--'");
		renderDiagram(d);
	}
}
