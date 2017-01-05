package org.kite9.diagram.thorough;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.functional.AbstractFunctionalTest;
import org.kite9.diagram.functional.NotAddressed;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.HelpMethods;

public class Test46MoreXMLFiles extends AbstractFunctionalTest {

	@Test
	public void test_46_1_CantComplete() throws IOException {
		generate("46_1.xml");
	}
	
	@Test
	public void test_46_2_UnnecessaryContradiction() throws IOException {
		generate("46_2.xml");
	}
	
	@Ignore("Broken in sprint 7")
	@Test
	public void test_46_3_ServerError() throws IOException {
		generate("46_3.xml");
	}
	
	@Ignore("Broken in sprint 7")
	@Test
	public void test_46_4_FailsToDraw() throws IOException {
		generate("46_4.xml");
	}
	
	@Ignore("Broken in sprint 7")
	@Test
	public void test_46_5_FailsToDraw() throws IOException {
		generate("46_5.xml");
	}
	
	@Test
	@NotAddressed 
	public void test_46_6_Overlap2() throws IOException {
		generate("overlap_2.xml");
	}
	
	@Test
	@NotAddressed
	public void test_46_7_BrokenContainer() throws IOException {
		generate("46_7.xml");
	}
	

	@Test
	public void test_46_8_ContentOverlap() throws IOException {
		generate("content_overlap.xml");
	}
	
	@Test
	public void test_46_9_4Turns() throws IOException {
		generate("4turns.xml");
	}
	
	@Test
	public void test_49_10_KeyTooSmall() throws IOException {
		renderDiagram(new DiagramXMLElement(HelpMethods.listOf(new Glyph("stereo", "Some Label", 
			HelpMethods.createList(
				new TextLine("Some Text Here To Make It A Bit Wider", 
						HelpMethods.createList(
								new Symbol("sdfs", 'W', SymbolShape.HEXAGON)))),				
			HelpMethods.createList(
				new Symbol("sdfsf", 's', SymbolShape.CIRCLE),
				new Symbol("sdfsf", 'w', SymbolShape.DIAMOND)))),
			
			new Key("bold", "body", 
				HelpMethods.createList(
					new Symbol("sdfs", 'W', SymbolShape.HEXAGON)))));
	}
	
	protected DiagramXMLElement createDiagram() {
		DiagramXMLElement d = new DiagramXMLElement(HelpMethods.listOf(new Glyph("stereo", "Some Label", 
			HelpMethods.createList(
				new TextLine("Some Text Here To Make It A Bit Wider", 
						HelpMethods.createList(
								new Symbol("sdfs", 'W', SymbolShape.HEXAGON)))),				
			HelpMethods.createList(
				new Symbol("sdfsf", 's', SymbolShape.CIRCLE),
				new Symbol("sdfsf", 'w', SymbolShape.DIAMOND)))),
			
			new Key("bold", "body", 
				HelpMethods.createList(
					new Symbol("sdfs", 'W', SymbolShape.HEXAGON))));
		
		return d;
	}
	
	@Override
	protected boolean checkNoHops() {
		return false;
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Override
	protected boolean checkNoContradictions() {
		return false;
	}
	

}
