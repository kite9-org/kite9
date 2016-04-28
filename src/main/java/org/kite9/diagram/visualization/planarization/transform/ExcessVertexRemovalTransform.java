package org.kite9.diagram.visualization.planarization.transform;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.framework.logging.Logable;


/**
 * This performs the following transformations: 
 * <ol>
 * <li> Remove excess start/end temporary vertices</li>
 * <li> Remove any dimensionless vertex with only two edges incident to it
 * </ol>
 * 
 * 
 * @author robmoffat
 *
 */
public class ExcessVertexRemovalTransform implements PlanarizationTransform, Logable {

	public void transform(Planarization pln) {
		removeExcessVertices(pln);

	}
	
	private void removeExcessVertices(Planarization pln) {
		Tools t = new Tools();
		List<Object> vertices = new ArrayList<Object>(pln.getEdgeOrderings().keySet());
		for (Object v : vertices) {
			if (v instanceof Vertex) {
				t.checkRemoveVertex(pln, (Vertex) v);
			}
		}
	}

	

	public String getPrefix() {
		return "EVRT";
	}

	public boolean isLoggingEnabled() {
		return false;
	}

}
