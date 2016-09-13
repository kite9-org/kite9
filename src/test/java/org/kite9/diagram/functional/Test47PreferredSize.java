package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.HelpMethods;

@Ignore
public class Test47PreferredSize extends AbstractFunctionalTest {

	public interface ContainedCreator {
		
		XMLElement create(Double px, Double py);
		
	}
	
	@Test
	public void test_47_1_PreferredGlyphSizeWithStereoAndLabel() throws IOException {
		createDiagram(new ContainedCreator() {
			
			@Override
			public XMLElement create(Double preferredWidth, Double preferredHeight) {
				Glyph g = new Glyph("test"+preferredWidth+"/"+preferredHeight, "This is my Label", null, HelpMethods
						.createList(new Symbol("g", 'x', SymbolShape.CIRCLE)));
				return g;
			}
		});

	}

	
	@Test
	public void test_47_2_PreferredGlyphSizeJustLabel() throws IOException {
		createDiagram(new ContainedCreator() {
			
			@Override
			public XMLElement create(Double preferredWidth, Double preferredHeight) {
				Glyph g = new Glyph(null, "This is my Label "+preferredWidth+" "+preferredHeight, null, null);
				return g;
			}
		});

	}

	@Test
	public void test_47_3_PreferredGlyphSizeLabelAndSymbol() throws IOException {
		createDiagram(new ContainedCreator() {
			
			@Override
			public XMLElement create(Double preferredWidth, Double preferredHeight) {
				Glyph g = new Glyph(null, "This is my Label", null, HelpMethods.createList(new Symbol("sdfds", 'x', SymbolShape.CIRCLE)));
				return g;
			}
		});

	}
	
	@Test
	public void test_47_4_PreferredGlyphSizeEverything() throws IOException {
		createDiagram(new ContainedCreator() {
			
			@Override
			public XMLElement create(Double preferredWidth, Double preferredHeight) {
				Glyph g = new Glyph(null, "This is my Label", HelpMethods.createList(
						new TextLine("This is one line"), 
						new TextLine("This is another line")),
						
						
						HelpMethods.createList(new Symbol("sdfds", 'x', SymbolShape.CIRCLE)));
				return g;
			}
		});

	}

	
	private void createDiagram(ContainedCreator cc) throws IOException {
		List<XMLElement> out = new ArrayList<XMLElement>();
		Double widths[] = { null, 0d, 30d, 100d, 300d }; 
		Double heights[] = { null, 0d, 30d, 100d, 300d };
		
		for (int x = 0; x < widths.length; x++) {
			for (int y = 0; y < heights.length; y++) {
				Double preferredWidth = widths[x];
				Double preferredHeight = heights[y];
				XMLElement g = cc.create(preferredWidth, preferredHeight);
				RectangleRenderingInformation rri = (RectangleRenderingInformation) g.getDiagramElement().getRenderingInformation();
//				TODO: these shouldn't even be in the rri
				//				rri.setPreferredWidth(preferredWidth);
//				rri.setPreferredHeight(preferredHeight);
				out.add(g);
			}
		}

		DiagramXMLElement d = new DiagramXMLElement(out, null);

		renderDiagram(d);
	}

}
