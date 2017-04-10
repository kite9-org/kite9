package org.kite9.diagram.visualization.planarization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering;

/**
 * Contains methods for:
 * <ul>
 * <li>outputting the planarization as text</li>
 * <li>storing the faces in the planarization</li>
 * <li>storing the order of edges around a vertex</li>
 * <li>storing the mapping of faces either side of an edge</li>
 * </ul>
 * 
 * @author moffatr
 * 
 */
public abstract class AbstractPlanarization implements Planarization {

	private Diagram d;

	public AbstractPlanarization(Diagram d) {
		this.d = d;
	}

	@Override
	public Diagram getDiagram() {
		return d;
	}
	
	public static class TextualRepresentation {
		Map<Vertex, Dimension2D> positions = new HashMap<Vertex, Dimension2D>();
		Map<Integer, char[]> b = new HashMap<Integer, char[]>();
		int length = 0;
		int minExtent = Integer.MAX_VALUE;
		int maxExtent = Integer.MIN_VALUE;

		public void hLine(int row, int cols, int coll, boolean highlight) {
			ensureRow(row);
			int increment = (cols > coll) ? -1 : 1;
			for (int i = cols; i != coll; i += increment) {
				setLinePart(row, i, true, highlight);
			}
			setLinePart(row, coll, true, highlight);
		}

		private void setLinePart(int row, int i, boolean horiz, boolean highlight) {
			if (highlight || (b.get(row)[i] == ' ')) {
				b.get(row)[i] = highlight ? (horiz ? '=' : '+') : (horiz ? '-' : '|') ;
			}
		}

		public void ensureRow(int i) {
			if (b.get(i) != null)
				return;
			char[] row = new char[length];
			b.put(i, row);
			Arrays.fill(row, ' ');
			minExtent = Math.min(minExtent, i);
			maxExtent = Math.max(maxExtent, i);

		}

		public void vLine(int rows, int col, int rowl, boolean highlight) {
			int increment = (rows > rowl) ? -1 : 1;
			for (int i = rows; i != rowl; i += increment) {
				ensureRow(i);
				setLinePart(i,col,false, highlight);
			}
			ensureRow(rowl);
			setLinePart(rowl,col,false, highlight);

		}

		public void outputString(int row, int col, String label) {
			ensureRow(row);
			for (int i = 0; i < label.length(); i++) {
				b.get(row)[col + i]= label.charAt(i);
			}
		}

		public String toString() {

			StringBuilder out = new StringBuilder(1000);
			out.append("\n");
			for (int i = minExtent; i <= maxExtent; i++) {
				char[] cs = b.get(i);
				for (int j = 0; j < cs.length; j++) {
					out.append(cs[j]);
				}
				;
				out.append('\n');
			}

			return out.toString();
		}

		public Map<Vertex, Dimension2D> getPositions() {
			return positions;
		}

		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}
	}

	List<Face> faces = new ArrayList<Face>();

	Map<Edge, List<Face>> edgeFaceMap = new HashMap<Edge, List<Face>>();

	Map<Object, EdgeOrdering> edgeOrderingMap = new HashMap<Object, EdgeOrdering>();
	
	Map<Vertex, List<Face>> vertexFaceMap = new HashMap<Vertex, List<Face>>();

	public Map<Vertex, List<Face>> getVertexFaceMap() {
		return vertexFaceMap;
	}

	public Map<Edge, List<Face>> getEdgeFaceMap() {
		return edgeFaceMap;
	}
	
	public List<Face> getFaces() {
		return faces;
	}

	public Map<Object, EdgeOrdering> getEdgeOrderings() {
		return edgeOrderingMap;
	}

	int nextFaceId = 0;

	/**
	 * Creates a face for this planarization
	 * @param group An object to indicate a logical grouping of faces.
	 * @return
	 */
	public Face createFace() {
		Face out = new Face("" + nextFaceId++, this);
		faces.add(out);
		return out;
	}

	public String toString() {
		StringBuilder out = new StringBuilder(1000);

		out.append("PLANARIZATION[\n"+getAllVertices()+"\n");
		for (Face f : faces) {
			out.append(f.toString());
			out.append("\n");
		}

		return out.toString();
	}
	
	private Map<DiagramElement, EdgeMapping> edgeListMap = new HashMap<DiagramElement, EdgeMapping>();

	@Override
	public Map<DiagramElement, EdgeMapping> getEdgeMappings() {
		return edgeListMap;
	}

	@Override
	public Collection<Vertex> getAllVertices() {
		return vertexFaceMap.keySet();
	} 

	
	
}
