package org.kite9.diagram.performance;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.kite9.diagram.AbstractPerformanceTest;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.framework.dom.elements.ADLDocument;
import org.kite9.framework.dom.elements.DiagramKite9XMLElement;
import org.kite9.framework.dom.elements.Kite9XMLElement;
import org.kite9.framework.logging.LogicException;

public class TestDirectedContainersMatrix extends AbstractPerformanceTest {

	public Map<Metrics, String> generateSuite(int minConnected, int maxConnected, int step1, int size) {
		Map<Metrics, String> out = new HashMap<Metrics, String>();
		for (int i = minConnected; i <= maxConnected; i += step1) {
			Metrics m = new Metrics("d-r" + i+" s "+size);
			m.connecteds = i;
			String d = generateDiagram(m, size);
			out.put(m, d);

		}

		return out;
	}

	@SuppressWarnings("rawtypes")
	private String generateDiagram(Metrics m, int size) {
		DiagramKite9XMLElement.TESTING_DOCUMENT = new ADLDocument();
		System.out.println("Starting"+size+" "+m.connecteds);
		Random r = new Random(666);

		Kite9XMLElement[][] space = new Kite9XMLElement[size][];
		for (int i = 0; i < space.length; i++) {
			space[i] = new Kite9XMLElement[size];
		}
		
		List<Glyph> items = new ArrayList<Glyph>(m.connecteds);
		while (items.size() < m.connecteds) {
			int x, y;
			do {
				x = r.nextInt(size);
				y = r.nextInt(size);
			} while (space[y][x] != null);

			Glyph g = new Glyph("x" + x+"y"+y, "", "x " + x+"y "+y, null, null);
			items.add(g);
			space[y][x] = g;
		}

		int connections = 0;

		// join horiz
		for (int y = 0; y < size; y++) {
			Kite9XMLElement current = null;
			for (int x = 0; x < size; x++) {
				if (space[y][x] != null) {
					if (current != null) {
						new Link(current, space[y][x], null, null, null, null, Direction.RIGHT);
						connections++;
					}
					current = space[y][x];
				}
			}
		}

		// join vert
		for (int x = 0; x < size; x++) {
			Kite9XMLElement current = null;
			for (int y = 0; y < size; y++) {
				if (space[y][x] != null) {
					if (current != null) {
						new Link(current, space[y][x], null, null, null, null, Direction.DOWN);
						connections++;
					}
					current = space[y][x];
				}
			}
		}
		
		List<Context> contexts = new ArrayList<Context>();
		
		int round = 0;
		
		// create some contexts
		while ((contexts.size() < size /3) && (round < 100)) {
			//System.out.println("NR:"+r.nextInt(100));
			int x1 = r.nextInt(size/2);
			int y1 = r.nextInt(size/2);
			int x2 = r.nextInt(size/2)+1;
			int y2 = r.nextInt(size/2)+1;
			boolean horiz = r.nextBoolean();
			int x3 = horiz ? Math.min(size-1, x1+x2) : x1;
			int y3 = !horiz ? Math.min(size-1, y1+y2) : y1;

			Context c = new Context("x"+x1+"y"+y1+"w"+x3+"h"+y3, new ArrayList<Kite9XMLElement>(), true, null, null);

			List<Kite9XMLElement> contents = new ArrayList<Kite9XMLElement>();
			boolean fail = false;
			for (int x = x1; x <= x3; x++) {
				for (int y = y1; y <= y3; y++) {
					Kite9XMLElement contained = space[x][y];
					if (contained != null) {
						contents.add(contained);
					}
					
					if (contained instanceof Context) {
						fail = true;
						break;
					}
					
					space[x][y]= c;
				}	
			}
			Collections.shuffle(contents, r);
			
			if (!fail) {
				
				for (Kite9XMLElement connected : contents) {
					c.appendChild(connected);
					items.remove(connected);
					System.out.println("Context "+c+" contains "+contents);
					System.out.println("Context size: "+x1+", "+y1+" - "+x2+", "+y2);
					
				}
				
				contexts.add(c);
			} else {
				round ++;
			}
		}
		

		Set<Kite9XMLElement> cl = new LinkedHashSet<Kite9XMLElement>(size*size);
		cl.addAll(contexts);
		cl.addAll(items);
		m.connections = connections;

		@SuppressWarnings("unchecked")
		DiagramKite9XMLElement out = new DiagramKite9XMLElement(new ArrayList(cl), null);
		return wrap(out);
	}

	@Test
	public void increasingConnectedsSize12() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(10, 45, 2, 12);
		render(suite1);
	}
	
	@Test
	public void increasingConnectedsSize15() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(45, 60, 2, 15);
		render(suite1);
	}
	
	@Test
	public void increasingConnectedsSize20() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(60, 90, 2, 20);
		render(suite1);
	}
	
	@Test
	public void broken() throws Exception {
		Map<Metrics, String> suite1 = generateSuite(62, 62, 2, 20);
		render(suite1);
	}
	
	private static int extract(char s, String id) {
		String sub = id.substring(id.indexOf(s)+1).replaceAll("[a-zA-Z]", " ");
		if (sub.indexOf(" ")> -1) {
			sub = sub.substring(0, sub.indexOf(" "));
		}
		
		return Integer.parseInt(sub);
	}
	
	public static void drawGroupMap(BasicMergeState ms, GroupPhase gp) {
		File f = new File("group-map.png");
		
		// figure out bounds
		int[] bounds = new int[]{100,100, 0, 0};
		for (Group g : gp.allGroups) {
			extendBounds(bounds, g);
		}
		
		int GRID = 40;
		int PAD = 3;
		BufferedImage bi = new BufferedImage((bounds[2]+1)*GRID*2, (bounds[2]+1)*GRID, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = bi.createGraphics();	
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, (bounds[2]+1)*GRID*2, (bounds[2]+1)*GRID);
		
		for (Group g : ms.groups()) {
			boolean left = true;
			try {
				left = g.getAxis().isHorizontal();
			} catch (LogicException e) {
			}
			
			g2.setColor(left ? new Color(0f, 0f, 1f, .2f) : new Color(0f, 1f, 0f, .2f));
			
			int[] gbounds = new int[]{100,100, 0, 0};
			extendBounds(gbounds, g);
			int x = gbounds[0]*GRID+PAD+(left ? 0 : bounds[2]*GRID+GRID);
			int y = gbounds[1]*GRID+PAD;
			int w = (gbounds[2]-gbounds[0])*GRID+GRID-PAD*2;
			int h = (gbounds[3]-gbounds[1])*GRID+GRID-PAD*2;
			g2.fillRoundRect(x, y, w, h, PAD, PAD);
			
			g2.setColor(left ? Color.BLUE : Color.GREEN);
			g2.drawRoundRect(x, y, w, h, PAD, PAD);
			g2.drawString(""+g.getGroupNumber(), x+5, y+10);
			
		}
		
		for (Group g : gp.allGroups) {
			int[] gbounds = new int[]{100,100, 0, 0};
			extendBounds(gbounds, g);
			int x = gbounds[0]*GRID+(GRID/2)-PAD;
			int y = gbounds[1]*GRID+(GRID/2)-PAD;
			int w = (gbounds[2]-gbounds[0])*GRID+PAD*2;
			int h = (gbounds[3]-gbounds[1])*GRID+PAD*2;
			g2.setColor(new Color(0f, 0f, 0f, .2f));
			g2.drawRoundRect(x, y, w, h, PAD, PAD);
			g2.drawRoundRect(x+bounds[2]*GRID+GRID, y, w, h, PAD, PAD);
		}
		
		try {
			ImageIO.write(bi, "PNG", f);
		} catch (Exception e) {
			throw new LogicException("Couldn't write file",e);
		}
		
	}

	private static void extendBounds(int[] bounds, Group g) {
		if (g instanceof LeafGroup) {
			Connected c = ((LeafGroup)g).getContained();
			if (c instanceof Glyph) {
				int x = extract('x', c.getID());
				int y = extract('y', c.getID());
				bounds[0] = Math.min(bounds[0], x);
				bounds[1] = Math.min(bounds[1], y);
				bounds[2] = Math.max(bounds[2], x);
				bounds[3] = Math.max(bounds[3], y);
			}
		} else if (g instanceof CompoundGroup) {
			CompoundGroup cg = (CompoundGroup) g;
			extendBounds(bounds, cg.getA());
			extendBounds(bounds, cg.getB());
		}
	}

}
