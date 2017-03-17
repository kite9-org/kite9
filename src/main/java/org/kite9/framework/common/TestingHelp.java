package org.kite9.framework.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Label;
import org.kite9.diagram.adl.Leaf;
import org.kite9.diagram.adl.Text;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.logging.Table;

/**
 * Helps with comparing the results of tests
 * 
 * @author robmoffat
 *
 */
public class TestingHelp {

    private static final String TARGET_DIR = "target/functional-test/outputs";
    
    private static final String TARGET_ERROR_DIR = "target/functional-test/outputs/errors";
    
    public static void copyDirectory(File sourceLocation , File targetLocation) {
        
        try {
			if (sourceLocation.isDirectory()) {
			    if (!targetLocation.exists()) {
			        targetLocation.mkdir();
			    }
			    
			    String[] children = sourceLocation.list();
			    for (int i=0; i<children.length; i++) {
			        copyDirectory(new File(sourceLocation, children[i]),
			                new File(targetLocation, children[i]));
			    }
			} else {
			    
			    InputStream in = new FileInputStream(sourceLocation);
			    OutputStream out = new FileOutputStream(targetLocation);
			    
			    // Copy the bits from instream to outstream
			    byte[] buf = new byte[1024];
			    int len;
			    while ((len = in.read(buf)) > 0) {
			        out.write(buf, 0, len);
			    }
			    in.close();
			    out.close();
			}
		} catch (IOException e) {
			throw new LogicException("Couldn't copy dir!");
		}
    }

	
	public static File prepareFileName(Class<?> theTest, String subtest, String item) {
		String directory = getFullFileName(theTest, subtest);
		File f = new File(TARGET_DIR);
		f.mkdirs();
		File f2 = new File(f, directory);
		f2.mkdirs();

		File f3 = new File(f2, item);
		return f3;
	}
	
	public static void moveToError(Class<?> theTest, String subtest, boolean emptyIt) {
		String directory = getFullFileName(theTest, subtest);
		File f = new File(TARGET_DIR);
		File f2 = new File(f, directory);
		File d = new File(TARGET_ERROR_DIR);
		d.mkdirs();
		File d2 = new File(d, directory);
		d2.mkdirs();
		d2.delete();
		
		if (!emptyIt) {
			copyDirectory(f2, d2);
		}
	}

	public static void writeOutput(Class<?> theTest, String subtest, String item, String contents) {
		File f = prepareFileName(theTest, subtest, item);
		try {
			FileWriter fw = new FileWriter(f);
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
	public String getPositionalInformationADL(DiagramXMLElement d) {
		StringBuffer details = new StringBuffer();
		Rowify cr = getIdentifiableRowify();
		Rowify lr = getLinkRowify();

		getPositions(d, details, Text.class, cr);
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
			    	
			    	for (int i = 0; i < ri.size(); i++) {
						Dimension2D pos = ri.getWaypoint(i);
						if (ri.isHop(i)) {
							crosses ++;
						}
						
						Direction currentD = null;
						if (lastP!=null) {
							if (pos.getWidth() > lastP.getWidth()) {
								currentD = Direction.RIGHT;
							} else if (pos.getWidth() < lastP.getWidth()) {
								currentD = Direction.LEFT;
							} else {
								if (pos.getHeight() < lastP.getHeight()) {
									currentD = Direction.UP;
								} else if (pos.getHeight() > lastP.getHeight()){
									currentD = Direction.DOWN;
								} else {
									currentD = lastD;
								}
							}
							
							distance += Math.abs(pos.getWidth() - lastP.getWidth()) + Math.abs(pos.getHeight() - lastP.getHeight());
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
			    		double width = rri.getSize().getWidth();
			    		double height = rri.getSize().getHeight();
					    return new String[] { id, ""+width, ""+height, ""+connections };
			    	} else {
			    		RouteRenderingInformation rri = (RouteRenderingInformation) ri;
			    		Dimension2D bounds = rri.getSize();
			    		double width = bounds.getWidth();
			    		double height = bounds.getHeight();
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
	
	private <X extends DiagramElement> void getPositions(DiagramXMLElement d, StringBuffer details, final Class<X> class1,
			Rowify r) {
		final SortedSet<X> items = new TreeSet<X>();
		new DiagramElementVisitor().visit(d.getDiagramElement(), new VisitorAction() {

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

	public static File getHandleToFileInClasspath(Class<?> testClass, String name) {
		String fullName = getFullFileName(testClass, name);
		URL u = testClass.getClassLoader().getResource(fullName);
		File file = new File(u.getFile());

		return file;
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
