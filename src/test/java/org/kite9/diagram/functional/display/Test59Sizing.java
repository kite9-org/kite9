package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;

public class Test59Sizing extends AbstractDisplayFunctionalTest {

	/**
	 * Uses contents to set the size of the decal
	 */
	@Test
	public void test_59_1_InsetShape() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(ellipse(), ""),"")));
	}
	
	/**
	 * Uses padding to effect the size of the decal
	 */
	@Test
	public void test_59_2_PaddedRect() throws Exception {
		renderDiagram(basicDiagram(glyphContainer("","kite9-padding: 15px;")));
	}
	
	/**
	 * Can we have decals in svg elements?  This doesn't work - svg type should _only_ contain svg elements.
	 * Need to improve exception
	 */
	@Test
	public void test_59_3_InsetShape() throws Exception {
		renderDiagram(basicDiagram(glyphLeaf(ellipse(), "")));
	}
	
	/**
	 * Uses padding to effect the size of the decal
	 */
	@Test
	public void test_59_4_MinimumSizeRect() throws Exception {
		renderDiagram(basicDiagram(glyphContainer("","kite9-min-width: 15px; kite9-min-height: 30px")));
	}
	
	/**
	 * Harder, as multiple elements in leaf.
	 */
	@Test
	public void test_59_5_InsetShapes() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(ellipse()+polygon(), ""),"")));
	}
	
	/**
	 * Uses contents AND padding to set the size of the decal
	 */
	@Test
	public void test_59_6_InsetShapeWithPadding1() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(ellipse(), ""),"kite9-padding: 15px;")));
	}
	
	/**
	 * Uses contents AND padding to set the size of the decal
	 */
	@Test
	public void test_59_7_InsetShapeWithPadding2() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(ellipse(), "kite9-padding: 15px;"),"")));
	}
	
	/**
	 * Uses contents to set the size of the decal again
	 */
	@Test
	public void test_59_8_InsetPolygon() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(polygon(), ""),"")));
	}
	
	/**
	 * Uses contents to set the size of the decal
	 */
	@Test
	public void test_59_9_InsetRect() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(redRect(), ""),"kite9-padding: 10px;")));
	}
	
	/**
	 * Uses contents to set the svg of the decal
	 */
	@Test
	public void test_59_10_TextAndBadge() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(text("hello something", "")+badgeDecal(),"kite9-padding: 10px;")));
	}
	
	@Test
	public void test_59_11_TextAndScaledDecal() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(text("hello something else", "")+polyDecal(),"kite9-padding: 10px;")));
	}

	private String svgLeaf(String xml, String extraAtts) {
		return    "      <shape style=\"kite9-type: svg; "+extraAtts+"\">\n" 
				+ xml 
				+ "      </shape>";
	}
	
	private String text(String xml, String extraAtts) {
		return    "      <text style=\"kite9-type: text; "+extraAtts+"\">\n" 
				+ "        "+xml+"\n"
				+ "      </text>\n";
	}

	private String basicDiagram(String xml) {
		return "\n  <diagram xmlns=\"http://www.kite9.org/schema/adl\" id=\"The Diagram\">\n" + xml +"\n  </diagram>\n"; 
	}

	private String glyphContainer(String xml, String extraAtts) {
		return "    <rect style=\"kite9-type: container; " +extraAtts+" \">\n"+
	           "      <decal style='kite9-usage: decal; kite9-type: svg; '>\n" + 
	           "        <svg:rect x='0' y='0' width='{$width}' height='{$height}' rx='8' ry='8' style='fill: url(#glyph-background); ' class=\"glyph-back\" />\n" + 
	           "      </decal>\n" + 
	           xml + 
	           "    </rect>";
	           
	}
	
	private String glyphLeaf(String xml, String extraAtts) {
		return "    <rect style=\"kite9-type: svg; " +extraAtts+" \">\n"+
	           "      <decal style='kite9-usage: decal; kite9-type: svg; '>\n" + 
	           "        <svg:rect x='0' y='0' width='{x1}' height='{y1}' rx='8' ry='8' style='fill: url(#glyph-background); ' class=\"glyph-back\" />\n" + 
	           "      </decal>\n" + 
	           xml + 
	           "    </rect>";
	           
	}

	private String ellipse() {
		return "      <svg:ellipse cx=\"15\" cy=\"15\" rx=\"20\" ry=\"20\" stroke=\"black\" stroke-width=\"1\" />";
	}

	private String redRect() {
		return "      <svg:rect x=\"0\" cy=\"0\" width=\"20\" height=\"20\" fill=\"red\" stroke-width=\"0\" />";
	}
	
	private String badgeDecal() {
		return "      <badge style='kite9-usage: decal; kite9-type: svg; '>\n" +
	           "        <svg:circle cx='{$width-5}' cy='{$height-5}' r='15' fill='red' /> \n"+
	           "      </badge>\n";
	}
	
	private String polyDecal() {
		return "      <bg style='kite9-usage: decal; kite9-type: svg; kite9-transform: rescale;'>\n" + polygon() + 
 	           "      </bg>\n";
	}
	
	private String polygon() {
		return "      <svg:polygon points=\"200,10 250,190 160,210\" style=\"fill:lime;stroke-width:0\" />";
	}
}
