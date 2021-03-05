package org.kite9.diagram.testing;

import org.kite9.diagram.logging.LogicException;
import org.kite9.diagram.logging.Table;
import org.kite9.diagram.model.*;
import org.kite9.diagram.model.position.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Helps with comparing the results of tests
 * 
 * @author robmoffat
 *
 */
public class TestingHelp {

    private static final String TARGET_DIR = "target/functional-test/outputs";
    
    
	public static File prepareFileName(Class<?> theTest, String subtest, String item) {
		String directory = getFullFileName(theTest, subtest);
		File f = new File(TARGET_DIR);
		f.mkdirs();
		File f2 = new File(f, directory);
		f2.mkdirs();

		File f3 = new File(f2, item);
		return f3;
	}

	public static void writeOutput(Class<?> theTest, String subtest, String item, byte[] contents) {
		File f = prepareFileName(theTest, subtest, item);
		try {
			FileOutputStream fw = new FileOutputStream(f);
			fw.write(contents);
			fw.close();
		} catch (IOException e) {
			throw new LogicException("Could not save output: " + f.toString(), e);
		}
	}

	public static void renderToFile(Class<?> theTest, String subtest, String item, BufferedImage bi) {
		File f = prepareFileName(theTest, subtest, item);
		try {
			ImageIO.write(bi, "PNG", f);
		} catch (IOException e) {
			throw new LogicException("Could not save output: " + f.toString(), e);
		}
	}


	/**
	 * Produces a report containing all the elements of the diagram
	 */
	public String getPositionalInformationADL(Diagram d) {
		StringBuilder details = new StringBuilder();
		Rowify cr = getIdentifiableRowify();
		Rowify lr = getLinkRowify();

		getPositions(d, details, Container.class, cr);
		getPositions(d, details, Connected.class, cr);
		getPositions(d, details, Diagram.class, cr);
		getPositions(d, details, Label.class, cr);
		getPositions(d, details, Leaf.class, cr);
		getPositions(d, details, Connection.class, cr);
		
		return details.toString();
	}

	

	private Rowify getLinkRowify() {
		return new Rowify() {

	    	double distance = 0;
	    	double turns = 0;
	    	double crosses = 0;
			
			public String[] rowify(Object o) {
			    if (o instanceof Connection ) {
			    	String fromid = ((Connection)o).getFrom().getID();
			    	String toid = ((Connection)o).getTo().getID();
			    	
			    	RouteRenderingInformation ri = (RouteRenderingInformation) ((Connection)o).getRenderingInformation();
			    	Direction lastD = null;
			    	Dimension2D lastP= null;
			    	
			    	for (int i = 0; i < ri.getLength(); i++) {
						Dimension2D pos = ri.getWaypoint(i);
						if (ri.isHop(i)) {
							crosses ++;
						}
						
						Direction currentD = null;
						if (lastP!=null) {
							if (pos.getW() > lastP.getW()) {
								currentD = Direction.RIGHT;
							} else if (pos.getW() < lastP.getW()) {
								currentD = Direction.LEFT;
							} else {
								if (pos.getH() < lastP.getH()) {
									currentD = Direction.UP;
								} else if (pos.getH() > lastP.getH()){
									currentD = Direction.DOWN;
								} else {
									currentD = lastD;
								}
							}
							
							distance += Math.abs(pos.getW() - lastP.getW()) + Math.abs(pos.getH() - lastP.getH());
						}
						
						if ((lastD != null) && (lastD!=currentD)) {
							turns ++;
						}
						
						
						lastD = currentD;
						lastP = pos;
						
					}
			    	
			    	return new String[] { fromid, toid,  "", "", ""  };			    		
			    }
			    throw new LogicException("Type not connected: "+o);
			}

			public String[] getHeaders() {
				return new String[] { "From ID", "To ID", "Length", "Turns", "Crosses"};
			}

			public String[] summary() {
				return new String[] { "Total", "", ""+distance, ""+turns, ""+crosses};
			}
		};
	}



	interface Rowify {

		String[] rowify(Object o);

		String[] getHeaders();
		
		String [] summary();

	}

	private Rowify getIdentifiableRowify() {
		return new Rowify() {

			public String[] rowify(Object o) {
			    if (o instanceof DiagramElement ) {
			    	int connections = (o instanceof Connected) ? ((Connected)o).getLinks().size() : 0;
			    
			    	String id = ((DiagramElement) o).getID();
			    	RenderingInformation ri = ((DiagramElement)o).getRenderingInformation();
			    	if (ri instanceof RectangleRenderingInformation) {
			    		RectangleRenderingInformation rri = (RectangleRenderingInformation) ri;
			    		if (rri.getSize() == null) {
			    			return new String[] { id, "na", "na", ""+connections };
			    		}
			    		double width = rri.getSize().getW();
			    		double height = rri.getSize().getH();
					    return new String[] { id, ""+width, ""+height, ""+connections };
			    	} else {
			    		RouteRenderingInformation rri = (RouteRenderingInformation) ri;
			    		Dimension2D bounds = rri.getSize();
			    		double width = bounds.getW();
			    		double height = bounds.getH();
					    return new String[] { id, ""+width, ""+height, ""+connections };			    		
			    	}
				
			    }
			    throw new LogicException("Type not connected: "+o);
			}

			public String[] getHeaders() {
				return new String[] { "ID", "Width", "Height", "Connections" };
			}

			public String[] summary() {
				return null;
			}
		};
	}
	
	private <X extends DiagramElement> void getPositions(Diagram d, StringBuilder details, final Class<X> class1,
			Rowify r) {
		final SortedSet<X> items = new TreeSet<X>();
		new DiagramElementVisitor().visit(d, new VisitorAction() {

			@SuppressWarnings("unchecked")
			public void visit(DiagramElement de) {
				if (class1.isInstance(de)) {
					items.add((X) de);
				}
			}
		});

		if (items.size() > 0) {
			details.append("\n\n");
			details.append(class1.getSimpleName());
			details.append("\n-------------------------------------\n");

			Table t = new Table();
			t.addRow((Object[]) r.getHeaders());

			for (X x : items) {
				t.addRow((Object[]) r.rowify(x));
			}

			String[] summary = r.summary();
			if (summary != null) {
				t.addRow((Object[])summary);
			}
			
			t.display(details);
		}
		
	}

	public static String getFullFileName(Class<?> testClass, String name) {
		String packageName = testClass.getPackage().getName().replace(".", "/");
		String className = testClass.getSimpleName();
		String directory = packageName + "/" + className;
		directory = directory.toLowerCase();
		if (name!=null) {
			return directory + "/" + name;
		} else {
			return directory;
		}
	}
}
