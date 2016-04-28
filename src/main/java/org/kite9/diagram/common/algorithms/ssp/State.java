package org.kite9.diagram.common.algorithms.ssp;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class State<P extends PathLocation<P>> {

	protected PriorityQueue<P> pq = new PriorityQueue<P>();
	private Map<Object, P> locationToPathMap = new HashMap<Object, P>(2000);
	public long adds;
	public long maxStack;
	private AbstractSSP<P> ssp;
	
	public State(AbstractSSP<P> in) {
		this.ssp = in;
	}

	public boolean add(P path) {
		try {
			adds ++;
			Object location = ssp.getLocation(path);
			P existing = location == null ? null : locationToPathMap.get(location);
			boolean newBetter = existing == null || existing.compareTo(path) > 0;

			if (newBetter) {
				if (existing != null) {
					existing.setActive(false);
				}
				//System.out.println("Replacing: \n\t "+existing+"\n\t"+path+"\n\t"+location);
				pq.add(path);
				locationToPathMap.put(location, path);
				maxStack = Math.max(pq.size(), maxStack);
				return true;
			} else {
				//System.out.println("Not Adding: "+path);
				return false;
			}
		} catch (OutOfMemoryError e) {
			throw new SSPTooLargeException("SSP too large: queue="+pq.size()+" map="+locationToPathMap.size());
		}
	}
	
	public P remove() {
		return pq.remove();
	}
}