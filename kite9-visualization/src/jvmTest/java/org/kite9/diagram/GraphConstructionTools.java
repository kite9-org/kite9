package org.kite9.diagram;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.adl.LinkBody;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.w3c.dom.Element;

public class GraphConstructionTools {

	public static Glyph[][] createGrid(int x, int y, List<Element> collection, boolean fixDirection) {
		return createGrid("", x, y, collection, fixDirection);
	}

	public static Glyph[][] createGrid(String prefix, int x, int y, List<Element> collection, boolean fixDirection) {
		int arrowNo = 0;
		Glyph[][] out = new Glyph[x][];
		for (int i = 0; i < x; i++) {
			out[i] = new Glyph[y];
			for (int j = 0; j < y; j++) {
				Glyph g = new Glyph("Glyph" + prefix + i + "_" + j, "", "Glyph" + prefix + i + "_" + j, null, null);
				out[i][j] = g;
				collection.add(g);
				if (j > 0) {
					String label = "a" + prefix + arrowNo++;
					LinkBody a = new LinkBody(label, label);
					collection.add(a);
					new Link(out[i][j - 1], a, null, null, null, null, fixDirection ? Direction.RIGHT : null);
					new Link(a, out[i][j], null, null, null, null, fixDirection ? Direction.RIGHT : null);
				}
				if (i > 0) {
					String label = "a" + prefix + arrowNo++;
					LinkBody a = new LinkBody(label, label);
					collection.add(a);
					new Link(out[i - 1][j], a, null, null, null, null, fixDirection ? Direction.DOWN : null);
					new Link(a, out[i][j], null, null, null, null, fixDirection ? Direction.DOWN : null);
				}
			}
		}

		return out;
	}

	public static Glyph[][] createXContainers(String prefix, int glyphsPerContainer, int containers,
			List<Element> out, Layout containerLayout) {
		int gi = 0;
		Glyph[][] result = new Glyph[containers][];
		for (int i = 0; i < containers; i++) {
			result[i] = new Glyph[glyphsPerContainer];
			List<Element> contents = new ArrayList<Element>(glyphsPerContainer);
			for (int j = 0; j < glyphsPerContainer; j++) {
				Glyph g = new Glyph(prefix + (gi), "", "" + gi, null, null);
				gi++;
				result[i][j] = g;
				contents.add(g);
			}
			Context c = new Context("c" + i, contents, true, null, /*new TextLine("c" + i) ,*/ containerLayout);
			out.add(c);
		}

		return result;
	}

	public static Glyph[] createX(String prefix, int glyphs, List<Element> out) {
		int gi = 0;
		Glyph[] result = new Glyph[glyphs];
		for (int j = 0; j < glyphs; j++) {
			Glyph g = new Glyph(prefix + (gi), "", prefix + gi, null, null);
			gi++;
			result[j] = g;
			out.add(g);
		}

		return result;
	}
}
