package org.kite9.diagram;

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
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.batik.bridge.Kite9DiagramBridge;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.functional.layout.TestingEngine;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.model.visitors.DiagramElementVisitor;
import org.kite9.diagram.model.visitors.VisitorAction;
import org.kite9.diagram.performance.Metrics;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Table;

public class AbstractPerformanceTest extends AbstractFunctionalTest {
	
	
	public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public void render(Map<Metrics, String> diagrams) throws IOException {
		if (diagrams.size()>2) {
			Kite9Log.setLogging(false);
		}
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();

		TreeSet<Metrics> metrics = new TreeSet<Metrics>();
		Throwable fail = null;

		for (Entry<Metrics, String> d1 : diagrams.entrySet()) {
			try {
				String xml = d1.getValue();
				renderDiagram(xml, theTest, m.getName(), true, d1.getKey());
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
	
	protected String wrap(DiagramKite9XMLElement x) {
		String xml = new XMLHelper().toXML(x.getOwnerDocument());
		return addSVGFurniture(xml);
	}

	private void writeFailedDiagram(Metrics key, String value) {
		File f = getOutputFile("failed.xml");
		
//		RepositoryHelp.
//		writeOutput(this.getClass(), "f.xm;ailed" , key.name+".xml", value);
	}

	private void renderDiagram(String xml, Class<?> theTest, String subtest, boolean watermark, Metrics m)
			throws Exception {
		try {
			System.out.println("Beginning: "+m);
			currentMetrics = m;
			
			transcodeSVG(xml);
			Kite9XMLElement d = Kite9DiagramBridge.lastDiagram;
			AbstractArrangementPipeline pipeline = Kite9DiagramBridge.lastPipeline;
			
			TestingEngine.drawPositions(((MGTPlanarization) pipeline.getPln()).getVertexOrder(), theTest, subtest, subtest+"-"+m.name+"-positions.png");
			TestingEngine.testConnectionPresence(d, false, true, true);
			TestingEngine.testLayout((Container) d.getDiagramElement());
			
			// write the outputs
			measure(d, m);

			// TestingEngine.testEdgeDirections(d);
		} catch (Throwable t) {
			throw new Exception("Could not run "+m+": ",t);
		}

	}

	private void measure(Kite9XMLElement d, final Metrics m) {
		new DiagramElementVisitor().visit((Container) d.getDiagramElement(), new VisitorAction() {

			public void visit(DiagramElement de) {
				if (de instanceof Connection) {
					measureLink((Connection) de, m);
				} else if ((de instanceof Glyph) || (de instanceof Arrow) || (de instanceof Context)) {
					measureElement(de, m);
				}
			}
		});
		Dimension2D ds = d.getDiagramElement().getRenderingInformation().getSize();
		
		m.totalDiagramSize =(long) (ds.getWidth() * ds.getHeight());
		m.crossings = m.crossings / 4;
		m.runDate = DF.format(new Date());
	}

	public void measureElement(DiagramElement o, Metrics m) {
		RenderingInformation ri = o.getRenderingInformation();
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

	private static Metrics currentMetrics;
	
	@Override
	protected File getOutputFile(String ending) {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		File f = TestingHelp.prepareFileName(theTest, "", m.getName()+currentMetrics.name+"-"+ending);
		return f;
	
	}
	

}
