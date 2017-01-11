package org.kite9.diagram.visualization.planarization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.elements.grid.GridPositioner;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.mgt.ContainerConnectionTransform1;
import org.kite9.diagram.visualization.planarization.mgt.ContainerConnectionTransform2;
import org.kite9.diagram.visualization.planarization.mgt.face.FaceConstructor;
import org.kite9.diagram.visualization.planarization.transform.ExcessVertexRemovalTransform;
import org.kite9.diagram.visualization.planarization.transform.LayoutSimplificationTransform;
import org.kite9.diagram.visualization.planarization.transform.OuterFaceIdentificationTransform;
import org.kite9.diagram.visualization.planarization.transform.PlanarizationTransform;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * This class defines the basic process of planarization, which is then extended
 * by the sub-packages.
 * 
 * @author moffatr
 * 
 */
public abstract class AbstractPlanarizer implements Logable {

	protected Kite9Log log = new Kite9Log(this);
   	
	private final ElementMapper em;
	
	public AbstractPlanarizer(ElementMapper elementMapper) {
		this.em = elementMapper;
	}

	public GridPositioner getGridPositioner() {
		return em.getGridPositioner();
	}
	
	public ElementMapper getElementMapper() {
		return em;
	}

	public Planarization planarize(Diagram c) {
		Planarization pln = buildPlanarization(c);
		
		try {
			for (PlanarizationTransform pt : getPlanarizationTransforms()) {
				log.send("PLan:"+pln.toString());
				checkIntegrity(pln);
				log.send("Applying transform: "+pt.getClass());
				pt.transform(pln);
			}
			
			checkIntegrity(pln);
			
			log.send(log.go() ? null : "Completed Planarization: \n" + pln.toString());
	
			return pln;
			
		} catch (Exception e) {
			throw new PlanarizationException("Planarization incomplete", pln, e);
		}
	}

	protected Planarization buildPlanarization(Diagram c) {
		PlanarizationBuilder po = getPlanarizationBuilder();
		Planarization pln = po.planarize(c);
		return pln;
	}

	private void checkIntegrity(Planarization pln) {
		for (Entry<Edge, List<Face>> ent: pln.getEdgeFaceMap().entrySet()) {
			
			for (Face f : ent.getValue()) {
				if (!f.contains(ent.getKey())) {
					throw new LogicException("Face doesn't contain edge that map says it does: "+ent.getKey()+ " " +f);
				}
			}
		}
		
		for (Entry<Vertex, List<Face>> ent: pln.getVertexFaceMap().entrySet()) {
			
			for (Face f : ent.getValue()) {
				if (!f.contains(ent.getKey())) {
					throw new LogicException("Face doesn't contain vertex that map says it does: "+ent.getKey()+ " " +f);
				}
			}
		}
		
		for (Face f : pln.getFaces()) {
			for (Edge e : f.edgeIterator()) {
				if (!pln.getEdgeFaceMap().get(e).contains(f)) {
					throw new LogicException("Face contains edge, map says it doesn't: "+e+ " " +f);
				}
			}
			
			for (Vertex v : f.cornerIterator()) {
				
				List<Face> faces = pln.getVertexFaceMap().get(v);
				if (!faces.contains(f)) {
					throw new LogicException("Face contains vertex, map says it doesn't: "+v+"  "+f);
				}
				
			}
		}
	}

	protected List<PlanarizationTransform> getPlanarizationTransforms() {
		List<PlanarizationTransform> out = new ArrayList<PlanarizationTransform>();
		out.add(new ExcessVertexRemovalTransform());
		out.add(new ContainerConnectionTransform1(getElementMapper()));
		out.add(new LayoutSimplificationTransform());
		out.add(new ContainerConnectionTransform2(getElementMapper()));
		out.add(new OuterFaceIdentificationTransform());
		return out;
	}

	protected abstract PlanarizationBuilder getPlanarizationBuilder();

	protected abstract FaceConstructor getFaceConstructor();

	public String getPrefix() {
		return "PLAN";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}