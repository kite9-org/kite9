package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.common.Kite9XMLProcessingException;

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
	@Test(expected=Kite9XMLProcessingException.class)
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
		renderDiagram(basicDiagram(glyphContainer(text("hello something", "font-size: 33px;")+badgeDecal(),"kite9-padding: 10px;")));
	}
	
	@Test
	public void test_59_11_TextAndScaledDecal() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(text("hello something else", "font-size: 25px;")+polyDecal(),"kite9-padding: 10px;")));
	}
	
	@Test
	public void test_59_12_TestContainerMinimumSize() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(text("hello something else", "font-size: 25px;")+text("hello b", "font-size: 15px; kite9-vertical-align: bottom;"),"kite9-padding: 10px; kite9-layout: down; kite9-min-height: 120px")));
	}
	
	@Test
	public void test_59_13_TestContainerMinimumSize() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(text("hello something else", "font-size: 25px;")+text("hello b", "font-size: 15px; kite9-vertical-align: bottom;"),"kite9-padding: 10px; kite9-layout: down; kite9-min-height: 120px")));
	}
	
	@Test
	public void test_59_14_TextAlign() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(
				text("hello something else", "font-size: 25px;")+
				text("hello 2", "font-size: 25px;")+
				text("bette\nmiddler", "font-size: 15px;kite9-vertical-align: center; kite9-horizontal-align: center; text-align: middle;")+
				text("hello\n bottom", "font-size: 15px; kite9-vertical-align: bottom; kite9-horizontal-align: right; text-align: end;"),
			"kite9-padding: 10px; kite9-layout: down; kite9-min-size: 150px 150px")));
	}
	
	@Test
	public void test_59_15_TextInShape() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(text(para(), "font-size: 8px;  ", textPath()),"")));
	}
	
	@Test
	public void test_59_16_ScaledLeaf() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(scaled(ellipse()),""),"")));
	}
	
	@Test
	public void test_59_17_TextDescenders() throws Exception {
		String frs =  "<svg:rect width=\"70pt\" height=\"400pt\" />";
		String text1 = "hello hello hello hello";
		String text2 = "pello hello pello";
		
		
		renderDiagram(basicDiagram(
				glyphContainer(
			glyphContainer(
				text("hello 1", "font-size: 25px; kite9-vertical-align: bottom; kite9-padding: 0px")+
				text("pello 2", "font-size: 25px; kite9-vertical-align: bottom; kite9-padding: 0px"),
			"kite9-padding: 0px; kite9-layout: right; kite9-min-size: 150px 150px") + 
			glyphContainer(
					text(text1, "font-size: 25px; kite9-vertical-align: bottom; kite9-padding: 0px", frs)+
					text(text2, "font-size: 25px; kite9-vertical-align: bottom; kite9-padding: 0px", frs),
				"kite9-padding: 0px; kite9-layout: down; ")+
			glyphContainer(
					text(text2, "font-size: 25px; kite9-vertical-align: bottom; kite9-padding: 0px", frs)+
					text(text2, "font-size: 25px; kite9-vertical-align: bottom; kite9-padding: 0px", frs),
				"kite9-padding: 00px; kite9-layout: down;"), "kite9-vertical-align: top")));
	}
	
	@Test
	public void test_59_18_TestTextBoundedSize() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(
				text("hello something else", "font-size: 25px; kite9-text-bounds: 150px 500px; ")
				+text("a b c d e f g h i j k l m n o p q r s t u v w x y z", "font-size: 25px; kite9-text-bounds: 150px 100px; ")
				+text("hello b this could be a long bit of text", "font-size: 15px; kite9-vertical-align: bottom; kite9-text-bounds: 100px 500px;"),"kite9-padding: 10px; kite9-layout: down; kite9-min-height: 120px")));
	}
	
	@Test
	public void test_59_19_TestTextBoundedSize() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(
				text("hello something else", "font-size: 25px; kite9-text-bounds: 150px 500px; ")
				+text("hello b this could be a long bit of text", "font-size: 15px; kite9-vertical-align: bottom; kite9-text-bounds: 100px 500px;"),"kite9-padding: 10px; kite9-layout: down; kite9-min-height: 120px")));
	}
	
	@Test
	public void test_59_20_AngledText() throws Exception {
		renderDiagram(basicDiagram(
				glyphContainer(
						textWithTransform(para(), "font-size: 8px;", "scale(4)"),"") + 
				glyphContainer(
						textWithTransform(para(), "font-size: 16px;", "rotate(55)"),"")+
				glyphContainer(
						textWithTransform(para(), "font-size: 8px;", "skewX(-45) scale(1.5)"),"")
				
				));
	}
	
	/**
	 * Uses contents to set the size of the decal
	 */
	@Test
	public void test_59_21_RotatedShape() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(longEllipse(), "", ""),"")));
	}
	
	
	private String para() {
		return "<svg:flowDiv><svg:flowPara>Search the world's information, including webpages, images, videos and more.</svg:flowPara> "+
			      "<svg:flowPara>Google has many special features to help you find exactly what you.</svg:flowPara></svg:flowDiv>";
	}
	
	private String textPath() {
		return "<svg:path d=\"M100,50L50,300L250,300L200,50z\" />";
	}
	
	private String svgLeaf(String xml, String extraAtts) {
		return    "      <shape style=\"kite9-type: svg; "+extraAtts+"\">\n" 
				+ xml 
				+ "      </shape>";
	}
	
	private String svgLeaf(String xml, String extraAtts, String transform) {
		return    "      <shape transform=\""+transform+"\" style=\"kite9-type: svg; "+extraAtts+"\">\n" 
				+ xml 
				+ "      </shape>";
	}
	
	private String textWithTransform(String xml, String extraAtts, String transform) {
		return    "      <text transform=\""+transform+"\" style=\"kite9-type: text; font-family:  opensans-light-webfont; "+extraAtts+"\">\n" 
				+ "        "+xml+"\n"
				+ "      </text>\n";
	}
	
	private String text(String xml, String extraAtts) {
		return    "      <text style=\"kite9-type: text; font-family:  opensans-light-webfont; "+extraAtts+"\">\n" 
				+ "        "+xml+"\n"
				+ "      </text>\n";
	}
	
	private String text(String xml, String extraAtts, String flowRegionShape) {
		return    "      <text style=\"kite9-type: text; font-family:  opensans-light-webfont; "+extraAtts+"\">\n" 
				+ "        "+xml+"\n"
				+ "        <svg:flowRegion>"
				+ "          "+flowRegionShape
				+ "        </svg:flowRegion>"
				+ "      </text>\n";
	}

	private String basicDiagram(String xml) {
		return "\n  <diagram xmlns=\"http://www.kite9.org/schema/adl\" id=\"The Diagram\">\n" + xml +"\n  </diagram>\n"; 
	}

	private String scaled(String xml) {
		return "      <svg:g transform=\"scale(3, 3)\">\n"+
	           xml + 
	           "      </svg:g>";
	           
	}
	
	private String glyphContainer(String xml, String extraAtts) {
		return "    <rect style=\"kite9-type: container; " +extraAtts+" \">\n"+
	           "      <decal style='kite9-usage: decal; kite9-type: svg; '>\n" + 
	           "        <svg:rect x='0' y='0' width='#{$width}' height='#{$height}' rx='8' ry='8' style='fill: url(#glyph-background); ' class=\"glyph-back\" />\n" + 
	           "      </decal>\n" + 
	           xml + 
	           "    </rect>";
	           
	}
	
	private String glyphLeaf(String xml, String extraAtts) {
		return "    <rect style=\"kite9-type: svg; " +extraAtts+" \">\n"+
	           "      <decal style='kite9-usage: decal; kite9-type: svg; '>\n" + 
	           "        <svg:rect x='0' y='0' width='#{$x1}' height='#{$y1}' rx='8' ry='8' style='fill: url(#glyph-background); ' class=\"glyph-back\" />\n" + 
	           "      </decal>\n" + 
	           xml + 
	           "    </rect>";
	           
	}

	private String ellipse() {
		return "      <svg:ellipse cx=\"20\" cy=\"20\" rx=\"20\" ry=\"20\" stroke=\"black\" stroke-width=\"1\" />";
	}
	
	private String longEllipse() {
		return "      <svg:ellipse cx=\"40\" cy=\"20\" rx=\"40\" ry=\"20\" stroke=\"black\" stroke-width=\"1\" transform=\"rotate(40)\" />";
	}


	private String redRect() {
		return "      <svg:rect x=\"0\" cy=\"0\" width=\"20\" height=\"20\" fill=\"red\" stroke-width=\"0\" />";
	}
	
	private String badgeDecal() {
		return "      <badge style='kite9-usage: decal; kite9-type: svg; kite9-transform: none; '>\n" +
	           "        <svg:circle cx='#{$width - (5 * $px)}' cy='5' r='15' fill='red' /> \n"+
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
