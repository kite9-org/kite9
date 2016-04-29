package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.display.style.sheets.BasicBlueStylesheet;
import org.kite9.diagram.visualization.display.style.sheets.BasicStylesheet;
import org.kite9.diagram.visualization.display.style.sheets.CGWhiteStylesheet;
import org.kite9.diagram.visualization.display.style.sheets.Designer2012Stylesheet;
import org.kite9.diagram.visualization.display.style.sheets.DesignerStylesheet;
import org.kite9.diagram.visualization.display.style.sheets.OutlinerStylesheet;
import org.kite9.framework.common.HelpMethods;

public class TestCard extends AbstractFunctionalTest {

	public Diagram createTestCard() {
		Symbol aDia = new Symbol("Some description\n taking multiple lines", 'a', SymbolShape.DIAMOND);
		Symbol MDia = new Symbol("Some description blah blah blah", 'M', SymbolShape.DIAMOND);
		Symbol QCir = new Symbol("Some description sdkjfsd flksdjf", 'Q', SymbolShape.CIRCLE);
		Symbol OCir = new Symbol("Some description", 'O', SymbolShape.CIRCLE);
		Symbol oCir = new Symbol("Some description da fsdfdsf", 'o', SymbolShape.CIRCLE);
		Symbol AHex = new Symbol("Some", 'A', SymbolShape.HEXAGON);
		Symbol gHex = new Symbol("Some description", 'g', SymbolShape.HEXAGON);
		
		
		
		Glyph simple = new Glyph("simple", null, "Simple Label", null, null);
		
		Glyph other = new Glyph("other", null, "4th Man", null, null);
		
		Glyph withStereo = new Glyph("withStereo", "Stereotype", "With Stereo", null, null);
		
		Glyph withSymbols = new Glyph("withSymbols", null, "With Symbols", null, HelpMethods.createList(aDia, QCir, gHex));

		Glyph text1 = new Glyph("text1", null, "With Text 1", HelpMethods.createList(
				new TextLine("This is a piece of text on one line",  HelpMethods.createList(AHex)),
				new TextLine("A pair of lines in a text\nline without symbols"),
				new TextLine("This is a piece of text\non two lines",  HelpMethods.createList(AHex, OCir)),
				new TextLine("Some line of text without symbols"),
				new TextLine("This is a piece\n of text\non three lines",  HelpMethods.createList(AHex, OCir))
				), null);
		
		Glyph fullGlyph = new Glyph("fullGlyph", "Some complex stereotype", "Full Glyph", HelpMethods.createList(
				new TextLine("Small amount of text",  HelpMethods.createList(MDia, aDia, gHex, oCir)),
				new TextLine("Bit more text",HelpMethods.createList(MDia, aDia, gHex, oCir)),
				new TextLine("Another text line", HelpMethods.createList(MDia, aDia, gHex, oCir))
				),  HelpMethods.createList(aDia, QCir, gHex));
		
		Arrow a1 = new Arrow("a1", "Arrow with a label");
		
		Arrow a2 = new Arrow("a2", null);
		
		

		Context occContext = new Context(HelpMethods.listOf(simple, withStereo), true, new TextLine("Multiline label\nfor this context"), null);
		
		Context emptyContext = new Context(null, true, new TextLine("Simple Label", HelpMethods.createList(AHex, gHex, QCir)), null);
		
		Context otherContext = new Context(HelpMethods.listOf(a1, fullGlyph, occContext), true, null, null);
		
		Key k = new Key("This is a test card", "It demonstrates all the attr in the ADL\nlanguage to allow you to check rendering", HelpMethods.createList(aDia, QCir, gHex, AHex, OCir, MDia, oCir));
		
		new Link(simple, a1);
		new Link(withSymbols, a1, 
				LinkEndStyle.ARROW, new TextLine("From Label", HelpMethods.createList(AHex, gHex, QCir)), 
				null, null, null);
		
		new Link(occContext, fullGlyph,
				LinkEndStyle.ARROW, new TextLine("From Label", HelpMethods.createList(AHex, gHex, QCir)), 
				LinkEndStyle.ARROW, new TextLine("To\n Label\nwith\n line\nbreaks\n\n\nflip", HelpMethods.createList(AHex, gHex, QCir)), null);
		
		new Link(occContext, fullGlyph);
		
		new Link(a2, fullGlyph);
		new Link(a2, occContext);
		new Link(a2, emptyContext);
		
		
		// get a crossing
		
		new Link(text1, simple);
		new Link(text1, withStereo);
		new Link(text1, other);
		new Link(simple, other, null, null, null, null, Direction.UP);
		new Link(withSymbols, other);
		new Link(withStereo, other);
		new Link(a1, other);
		
		new Link(a1, withStereo);
		
		
		

		Diagram d = new Diagram(HelpMethods.listOf(emptyContext, otherContext, a2, withSymbols, text1, other), k);
		return d;
	}
	
	@Test
	public void testCardOutliner() throws IOException {
		Diagram d = createTestCard();
		renderDiagramSVG(d, new OutlinerStylesheet());
		renderDiagramPDF(d, new OutlinerStylesheet());
		renderDiagram(d, new OutlinerStylesheet());
	}
	
	@Test
	public void testCardBasic() throws IOException {
		Diagram d = createTestCard();
		renderDiagramSVG(d, new BasicStylesheet());
		renderDiagramPDF(d, new BasicStylesheet());
		renderDiagram(d, new BasicStylesheet());
	}
	
	@Test
	public void testCardDesigner() throws IOException {
		Diagram d = createTestCard();
		renderDiagramSVG(d, new DesignerStylesheet());
		renderDiagramPDF(d, new DesignerStylesheet());
		renderDiagram(d, new DesignerStylesheet());
	}
	
	@Test
	public void testCardDesigner2012() throws IOException {
		Diagram d = createTestCard();
		renderDiagramSVG(d, new Designer2012Stylesheet());
		renderDiagramPDF(d, new Designer2012Stylesheet());
		renderDiagram(d, new Designer2012Stylesheet());
	}
	
	@Test
	public void testCardCGWhite() throws IOException {
		Diagram d = createTestCard();
		renderDiagramSVG(d, new CGWhiteStylesheet());
		renderDiagramPDF(d, new CGWhiteStylesheet());
		renderDiagram(d, new CGWhiteStylesheet());
	}
	
	@Test
	public void testCardBasicBlue() throws IOException {
		Diagram d = createTestCard();
		renderDiagramSVG(d, new BasicBlueStylesheet());
		renderDiagramPDF(d, new BasicBlueStylesheet());
		renderDiagram(d, new BasicBlueStylesheet());
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Override
	protected boolean checkNoHops() {
		return false;
	}
	
	
	
}

