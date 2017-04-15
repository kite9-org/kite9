package org.kite9.diagram.visualization.orthogonalization.vertices;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.algorithms.Tools;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.ConnectionEdgeBendVertex;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.logging.LogicException;

/**
 * Adds fanning in and out of a vertex where there are > 1 darts on the same
 * side.  There are various reasons why a fan would not be done:
 * 1) middle edge requires no fan
 * 2) the destination vertex must be met in a straight line
 * 3) the destination must be different from the middle edge
 * 
 * @author robmoffat
 * 
 */
public class FanInVertexArranger extends BasicVertexArranger {

	public FanInVertexArranger(ElementMapper em) {
		super(em);
	}
	
	@Override
	protected Side convertEdgeToDarts(Vertex tl, Vertex tr, Direction d, DiagramElement underDe, Vertex from, List<Dart> onSide,
			Orthogonalization o, int oppositeDarts, double minLength, boolean requiresSize, BorderEdge borderEdge) {
		Side out = super.convertEdgeToDarts(tl, tr, d, underDe, from, onSide, o, oppositeDarts, minLength, requiresSize, borderEdge);

		
		if (onSide.size() > 1) {
			// do the fanning.  If there are no straight edges, fanning is from middle point
			int mp = Math.round(((float) onSide.size() - 1) / 2.0f);
			int lastStraight = getNextStraight(onSide, -1, onSide.size()-1, mp);
			int firstStraight = getNextStraight(onSide, 1, 0, mp);
			
			Set<DiagramElement> noFan = buildNoFanList(onSide, firstStraight, lastStraight, from);
			boolean lastFan = true, thisFan = false;
			
			for (int i = 0; i < onSide.size(); i++) {
				Dart toFan = onSide.get(i);
				boolean reversed = toFan.getDrawDirection() == Direction.reverse(d);
				Vertex fromEnd = reversed ? toFan.getTo() : toFan.getFrom();
				Vertex otherEnd = reversed ? toFan.getFrom() : toFan.getTo();
				ConnectionEdge underlying = (ConnectionEdge) toFan.getUnderlying();
				Connection link = (underlying.getOriginalUnderlying() instanceof Connection) ? (Connection) underlying.getOriginalUnderlying() : null;
				thisFan = ((i < firstStraight) || (i>lastStraight)) && (fanFor(underlying, noFan, from)) && (fanEdge(link, toFan, fromEnd, underlying.otherEnd(from), d)); 
				if (thisFan) {

					// need to split the current dart into 3 sections, the
					// middle section of which
					// will be given a new direction


					ConnectionEdgeBendVertex ebv1 = new ConnectionEdgeBendVertex(fromEnd.getID() + "/f1", underlying);
					ConnectionEdgeBendVertex ebv2 = new ConnectionEdgeBendVertex(fromEnd.getID() + "/f2", underlying);
					o.getAllVertices().add(ebv1);
					o.getAllVertices().add(ebv2);
					Direction fanDir = i < firstStraight ? Direction.rotateAntiClockwise(d) : Direction.rotateClockwise(d);

					Dart toFanPt2 = o.createDart(ebv1, ebv2, underlying, fanDir);
					Dart toFanPt3 = o.createDart(ebv2, otherEnd, underlying, d);
					
					// fix vertices
					otherEnd.removeEdge(toFan);
					otherEnd.addEdge(toFanPt3);
					ebv1.addEdge(toFan);
					if (reversed) {
						toFan.setFrom(ebv1);
					} else {
						toFan.setTo(ebv1);
					}
					
					// fix dart ordering
					List<Dart> dartOrder = o.getDartOrdering().get(otherEnd);
					if (dartOrder != null) {
						int idx = dartOrder.indexOf(toFan);
						dartOrder.set(idx, toFanPt3);
					}
					
					// fix waypoint map
					List<Vertex> waypoints = o.getWaypointMap().get(underlying);
					int wp1 = waypoints.indexOf(fromEnd);
					int wp2 = waypoints.indexOf(otherEnd);
					
					if (Math.abs(wp1-wp2) != 1) {
						throw new LogicException("Waypoints should be next to one another");
					}
					if (wp1 < wp2) {
						List<Vertex> toInsert = HelpMethods.createList((Vertex) ebv1, ebv2);
						Tools.insertIntoList(waypoints, wp1, toInsert);
					} else {
						List<Vertex> toInsert = HelpMethods.createList((Vertex) ebv2, ebv1);
						Tools.insertIntoList(waypoints, wp2, toInsert);						
					}
					
					// fix faces
					for (DartFace f : o.getFaces()) {
						for (int j = 0; j < f.dartsInFace.size(); j++) {
							DartDirection dartDirection = f.dartsInFace.get(j);
							if (dartDirection.getDart()==toFan) {
								// need to add other items either before or after
								List<DartDirection> dd = new ArrayList<DartDirection>(2);
								if (d==dartDirection.getDirection()) {
									dd.add(new DartDirection(toFanPt2, fanDir));
									dd.add(new DartDirection(toFanPt3, d));
									Tools.insertIntoList(f.dartsInFace, j, dd);
									j+=2;
								} else {
									dd.add(new DartDirection(toFanPt3, Direction.reverse(d)));
									dd.add(new DartDirection(toFanPt2, Direction.reverse(fanDir)));
									Tools.insertIntoList(f.dartsInFace, j-1, dd);
									j+=2;									
								}
							}
						}
					}
					
					// some compaction settings
					toFan.setChangeCost(Dart.CONNECTION_DART, null);
//					double len = toFan.getLength();
//					double minLen = getMinimumDartLength(toFan, underlying); 
//					toFan.setLength(Math.max(len, minLen));
					toFan.setOrthogonalPositionPreference(i < firstStraight ? Direction.rotateAntiClockwise(d) : Direction.rotateClockwise(d));
					toFanPt2.setOrthogonalPositionPreference(Direction.reverse(d));
					//toFanPt2.setLength(0);
					toFanPt2.setChangeCost(Dart.CONNECTION_DART_FAN, ebv1);
					//toFanPt3.setLength(0);
					toFanPt3.setChangeCost(Dart.CONNECTION_DART, null);
					// opposite to toFan
					toFanPt3.setOrthogonalPositionPreference(i > lastStraight ? Direction.rotateAntiClockwise(d) : Direction.rotateClockwise(d));
				} else if (!lastFan && !thisFan) {
					// if we have no fanning, we have to allow the vertex to increase in size
					Dart sideDart = out.newEdgeDarts.get(i);
					sideDart.setChangeCost(Dart.VERTEX_DART_GROW, null);
				}
				
				lastFan = thisFan;
			}
		}

		
		return out;
	}

	/**
	 * Only allows edges to fan if they are straight
	 */
	private boolean fanEdge(Connection link, Dart toFan, Vertex from, Vertex to, Direction d) {
		while (true) {
			boolean found = false;
			for (Edge e : from.getEdges()) {
				if ((e instanceof Dart) && (e.getOriginalUnderlying() == link)) {
					if (e.getDrawDirectionFrom(from) == d) {
						// edge continues, persevere with it
						from = e.otherEnd(from);
						found = true;
						break;
					} else if ((e.getDrawDirectionFrom(from) == Direction.rotateClockwise(d)) || 
							(e.getDrawDirectionFrom(from) == Direction.rotateAntiClockwise(d))) {
						// we've got a turn
						return false;
					}
				}
			}
			
			if (!found) {
				return true; 
			}
		}
	}

	private DiagramElement getOpposingElement(DiagramElement de, Vertex from) {
		DiagramElement underlying = from.getOriginalUnderlying();
		if (de instanceof Connection) {
			Connection c = (Connection)de;
			if (c.getFrom()==underlying) {
				return c.getTo();
			} else if (c.getTo() == underlying) {
				return c.getFrom();
			} else {
				throw new LogicException("couldn't match this link to the given vertex"+de+" -> "+from);
			}
		} else {
			return null;
		}
	}
	
	protected Set<DiagramElement> buildNoFanList(List<Dart> onSide,
			float firstStraight, float lastStraight, Vertex from) {
		Set<DiagramElement> out = new UnorderedSet<DiagramElement>((int) (lastStraight - firstStraight) * 2);
		
		for (int i = 0; i < onSide.size(); i++) {
			if ((i>=firstStraight) && (i<=lastStraight)) {
				Dart d = onSide.get(i);
				out.add(getOpposingElement(d.getOriginalUnderlying(), from));
			}
		}
		
		return out;
	}

	protected boolean fanFor(Edge underlying, Set<DiagramElement> noFan, Vertex from ) {
		return !noFan.contains(getOpposingElement(underlying.getOriginalUnderlying(), from));
	}

	private int getNextStraight(List<Dart> onSide, int step, int from, int midPoint) {
		int current = from;
		while ((current > -1) && (current < onSide.size())) {
			Dart d = onSide.get(current);
			Object e = d.getUnderlying();
			if ((e instanceof Edge) && (((Edge) e).getDrawDirection() != null)) {
				return current;
			}
			current += step;
		}
		
		return midPoint;
	}
	
//	@Override
//	protected Dart createSideDart(DiagramElement underlying, Orthogonalization o, Vertex last, Direction segmentDirection,
//			int oppSideDarts, double minDist, boolean endDart, Vertex vsv, int onSideDarts, Edge thisEdge, Edge lastEdge, boolean requiresMinLength, Edge borderEdge) {
//		
//		//double endDist = requiresMinLength ? sizer.getLinkPadding(underlying, segmentDirection) : 0;
//		//double interDist = sizer.getLinkGutter(underlying, segmentDirection);
//		
//		//double oppSideDist = (endDist * 2) + (Math.max(0, oppSideDarts - 1) * interDist);
//		//double thisSideDist = (endDist * 2) + (Math.max(0, onSideDarts - 1) * interDist);
//		//double totalDistDueToFan = Math.max(oppSideDist, Math.max(thisSideDist, minDist));
//		
//		if (thisSideDist == totalDistDueToFan) {
//			// all lengths on side are known
//			Dart out = o.createDart(last, vsv, borderEdge, segmentDirection); //, endDart ? endDist : interDist);
//			//out.setVertexLengthKnown(true);
//			return out;
//		} else if ((oppSideDarts<=1) && (onSideDarts <= 1)) {
//			// we can use the basic vertex arranger approach
//			return super.createSideDart(underlying, o, last, segmentDirection, oppSideDarts, minDist, endDart, vsv, onSideDarts, thisEdge, lastEdge, requiresMinLength, borderEdge);
//		} else {
//			// side lengths could go larger than the amount provided
//			Dart out = o.createDart(last, vsv, borderEdge, segmentDirection, endDart ? endDist : interDist);
//			return out;
//		}
//	}

}
