package org.kite9.diagram.common.algorithms.fg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kite9.diagram.common.algorithms.ssp.AbstractSSP;
import org.kite9.diagram.common.algorithms.ssp.NoFurtherPathException;
import org.kite9.diagram.common.algorithms.ssp.State;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * This algorithm uses a Dijkstra-style shortest path in order to maximise flow
 * and minimise cost over the network.
 * 
 * @author robmoffat
 * 
 */
public class FlowGraphSPP<X extends FlowGraph> extends AbstractSSP<Path> implements FlowAlgorithm<X>, Logable {

	Kite9Log log = new Kite9Log(this);
	
	public static final boolean LOG_FLOW_INFORMATION = false;
	
	List<Node> destination;
	List<Node> startingPoints;

	int iterations = 0;
	int cost = 0;
	long paths = 0;

	public int maximiseFlow(X fg) {
		iterations = 0;
		paths = 0;
		cost = 0;
		Path p = null;
		while (true) {
			p = getShortestPath(fg);
			if (p == null)
				break;
			p.pushFlow(1);
			cost += p.getCost();
			State<Path> s = getLastState();
			log.send(log.go() ? null : "Round " + (iterations) + " generated " + s.adds + " paths, maxstack "+s.maxStack+" chose " + displayPath(p)
					+ " pushing 1 with cost " + p.getCost());
			paths += s.adds;
		}
		log.send(log.go() ? null : "Completed flow maximisation with: \n\titerations: " + iterations + 
				"\n\tpaths:    "+paths+
				"\n\tcost:     " + cost+
				"\n\tnodes:    "+fg.getAllNodes().size()+
				"\n\tarcs:     "+fg.getAllArcs().size());
		
		if (!log.go()) {
			File out = new File("ssp.info");
			try {
				FileWriter w = new FileWriter(out, true);
				w.write(paths+","+cost+","+fg.getAllNodes().size()+","+fg.getAllArcs().size()+"\n");
				w.close();
			} catch (IOException e) {
				throw new LogicException("could not write ssp file: ",e);
			}
		}
		
		displayFlowInformation(fg);
		return cost;
	}

	private String displayPath(Path p) {
		if (p == null)
			return "";

		Path next = p.nextPathItem;

		if (next != null) {
			return p.getEndNode() + " -- " + displayPath(next);
		} else {
			return p.getEndNode().toString();
		}

	}

	public void displayFlowInformation(FlowGraph fg) {
		if (!LOG_FLOW_INFORMATION)
			return;
		
		List<String> lines = new ArrayList<String>();
		for (Node n : fg.getAllNodes()) {
			n.ensureEulersEquilibrium();
			StringBuffer arcInfo = new StringBuffer();
			for (Arc a : n.getArcs()) {
				if (a.getFlow() != 0) {
					if (a.getFrom() == n) {
						arcInfo.append(a + " " + a.getFlow() + "   ");
					} else {
						arcInfo.append(a + " " + (-a.getFlow()) + "   ");
					}
				}
			}
			lines.add("Flow on: " + n.getId() + " = " + n.getFlow() + ", requires " + n.getSupply() + ", due to "
					+ arcInfo);
		}
		Collections.sort(lines);
		log.send(log.go() ? null : "Flow Information", lines);
	}

	/**
	 * Returns a lowest-cost path from source to sink, using Dijkstra algorithm
	 */
	public Path getShortestPath(FlowGraph fg) {
		iterations++;
		destination = getResidualSources(fg);
		startingPoints = getResidualSinks(fg);

		if ((startingPoints.size() == 0) && (destination.size() == 0)) {
			return null;
		}

		if ((startingPoints.size() == 0) || (destination.size() == 0)) {
			// we have a problem, since you need both a starting point and a
			// destination
			displayFlowInformation(fg);

			log.send(log.go() ? null : "New path not available from " + startingPoints.toString() + " TO: " + destination.toString());
			throw new LogicException("Graph is unbalanced: " + startingPoints + " vs " + destination);
		}

		try {
			return createShortestPath();
		} catch (NoFurtherPathException nsee) {
			displayFlowInformation(fg);
			displayRemainderInfo(startingPoints);
			displayRemainderInfo(destination);
			
			throw new LogicException(
					"Graph cannot be completed after "+iterations+".  Please check directional constraints don't prohibit diagram from drawing: "
							+ startingPoints + " to " + destination, nsee);
		} catch (Throwable other) {
			throw new LogicException("Graph cannot be completed after "+iterations+":"+ startingPoints + " to " + destination + " has "
					+ fg.getAllNodes().size() + " nodes and " + fg.getAllArcs().size() + " arcs" 
					+ " paths, lowest cost " + cost, other);
		}
	}

	private void displayRemainderInfo(List<Node> ns) {
		for (Node node : ns) {
			Object memento = (node instanceof SimpleNode) ? ((SimpleNode)node).getRepresentation() : null;
			log.error("Node: "+node +"\n\t"+ memento);
		}
	}

	public void generateSuccessivePaths(Path p, State<Path> pq) {
		Node fromNode = p.getEndNode();
		for (Arc a : fromNode.getArcs()) {
			if (!p.contains(a)) {
				boolean reversed = a.getFrom() != fromNode;
				Node to = reversed ? a.getFrom() : a.getTo();
				boolean capacity = a.hasCapacity(reversed);
				if ((capacity) && (!checkForLoopback(to, p))) {
					Path np = generateNewPath(p, reversed, a);
					if (np!=null)
						pq.add(np);
				}
			}
		}
	}

	private boolean checkForLoopback(Node to, Path p) {
		//return p.contains(to);
		return false;
	}

	protected Path generateNewPath(Path p, boolean reversed, Arc a) {
		if (a.hasCapacity(reversed)) {
			return new Path(p, a, reversed);
		} else {
			return null;
		}
	}

	public List<Node> getResidualSources(FlowGraph fg) {
		List<Node> out = new ArrayList<Node>();
		for (Node n : fg.getAllNodes()) {
			if (n.getResidualStatus() == Node.ResidualStatus.SOURCE) {
				out.add(n);
			}
		}

		return out;
	}

	public List<Node> getResidualSinks(FlowGraph fg) {
		List<Node> out = new ArrayList<Node>();
		for (Node n : fg.getAllNodes()) {
			if (n.getResidualStatus() == Node.ResidualStatus.SINK) {
				out.add(n);
			}
		}

		return out;
	}

	public int getIterations() {
		return iterations;
	}

	@Override
	protected void createInitialPaths(State<Path> pq) {
		for (Node n : destination) {
			pq.add(new Path(n));
		}
	}

	@Override
	protected boolean pathComplete(Path r) {
		return startingPoints.contains(r.endNode);
	}

	
}
