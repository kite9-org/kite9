package org.kite9.diagram.visualization.planarization.rhd.mergetype;


/**
 * There are basically 3 types of merge.

 * <ol>
 * <li>Linked merge:  where we are combining two groups together which share links between each other.  This is the best kind.
 * <li>Aligned merge:  where we combine two groups together which have a common link to a third group.   This is also useful, especially when the links are directed.
 * <li>Neighbour merge: where we combine two groups based on the fact that they are in the same container.  This is kind of a last-ditch option.
 * </ol>

 * @author robmoffat
 *
 */
public class MergeType implements Comparable<MergeType>{ 

	private int priority;
	private String name;
	
	public MergeType(int priority, String name) {
		super();
		this.priority = priority;
		this.name = name;
	}

	@Override
	public int compareTo(MergeType arg0) {
		return ((Integer)this.priority).compareTo(arg0.priority);
	}

	@Override
	public String toString() {
		return name+"("+priority+")";
	}

}