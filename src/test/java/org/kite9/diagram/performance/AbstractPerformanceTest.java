package org.kite9.diagram.performance;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.PositionableDiagramElement;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.display.complete.ADLBasicCompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.GriddedCompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.RequiresGraphicsSourceRendererCompleteDisplayer;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;
import org.kite9.diagram.visualization.format.png.BufferedImageRenderer;
import org.kite9.diagram.visualization.pipeline.full.BufferedImageProcessingPipeline;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Table;
import org.kite9.framework.serialization.XMLHelper;

public class AbstractPerformanceTest extends TestingHelp {
	
	
	public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public void render(Map<Metrics, DiagramXMLElement> diagrams) throws IOException {
		if (diagrams.size()>2) {
			Kite9Log.setLogging(false);
		}
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();

		TreeSet<Metrics> metrics = new TreeSet<Metrics>();
		Throwable fail = null;

		for (Entry<Metrics, DiagramXMLElement> d1 : diagrams.entrySet()) {
			try {
				renderDiagram(d1.getValue(), theTest, m.getName(), true, d1.getKey());
				metrics.add(d1.getKey());
			} catch (Throwable e) {
				e.printStackTrace();
				d1.getKey().exception = e.getMessage();
				fail = e;
				writeFailedDiagram(d1.getKey(), d1.getValue());
			}
		}

		Table t = new Table();
		t.addArrayRow(Metrics.HEADINGS);

		File metricsFile = getMetricsFile();
		FileWriter fw = new FileWriter(metricsFile, true);
		if (metricsFile.length()==0) {
			t.addArrayRow(Metrics.HEADINGS);
			for (String s : Metrics.HEADINGS) {
				fw.write(s);
				fw.write(",");
			}
			fw.write("\n");
		}
		
		
		for (Metrics metrics2 : metrics) {
			String[] items = metrics2.getMetrics();
			t.addArrayRow(items);
			for (String s : items) {
				fw.write(s);
				fw.write(",");
			}
			fw.write("\n");
		}
		fw.close();

		StringBuffer out = new StringBuffer(200);
		t.display(out);
		System.out.println(out);

		System.gc();
		
		if (fail != null) {
			throw new Kite9ProcessingException("Could not run all tests. Last error: " ,fail);
		}
	}

	private void writeFailedDiagram(Metrics key, DiagramXMLElement value) {
		writeOutput(this.getClass(), "failed" , key.name+".xml", new XMLHelper().toXML(value));
	}

	private void renderDiagram(DiagramXMLElement d2, Class<?> theTest, String subtest, boolean watermark, Metrics m)
			throws Exception {
		try {
			System.out.println("Beginning: "+m);
			XMLHelper helper = new XMLHelper();
			String xml = helper.toXML(d2);

			writeOutput(theTest, subtest, "diagram.xml", xml);

			DiagramXMLElement d = (DiagramXMLElement) helper.fromXML(xml);
			BufferedImageProcessingPipeline pipeline = getPipeline(watermark, m);
			BufferedImage bi = pipeline.process(d);

			writeOutput(theTest, subtest, "positions-adl.txt", getPositionalInformationADL(d));
			renderToFile(theTest, subtest, subtest + "-" + m.name + ".png", bi);

			TestingEngine.drawPositions(((MGTPlanarization) pipeline.getPln()).getVertexOrder(), theTest, subtest, subtest+"-"+m.name+"-positions.png");
			TestingEngine.testConnectionPresence(d, false, true, true);
			TestingEngine.testLayout(d);
			
			// write the outputs
			measure(d, m);

			// TestingEngine.testEdgeDirections(d);
		} catch (Throwable t) {
			throw new Exception("Could not run "+m+": ",t);
		}

	}

	private BufferedImageProcessingPipeline getPipeline(boolean watermark, final Metrics m) {
		final RequiresGraphicsSourceRendererCompleteDisplayer cd = new GriddedCompleteDisplayer(new ADLBasicCompleteDisplayer(watermark, false));
		GraphicsSourceRenderer<BufferedImage> renderer = new BufferedImageRenderer();
		return new TimingPipeline(cd, renderer);
	}

	private void measure(DiagramXMLElement d, final Metrics m) {
		new DiagramElementVisitor().visit(d, new VisitorAction() {

			public void visit(DiagramElement de) {
				if (de instanceof Connection) {
					measureLink((Connection) de, m);
				} else if ((de instanceof Glyph) || (de instanceof Arrow) || (de instanceof Context)) {
					measureElement(de, m);
				}
			}
		});
		Dimension2D ds = d.getRenderingInformation().getSize();
		
		m.totalDiagramSize =(long) (ds.getWidth() * ds.getHeight());
		m.crossings = m.crossings / 4;
		m.runDate = DF.format(new Date());
	}

	public void measureElement(DiagramElement o, Metrics m) {
		if (o instanceof PositionableDiagramElement) {
			RenderingInformation ri = ((PositionableDiagramElement) o).getRenderingInformation();
			if (ri instanceof RectangleRenderingInformation) {
				RectangleRenderingInformation rri = (RectangleRenderingInformation) ri;
				double width = rri.getSize().getWidth();
				double height = rri.getSize().getHeight();
				m.totalGlyphSize += width * height;
			} else {
				RouteRenderingInformation rri = (RouteRenderingInformation) ri;
				Dimension2D bounds = rri.getSize();
				double width = bounds.getWidth();
				double height = bounds.getHeight();
				m.totalGlyphSize += width * height;
			}
		}
	}

	private void measureLink(Connection o, Metrics m) {
		RouteRenderingInformation ri = (RouteRenderingInformation) ((Connection) o).getRenderingInformation();
		Direction lastD = null;
		Dimension2D lastP = null;
		double distance = 0;
		double turns = 0;
		double crosses = 0;

		for (int i = 0; i < ri.size(); i++) {
			Dimension2D pos = ri.getWaypoint(i);
			boolean cross = ri.isHop(i);
			if (cross) {
				crosses+=1;
			}

			Direction currentD = null;
			if (lastP != null) {
				if (pos.getWidth() > lastP.getWidth()) {
					currentD = Direction.RIGHT;
				} else if (pos.getWidth() < lastP.getWidth()) {
					currentD = Direction.LEFT;
				} else {
					if (pos.getHeight() < lastP.getHeight()) {
						currentD = Direction.UP;
					} else if (pos.getHeight() > lastP.getHeight()) {
						currentD = Direction.DOWN;
					} else {
						currentD = lastD;
					}
				}

				distance += Math.abs(pos.getWidth() - lastP.getWidth()) + Math.abs(pos.getHeight() - lastP.getHeight());
			}

			if ((lastD != null) && (lastD != currentD)) {
				turns++;
			}

			lastD = currentD;
			lastP = pos;

		}

		m.totalEdgeLength += distance;
		m.crossings += crosses;
		m.turns += turns;

	}
	
	public File getMetricsFile() {
		File dir = new File("target/functional-test/metrics/");
		dir.mkdirs();
		File metrics = new File(dir, "metrics.csv");
		return metrics;
	}
	

}
