package org.kite9.diagram.functional.thorough;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.NotAddressed;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.adl.TextLineWithSymbols;
import org.kite9.framework.common.HelpMethods;

public class Test46MoreXMLFiles extends AbstractLayoutFunctionalTest {

	@Test
	public void test_46_1_CantComplete() throws Exception {
		generate("46_1.xml");
	}
	
	@Test
	public void test_46_2_UnnecessaryContradiction() throws Exception {
		generate("46_2.xml");
	}
	
	@Test
	public void test_46_3_ServerError() throws Exception {
		generate("46_3.xml");
	}
	
	@Test
	public void test_46_4_FailsToDraw() throws Exception {
		generate("46_4.xml");
	}
	
	@Ignore("Broken in sprint 7")
	@Test
	public void test_46_5_FailsToDraw() throws Exception {
		generate("46_5.xml");
	}
	
	@Test
	@NotAddressed 
	public void test_46_6_Overlap2() throws Exception {
		generate("overlap_2.xml");
	}
	
	@Test
	@NotAddressed
	public void test_46_7_BrokenContainer() throws Exception {
		generate("46_7.xml");
	}
	

	@Test
	public void test_46_8_ContentOverlap() throws Exception {
		generate("content_overlap.xml");
	}
	
	@Test
	public void test_46_9_4Turns() throws Exception {
		generate("4turns.xml");
	}
	
	@Test
	public void test_46_10_PushingRect() throws Exception {
		generate("label_push_error.xml");
	}
	
	@Test
	public void test_46_11_KeyTooSmall() throws Exception {
		renderDiagram(new DiagramKite9XMLElement(HelpMethods.listOf(new Glyph("stereo", "Some Label", 
			HelpMethods.createList(
				new TextLineWithSymbols("Some Text Here To Make It A Bit Wider", 
						HelpMethods.createList(
								new Symbol("sdfs", 'W', SymbolShape.HEXAGON)))),				
			HelpMethods.createList(
				new Symbol("sdfsf", 's', SymbolShape.CIRCLE),
				new Symbol("sdfsf", 'w', SymbolShape.DIAMOND)))),
			
			new Key("bold", "body", 
				HelpMethods.createList(
					new Symbol("sdfs", 'W', SymbolShape.HEXAGON)))));
	}
	
	@Test
	public void test_46_12_UnnecessaryDogleg() throws Exception {
		generate("dogleg.xml");
	}

	@Test
	public void test_46_13_WonkyTable() throws Exception {
		generate("wonky_table.xml");
	}
	
	protected DiagramKite9XMLElement createDiagram() {
		DiagramKite9XMLElement d = new DiagramKite9XMLElement(HelpMethods.listOf(new Glyph("stereo", "Some Label", 
			HelpMethods.createList(
				new TextLineWithSymbols("Some Text Here To Make It A Bit Wider", 
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
