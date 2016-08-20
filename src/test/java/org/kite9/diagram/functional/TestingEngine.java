package org.kite9.diagram.functional;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.Source;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visualization.display.complete.ADLBasicCompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.GriddedCompleteDisplayer;
import org.kite9.diagram.visualization.format.pdf.PDFRenderer;
import org.kite9.diagram.visualization.format.pos.DiagramChecker;
import org.kite9.diagram.visualization.format.pos.DiagramChecker.ConnectionAction;
import org.kite9.diagram.visualization.format.pos.DiagramChecker.ExpectedLayoutException;
import org.kite9.diagram.visualization.format.pos.HopChecker;
import org.kite9.diagram.visualization.format.pos.HopChecker.HopAction;
import org.kite9.diagram.visualization.format.pos.PositionInfoRenderer;
import org.kite9.diagram.visualization.format.svg.ADLAndSVGRenderer;
import org.kite9.diagram.visualization.format.svg.SVGRenderer;
import org.kite9.diagram.visualization.pipeline.full.BufferedImageProcessingPipeline;
import org.kite9.diagram.visualization.pipeline.full.ImageProcessingPipeline;
import org.kite9.diagram.visualization.pipeline.rendering.ClientSideMapRenderingPipeline;
import org.kite9.diagram.visualization.pipeline.rendering.ImageRenderingPipeline;
import org.kite9.diagram.visualization.planarization.AbstractPlanarizer;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.PlanarizationException;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutingInfo;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.StylesheetReference;
import org.kite9.framework.common.DiffException;
import org.kite9.framework.common.FileDiff;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.serialization.XMLHelper;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonListener;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DOMDifferenceEngine;
import org.xmlunit.diff.DifferenceEngine;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

@Ignore
public class TestingEngine extends TestingHelp {

	boolean serialize = false;

	String resultsFile;

	public TestingEngine(String resultsFile) {
		this.resultsFile = resultsFile;
	}

	public TestingEngine(String resultsFile, boolean serialize) {
		this.resultsFile = resultsFile;
		this.serialize = serialize;
	}

	public BufferedImageProcessingPipeline getPipeline(Class<?> test, String subtest, boolean watermark) {
		return new BufferedImageProcessingPipeline(subtest, test, watermark);
	}

	public DiagramXMLElement renderDiagram(DiagramXMLElement d, boolean watermark, boolean checkDiagramSize, boolean checkEdgeDirections, boolean checkNoHops, boolean everythingStraight, boolean checkLayout,
			boolean checkNoContradictions, boolean checkImage) throws IOException {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		boolean addressed = m.getAnnotation(NotAddressed.class) == null;
		Class<?> theTest = m.getDeclaringClass();
		try {
			if (d.getStylesheetReference() == null) {
				URL u = getClass().getResource("/stylesheets/designer2012.css");
				d.setStylesheetReference(new StylesheetReference(d.getOwnerDocument(), u.toString()));
			}
			
			
			BufferedImageProcessingPipeline pipeline = getPipeline(theTest, m.getName(), watermark);
			return renderDiagram(d, theTest, m.getName(), watermark, checkDiagramSize, checkEdgeDirections, checkNoHops, everythingStraight, checkLayout, checkNoContradictions, checkImage,
					addressed, pipeline);
		} catch (RuntimeException t) {
			if (!addressed) {
				if (!System.getProperties().containsKey("ignoreNotAddressed")) {
					Assert.fail("Not addressed - " + t.getMessage());
				}
				t.printStackTrace();
				return null;
			} else {
				throw t;
			}
		}
	}

	private DiagramXMLElement renderDiagram(DiagramXMLElement d2, Class<?> theTest, String subtest, boolean watermark, boolean checkDiagramSize, boolean checkEdgeDirections, boolean checkNoHops,
			boolean everythingStraight, boolean checkLayout, boolean checkNoContradictions, boolean checkImage, boolean addressed, BufferedImageProcessingPipeline pipeline)
					throws IOException {

		try {
			XMLHelper helper = new XMLHelper();
			String xml = helper.toXML(d2);

			writeOutput(theTest, subtest, "diagram.xml", xml);

			DiagramXMLElement d = serialize ? (DiagramXMLElement) helper.fromXML(xml) : d2;
			LogicException out = null;
			Planarization pln = null;
			try {
				BufferedImage bi = pipeline.process(d);
				// write the outputs
				writeOutput(theTest, subtest, "positions-adl.txt", getPositionalInformationADL(d));
				renderToFile(theTest, subtest, subtest + "-graph.png", bi);
			} catch (PlanarizationException pe) {
				pln = pe.getPlanarization();
				out = pe;
			} catch (LogicException le) {
				out = le;
			}

			if (pipeline.getPln() != null) {
				pln = pipeline.getPln();
			}

			if (pln != null) {
				AbstractPlanarizer planarizer = (AbstractPlanarizer) pipeline.getPlanarizer();
				drawPositions(planarizer.getElementMapper().allVertices(), theTest, subtest, subtest + "-positions.png");
				writeVertexOrder((MGTPlanarization) pln, theTest, subtest, subtest + "-vertex-order.txt");
			}

			if (out != null) {
				throw out;
			}

			if (checkNoHops) {
				testHopCount(d);
			}

			if (checkLayout) {
				testLayout(d.getDiagramElement());
			}

			// check the outputs. only going to check final diagrams now
			boolean ok = false;
			testConnectionPresence(d, everythingStraight, checkEdgeDirections, checkNoContradictions);

			if (checkDiagramSize) {
				ok = checkOutputs(theTest, subtest, "positions-adl.txt") || ok;
				ok = checkOutputs(theTest, subtest, "diagram.xml") || ok;
			}

			if (checkImage) {
				ok = checkIdentical(theTest, subtest, subtest + "-graph.png") || ok;
			}

			ok = true;

			if (!ok) {
				Assert.fail("No test results found for test");
			}

			handleError(theTest, subtest, true, "png");

			return d;
		} catch (RuntimeException afe) {
			handleError(theTest, subtest, !addressed, "png");
			throw afe;
		} catch (AssertionFailedError afe) {
			handleError(theTest, subtest, !addressed, "png");
			throw afe;
		}
	}

	private void writeVertexOrder(MGTPlanarization pln, Class<?> theTest, String subtest, String item) {
		List<Vertex> vertices = pln.getVertexOrder();
		StringBuilder sb = new StringBuilder();
		for (Vertex vertex : vertices) {
			sb.append(vertex);
			sb.append("\t" + vertex.getRoutingInfo());
			sb.append("\n");
		}

		writeOutput(theTest, subtest, item, sb.toString());
	}

	public static void drawPositions(Collection<Vertex> out, Class<?> theTest, String subtest, String item) {
		File target = new File("target");
		if (!target.isDirectory()) {
			return;
		}

		double size = out.size() * 40;
		size = Math.min(size, 1000);
		BufferedImage bi = new BufferedImage((int) size + 60, (int) size + 60, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) size + 60, (int) size + 60);

		Color[] cols = { Color.GREEN, Color.RED, Color.BLUE, Color.DARK_GRAY };
		int i = 0;

		for (Vertex vertex : out) {
			int xoffset = 0;
			int yoffset = 0;
			if (vertex instanceof ContainerVertex) {
				if (((ContainerVertex) vertex).getXOrdinal() == ContainerVertex.HIGHEST_ORD) {
					xoffset = -20;
				} else {
					yoffset = 5;
				}
				if (((ContainerVertex) vertex).getYOrdinal() == ContainerVertex.HIGHEST_ORD) {
					yoffset = -20;
				} else {
					yoffset = 5;
				}

			}
			PositionRoutingInfo pri = (PositionRoutingInfo) vertex.getRoutingInfo();
			if (pri != null) {
				g.setColor(cols[i % 4]);
				g.setStroke(new BasicStroke(1));
				i++;
				g.drawRoundRect((int) (pri.getMinX() * size + 20), (int) (pri.getMinY() * size + 20), (int) (pri.getWidth() * size), (int) (pri.getHeight() * size), 3, 3);
				g.drawString(vertex.getID(), (int) (pri.centerX() * size + 20) + xoffset, (int) (pri.centerY() * size + 20) + yoffset);
			}
		}
		g.dispose();
		renderToFile(theTest, subtest, item, bi);

	}

	private void handleError(Class<?> theTest, String subtest, boolean emptyIt, String extension) {
		try {
			File f = prepareFileName(theTest, subtest, subtest + "-correct."+extension);
			InputStream is = getHandleToZipEntry(theTest, subtest + "/" + subtest + "-graph."+extension);
			OutputStream os = new FileOutputStream(f);
			RepositoryHelp.streamCopy(is, os, true);

			f = prepareFileName(theTest, subtest, "positions-adl-correct.txt");
			is = getHandleToZipEntry(theTest, subtest + "/" + "positions-adl.txt");
			os = new FileOutputStream(f);
			RepositoryHelp.streamCopy(is, os, true);
		} catch (Throwable e) {
		}

		moveToError(theTest, subtest, emptyIt);
	}

	public void renderDiagramADLAndSVG(DiagramXMLElement d) throws IOException {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		boolean addressed = m.getAnnotation(NotAddressed.class) == null;
		Class<?> theTest = m.getDeclaringClass();
		try {
			renderDiagramADLAndSVG(d, theTest, m.getName(), true, addressed);
		} catch (RuntimeException t) {
			if (!addressed) {
				if (!System.getProperties().containsKey("ignoreNotAddressed")) {
					Assert.fail("Not addressed - " + t.getMessage());
				}
				t.printStackTrace();
			} else {
				throw t;
			}
		}
		
		System.out.println("d");
	}

	public void renderDiagramPDF(DiagramXMLElement d) throws IOException {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		renderDiagramPDF(d, theTest, m.getName());
	}

	public void renderDiagramSVG(DiagramXMLElement d) throws IOException {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		boolean addressed = m.getAnnotation(NotAddressed.class) == null;
		Class<?> theTest = m.getDeclaringClass();
		renderDiagramSVG(d, theTest, m.getName(), true, addressed);
	}

	public static void renderToFile(Class<?> theTest, String subtest, String item, byte[] bytes) {
		File f = prepareFileName(theTest, subtest, item);
		try {
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(bytes);
			fos.close();
		} catch (IOException e) {
			throw new LogicException("Could not save output: " + f.toString(), e);
		}
	}

	private void renderDiagramPDF(DiagramXMLElement d, Class<?> theTest, String subtest) throws IOException {
		XMLHelper helper = new XMLHelper();
		String xml = helper.toXML(d);

		writeOutput(theTest, subtest, "diagram.xml", xml);
		d = (DiagramXMLElement) helper.fromXML(xml);

		// no watermarks
		ImageProcessingPipeline<byte[]> pipeline = new ImageProcessingPipeline<byte[]>(new GriddedCompleteDisplayer(new ADLBasicCompleteDisplayer(false, false)), new PDFRenderer());
		byte[] bytes = pipeline.process(d);

		writeOutput(theTest, subtest, "positions-adl.txt", getPositionalInformationADL(d));
		renderToFile(theTest, subtest, subtest + "-graph.pdf", bytes);
		testConnectionPresence(d, false, false, false);

		// can't test pdfs, sadly
	}

	private void renderDiagramSVG(DiagramXMLElement d, Class<?> theTest, String subtest, boolean checkImage, boolean addressed) throws IOException {
		try {
			XMLHelper helper = new XMLHelper();
			String xml = helper.toXML(d);

			writeOutput(theTest, subtest, "diagram.xml", xml);
			d = (DiagramXMLElement) helper.fromXML(xml);

			// 2-stage rendering
			ImageProcessingPipeline<DiagramXMLElement> pipeline = new ImageProcessingPipeline<DiagramXMLElement>(new GriddedCompleteDisplayer(new ADLBasicCompleteDisplayer(false, true)),
					new PositionInfoRenderer());

			DiagramXMLElement processed = pipeline.process(d);

			ImageRenderingPipeline<String> p = new ImageRenderingPipeline<String>(new GriddedCompleteDisplayer(new ADLBasicCompleteDisplayer(false, false)), new SVGRenderer());

			String svg = p.render(processed);

			writeOutput(theTest, subtest, "positions-adl.txt", getPositionalInformationADL(d));
			renderToFile(theTest, subtest, subtest + "-graph.svg", svg.getBytes());
			testConnectionPresence(d, false, false, false);

			boolean ok = true;

			if (checkImage) {
				ok = checkIdentical(theTest, subtest, subtest + "-graph.svg") || ok;
			}

			if (!ok) {
				Assert.fail("No test results found for test");
			}
		} catch (RuntimeException afe) {
			handleError(theTest, subtest, !addressed, "svg");
			throw afe;
		} catch (AssertionFailedError afe) {
			handleError(theTest, subtest, !addressed, "svg");
			throw afe;
		}
	}

	public boolean checkOutputs(Class<?> theTest, String subtest, String item) throws IOException {
		try {
			File output = prepareFileName(theTest, subtest, item);
			BufferedReader check = new BufferedReader(new InputStreamReader(getHandleToZipEntry(theTest, subtest + "/" + item)));
			FileDiff.filesContainSameLines(new BufferedReader(new FileReader(output)), output.getPath(), check, getFullFileName(theTest, subtest) + "/" + item);
		} catch (NullPointerException e) {
			Assert.fail("Missing size comparison file:" + e.getMessage());
		} catch (DiffException e) {
			// Assert.fail(e.getMessage());
			return false;
		}

		return true;
	}

	public boolean checkIdentical(Class<?> theTest, String subtest, String item) throws IOException {
		try {
			File output = prepareFileName(theTest, subtest, item);
			InputStream is1 = new FileInputStream(output);
			InputStream is2 = getHandleToZipEntry(theTest, subtest + "/" + item);
			FileDiff.areFilesSame(item, item, new BufferedInputStream(is1), new BufferedInputStream(is2));
		} catch (NullPointerException e) {
			Assert.fail("Missing diagram file: " + e.getMessage());
			return false;
		} catch (DiffException e) {
			Assert.fail(e.getMessage());
			return false;
		}

		return true;
	}

	private InputStream getHandleToZipEntry(Class<?> theTest, String item) throws IOException {
		File f = new File(theTest.getClass().getResource(resultsFile).getFile());
		@SuppressWarnings("resource")
		ZipFile zip = new ZipFile(f);
		String nameReq = getFullFileName(theTest, item);
		ZipEntry ze = zip.getEntry(nameReq);
		return zip.getInputStream(ze);
	}

	public static void testConnectionPresence(DiagramXMLElement d, final boolean checkStraight, final boolean checkEdgeDirections, final boolean checkNoContradictions) {
		final int[] notPresent = { 0 };

		ConnectionAction ca = new ConnectionAction() {

			public void action(RouteRenderingInformation rri, Object d, Connection c) {
				if ((rri == null) || (rri.size() == 0)) {
					if (!isInvisible(c)) {
						notPresent[0]++;
					}
				}

				if (rri.isContradicting()) {
					if (checkNoContradictions && (!(c instanceof ContradictingLink))) {
						throw new ExpectedLayoutException("Connection contradiction set: " + c);
					}
				} else {
					if (d == DiagramChecker.MULTIPLE_DIRECTIONS) {
						if (checkStraight) {
							if (!(c instanceof TurnLink)) {
								throw new ExpectedLayoutException("Connection not straight: " + c);
							}
						}
						if (checkEdgeDirections) {
							if ((d != c.getDrawDirection() && (d != DiagramChecker.NO_DISTANCE) && (c.getDrawDirection() != null))) {
								throw new ExpectedLayoutException("Connection in wrong direction: " + c + " " + c.getDrawDirection() + " " + d);
							}
						}
					} else if (c.getDrawDirection() != null) {
						if (checkEdgeDirections) {
							if ((d != c.getDrawDirection()) && (d != DiagramChecker.NO_DISTANCE)) {
								throw new ExpectedLayoutException("Connection in wrong direction: " + c + " " + c.getDrawDirection() + " " + d);
							}
						}
					}
				}
			}

			/**
			 * It's ok not to render invisible items sometimes.
			 */
			private boolean isInvisible(Connection c) {
				return "INVISIBLE".equals(c.getShapeName());
			}
		};

		DiagramChecker.checkConnnectionElements(d, ca);
		if (notPresent[0] > 0) {
			throw new ElementsMissingException("Diagram Elements not included " + notPresent[0] + " missing", notPresent[0]);
		}
	}

	public static void testHopCount(DiagramXMLElement d) {
		HopChecker.checkHops(d, new HopAction() {

			@Override
			public void action(RouteRenderingInformation rri, int hopCount, Connection c) {
				if ((hopCount > 0) && (!(c instanceof HopLink))) {
					throw new ExpectedLayoutException("Connection overlap on: " + c);
				}
			}
		});
	}

	public static void testLayout(Container d) {
		Layout l = d.getLayoutDirection();

		DiagramElement prev = null;

		if (d.getContents() != null) {
			if (l != null) {
				switch (l) {
				case LEFT:
				case RIGHT:
				case UP:
				case DOWN:
					checkLayoutOrder(d, l, prev);
					break;
				case HORIZONTAL:
				case VERTICAL:
					checkContentsOverlap(d, l);
				}
			}
			for (DiagramElement cc : d.getContents()) {
				if (cc instanceof Container) {
					testLayout((Container) cc);
				}
			}
		}
	}

	private static void checkContentsOverlap(Container d, final Layout l) {
		List<RectangleRenderingInformation> contRI = new ArrayList<RectangleRenderingInformation>(d.getContents().size());
		for (DiagramElement c : d.getContents()) {
			RectangleRenderingInformation cRI = (RectangleRenderingInformation) c.getRenderingInformation();
			contRI.add(cRI);
		}

		Collections.sort(contRI, new Comparator<RectangleRenderingInformation>() {

			@Override
			public int compare(RectangleRenderingInformation o1, RectangleRenderingInformation o2) {
				if (l == Layout.HORIZONTAL) {
					return ((Double) o1.getPosition().x()).compareTo(o2.getPosition().x());
				} else if (l == Layout.VERTICAL) {
					return ((Double) o1.getPosition().y()).compareTo(o2.getPosition().y());
				} else {
					throw new LogicException("Wrong layout " + l);
				}
			}
		});

		RectangleRenderingInformation last = null;
		for (RectangleRenderingInformation current : contRI) {
			if (last != null) {
				if (l == Layout.HORIZONTAL) {
					if (last.getPosition().x() + last.getSize().x() > current.getPosition().x()) {
						throw new LogicException("Elements of " + d + " are not laid out horizontally");
					}
				} else {
					if (last.getPosition().y() + last.getSize().y() > current.getPosition().y()) {
						throw new LogicException("Elements of " + d + " are not laid out vertically");
					}
				}
			}
			last = current;
		}
	}

	private static void checkLayoutOrder(Container d, Layout l, DiagramElement prev) {
		for (DiagramElement cc : d.getContents()) {
			if (prev != null) {
				RectangleRenderingInformation prevRI = (RectangleRenderingInformation) prev.getRenderingInformation();
				RectangleRenderingInformation ccRI = (RectangleRenderingInformation) cc.getRenderingInformation();
				Dimension2D prevPos = prevRI.getPosition();
				Dimension2D ccPos = ccRI.getPosition();
				Dimension2D ccSize = ccRI.getSize();
				Dimension2D prevSize = prevRI.getSize();

				if (l != null) {
					switch (l) {
					case HORIZONTAL:
						checkAligned(prevPos.getHeight(), prevSize.getHeight(), ccPos.getHeight(), ccSize.getHeight(), prev, cc, l);
						break;
					case LEFT:
						checkAligned(prevPos.getHeight(), prevSize.getHeight(), ccPos.getHeight(), ccSize.getHeight(), prev, cc, l);
						checkBefore(ccPos.getWidth(), ccSize.getWidth(), prevPos.getWidth(), prevSize.getWidth(), cc, prev, l);
						break;
					case RIGHT:
						checkAligned(prevPos.getHeight(), prevSize.getHeight(), ccPos.getHeight(), ccSize.getHeight(), prev, cc, l);
						checkBefore(prevPos.getWidth(), prevSize.getWidth(), ccPos.getWidth(), ccSize.getWidth(), prev, cc, l);
						break;
					case VERTICAL:
						checkAligned(prevPos.getWidth(), prevSize.getWidth(), ccPos.getWidth(), ccSize.getWidth(), prev, cc, l);
						break;
					case UP:
						checkAligned(prevPos.getWidth(), prevSize.getWidth(), ccPos.getWidth(), ccSize.getWidth(), prev, cc, l);
						checkBefore(ccPos.getHeight(), ccSize.getHeight(), prevPos.getHeight(), prevSize.getHeight(), cc, prev, l);
						break;
					case DOWN:
						checkAligned(prevPos.getWidth(), prevSize.getWidth(), ccPos.getWidth(), ccSize.getWidth(), prev, cc, l);
						checkBefore(prevPos.getHeight(), prevSize.getHeight(), ccPos.getHeight(), ccSize.getHeight(), prev, cc, l);
						break;

					}

				}
			}
			prev = cc;
		}
	}

	private static void checkBefore(double x1, double w1, double x2, double w2, DiagramElement prev, DiagramElement cc, Layout l) {
		if (x1 + w1 > x2) {
			throw new ExpectedLayoutException("Was expecting " + prev + " before" + cc + " " + l);
		}
	}

	private static void checkAligned(double x1, double w1, double x2, double w2, DiagramElement prev, DiagramElement cc, Layout l) {
		if (x1 + w1 < x2) {
			throw new ExpectedLayoutException("Was expecting alignment of " + prev + "  and " + cc + " " + l);
		}

		if (x2 + w2 < x1) {
			throw new ExpectedLayoutException("Was expecting some alignment of " + prev + " and " + cc + " " + l);
		}
	}

	public static class ElementsMissingException extends LogicException {

		private static final long serialVersionUID = 1L;

		int count;

		public int getCountOfMissingElements() {
			return count;
		}

		public ElementsMissingException(String arg0, int count) {
			super(arg0);
			this.count = count;
		}

	}

	public void renderDiagramSizes(DiagramXMLElement d) throws IOException {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		renderDiagramSizes(d, theTest, m.getName());
	}

	public void renderDiagramSizes(DiagramXMLElement d, Class<?> theTest, String subtest) throws IOException {
		XMLHelper helper = new XMLHelper();
		String xml = helper.toXML(d);

		writeOutput(theTest, subtest, "diagram.xml", xml);

		// no watermarks
		ImageProcessingPipeline<DiagramXMLElement> pipeline = new ImageProcessingPipeline<DiagramXMLElement>(new GriddedCompleteDisplayer(new ADLBasicCompleteDisplayer(false, true)), new PositionInfoRenderer());
		DiagramXMLElement out = pipeline.process(d);

		writeOutput(theTest, subtest, "positions-adl.txt", getPositionalInformationADL(d));
		writeOutput(theTest, subtest, "sizes.xml", helper.toXML(out));

		// check the outputs. only going to check final diagrams now
		boolean ok = false;
		ok = checkOutputs(theTest, subtest, "positions-adl.txt") || ok;
		ok = checkOutputs(theTest, subtest, "sizes.xml") || ok;

		ok = true;

		if (!ok) {
			Assert.fail("No test results found for test");
		}

	}

	public void renderMap(DiagramXMLElement d) throws IOException {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();

		XMLHelper helper = new XMLHelper();
		String xml = helper.toXML(d);

		String subtest = m.getName();
		writeOutput(theTest, subtest, "diagram.xml", xml);

		// no watermarks
		BufferedImageProcessingPipeline pipeline = getPipeline(theTest, subtest, true);
		pipeline.process(d);

		ClientSideMapRenderingPipeline csmr = new ClientSideMapRenderingPipeline();
		String map = csmr.render(d);

		writeOutput(theTest, subtest, "map.txt", map);

		// check the outputs. only going to check final diagrams now
		boolean ok = false;
		ok = checkOutputs(theTest, subtest, "map.txt") || ok;

		if (!ok) {
			Assert.fail("No test results found for test");
		}
	}

	public void renderDiagramNoCopy(DiagramXMLElement d, boolean b, boolean checkDiagram) {
		// TODO Auto-generated method stub

	}

	private void renderDiagramADLAndSVG(DiagramXMLElement d, Class<?> theTest, String subtest, boolean checkImage, boolean addressed) throws IOException {
		// no watermarks
		try {
			ImageProcessingPipeline<String> pipeline = new ImageProcessingPipeline<String>(new GriddedCompleteDisplayer(new ADLBasicCompleteDisplayer(false, false), 12), new ADLAndSVGRenderer());
			String out = pipeline.process(d);

			writeOutput(theTest, subtest, "positions-adl.txt", getPositionalInformationADL(d));
			renderToFile(theTest, subtest, subtest + "-graph.adlsvg.xml", out.getBytes());
			testConnectionPresence(d, false, false, false);

			// check convert back again
			String xml = new XMLHelper().toXML(d);
			Object o = new XMLHelper().fromXML(xml);
			DiagramXMLElement d2 = (DiagramXMLElement) o;
			String xml2 = new XMLHelper().toXML(d2);

			Assert.assertEquals(xml, xml2);
			boolean ok = true;

			if (checkImage) {
				ok = checkIdentical(theTest, subtest, subtest + "-graph.adlsvg.xml") || ok;
			}

			if (!ok) {
				Assert.fail("No test results found for test");
			}

			handleError(theTest, subtest, true, "adlsvg.xml");

			
		} catch (Throwable afe) {
			handleError(theTest, subtest, !addressed, "adlsvg.xml");
			throw afe;
		}
	}

	public boolean checkIdenticalXML(Class<?> theTest, String subtest, String item) throws IOException {
		try {
			File output = prepareFileName(theTest, subtest, item);
			InputStream is1 = new FileInputStream(output);
			InputStream is2 = getHandleToZipEntry(theTest, subtest + "/" + item);
			
			Source in1 = Input.fromStream(is1).build();
			Source in2 = Input.fromStream(is2).build();
			
			DifferenceEngine diff = new DOMDifferenceEngine();
			diff.addDifferenceListener(new ComparisonListener() {
				
		        public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
		            Assert.fail("found a difference: " + comparison);
		        }
		    });
			
			diff.compare(in1, in2);
		} catch (NullPointerException e) {
			Assert.fail("Missing diagram file: " + e.getMessage());
			return false;
		}
	
		return true;
	}

}
