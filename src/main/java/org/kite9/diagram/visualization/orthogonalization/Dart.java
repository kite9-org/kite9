package org.kite9.diagram.visualization.orthogonalization;

import java.util.Map;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;

/**
 * Darts are created by the orthogonalization process.  They are directed edges within the diagram which 
 * represent the borders of Retangular diagram elements, or Connections.
 * 
 * @author robmoffat
 *
 */
public interface Dart extends Edge {
	
	
	public static final int EXTEND_IF_NEEDED = 0;
	public static final int CONNECTION_DART_FAN = 2;	
	public static final int VERTEX_DART_GROW = 3;
 	public static final int CONNECTION_DART = 4;	
	public static final int VERTEX_DART_PRESERVE = 5;

	int getChangeCost();

	boolean isChangeEarly(Vertex end);

	boolean isDirected();

	int getBendCost();

	int getCrossCost();

	Map<DiagramElement, Direction> getDiagramElements();

	boolean isPartOf(DiagramElement de);
	
	public String getID();
	
	public void setChangeCost(int changeCost, Vertex changeEarlyEnd);
	
	public void setChangeCostChangeEarlyBothEnds(int changeCost);

	void setOrthogonalPositionPreference(Direction d);

}