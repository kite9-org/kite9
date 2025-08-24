package org.kite9.diagram.functional;

import kotlin.Pair;
import org.junit.Assert;
import org.kite9.diagram.adl.ContradictingLink;
import org.kite9.diagram.adl.HopLink;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.common.elements.Dimension;
import org.kite9.diagram.common.elements.factory.TemporaryConnectedRectangular;
import org.kite9.diagram.dom.model.AbstractDOMDiagramElement;
import org.kite9.diagram.logging.LogicException;
import org.kite9.diagram.model.*;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.position.*;
import org.kite9.diagram.model.style.ContainerPosition;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.GridContainerPosition;
import org.kite9.diagram.model.style.Placement;
import org.kite9.diagram.testing.*;
import org.kite9.diagram.testing.DiagramChecker.ConnectionAction;
import org.kite9.diagram.testing.DiagramChecker.ExpectedLayoutException;
import org.kite9.diagram.testing.HopChecker.HopAction;
import org.kite9.diagram.visualization.compaction.rect.second.popout.AligningRectangularizer;
import org.kite9.diagram.visualization.compaction2.C2Compaction;
import org.kite9.diagram.visualization.compaction2.C2Slideable;
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet;
import org.kite9.diagram.visualization.display.BasicCompleteDisplayer;
import org.kite9.diagram.visualization.pipeline.NGArrangementPipeline;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AxisHandlingGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutingInfo;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


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
	
	public void testDiagram(Diagram d, Class<?> theTest, String subtest, Checks c, boolean addressed, NGArrangementPipeline pipeline) throws IOException {
		try {
			LogicException out = null;
			GroupResult gr = null;
			try {
				// write the outputs
				writeOutput(theTest, subtest, "positions-adl.txt", getPositionalInformationADL(d).getBytes());
				if (AxisHandlingGroupingStrategy.Companion.getLAST_MERGE_DEBUG() != null) {
					writeOutput(theTest, subtest, "merges.txt", AxisHandlingGroupingStrategy.Companion.getLAST_MERGE_DEBUG().getBytes());
				}
			} catch (LogicException le) {
				out = le;
			}

			if (pipeline.getGrouping() != null) {
				TestingEngine.drawPositions(pipeline.getGrouping(), pipeline.getRoutableReader(), theTest, subtest, subtest + "-positions.png");
				TestingEngine.drawSlideables(pipeline.getCompaction(), theTest, subtest, subtest+"-compaction.png");
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
				testLayout(d);
			}
			
			if (c.checkOcclusion) {
				checkOverlap(d);
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
		} catch (AssertionError afe) {
			throw afe;
		} finally {

		}
	}

	public static void testConnectionsMeetConnecteds(Diagram d2, final boolean checkMidConnection) {
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
					Rectangle2D largerRect = new Rectangle2D.Double(r2d.getX() - 1, r2d.getY() - 1, r2d.getWidth() + 2, r2d.getHeight() + 2);
					if (largerRect.contains(p2d)) {
						// it's on the border
					} else {
						throw new LayoutErrorException(c + " doesn't meet " + v + "\nc = " + rri.getRoutePositions() + "\n v= " + r2d);
					}

					if (checkMidConnection) {
						Direction connectionSide = getConnectionSide(c, v, r2d);
						if (connectionsOnSide(v, connectionSide, r2d) == 1) {
							switch (connectionSide) {
							case UP:
							case DOWN:
								if (isAligning(v, connectionSide)) {
									double alignPoint = getAlignPoint(r2d.getX(), r2d.getWidth(), v, connectionSide);
									if (Math.abs(p2d.getX() - alignPoint) > 2) {
										if (!straightWithLayoutException(c, v)) {
											throw new LayoutErrorException(c + " Not mid side of " + v + ": " + r2d + " and " + p2d);
										}
									}
								}
								break;
							case LEFT:
							case RIGHT:
								if (isAligning(v, connectionSide)) {
									double alignPoint = getAlignPoint(r2d.getY(), r2d.getHeight(), v, connectionSide);
									if (Math.abs(p2d.getY() - alignPoint) > 2) {
										if (!straightWithLayoutException(c, v)) {
											throw new LayoutErrorException(c + " Not mid side of " + v + ": " + r2d + " and " + p2d);
										}
									}
								}
								break;
							}

						}

					}
				}
			}

			private double getAlignPoint(double s, double len, Connected v, Direction connectionSide) {
				Placement p = v.getConnectionAlignment(connectionSide);
				Pair<Double, Double> out = AligningRectangularizer.Companion.calculatePositionForPlacement(p, (int) len);
				return s + out.component1();
			}

			private boolean isAligning(Connected v, Direction side) {
				if (v instanceof SizedRectangular) {
					if (((SizedRectangular) v).getSizing(Direction.Companion.isHorizontal(side)) == DiagramElementSizing.MAXIMIZE) {
						return false;
					}
				}
				return (!(v instanceof Port)) && (v.getConnectionAlignment(side) != Placement.Companion.getNONE());
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

	public static void drawPositions(GroupResult gr, RoutableReader rr, Class<?> theTest, String subtest, String item) {
		File target = new File("build");
		if (!target.isDirectory()) {
			return;
		}

		int size = 1000;
		BufferedImage bi = new BufferedImage((int) size + 60, (int) size + 60, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) size + 60, (int) size + 60);

		Color[] cols = { Color.GREEN, Color.RED, Color.BLUE, Color.DARK_GRAY };

		drawGroup(gr.groups().iterator().next(), rr, cols, g, size, new HashSet<Group>());
		g.dispose();
		renderToFile(theTest, subtest, item, bi);

	}

	private static void setColour(C2Slideable s, Graphics2D g) {
		if (s.getOrbitAnchors().size() > 0) {
			g.setColor(Color.GRAY);
		} else if (s.getIntersectAnchors().size() >0) {
			g.setColor(Color.BLACK);
		} else if (s.getRectAnchors().size() > 0) {
			g.setColor(Color.RED);
		} else if (s.getConnAnchors().size() > 0) {
			g.setColor(Color.BLUE);
		} else {
			g.setColor(Color.GREEN);
		}
	}

	private static void outlineShape(Rectangular d, C2Compaction c, Graphics2D g) {
		RectangularSlideableSet hs = c.getSlackOptimisation(Dimension.H).getSlideablesFor(d);
		RectangularSlideableSet vs = c.getSlackOptimisation(Dimension.V).getSlideablesFor(d);
		g.setPaint(new Color(0x77777722, true));
		int width = hs.getR().getMinimumPosition() - hs.getL().getMinimumPosition();
		int height = vs.getR().getMinimumPosition() - vs.getL().getMinimumPosition();
		g.fillRect(hs.getL().getMinimumPosition()*10+30, vs.getL().getMinimumPosition()*10+30,
				width*10, height*10);

		if (d instanceof Container) {
			((Container) d).getContents().forEach(i -> {
				if (i instanceof Rectangular)
					outlineShape((Rectangular) i, c, g);
				}
			);
		}
	}

    public static boolean hasConnectedContents(DiagramElement e) {
        if (e instanceof Container) {
            Container c = (Container) e;
            long count = c.getContents()
                    .stream()
                    .filter(o -> o instanceof Connected)
                    .count();
            return count > 0;
        }

        return false;
    }


    public static boolean isContainerIntersection(C2Slideable s) {
        return s.getIntersectingElements()
                .stream()
                .filter(it -> hasConnectedContents(it))
                .collect(Collectors.toList())
                .size() > 0;
    }

	public static void drawSlideables(C2Compaction c2, Class<?> theTest, String subtest, String item) {
		File target = new File("build");
		if (!target.isDirectory()) {
			return;
		}

		int xSize = c2.getSlackOptimisation(Dimension.H).getAllSlideables().stream()
				.max((s1, s2) -> Integer.valueOf(s1.getMinimumPosition()).compareTo(s2.getMinimumPosition()))
				.get().getMinimumPosition();

		int ySize = c2.getSlackOptimisation(Dimension.V).getAllSlideables().stream()
				.max((s1, s2) -> Integer.valueOf(s1.getMinimumPosition()).compareTo(s2.getMinimumPosition()))
				.get().getMinimumPosition();


		BufferedImage bi = new BufferedImage((int) xSize*10 + 60, (int) ySize*10 + 60, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		//g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1f));
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) xSize*10 + 60, (int) ySize*10 + 60);

		outlineShape(c2.getDiagram(), c2, g);

		final int[] nextCol = { 0 };
		c2.getSlackOptimisation(Dimension.V).getAllSlideables().stream().forEach(s -> {
			if (!s.isDone()) {
				setColour(s, g);
				g.drawLine(20, s.getMinimumPosition() * 10 + 30, xSize * 10 + 40, s.getMinimumPosition() * 10 + 30);
				nextCol[0]++;
				g.drawString("v" + s.getNumber(), new Random().nextInt(20), s.getMinimumPosition() * 10 + 15+new Random().nextInt(10));
				g.drawString("" + s.getMinimumPosition(), xSize * 10, s.getMinimumPosition() * 10 + 20);
				Set<C2Slideable> is = c2.getIntersections(s);
				if (is != null) {
					is.forEach(s2 -> {
						g.setPaint(Color.BLACK);
						g.fillRect(s2.getMinimumPosition() * 10 + 20, s.getMinimumPosition() * 10 + 20,
								20, 20);
					});
				}
			}
		});

		c2.getSlackOptimisation(Dimension.H).getAllSlideables().stream().forEach(s -> {
			if (!s.isDone()) {
				setColour(s, g);
				g.drawLine(s.getMinimumPosition() * 10 + 30, 20, s.getMinimumPosition() * 10 + 30, ySize * 10 + 40);
				nextCol[0]++;
				g.drawString("h" + s.getNumber(), s.getMinimumPosition() * 10 + 30, 10);
				g.drawString("" + s.getMinimumPosition(), s.getMinimumPosition() * 10 + 30, ySize * 10 + 50);
				Set<C2Slideable> is = c2.getIntersections(s);
				if (is != null) {
					is.forEach(s2 -> {
                        if (isContainerIntersection(s2) || isContainerIntersection(s)) {
                            g.setPaint(Color.BLUE);
                            g.setPaint(new Color(60, 60, 255, 189));
                        } else {
                            g.setPaint(Color.BLACK);
                        }
						g.fillRect(s.getMinimumPosition() * 10 + 20, s2.getMinimumPosition() * 10 + 20,
								20, 20);
					});
				}
			}
		});



		g.dispose();
		renderToFile(theTest, subtest, item, bi);

	}

	private static void drawGroup(Group group, RoutableReader rr, Color[] cols, Graphics2D g, int size, HashSet<Group> done) {
		if (group instanceof LeafGroup) {
			if (!done.contains(group)) {
				PositionRoutingInfo pri = (PositionRoutingInfo) rr.getPlacedPosition(group);

				if (pri != null) {
					int xr = new Random().nextInt(10) - 5;
					int yr = new Random().nextInt(10) - 5;

					g.setColor(cols[Math.abs(group.hashCode()) % 4]);
					g.setStroke(new BasicStroke(1));
					g.drawRoundRect((int) (pri.getMinX() * size + xr), (int) (pri.getMinY() * size + yr), (int) (pri.getWidth() * size), (int) (pri.getHeight() * size), 3, 3);

					String id = group.getID();
					if (((LeafGroup) group).getConnected() != null) {
						id += " "+((LeafGroup) group).getConnected().getID();
					}

					g.drawString(id, (int) (pri.centerX() * size + xr), (int) (pri.centerY() * size + yr));
				}
				done.add(group);
			}
		} else {
			drawGroup(((CompoundGroup) group).getA(), rr, cols, g, size, done);
			drawGroup(((CompoundGroup) group).getB(), rr, cols, g, size, done);
		}
	}

	public static void testConnectionPresence(Diagram d, final boolean checkStraight, final boolean checkEdgeDirections, final boolean checkNoContradictions) {
		final Set<Connection> notPresent = new HashSet<>();

		ConnectionAction ca = new ConnectionAction() {

			public void action(RouteRenderingInformation rri, Object d, Connection c) {
				if ((rri == null) || (rri.getLength() == 0)) {
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
				return ((AbstractDOMDiagramElement)c).getDOMElement().getAttribute(Link.LINK_TEST).equals(ContradictingLink.CONTRADICTING);
			}

			private boolean isTurnLink(Connection c) {
				return ((AbstractDOMDiagramElement)c).getDOMElement().getAttribute(Link.LINK_TEST).equals(TurnLink.TURN);
			}

		};

		DiagramChecker.checkConnnectionElements(d, ca);
		if (notPresent.size() > 0) {
			throw new ElementsMissingException("Diagram Elements not included " + notPresent + " missing", notPresent.size());
		}
	}

	public static void testHopCount(Diagram d) {
		HopChecker.checkHops(d, new HopAction() {

			@Override
			public void action(RouteRenderingInformation rri, int hopCount, Connection c) {
				if ((hopCount > 0) && (!isHopLink(c))) {
					throw new ExpectedLayoutException("Connection overlap on: " + c);
				}
			}

			private boolean isHopLink(Connection c) {
				return ((AbstractDOMDiagramElement)c).getDOMElement().getAttribute(Link.LINK_TEST).equals(HopLink.HOP);
			}
		});
	}

	private static void checkContentsGrid(Container con) {
		List<ConnectedRectangular> connecteds = con.getContents().stream()
				.filter(cc -> cc instanceof ConnectedRectangular)
				.map(cc -> (ConnectedRectangular) cc)
				.collect(Collectors.toList());

		connecteds.forEach(c -> {
			GridContainerPosition gcpC = getGridContainerPosition(c);
			RectangleRenderingInformation rriC = c.getRenderingInformation();
			connecteds.forEach(d -> {
				if (d != c) {
					GridContainerPosition gcpD = getGridContainerPosition(d);
					if (gcpC.isSet()&& gcpD.isSet()) {
						RectangleRenderingInformation rriD = d.getRenderingInformation();
						boolean dBeforeCX = gcpD.getX().getTo() < gcpC.getX().getFrom();
						boolean dAfterCX = gcpD.getX().getFrom() > gcpC.getX().getTo();

						if (dBeforeCX) {
							checkBefore(rriD.getPosition().x(), rriD.getSize().width(), rriC.getPosition().x(), rriC.getSize().width(), d, c, Layout.RIGHT);
						}

						if (dAfterCX) {
							checkBefore(rriC.getPosition().x(), rriC.getSize().width(), rriD.getPosition().x(), rriD.getSize().width(), c, d, Layout.RIGHT);
						}

						boolean dBeforeCY = gcpD.getY().getTo() < gcpC.getY().getFrom();
						boolean dAfterCY = gcpD.getY().getFrom() > gcpC.getY().getTo();

						if (dBeforeCY) {
							checkBefore(rriD.getPosition().y(), rriD.getSize().height(), rriC.getPosition().y(), rriC.getSize().height(), d, c, Layout.DOWN);
						}

						if (dAfterCY) {
							checkBefore(rriC.getPosition().y(), rriC.getSize().height(), rriD.getPosition().y(), rriD.getSize().height(), c, d, Layout.DOWN);
						}
					}
				}
			});
		});
	}

	private static GridContainerPosition getGridContainerPosition(ConnectedRectangular c) {
		ContainerPosition cp = c.getContainerPosition();
		if (!(cp instanceof GridContainerPosition)) {
			throw new ExpectedLayoutException("Was expecting grid for "+ c.getID());
		}

		return (GridContainerPosition) cp;
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
					break;
				case GRID:
					checkContentsGrid(d);
				}
			}
			for (DiagramElement cc : d.getContents()) {
				if (!(cc instanceof Label)) {
					RenderingInformation ri = cc.getRenderingInformation();
					if ((ri instanceof RectangleRenderingInformation)) {
						checkContentContainment(cc, d, (RectangleRenderingInformation) ri);
					}
					if (cc instanceof Container) {
						testLayout((Container) cc);
					}
				}
			}
		}
	}
	
	private static void checkContentContainment(DiagramElement cc, Container d, RectangleRenderingInformation inside) {
		RectangleRenderingInformation outside = d.getRenderingInformation();
		
		Rectangle2D inR;
		Rectangle2D outR;
		try {
			inR = createRect(inside);
			outR = createRect(outside);
		} catch (Exception e) {
			e.printStackTrace();
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
		return cc instanceof TemporaryConnectedRectangular;
	}

	private void checkOverlap(final Diagram d) {
		BasicCompleteDisplayer disp = new BasicCompleteDisplayer(false);
		new DiagramElementVisitor().visit(d, new VisitorAction() {
			
			@Override
			public void visit(DiagramElement de) {
				if ((de instanceof Label) || (de instanceof ConnectedRectangular))  {
					checkOverlap((Rectangular) de, d, disp);
				}
			}
		});
		
	}
		
	private void checkOverlap(Rectangular outer, Diagram d, BasicCompleteDisplayer disp) {

		new DiagramElementVisitor().visit(d, new VisitorAction() {
			
			@Override
			public void visit(DiagramElement inner) {
				if (inner instanceof Rectangular) {
					Rectangular outer = inner.getContainer();

					if (outer == null) {
						return;
					}

					Rectangle2D outerRect;
					try {
						RectangleRenderingInformation ri = outer.getRenderingInformation();
						outerRect = createRect(ri);
					} catch (NullPointerException e) {
						throw new ElementsMissingException(outer.getID(), 1);
					}

					if ((inner != outer) && (!(inner instanceof Decal)) && (!isChildOf(outer, inner))) {
						Rectangle2D innerRect = createRect(inner.getRenderingInformation());
	
						if ((innerRect.getWidth() == 0) || (innerRect.getHeight() == 0)) {
							return;
						}
	
						if (isChildOf(inner, outer)) {
							checkContainmentPadding(outer, disp, outerRect, inner, innerRect);
	 					} else if (isIgnorableLabel((Rectangular) inner, outer)) {
							// don't do the check
						} else if (inner instanceof Rectangular) {
							checkSiblingMargins(outer, disp, outerRect, inner, innerRect);
						}
					}
				} 
			}

			private boolean isIgnorableLabel(Rectangular r, Rectangular o) {
				if (r instanceof Label) {
					DiagramElement p = r.getParent();
					if (p instanceof Connection) {
						Connection c= (Connection) p;
						if ((c.getFromLabel() == r) && (c.getFrom().getParent() == o)) {
							return true;
						}

						if ((c.getToLabel() == r) && (c.getTo().getParent() == o)) {
							return true;
						}
					}
				}

				return false;
			}

		
			private void checkSiblingMargins(Rectangular outer, BasicCompleteDisplayer disp, Rectangle2D outerRect, DiagramElement inner, Rectangle2D innerRect) {
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
							throw new LogicException(createExceptionText(outer, "DOWN", inner, downDist, outerRect, innerRect));
						}
					} else if (innerRect.getMinY() >= outerRect.getMaxY()) {
						// inner below outer
						double upDist = Math.max(disp.getMargin(inner, Direction.UP), disp.getMargin(outer, Direction.DOWN));
						if (innerRect.getMinY() - upDist < outerRect.getMaxY()) {
							throw new LogicException(createExceptionText(outer, "UP", inner, upDist, outerRect, innerRect));
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
							throw new LogicException(createExceptionText(outer, "RIGHT", inner, rightDist, outerRect, innerRect));
						}
						
					} else if (innerRect.getMinX() >= outerRect.getMaxX()) {
						// inner to right of outer
						double leftDist = Math.max(disp.getMargin(inner, Direction.LEFT), disp.getMargin(outer, Direction.RIGHT));
						if (innerRect.getMinX() - leftDist < outerRect.getMaxX()) {
							throw new LogicException(createExceptionText(outer, "LEFT", inner, leftDist, outerRect, innerRect));
						}
					} else {
						throw new LogicException("Overlapped: " + outer + " by " + inner);
					}
					
				}
			}

			private void checkContainmentPadding(Rectangular outer, BasicCompleteDisplayer disp, Rectangle2D outerRect, DiagramElement inner, Rectangle2D innerRect) {
				// de must be inside l

				if (!innerRect.intersects(outerRect)) {
					throw new LogicException("Should be inside: " + outer + " doesn't contain " + inner);
				}

				if (((Rectangular) inner).getContainer() == outer) {
					// check margins / padding

					double rightDist = disp.getPadding(outer, Direction.RIGHT);
					double leftDist = disp.getPadding(outer, Direction.LEFT);
					double upDist = disp.getPadding(outer, Direction.UP);
					double downDist = disp.getPadding(outer, Direction.DOWN);
					
					if (innerRect.getMaxX() + rightDist > outerRect.getMaxX()) {
						if (!isLabelOn(Direction.RIGHT, inner)) {
							throw new LogicException(createExceptionText(outer, "RIGHT", inner, rightDist, outerRect, innerRect));
						}
					}

					if (innerRect.getMinX() - leftDist < outerRect.getMinX()) {
						if (!isLabelOn(Direction.LEFT, inner)) {
							throw new LogicException(createExceptionText(outer, "LEFT", inner, leftDist, outerRect, innerRect));
						}
					}
					
					if (innerRect.getMaxY() + downDist > outerRect.getMaxY()) {
						if (!isLabelOn(Direction.DOWN, inner)) {
							throw new LogicException(createExceptionText(outer, "DOWN", inner, downDist, outerRect, innerRect));
						}
					}

					if (innerRect.getMinY() - upDist < outerRect.getMinY()) {
						if (!isLabelOn(Direction.UP, inner)) {
							throw new LogicException(createExceptionText(outer, "UP", inner, upDist, outerRect, innerRect));
						}
					}
				}
			}


			private boolean isLabelOn(Direction d, DiagramElement inner) {
				return (inner instanceof Label) &&
						Label.Companion.containerLabelPlacement(((Label)inner), d, Direction.UP);
			}

			private String createExceptionText(Rectangular outer, String string, DiagramElement inner, double d, Rectangle2D outerRect, Rectangle2D innerRect) {
				return "Too Close on "+string+" dist: "+d+" side: \n"+
						" inner: "+inner+" "+innerRect+"\n"+
						" outer: "+outer+" "+outerRect+"\n";
			}


		
			private boolean isInGrid(Rectangular inner) {
				return (inner instanceof ConnectedRectangular) && (inner.getContainer().getLayout() == Layout.GRID);
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
						if (((Container) p).getContents().contains(de)) {
							return true;
						} else if (de.getContainer() == null) {
							return false;
						} else {
							return isChildOf(de.getContainer(), p);
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
			try {
				RectangleRenderingInformation ri = (RectangleRenderingInformation) r;
				return new Rectangle2D.Double(ri.getPosition().x(), ri.getPosition().y(), ri.getSize().getW(), ri.getSize().getH());
			} catch (Exception e) {
				return new Rectangle2D.Double(0,0,0,0);
			}
		} else if (r instanceof RouteRenderingInformation) {
			Optional<Rectangle2D> out = ((RouteRenderingInformation) r).getRoutePositions().stream()
					.map(p -> (Rectangle2D) new Rectangle2D.Double(p.x(), p.y(), 0, 0))
					.reduce((a, b) -> a.createUnion(b));
			
			
			return out.orElseGet(() -> new Rectangle2D.Double(0,0,0,0));
		} else {
			throw new LogicException();
		}
	}

	public static void checkContentsOverlap(Container d, final Layout l) {
		List<RectangleRenderingInformation> contRI = new ArrayList<RectangleRenderingInformation>(d.getContents().size());
		for (DiagramElement c : d.getContents()) {
			if (c instanceof ConnectedRectangular) {
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
						checkAligned(prevPos.getH(), prevSize.getH(), ccPos.getH(), ccSize.getH(), prev, cc, l);
						break;
					case LEFT:
						checkAligned(prevPos.getH(), prevSize.getH(), ccPos.getH(), ccSize.getH(), prev, cc, l);
						checkBefore(ccPos.getW(), ccSize.getW(), prevPos.getW(), prevSize.getW(), cc, prev, l);
						break;
					case RIGHT:
						checkAligned(prevPos.getH(), prevSize.getH(), ccPos.getH(), ccSize.getH(), prev, cc, l);
						checkBefore(prevPos.getW(), prevSize.getW(), ccPos.getW(), ccSize.getW(), prev, cc, l);
						break;
					case VERTICAL:
						checkAligned(prevPos.getW(), prevSize.getW(), ccPos.getW(), ccSize.getW(), prev, cc, l);
						break;
					case UP:
						checkAligned(prevPos.getW(), prevSize.getW(), ccPos.getW(), ccSize.getW(), prev, cc, l);
						checkBefore(ccPos.getH(), ccSize.getH(), prevPos.getH(), prevSize.getH(), cc, prev, l);
						break;
					case DOWN:
						checkAligned(prevPos.getW(), prevSize.getW(), ccPos.getW(), ccSize.getW(), prev, cc, l);
						checkBefore(prevPos.getH(), prevSize.getH(), ccPos.getH(), ccSize.getH(), prev, cc, l);
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
