package org.kite9.diagram.visualization.planarization.mgt.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.visualization.planarization.grid.GridPositioner;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarizationImpl;
import org.kite9.diagram.visualization.planarization.rhd.RHDPlanarizationBuilder;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.Table;

/**
 * This follows the general GT approach to producing a maximal planar subgraph
 * by introducing edges above and below the line of the planarization, and as
 * many as possible.
 * 
 * @author moffatr
 * 
 */
public abstract class MGTPlanarizationBuilder extends RHDPlanarizationBuilder implements Logable {
	
	public MGTPlanarizationBuilder(ElementMapper em, GridPositioner gp) {
		super(em, gp);
	}
	
	protected Kite9Log log = new Kite9Log(this);
		
	protected MGTPlanarizationImpl buildPlanarization(Diagram d, List<Vertex> vertexOrder, Collection<BiDirectional<Connected>> initialUninsertedConnections, Map<Container, List<DiagramElement>> sortedContainerContents) {
		MGTPlanarizationImpl p = new MGTPlanarizationImpl(d, vertexOrder, initialUninsertedConnections, sortedContainerContents);
		logPlanarEmbeddingDetails(p, log);
		getRoutableReader().initRoutableOrdering(vertexOrder);
		completeEmbedding(p);
		log.send(log.go() ? null : "Initial Planar Embedding: \n" + p.toString());
		return p;
	}

	public static void logPlanarEmbeddingDetails(MGTPlanarization pln, Kite9Log log) {
		Table t = new Table();
		List<Vertex> vertexOrder = pln.getVertexOrder();
		int size = vertexOrder.size();
		
		List<String> xPositions = new ArrayList<String>(size);
		List<String> yPositions = new ArrayList<String>(size);
		List<String> index = new ArrayList<String>(size);
		
		for (int i = 0; i < size; i++) {
			RoutingInfo routingInfo = vertexOrder.get(i).getRoutingInfo();
			xPositions.add(routingInfo == null ? "" : routingInfo.outputX());
			yPositions.add(routingInfo == null ? "" : routingInfo.outputY());
			index.add(""+i);
			
		}
		
		t.addRow(index);
		t.addRow(vertexOrder);
		t.addRow(xPositions);
		t.addRow(yPositions);
		
		
		log.send(log.go() ? null : "Vertex Notional Positions: \n",t);
	}

	/**
	 * This method allows you to do any post-processing of the planarization.
	 */
	protected void completeEmbedding(MGTPlanarization p) {
		processConnections(p);
	}

	protected abstract void processConnections(MGTPlanarization p);

	@Override
	public String getPrefix() {
		return "GTPB";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

}
