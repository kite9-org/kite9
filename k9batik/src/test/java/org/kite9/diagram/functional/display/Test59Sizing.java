package org.kite9.diagram.functional.display;

import org.junit.Assert;
import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.processors.AbstractProcessor;

import java.io.File;
import java.net.URI;

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
		renderDiagram(basicDiagram(glyphContainer("","--kite9-padding: 15px;")));
	}
	
	/**
	 * Can we have decals in svg elements?  Apparently yes.
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
		renderDiagram(basicDiagram(glyphContainer("","--kite9-min-width: 15px; --kite9-min-height: 30px")));
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
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(ellipse(), ""),"--kite9-padding: 15px;")));
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
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(redRect(), ""),"--kite9-padding: 10px;")));
	}
	
	/**
	 * Uses contents to set the svg of the decal
	 */
	@Test
	public void test_59_10_TextAndBadge() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(text("<svg:text>hello something</svg:text>", "font-size: 33px;")+badgeDecal(),"--kite9-padding: 10px;")));
	}
	
	@Test
	public void test_59_11_TextAndScaledDecal() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(text("<svg:text>hello something else</svg:text>", "font-size: 25px;")+polyDecal(),"--kite9-padding: 10px;")));
	}
	
	@Test
	public void test_59_12_TestContainerMinimumSize() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(text("<svg:text>hello something else</svg:text>", "font-size: 25px;")+text("hello b", "font-size: 15px; --kite9-vertical-align: bottom;"),"--kite9-padding: 10px; --kite9-layout: down; --kite9-min-height: 120px")));
	}
	
	@Test
	public void test_59_14_TextAlign() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(
				text("<svg:text>hello something else</svg:text>", "font-size: 25px; line-height: 120%;")+
				text("<svg:text>hello 2</svg:text>", "font-size: 25px;")+
				text("<svg:text>bette\nmiddler</svg:text>", "font-size: 15px;--kite9-vertical-align: center; --kite9-horizontal-align: center; text-align: middle;")+
				text("<svg:text>hello\n bottom</svg:text>", "font-size: 15px; --kite9-vertical-align: bottom; --kite9-horizontal-align: right; text-align: end;"),
			"--kite9-padding: 10px; --kite9-layout: down; --kite9-min-size: 150px 150px")));
	}

	@Test
	public void test_59_16_ScaledLeaf() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(scaled(ellipse()),""),"")));
	}
	
	@Test
	public void test_59_17_TextDescenders() throws Exception {
		String frs =  "<svg:rect width=\"70pt\" height=\"400pt\" />";
		String text1 = "<svg:text>hello hello hello hello</svg:text>";
		String text2 = "<svg:text>pello hello pello</svg:text>";
		
		
		renderDiagram(basicDiagram(
				glyphContainer(
			glyphContainer(
				text("<svg:text>hello 1</svg:text>", "font-size: 25px; --kite9-vertical-align: bottom; --kite9-padding: 0px")+
				text("<svg:text>pello 2</svg:text>", "font-size: 25px; --kite9-vertical-align: bottom; --kite9-padding: 0px"),
			"--kite9-padding: 0px; --kite9-layout: right; --kite9-min-size: 150px 150px") +
			glyphContainer(
					text(text1, "font-size: 25px; --kite9-vertical-align: bottom; --kite9-padding: 0px")+
					text(text2, "font-size: 25px; --kite9-vertical-align: bottom; --kite9-padding: 0px"),
				"--kite9-padding: 0px; --kite9-layout: down; ")+
			glyphContainer(
					text(text2, "font-size: 25px; --kite9-vertical-align: bottom; --kite9-padding: 0px")+
					text(text2, "font-size: 25px; --kite9-vertical-align: bottom; --kite9-padding: 0px"),
				"--kite9-padding: 00px; --kite9-layout: down;"), "--kite9-vertical-align: top")));
	}
	
	@Test
	public void test_59_18_TestTextBoundedSize() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(
				text("<svg:text>hello something else</svg:text>", "font-size: 25px; --kite9-text-bounds: 150px 500px; ")
				+text("<svg:text>a b c d e f g h i j k l m n o p q r s t u v w x y z</svg:text>", "font-size: 25px; --kite9-text-bounds: 150px 100px; ")
				+text("<svg:text>hello b this could be a long bit of text</svg:text>", "font-size: 15px; --kite9-vertical-align: bottom; --kite9-text-bounds: 100px 500px;"),"--kite9-padding: 10px; --kite9-layout: down; --kite9-min-height: 120px")));
	}
	
	@Test
	public void test_59_19_TestTextBoundedSize() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(
				text("<svg:text style=\" white-space: pre;\" >hello something else</svg:text>", "font-size: 25px; --kite9-text-bounds: 150px 500px; ")
				+text("<svg:text>hello b this could be a long bit of text</svg:text>", "font-size: 15px; --kite9-vertical-align: bottom; --kite9-text-bounds: 100px 500px;"),"--kite9-padding: 10px; --kite9-layout: down; --kite9-min-height: 120px")));
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
		return "<svg:text>Search the world's information, including webpages, images, videos and more.\n"+
			      "Google has many special features to help you find exactly what you.</svg:text>";
	}
	
	private String svgLeaf(String xml, String extraAtts) {
		return    "      <svg:g style=\"--kite9-type: svg; "+extraAtts+"\">\n"
				+ xml 
				+ "      </svg:g>";
	}
	
	private String svgLeaf(String xml, String extraAtts, String transform) {
		return    "      <svg:g transform=\""+transform+"\" style=\"--kite9-type: svg; "+extraAtts+"\">\n"
				+ xml 
				+ "      </svg:g>";
	}
	
	private String textWithTransform(String xml, String extraAtts, String transform) {
		return    "      <svg:g style=\"--kite9-type: text; font-family:  opensans-light-webfont; "+extraAtts+"\"><svg:g transform=\""+transform+"\">\n"
				+ "        "+xml+"\n"
				+ "      </svg:g></svg:g>\n";
	}
	
	private String text(String xml, String extraAtts) {
		return    "      <svg:g style=\"--kite9-type: text; font-family:  opensans-light-webfont; "+extraAtts+"\">\n"
				+ "        "+xml+"\n"
				+ "      </svg:g>\n";
	}

	private String basicDiagram(String xml) throws Exception {
		return "\n  <svg:svg style=\"--kite9-type: diagram; \" "+
			//	"transform=\""+ DiagramKite9XMLElement.TRANSFORM+"\" "+
				"xmlns:svg=\""+ AbstractProcessor.Companion.getSVG_NAMESPACE()+"\" "+
				"xmlns:k9=\""+AbstractProcessor.Companion.getKITE9_NAMESPACE()+"\" "+
				"k9:width=\"$width\" k9:height=\"$height\" "+
				"id=\"The Diagram\">\n" +

				"<svg:defs>"+
				"<svg:style> @import url(\""+Test59Sizing.class.getResource("/stylesheets/designer.css").toURI().toString()+"\");</svg:style>"+
                "<svg:linearGradient id='glyph-background' x1='0%' x2='0%' y1='0%' y2='100%'>"+
                    "<svg:stop offset='0%' stop-color='#FFF'/>"+
                    "<svg:stop offset='100%' stop-color='#DDD'/>"+
                "</svg:linearGradient>"+
				"</svg:defs>"+

				xml +"\n  </svg:svg>\n";
	}

	private String scaled(String xml) {
		return "      <svg:g transform=\"scale(3, 3)\">\n"+
	           xml + 
	           "      </svg:g>";
	           
	}
	
	private String glyphContainer(String xml, String extraAtts) {
		return "    <svg:g style=\"--kite9-type: container; " +extraAtts+" \">\n"+
	           "      <svg:rect x='0' y='0' width='0' k9:width='$width' height='0' k9:height='$height' rx='8' ry='8' style='fill: url(#glyph-background); ' class=\"glyph-back\" />\n" +
	           xml + 
	           "    </svg:g>";
	           
	}
	
	private String glyphLeaf(String xml, String extraAtts) {
		return "    <svg:g style=\"--kite9-type: svg; " +extraAtts+" \">\n"+
	           "      <svg:g style='--kite9-usage: decal; --kite9-type: svg; '>\n" +
	           "        <svg:rect x='0' y='0' width='0' k9:width='$width' k9:height='$height' height='0' rx='8' ry='8' style='fill: url(#glyph-background); ' class=\"glyph-back\" />\n" +
	           "      </svg:g>\n" +
	           xml + 
	           "    </svg:g>";
	           
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
		return "      <svg:g style='--kite9-usage: decal; --kite9-type: svg; --kite9-transform: none; '>\n" +
	           "        <svg:circle cx='0' k9:cx='$width - (5 * $px)' cy='5' r='15' fill='red' /> \n"+
	           "      </svg:g>\n";
	}
	
	private String polyDecal() {
		return "      <svg:g style='--kite9-usage: decal; --kite9-type: svg; --kite9-transform: rescale;'>\n" + polygon() +
 	           "      </svg:g>\n";
	}
	
	private String polygon() {
		return "      <svg:polygon points=\"200,10 250,190 160,210\" style=\"fill:lime;stroke-width:0\" />";
	}
}
