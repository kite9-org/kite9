package org.kite9.diagram.visualization.planarization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.algorithms.det.Deterministic;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.logging.Table;


/**
 * <p>Describes a face in the graph, which is an area bounded by edges.</p>
 * 
 * <p>A clockwise ordering of edges is an internal face, whereas an anti-clockwise ordering is
 * an external face.</p>
 * 
 * <p>Note that the order of the nodes in the corners and boundary are important, and should follow 
 * each other.  The first element of boundary should have as it's vertices the first two attr of the corners, and 
 * so on.  CheckFaceIntegrity aims to make sure this is the case.</p>
 * 
 * <p>Note that since an edge can only have a face either side of it, an edge can only appear a maximum of twice
 * in the boundary.  There is no upper limit on the number of times a vertex can appear in the boundary.
 * </p>
 * 
 * <p>Faces can contain other faces, when there is content within the face that is not connected to 
 * it's border.  This means that there is a hierarchy within the face system.  Any face that is contained 
 * within another face must be an outer face (since it faces outwards), and it's container must be an inner
 * face.  Therefore, only inner faces may contain others. </p>
 * 
 * @author robmoffat
 *
 */
public class Face implements Deterministic {
	
	private List<PlanarizationEdge> boundary = new ArrayList<PlanarizationEdge>();
	
	private List<Vertex> corners = new ArrayList<Vertex>();

	private Rectangular partOf;
	
	private boolean outerFace = false;
	
	public String toString() {
		StringBuffer out = new StringBuffer(300);
		out.append("[FACE: "+id+(outerFace?"outer"+(containedBy == null ? "" : ", inside "+containedBy.id) :"inner")+"\n");
		
		if (partOf != null) {
			out.append("  Part of: ");
			out.append(partOf);
			out.append("\n");
		}
		
		if (boundary.size() == 0) {
			if (corners.size() > 0) {
				out.append(" "+corners.get(0));
			}
		} else {
		
			Table t = new Table();
			t.addRow("Vertex", "Direction", "Contradicting", "Underlyings");
			for (int i = 0; i < this.vertexCount(); i++) {
				Vertex v = corners.get(i);
				PlanarizationEdge e = boundary.get(i);
				Direction d = e.getDrawDirectionFrom(v);
				Set<DiagramElement> underlyings = e.getDiagramElements().keySet();
				boolean contradicting = Tools.isUnderlyingContradicting(e);
				t.addRow(v.toString(),d, contradicting ? "C" : "", underlyings);
			}
			t.display(out);
		}
		
		
		return out.toString();
	}
	
	AbstractPlanarization pln;
	
	Face(String id, AbstractPlanarization pln) {
		this.id = id;
		this.pln = pln;
	}
	
	public final String id;

	/**
	 * This is a simple check to make sure that the boundary and list of corners reconciles ok.
	 */
	public boolean checkFaceIntegrity() {
		if ((corners.size()==1) && (boundary.size()==0)) {
			return true;
		}
		
		if (corners.size()!=boundary.size()) {
			throw new LogicException("Face: "+this.id+": Faces need same numbers of edges and corners");
		}
		
		for (int i = 0; i < corners.size(); i++) {
			Vertex corner = corners.get(i);
			Vertex otherEnd = corners.get((i+1) % corners.size());
			Edge e = boundary.get(i);
			if (!e.meets(corner)) {
				throw new LogicException("Face: "+this.id+": Edge should always meet the corner with same index: "+i+" "+corner+" "+e);
			}
			if (otherEnd!=e.otherEnd(corner)) {
				throw new LogicException("Face: "+this.id+": Edge doesn't meet the other end expected: "+i+" "+corner+" "+e+" expected: "+otherEnd);				
			}
		}
		
		return true;
	}

	public boolean isOuterFace() {
		return outerFace;
	}

	public void setOuterFace(boolean outerFace) {
		this.outerFace = outerFace;
	}
	
	public boolean contains(Vertex v) {
		return corners.contains(v);
	}

	public boolean contains(Edge e) {
		return boundary.contains(e);
	}
	
	public Iterable<PlanarizationEdge> edgeIterator() {
		return boundary;
	}
	
	public Iterable<Vertex> cornerIterator() {
		return corners;
	}
	
	public void add(Vertex v, PlanarizationEdge e) {
		corners.add(v);
		boundary.add(e);
	}
	
	public void add(int index, Vertex v, PlanarizationEdge e) {
		corners.add(index, v);
		boundary.add(index, e);
	}
	
	public void remove(int index) {
		corners.remove(index);
		boundary.remove(index);
	}
	
	public void replaceEdge(PlanarizationEdge e, PlanarizationEdge with) {
		for (int i = 0; i < edgeCount(); i++) {
			Edge current = boundary.get(i);
			if (current==e) {
				boundary.set(i, with);
			}
		}
	}
	
	public int vertexCount() {
		return corners.size();
	}
	
	public int edgeCount() {
	    	return boundary.size();
	}
	
	public Vertex getCorner(int i) {
		i = normalize(i);
		return corners.get(i);
	}
	
	private int normalize(int i) {
		if (boundary.size() == 0) {
			return 0;
		} else if (i>=boundary.size()) {
			return i % boundary.size();
		} else if (i<0) {
			return (i+boundary.size()) % boundary.size();
		} else {
			return i;
		}
	}

	public PlanarizationEdge getBoundary(int i) {
		i = normalize(i);
		return boundary.get(i);
	}
	
	public void reset(List<PlanarizationEdge> boundary, List<Vertex> corners) {
		this.boundary = boundary;
		this.corners  =corners;
		checkFaceIntegrity();
	}
	
	/**
	 * Works out the list of vertices for the edges
	 */
	public void reset(List<PlanarizationEdge> boundary) {
		List<Vertex> vList = createVertexList(boundary.get(0).getFrom(), boundary);
		if (vList == null) {
			vList = createVertexList(boundary.get(0).getTo(), boundary);
		}
		if (vList==null) {
			throw new LogicException("Boundary is not consistent: "+boundary);
		}
		reset(boundary, vList);
	}
	
	private List<Vertex> createVertexList(Vertex from, List<PlanarizationEdge> boundary) {
		ArrayList<Vertex> out = new ArrayList<Vertex>(boundary.size());
		int b = 0;
		do {
			out.add(from);
			Edge nextEdge = boundary.get(b);
			if (nextEdge.meets(from)) {
				from = nextEdge.otherEnd(from);
				b++;
			} else {
				return null;
			}
		} while (b < boundary.size());
		
		return out;
	}

	/**
	 * For two edges that follow each other on a face boundary, returns true
	 * if to follows from in the order of the face.
	 */
	public boolean toAfterFrom(Edge from, Edge to, Vertex first) {
		if (!(from.meets(first) && to.meets(first))) {
			// this ensures the calling code has got the right common vertex
			throw new LogicException("logic Error");
		} 
		
		int ind = indexOf(first, to);
		if (ind==-1)
			return false;
		
		Edge before = getBoundary(ind - 1);
		Edge after = getBoundary(ind + 1);
		if (before==from) {
			return true;
		} else if (after==from) {
			return false;
		} else {
			return false;
		}
	}

	public static Vertex getCommonVertex(Edge thisDart, Edge nextDart) {
		if (thisDart.meets(nextDart.getFrom())) {
			return nextDart.getFrom();
		} else if (thisDart.meets(nextDart.getTo())) {
			return nextDart.getTo();
		} else {
			throw new LogicException("Logic Error");
		}
	}
	
	
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Face) ? this.id.equals(((Face)obj).id) : false;
	}

	/**
	 * A specific vertex being followed by a specific edge occurs uniquely in a face.
	 * This is because each edge can only occur twice in a face, and the ends of the edge
	 * must be different.  
	 *   
	 * This returns the index of that occurrence or -1 if there is no occurrence.
	 */
	public int indexOf(Vertex v, Edge e) {
		for (int i = 0; i < boundary.size(); i++) {
			if ((boundary.get(i)==e) && (corners.get(i)==v)) { 
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Returns a list of 0, 1 or 2 integers giving the positions of the edge in the face.
	 */
	public List<Integer> indexOf(Edge e) {
		List<Integer> out = new ArrayList<Integer>();
		for (int i = 0; i < boundary.size(); i++) {
			if (boundary.get(i)==e) {
				out.add(i);
			}
		}
		return out;
	}
	
	public List<PlanarizationEdge> getEdgesCopy() {
		return new ArrayList<PlanarizationEdge>(boundary);
	}
	
	/**
	 * Returns a list of integers giving the positions of the vertex in the face.
	 */
	public List<Integer> indexOf(Vertex v) {
		List<Integer> out = new ArrayList<Integer>();
		for (int i = 0; i < corners.size(); i++) {
			if (corners.get(i)==v) {
				out.add(i);
			}
		}
		return out;
	}

	/**
	 * Return the number of times vertex v is visited by the face.
	 */
	public int occurrences(Vertex v) {
		int out = 0;
		for (Vertex c : corners) {
			if (c==v) {
				out++;
			}
		}
		return out;
	}
	
	/**
	 * Return the number of times edge e is visited by the face.
	 */
	public int occurrences(Edge e) {
		int out = 0;
		for (Edge ee : boundary) {
			if (ee==e) {
				out++;
			}
		}
		return out;
	}

	/**
	 * Splits the current face in two from the given indexes.  The new face is returned, this face is modified.
	 */
	public Face split(int start, int end, PlanarizationEdge repair) { 
		Face f2 = pln.createFace();
		f2.boundary = getRotatingSubset(boundary, end, start, false);
		boundary = getRotatingSubset(boundary, start, end, false);
		f2.corners = getRotatingSubset(corners, end, start, true);
		corners = getRotatingSubset(corners, start, end, true);
		f2.boundary.add(repair);
		boundary.add(repair);
		
		return f2;
	}
	
	/**
	 * Splits the current face in two using the given edge.  The new face is returned, this face is modified.
	 * This is used where an edge appears twice in the face already. 
	 */
	public Face split(Edge e) {
		List<PlanarizationEdge> newBoundary = new ArrayList<PlanarizationEdge>();
		List<Vertex> newCorners = new ArrayList<Vertex>();
		Face f2 = pln.createFace();
		
		boolean addToNew = false;
		for (int i = 0; i < corners.size(); i++) {
			if (boundary.get(i)==e) {
				addToNew = !addToNew;
			} else {
				if (addToNew) {
					f2.boundary.add(boundary.get(i));	
					f2.corners.add(corners.get(i));
				} else {
					newBoundary.add(boundary.get(i));
					newCorners.add(corners.get(i));
				}		
			}
		}
		
		this.corners = newCorners;
		this.boundary = newBoundary;
		
		// only problem now is where we end up with a single isolated vertex to handle
		Vertex from = e.getFrom();
		Vertex to = e.getTo();
		
		Face fromFace = this.corners.contains(from) ? this : (f2.corners.contains(from) ? f2 : null);
		Face toFace = this.corners.contains(to) ? this : (f2.corners.contains(to) ? f2 : null);
		
		if (fromFace==null) {
			if (this.corners.size()==0) { 
				this.corners.add(from);
			} else {
				f2.corners.add(from);
			}
		}
		
		if (toFace==null) {
			if (this.corners.size()==0) { 
				this.corners.add(to);
			} else {
				f2.corners.add(to);
			}
		}
		
		checkFaceIntegrity();
		f2.checkFaceIntegrity();		
		
		return f2;
	}
	
	/**
	 * if from==to, you get nothing back
	 * includes from, excludes to
	 */
	public static <X> List<X> getRotatingSubset(List<X> l, int from, int to, boolean inclusive) {
		from = (from+l.size()) % (l.size());
		to = (to+l.size()) % (l.size());
		
		List<X> out = new ArrayList<X>();
		int i = from;
		
		while (i!=to) {
			out.add(l.get(i));
			i = i + 1;
			if (i==l.size()) {
				i = 0;
			}
		};
		if (inclusive) {
			out.add(l.get(i));
		}
		
		return out;
	}

	public String getId() {
		return id;
	}
	
	private Set<Face> containedFaces = new DetHashSet<Face>();
	
	private Face containedBy;
	
	public Face getContainedBy() {
		return containedBy;
	}

	public void setContainedBy(Face containedBy) {
		this.containedBy = containedBy;
	}

	public Collection<Face> getContainedFaces() {
		return containedFaces;
	}

	public void setCorner(int i, Vertex newVertex) {
	    this.corners.set(i, newVertex);
	}
	
	public int size() {
		return boundary.size();
	}

	public Rectangular getPartOf() {
		return partOf;
	}

	public void setPartOf(Rectangular partOf) {
		this.partOf = partOf;
	}
	
}
