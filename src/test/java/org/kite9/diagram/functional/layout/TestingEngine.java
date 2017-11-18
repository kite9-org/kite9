package org.kite9.diagram.functional.layout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.ContradictingLink;
import org.kite9.diagram.adl.HopLink;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.batik.BatikDisplayer;
import org.kite9.diagram.batik.element.AbstractDOMDiagramElement;
import org.kite9.diagram.common.elements.grid.GridTemporaryConnected;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.visitors.DiagramChecker;
import org.kite9.diagram.model.visitors.DiagramChecker.ConnectionAction;
import org.kite9.diagram.model.visitors.DiagramChecker.ExpectedLayoutException;
import org.kite9.diagram.model.visitors.DiagramElementVisitor;
import org.kite9.diagram.model.visitors.HopChecker;
import org.kite9.diagram.model.visitors.HopChecker.HopAction;
import org.kite9.diagram.model.visitors.VisitorAction;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;
import org.kite9.diagram.visualization.planarization.AbstractPlanarizer;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.PlanarizationException;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutingInfo;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.common.TestingHelp;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.xml.DiagramKite9XMLElement;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 * Responsible for running tests which check edges, hops, lines are straight etc.\
 * 
 * @author robmoffat
 *
 */
public class TestingEngine extends TestingHelp {
	
	public static class Checks {
		public boolean checkEdgeDirections = true;
		public boolean checkNoHops = true;
		public boolean everythingStraight = true;
		public boolean checkLayout = true;
		public boolean checkNoContradictions = true;
		public boolean checkOcclusion = true;
		public boolean checkConnectionsMeetConnecteds = true;
		public boolean checkMidConnection = true;
	}
	
	public void testDiagram(DiagramKite9XMLElement d, Class<?> theTest, String subtest, Checks c, boolean addressed, AbstractArrangementPipeline pipeline) throws IOException {
		try {
			LogicException out = null;
			Planarization pln = null;
			try {
				// write the outputs
				writeOutput(theTest, subtest, "positions-adl.txt", getPositionalInformationADL(d));
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

			if (c.checkNoHops) {
				testHopCount(d);
			}
			
			if (c.checkConnectionsMeetConnecteds) {
				testConnectionsMeetConnecteds(d, c.checkMidConnection);
			}

			if (c.checkLayout) {
				testLayout(d.getDiagramElement());
			}
			
			if (c.checkOcclusion) {
				checkOverlap(d.getDiagramElement());
			}

			// check the outputs. only going to check final diagrams now
			boolean ok = false;
			testConnectionPresence(d, c.everythingStraight, c.checkEdgeDirections, c.checkNoContradictions);
			
			ok = true;

			if (!ok) {
				Assert.fail("No test results found for test");
			}
		} catch (RuntimeException afe) {
			if (addressed) {
				throw afe;
			} else {
				System.err.println("Not Addressed: "+afe.getMessage());
			}
		} catch (AssertionFailedError afe) {
			throw afe;
		}
	}

	public static void testConnectionsMeetConnecteds(DiagramKite9XMLElement d, final boolean checkMidConnection) {
		Diagram d2 = d.getDiagramElement();
		new DiagramElementVisitor().visit(d2, new VisitorAction() {
			
			@Override
			public void visit(DiagramElement de) {
				if (de instanceof Connection) {
					meets((Connection)de, ((Connection) de).getFrom(), true);
					meets((Connection)de, ((Connection) de).getTo(), false);
				}
			}

			private void meets(Connection c, Connected v, boolean start) {
				RouteRenderingInformation rri = c.getRenderingInformation();
				RectangleRenderingInformation rect = v.getRenderingInformation();
				
				if (rri.getRoutePositions().size() == 0) {
					// missing element - will be picked up later though
				} else {
					Point2D p2d = getCorrectEndPoint(start, c);
					Rectangle2D r2d = createRect(rect);
					Rectangle2D largerRect = new Rectangle2D.Double(r2d.getX()-1, r2d.getY()-1, r2d.getWidth()+2, r2d.getHeight()+2);
					if (largerRect.contains(p2d)) {
						// it's on the border
					} else {
						throw new LayoutErrorException(c+" doesn't meet "+v+"\nc = " +rri.getRoutePositions()+"\n v= "+r2d);
					}
					
					if (checkMidConnection) {
						if (isMinimizedContainer(v)) {
							Direction connectionSide = getConnectionSide(c, v, r2d);
							if (connectionsOnSide(v, connectionSide, r2d) == 1) {
								switch (connectionSide) {
								case UP:
								case DOWN:
									if (Math.abs(p2d.getX() - r2d.getCenterX()) > 1) {
										if (!straightWithLayoutException(c, v)) {
											throw new LayoutErrorException(c+" Not mid side of "+v+": "+r2d+" and "+p2d);
										}
									}
									break;
								case LEFT:
								case RIGHT:
									if (Math.abs(p2d.getY() - r2d.getCenterY()) > 1) {
										if (!straightWithLayoutException(c, v)) {
											throw new LayoutErrorException(c+" Not mid side of "+v+": "+r2d+" and "+p2d);
										}
									}
									break;
								}
		 						
							}
						}
					}
				}
			}

			private boolean isMinimizedContainer(Connected v) {
				return (v instanceof Container) && (((Container) v).getSizing()==DiagramElementSizing.MINIMIZE);
			}

			/**
			 * Connections often cannot be centered when there is a layout also running through the connected.
			 */
			private boolean straightWithLayoutException(Connection c, Connected v) {
				Layout l = v.getContainer().getLayout();
				if ((l != Layout.GRID) && (l != null)) {
					// has directed layout
					
					if (c.getDrawDirection() != null) {
						return true;
					}
					
				}
				
				return false;
			}

			private long connectionsOnSide(Connected v, Direction connectionSide, Rectangle2D r) {
				return v.getLinks().stream()
					.filter(l -> getConnectionSide(l, v, r) == connectionSide)
					.count();
			}

			private Direction getConnectionSide(Connection c, Connected v, Rectangle2D r) {
				boolean start = (c.getFrom() == v);
				Point2D p = getCorrectEndPoint(start, c);
				
				if (p == null) {
					return null;
				}
				
				if (r.getMinX() == p.getX()) {
					return Direction.LEFT;
				} else if (r.getMaxX() == p.getX()) {
					return Direction.RIGHT;
				} else if (r.getMinY() == p.getY()) {
					return Direction.UP;
				} else if (r.getMaxY() == p.getY()) {
					return Direction.DOWN;
				}
				
				throw new LogicException("should meet, we tested this");
			}

			private Point2D getCorrectEndPoint(boolean start, Connection c) {
				RouteRenderingInformation rri = c.getRenderingInformation();
				if (rri.getRoutePositions().size() == 0) {
					return null;
				}
				Dimension2D d2 = start ? rri.getRoutePositions().get(0) : rri.getRoutePositions().get(rri.getRoutePositions().size()-1);
				Point2D p2d = new Point2D.Double(d2.x(), d2.y());
				return p2d;
			}
		});
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

		for (Vertex vertex : out) {
			int xoffset = 0;
			int yoffset = 0;
			if (vertex instanceof MultiCornerVertex) {
				if (((MultiCornerVertex) vertex).getXOrdinal().equals(BigFraction.ONE)) {
					xoffset = -20;
				} else {
					yoffset = 5;
				}
				if (((MultiCornerVertex) vertex).getYOrdinal().equals(BigFraction.ONE)) {
					yoffset = -20;
				} else {
					yoffset = 5;
				}

			}
			PositionRoutingInfo pri = (PositionRoutingInfo) vertex.getRoutingInfo();
			if (pri != null) {
				g.setColor(cols[Math.abs(vertex.hashCode()) % 4]);
				g.setStroke(new BasicStroke(1));
				g.drawRoundRect((int) (pri.getMinX() * size + 20), (int) (pri.getMinY() * size + 20), (int) (pri.getWidth() * size), (int) (pri.getHeight() * size), 3, 3);
				g.drawString(vertex.getID(), (int) (pri.centerX() * size + 20) + xoffset, (int) (pri.centerY() * size + 20) + yoffset);
			}
		}
		g.dispose();
		renderToFile(theTest, subtest, item, bi);

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

	public static void testConnectionPresence(DiagramKite9XMLElement d, final boolean checkStraight, final boolean checkEdgeDirections, final boolean checkNoContradictions) {
		final Set<Connection> notPresent = new HashSet<>();

		ConnectionAction ca = new ConnectionAction() {

			public void action(RouteRenderingInformation rri, Object d, Connection c) {
				if ((rri == null) || (rri.size() == 0)) {
					notPresent.add(c);
				}
				
//				if (isContradictingLink(c)) {
//					if (!rri.isContradicting()) {
//						throw new ExpectedLayoutException("Connection contradiction expected: " + c);
//					}
//				}
//				

				if (rri.isContradicting()) {
					if (checkNoContradictions && !isContradictingLink(c)) {
						throw new ExpectedLayoutException("Connection contradiction set: " + c);
					}
				} else {
					if (d == DiagramChecker.MULTIPLE_DIRECTIONS) {
						if (checkStraight) {
							if (!isTurnLink(c)) {
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

			private boolean isContradictingLink(Connection c) {
				return ((AbstractDOMDiagramElement)c).getTheElement().getAttribute(Link.LINK_TEST).equals(ContradictingLink.CONTRADICTING);
			}

			private boolean isTurnLink(Connection c) {
				return ((AbstractDOMDiagramElement)c).getTheElement().getAttribute(Link.LINK_TEST).equals(TurnLink.TURN);
			}

		};

		DiagramChecker.checkConnnectionElements(d, ca);
		if (notPresent.size() > 0) {
			throw new ElementsMissingException("Diagram Elements not included " + notPresent + " missing", notPresent.size());
		}
	}

	public static void testHopCount(DiagramKite9XMLElement d) {
		HopChecker.checkHops(d, new HopAction() {

			@Override
			public void action(RouteRenderingInformation rri, int hopCount, Connection c) {
				if ((hopCount > 0) && (!isHopLink(c))) {
					throw new ExpectedLayoutException("Connection overlap on: " + c);
				}
			}

			private boolean isHopLink(Connection c) {
				return ((AbstractDOMDiagramElement)c).getTheElement().getAttribute(Link.LINK_TEST).equals(HopLink.HOP);
			}
		});
	}

	public static void testLayout(Container d) {
		Layout l = d.getLayout();

		Connected prev = null;

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
				if ((cc instanceof Label) || (cc instanceof Connected)) {
					checkContentContainment((Rectangular) cc, d);
				}
				if (cc instanceof Container) {
					testLayout((Container) cc);
				}
			}
		}
	}
	
	private static void checkContentContainment(Rectangular cc, Container d) {
		
		if (checkTemporary(cc)) {
			return;
		}
		
		if ((cc instanceof Label) && (cc.getParent() instanceof Container)) {
			return; // not processing container labels just yet
		}
		
		RectangleRenderingInformation inside =  cc.getRenderingInformation();
		RectangleRenderingInformation outside = d.getRenderingInformation();
		
		Rectangle2D inR;
		Rectangle2D outR;
		try {
			inR = createRect(inside);
			outR = createRect(outside);
		} catch (Exception e) {
			throw new ElementsMissingException(cc.getID(), 1);
		}
		
		if ((!outR.contains(inR)) && (inR.getWidth()>0) && (inR.getHeight()>0)) {
			throw new LayoutErrorException(cc+" not entirely within "+d);
		}
	}

	/**
	 * Eventually, we shouldn't be modifying the container structure, it should be immutable.
	 * @param cc
	 * @return
	 */
	@Deprecated()
	private static boolean checkTemporary(Rectangular cc) {
		return cc instanceof GridTemporaryConnected;
	}

	private void checkOverlap(final Diagram d) {
		BatikDisplayer disp = new BatikDisplayer(false, 0);
		new DiagramElementVisitor().visit(d, new VisitorAction() {
			
			@Override
			public void visit(DiagramElement de) {
				if ((de instanceof Label) || (de instanceof Connected))  {
					checkOverlap((Rectangular) de, d, disp);
				}
			}
		});
		
	}
		
	private void checkOverlap(Rectangular outer, Diagram d, BatikDisplayer disp) {
		RectangleRenderingInformation ri = (RectangleRenderingInformation) outer.getRenderingInformation();
		Rectangle2D outerRect;
		try {
			outerRect = createRect(ri);
		} catch (NullPointerException e) {
			throw new ElementsMissingException(outer.getID(), 1);
		}
		new DiagramElementVisitor().visit(d, new VisitorAction() {
			
			@Override
			public void visit(DiagramElement inner) {
				if (inner instanceof Rectangular) {
					if ((inner != outer) && (!(inner instanceof Decal)) && (!isChildOf(outer, inner))) {
						Rectangle2D innerRect = createRect(inner.getRenderingInformation());
	
						if ((innerRect.getWidth() == 0) || (innerRect.getHeight() == 0)) {
							return;
						}
	
						if (isChildOf(inner, outer)) {
							checkContainmentPadding(outer, disp, outerRect, inner, innerRect);
	 
						} else if (inner instanceof Rectangular) {
							checkSiblingMargins(outer, disp, outerRect, inner, innerRect);
						}
					}
				} 
			}

		
			private void checkSiblingMargins(Rectangular outer, BatikDisplayer disp, Rectangle2D outerRect, DiagramElement inner, Rectangle2D innerRect) {
				if (innerRect.intersects(outerRect)) {
					throw new LogicException("Overlapped: " + outer + " by " + inner);
				}
				
				if (isInGrid((Rectangular) inner)) {
					return;
				}
				
				
				if (alongside(innerRect.getMinX(), innerRect.getMaxX(), outerRect.getMinX(), outerRect.getMaxX())) {
					// check y-separation
					
					if (innerRect.getMaxY() <= outerRect.getMinY()) {
						// inner above outer
						double downDist = Math.max(disp.getMargin(inner, Direction.DOWN), disp.getMargin(outer, Direction.UP));
						if (innerRect.getMaxY() + downDist > outerRect.getMinY()) {
							throw new LogicException("Too Close on DOWN side: "+inner+" to "+outer);
						}
					} else if (innerRect.getMinY() >= outerRect.getMaxY()) {
						// inner below outer
						double upDist = Math.max(disp.getMargin(inner, Direction.UP), disp.getMargin(outer, Direction.DOWN));
						if (innerRect.getMinY() - upDist < outerRect.getMaxY()) {
							throw new LogicException("Too Close on UP side: "+inner+" to "+outer);
						}						
					} else {
						throw new LogicException("Overlapped: " + outer + " by " + inner);
					}
					
				}
				
				if (alongside(innerRect.getMinY(), innerRect.getMaxY(), outerRect.getMinY(), outerRect.getMaxY())) {
					// check y-separation
					
					if (innerRect.getMaxX() <= outerRect.getMinX()) {
						// inner to left of outer
						double rightDist = Math.max(disp.getMargin(inner, Direction.RIGHT), disp.getMargin(outer, Direction.LEFT));
						if (innerRect.getMaxX() + rightDist > outerRect.getMinX()) {
							throw new LogicException("Too Close on RIGHT side: "+inner+" to "+outer);
						}
						
					} else if (innerRect.getMinX() >= outerRect.getMaxX()) {
						// inner to right of outer
						double leftDist = Math.max(disp.getMargin(inner, Direction.LEFT), disp.getMargin(outer, Direction.RIGHT));
						if (innerRect.getMinX() - leftDist < outerRect.getMaxX()) {
							throw new LogicException("Too Close on LEFT side: "+inner+" to "+outer);
						}
					} else {
						throw new LogicException("Overlapped: " + outer + " by " + inner);
					}
					
				}
			}

			private void checkContainmentPadding(Rectangular outer, BatikDisplayer disp, Rectangle2D outerRect, DiagramElement inner, Rectangle2D innerRect) {
				// de must be inside l

				if (!innerRect.intersects(outerRect)) {
					throw new LogicException("Should be inside: " + outer + " doesn't contain " + inner);
				}

				if (((Rectangular) inner).getContainer() == outer) {
					// check margins / padding

					double rightDist = disp.getPadding(outer, Direction.LEFT);
					double leftDist = disp.getPadding(outer, Direction.RIGHT);
					double upDist = disp.getPadding(outer, Direction.DOWN);
					double downDist = disp.getPadding(outer, Direction.UP);
					
					if (innerRect.getMaxX() + rightDist > outerRect.getMaxX()) {
						throw new LogicException("Too Close on RIGHT side: "+inner+" to "+outer);
					}

					if (innerRect.getMinX() - leftDist < outerRect.getMinX()) {
						throw new LogicException("Too Close on LEFT side: "+inner+" to "+outer);
					}
					
					if (!(inner instanceof Label)) {
						if (innerRect.getMaxY() + downDist > outerRect.getMaxY()) {
							throw new LogicException("Too Close on DOWN side: "+inner+" to "+outer);
						}
					}

					if (innerRect.getMinY() - upDist < outerRect.getMinY()) {
						throw new LogicException("Too Close on UP side: "+inner+" to "+outer);
					}
				}
			}
			
			private boolean isInGrid(Rectangular inner) {
				return (inner instanceof Connected) && (inner.getContainer().getLayout() == Layout.GRID);
			}

			protected boolean alongside(double a1, double a2, double b1, double b2) {
				return within(a1, b1, b2) || 
						within(a2, b1, b2) ||
						within(b1, a1, a2) ||
						within(b2, a1, a2);
			}

			protected boolean within(double a, double ja, double jb) {
				return ((a > ja) && (a < jb));
			}

			private boolean isChildOf(DiagramElement de, DiagramElement p) {
				if (p instanceof Container) {
					if (de instanceof Rectangular) {
						if (((Rectangular) de).getContainer() == p) {
							return true;
						} else if (((Rectangular) de).getContainer() == null) {
							return false;
						} else {
							return isChildOf(((Rectangular) de).getContainer(), p);
						}
					} else {
						return false;
					}
				} else if (p instanceof Connection) {
					return (((Connection) p).getFromLabel() == de) || (((Connection) p).getToLabel() == de);
				} else {
					return false;
				}
			}
		});
		
	}

	private static Rectangle2D createRect(RenderingInformation r) {
		if (r instanceof RectangleRenderingInformation) {
			RectangleRenderingInformation ri = (RectangleRenderingInformation) r;
			return new Rectangle2D.Double(ri.getPosition().x(), ri.getPosition().y(), ri.getSize().getWidth(), ri.getSize().getHeight());
		} else if (r instanceof RouteRenderingInformation) {
			Optional<Rectangle2D> out = ((RouteRenderingInformation) r).getRoutePositions().stream()
					.map(p -> (Rectangle2D) new Rectangle2D.Double(p.x(), p.y(), 0, 0))
					.reduce((a, b) -> a.createUnion(b));
			
			
			return out.orElseGet(() -> new Rectangle2D.Double(0,0,0,0));
		} else {
			throw new Kite9ProcessingException();
		}
	}

	public static void checkContentsOverlap(Container d, final Layout l) {
		List<RectangleRenderingInformation> contRI = new ArrayList<RectangleRenderingInformation>(d.getContents().size());
		for (DiagramElement c : d.getContents()) {
			if (c instanceof Connected) {
				RectangleRenderingInformation cRI = (RectangleRenderingInformation) c.getRenderingInformation();
				contRI.add(cRI);
			}
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

	public static void checkLayoutOrder(Container d, Layout l, Connected prev) {
		for (DiagramElement c : d.getContents()) {
			if ((prev != null) && (c instanceof Connected)) {
				Connected cc = (Connected) c;
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
					prev = cc;

				}
			}
		}
	}

	public static void checkBefore(double x1, double w1, double x2, double w2, Connected prev, Connected cc, Layout l) {
		if (x1 + w1 > x2) {
			throw new ExpectedLayoutException("Was expecting " + prev + " before" + cc + " " + l);
		}
	}

	public static void checkAligned(double x1, double w1, double x2, double w2, DiagramElement prev, DiagramElement cc, Layout l) {
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
	
	public static class LayoutErrorException extends LogicException {

		private static final long serialVersionUID = 1L;

		public LayoutErrorException(String arg0) {
			super(arg0);
		}

	}



}
