package org.kite9.diagram.functional.display;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.NotAddressed;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.KeyHelper;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.adl.TextLabel;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.batik.bridge.Kite9DiagramBridge;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.functional.TestingEngine.LayoutErrorException;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.LabelPlacement;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.common.StreamHelp;


public class Test12LabelledArrowsEncapsulated extends AbstractDisplayFunctionalTest {

	
	protected void transcodeSVG(String s) throws Exception {
		try {
			TranscoderOutput out = getTranscoderOutputSVG();
			TranscoderInput in = getTranscoderInput(s);
			Transcoder transcoder = new Kite9SVGTranscoder(Cache.NO_CACHE);
			transcoder.addTranscodingHint(Kite9SVGTranscoder.KEY_ENCAPSULATING, true);
			transcoder.transcode(in, out);
			
			Kite9XMLElement lastDiagram = Kite9DiagramBridge.lastDiagram;
			if (lastDiagram != null) {
				AbstractArrangementPipeline lastPipeline = Kite9DiagramBridge.lastPipeline;
				writeTemplateExpandedSVG(lastDiagram);
				new TestingEngine().testDiagram(lastDiagram, this.getClass(), getTestMethod(), checks(), true, lastPipeline);		
			}
			if (checkXML()) {
				checkIdenticalXML();
			}
		} finally {
			try {
				copyTo(getOutputFile(".svg"), "svg-output");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	

	@Test
	public void test_12_1_LabelledLeftRight() throws Exception {
		
//		Glyph a = new Glyph("g1", "", "aasdsad", null, null);
		Glyph b = new Glyph("g2", "", "b", null, null);
		
		Arrow i1 = new Arrow("arrow1", "i1asdas ");
		
		TextLabel fromLabel = new TextLabel("from (down)", LabelPlacement.BOTTOM);
		fromLabel.setID("fromLabel");
//		TextLabel toLabel = new TextLabel("to dsdsfds f ds f (up)", LabelPlacement.TOP);
//		toLabel.setID("toLabel");
		
//		new Link(i1, a, null, null, null, from, Direction.LEFT);
		new Link(i1, b, null, fromLabel, LinkEndStyle.ARROW, null, Direction.RIGHT);
						
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(b, i1), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_2_LabelledUpDown() throws Exception {
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		
		Arrow i1 = new Arrow("i1", "i1");
		
		new Link(i1, a, null, null, null, new TextLabel("from (right)", LabelPlacement.RIGHT), Direction.UP);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, new TextLabel("to (left)", LabelPlacement.LEFT), Direction.DOWN);
						
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a, b, i1), null);
		renderDiagram(d);
	}
	
	@NotAddressed("See technical_debt/getLength()")
	@Test(expected=LayoutErrorException.class)
	public void test_12_3_SymbolLabels() throws Exception {
		KeyHelper kh = new KeyHelper();
		
		
		Glyph a = new Glyph("stereo", "a", null, createList(kh.createSymbol("bob", 'b', SymbolShape.CIRCLE)));
		Glyph b = new Glyph("", "Something\nWicked", createList(new TextLine("some line of data")), createList(kh.createSymbol("terv", 'b', SymbolShape.HEXAGON)));
		
		Arrow i1 = new Arrow("i1");

		
		new Link(i1, a, null, new TextLine("lines"), null, null, Direction.LEFT);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		
		Symbol s1 = kh.createSymbol("bob");
		Symbol s2 = kh.createSymbol("jeff");
		
		
//		TextLineWithSymbols clabel = new TextLineWithSymbols("Container Label", createList(s1, s2));
//		
//		Context con = new Context("c1",createList( a, b, i1), true, clabel, null);
//				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList( a,b,i1), null);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_12_4_VeryLongLabels() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		Glyph b = new Glyph("", "b", null, null);
		
		Arrow i1 = new Arrow("i1");
		
		new Link(i1, a, null, null, null, new TextLabel("from the wild side"), Direction.UP);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, new TextLabel("to the safe side"), Direction.DOWN);
		
		Context con = new Context("c1",createList(a, b, i1), true, new TextLabel("Container Label, oh the old container"), null);
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList( con), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_5_MultiLineLongLabels() throws Exception {
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		
		Arrow i1 = new Arrow("i1", "i1");
		
		new Link(i1, a, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog"), Direction.UP);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, new TextLabel("to the safe side"), Direction.DOWN);
		
		Context con = new Context("c1",createList(a, b, i1), true, new TextLabel("Container Label\n oh the old container\nhas a very long and tedious label"), null);
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_6_GlyphMultipleLabels() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Arrow i1 = new Arrow("i1");
		Arrow i2 = new Arrow("i2");
		Arrow i3 = new Arrow("i3");
		
		new Link(i1, a, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog"), Direction.UP);
		new Link(i2, a, null, null, LinkEndStyle.ARROW, null /* new LabelTextLine("to the safe side A") */, Direction.UP);
		new Link(i3, a, null, null, LinkEndStyle.ARROW, null /* new LabelTextLine("to the safe side B") */, Direction.UP);
		
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a, i1, i2, i3), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_7_LabelsInside() throws Exception {
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		
		
		Arrow i1 = new Arrow("i1", "i1");
		Arrow i2 = new Arrow("i2", "i2");
		
		new Link(i1, a, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog 1"), Direction.RIGHT);
		new Link(i1, b, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog 2"), Direction.LEFT);
		new TurnLink(i2, a, null, null, LinkEndStyle.ARROW, new TextLabel("to the safe side A"), null);
		new TurnLink(i2, b, null, null, LinkEndStyle.ARROW, new TextLabel("to the safe side B"), null);
		
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList( a, i1, i2, b), null);
		renderDiagram(d);
	}

	@Test
	public void test_12_8_TestLabelledBothEnds() throws Exception {
		Glyph a = new Glyph("glyph","", "a", null, null);
		Arrow i1 = new Arrow("arrow", "i1");
		Link l = new Link(i1, a);
		l.setDrawDirection(Direction.RIGHT);
		l.setFromLabel(new TextLabel("arrow-hello"));
		l.setToLabel(new TextLabel("glyph-gopher"));

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a, i1), null);
		renderDiagram(d);

	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_12_9_ChainOfLabels() throws Exception {
				
		Arrow i1 = new Arrow("i1", "i1");
		Arrow i2 = new Arrow("i2", "i2");
		
		Context c = new Context("c1", Collections.EMPTY_LIST, true, new TextLabel("Big C"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(i1, i2), true, new TextLabel("Arrow Holder"), null);
		
		new Link(c, i1, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog 1"), Direction.RIGHT);
		new Link(c, i2, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog 2"), Direction.RIGHT);
		
		new Link(i1, i2, null, null, null, null, Direction.DOWN);
		
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(c, c2), null);
		renderDiagram(d);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_12_10_BlankLabels() throws Exception {
		Arrow i1 = new Arrow("i1", "i1");
		Arrow i2 = new Arrow("i2", "i2");
		
		Context c = new Context("c1", Collections.EMPTY_LIST, true, new TextLabel("Big C"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(i1, i2), true, new TextLabel(""), null);
		
		new Link(c, i1, null, null, null, new TextLabel(null), Direction.RIGHT);
		new Link(c, i2, null, null, null, new TextLabel("  "), Direction.RIGHT);
		
		new Link(i1, i2, null, null, null, null, Direction.DOWN);
		
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(c, c2), new Key(null,"", new ArrayList<Symbol>()));
		renderDiagram(d);
		
	}
	

	@Test
	public void test_12_11_ImageSVGTranscoding() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("test_54_image.svg"));
		StreamHelp.streamCopy(in, out, true);
		String xml = out.toString();
		transcodeSVG(xml);
	}
	
	@Test
	public void test_12_12_BrokenImageSVGTranscoding() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("test_54_image_broken.svg"));
		StreamHelp.streamCopy(in, out, true);
		String xml = out.toString();
		transcodeSVG(xml);
	}
	
}
