package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.dom.ns.Kite9Namespaces;

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
				text("<svg:text>hello 2 left</svg:text>", "font-size: 15px; --kite9-text-bounds-width: 50px; --kite9-horizontal-align: left;")+
				text("<svg:text>middle align</svg:text>", "font-size: 15px;--kite9-vertical-align: center; --kite9-horizontal-align: center; text-align: center; --kite9-text-bounds-width: 50px; ")+
				text("<svg:text>right align</svg:text>", "font-size: 15px; --kite9-vertical-align: bottom; --kite9-horizontal-align: right; text-align: right; --kite9-text-bounds-width: 50px;")+
				text("<svg:text>hello this is some fully\nall over the place happening\njustified bottom</svg:text>", "font-size: 15px; --kite9-vertical-align: bottom; text-align: justify; --kite9-text-bounds-width: 3000px;"),
				"--kite9-padding: 10px; --kite9-layout: down; --kite9-min-size: 150px 150px")));
	}

	@Test
	public void test_59_16_ScaledLeaf() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(scaled(ellipse()),""),"")));
	}

	private String textWithBaseline(String text) {
		return text("<svg:text>"+text+"</svg:text>", "font-size: 25px; --kite9-vertical-align: bottom; --kite9-padding: 0px;");
	}

	private String textWithHanging(String text) {
		return text("<svg:text>"+text+"</svg:text>", "font-size: 25px; --kite9-vertical-align: bottom; --kite9-padding: 0px;");
	}
	
	@Test
	public void test_59_17_TextDescenders() throws Exception {
		String text1 = "hello hello hello hello";
		String text2 = "pello hello pello,";
		String text3 = ",,,,,,,,";
		String text4 = "pppp";
		String text5 = "___";


		renderDiagram(basicDiagram(
				glyphContainer(
			glyphContainer(
					textWithBaseline("hello 1")+
					textWithHanging("pello 2"),
			"--kite9-padding: 0px; --kite9-layout: right; --kite9-min-size: 150px 150px") +
					glyphContainer(
							textWithBaseline(text1) + textWithHanging(text2),
						"--kite9-padding: 0px; --kite9-layout: down; ")+
					glyphContainer(
							textWithBaseline(text4)+ textWithHanging(text4),
							"--kite9-padding: 0px; --kite9-layout: down; ")+
					glyphContainer(
							textWithBaseline(text5)+ textWithHanging(text5),
							"--kite9-padding: 0px; --kite9-layout: down; ")+
					glyphContainer(
							textWithBaseline(text3)+ textWithHanging(text3),
							"--kite9-padding: 0px; --kite9-layout: down; ")+
			glyphContainer(
					textWithBaseline(text2)+
							textWithHanging(text2),
				"--kite9-padding: 00px; --kite9-layout: down;"), "--kite9-vertical-align: top")
				));
	}

	@Test
	public void test_59_18_TestTextBoundedSize() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(
				text("<svg:text>hello something else</svg:text>", "font-size: 25px; --kite9-text-bounds-size: 20px 500px; ")
				+text("<svg:text>hello b this could be a long bit of text</svg:text>", "font-size: 15px; --kite9-vertical-align: bottom; --kite9-text-bounds-size: 100px 500px;"),"--kite9-padding: 0px; --kite9-layout: down; --kite9-min-height: 120px")));
	}
	
	/**
	 * This proves that the font choice determines the wrap.
	 */
	@Test
	public void test_59_19_TestWrap() throws Exception {
		renderDiagram(basicDiagram(
				
				glyphContainer(
					text("<svg:text>a b c d e f g h i j k l m n o p q r s t u v w x y z</svg:text>", "font-family: opensans-bold-webfont; font-size: 25px; --kite9-text-bounds-size: 200px 100px; --kite9-margin: 0; "),
						"--kite9-padding: 0px; --kite9-layout: down; ")
				+glyphContainer(
						text("<svg:text>a b c d e f g h i j k l m n o p q r s t u v w x y z</svg:text>", "font-family: opensans-light-webfont; font-size: 25px; --kite9-text-bounds-size: 200px 100px; --kite9-margin: 0; "),
							"--kite9-padding: 0px; --kite9-layout: down; "))
				);
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

	@Test
	public void test_59_21_MultipleStyleText() throws Exception {
		transcodePNG(basicDiagram(
				"<svg:g style=\"--kite9-type: text; font-weight:700; font-family: chirp; font-size: 25px; line-height: 120%;\">\n" +
				"          <svg:text>ABC Bold</svg:text>\n" +
				"      </svg:g>\n" +
				"		<svg:g style=\"--kite9-type: text; font-weight:400; font-family: chirp; font-size: 25px; line-height: 120%;\">\n" +
				"          <svg:text>ABC Regular</svg:text>\n" +
				"      </svg:g>\n"));
	}


	/**
	 * Uses contents to set the size of the decal
	 */
	@Test
	public void test_59_22_RotatedShape() throws Exception {
		renderDiagram(basicDiagram(glyphContainer(svgLeaf(longEllipse(), "", ""),"")));
	}
	
	/**
	 * More complex text wrap which includes embedded images.
	 */
	@Test
	public void test_59_23_TestImageAndTextWrap() throws Exception {
		String imageFile = Test59Sizing.class.getResource("1f170.svg").toURI().toString();
		renderDiagram(basicDiagram(
				glyphContainer(
					text("<svg:g wrap=\"true\">" +
							"<svg:text>a b c d e f g h i j k      </svg:text>"+
							"<svg:image xlink:href=\""+imageFile+"\" width=\"20\" height=\"20\"/>"+
						  	"<svg:text>     l m n o q r s t u \n v w x y z</svg:text>"+
							"</svg:g>",
							"font-family: opensans-bold-webfont; font-size: 25px; --kite9-text-bounds-size: 200px 150px; --kite9-margin: 0; "),
						"--kite9-padding: 0px; --kite9-layout: down; ")));
	}

	/**
	 * Make sure that we keep the &lt;g&gt; structure within the text element
	 * @return
	 */
	@Test
	public void test_59_24_TestTextWrapWithGroups() throws Exception {
		renderDiagram(basicDiagram(
				glyphContainer(
						text("<svg:g wrap=\"true\">" +
									    "<svg:g style=\"font-size: 25px;\"> "+
										    "<svg:text>a b c d e f g h i j k</svg:text>"+
								          "</svg:g><svg:g style=\"font-size: 45px;\">"+
										    "<svg:text>1 2 3 4 5 6 7</svg:text>"+
										  "</svg:g>" +
										"</svg:g>", "--kite9-text-bounds-size: 200px 650px;"),"")));
	}

	/**
	 * Make sure that we keep the &lt;g&gt; structure within the text element
	 * @return
	 */
	@Test
	public void test_59_25_TestTextWrapWithoutGroups() throws Exception {
		renderDiagram(basicDiagram(
				glyphContainer(
						text("<svg:g wrap=\"true\">" +
								"<svg:g style=\"font-size: 25px;\"> "+
								"<svg:text>a b c d e f g h i j k</svg:text>"+
								"<svg:text>1 2 3 4 5 6 7</svg:text>"+
								"</svg:g>" +
								"</svg:g>", "--kite9-text-bounds-size: 200px 650px;"),"")));
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
		return    "      <svg:g style=\"--kite9-type: svg; font-family:  opensans-light-webfont; "+extraAtts+"\"><svg:g transform=\""+transform+"\">\n"
				+ "        "+xml+"\n"
				+ "      </svg:g></svg:g>\n";
	}
	
	private String text(String xml, String extraAtts) {
		return    "      <svg:g style=\"--kite9-type: text; font-family:  opensans-light-webfont; "+extraAtts+"\">\n"
				+ "        "+xml+"\n"
				+ "      </svg:g>\n";
	}

	private String basicDiagram(String xml) throws Exception {
		return "\n  <svg:svg xmlns:xlink=\"http://www.w3.org/1999/xlink\" style=\"--kite9-type: diagram; --kite9-layout: down; \" "+
			//	"transform=\""+ DiagramKite9XMLElement.TRANSFORM+"\" "+
				"xmlns:svg=\""+ Kite9Namespaces.SVG_NAMESPACE +"\" "+
				"xmlns:pp=\""+Kite9Namespaces.POSTPROCESSOR_NAMESPACE+"\" "+
				"pp:width=\"$width\" pp:height=\"$height\" "+
				"id=\"The Diagram\">\n" +

				"<svg:defs>"+
				"<svg:style> @import url(\""+Test59Sizing.class.getResource("/stylesheets/tester.css").toURI().toString()+"\");</svg:style>"+
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
	           "      <svg:rect x='0' y='0' width='0' pp:width='$width' height='0' pp:height='$height' rx='8' ry='8' style='fill: url(#glyph-background); ' class=\"glyph-back\" />\n" +
	           xml + 
	           "    </svg:g>";
	           
	}
	
	private String glyphLeaf(String xml, String extraAtts) {
		return "    <svg:g style=\"--kite9-type: svg; " +extraAtts+" \">\n"+
	           "      <svg:g style='--kite9-usage: decal; --kite9-type: svg; '>\n" +
	           "        <svg:rect x='0' y='0' width='0' pp:width='[[$width]]' pp:height='$height' height='0' rx='8' ry='8' style='fill: url(#glyph-background); ' class=\"glyph-back\" />\n" +
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
	           "        <svg:circle cx='0' pp:cx='$width - (5 * $px)' cy='5' r='15' fill='red' /> \n"+
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
