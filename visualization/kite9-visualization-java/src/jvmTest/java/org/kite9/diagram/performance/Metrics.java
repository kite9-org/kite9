package org.kite9.diagram.performance;

public class Metrics implements Comparable<Metrics>{
	
	public Metrics(String name) {
		this.name = name;
	}

	public String name;
	public int connecteds;
	public int connections;
	
	public long totalTime;
	public long planarizationTime;
	public long orthogonalizationTime;
	public long compactionTime;
	public long renderingTime;
	
	public long turns;
	public long crossings;
	public long totalEdgeLength;
	public long totalDiagramSize;
	public long totalGlyphSize;
	public String runDate;
	public String exception;
	
	public static final String[] HEADINGS = new String[] { "name", "runDate", "cnx", "links", "total ms", "plan ms", "orth ms", "comp ms", "rend ms", "xing", "turns", "edge", "dsize", "gsize", "exception" };
	
	public String[] getMetrics() {
		return new String[] { name, runDate, "" + connecteds, "" +connections, "" + totalTime, "" + planarizationTime, "" + orthogonalizationTime,"" + compactionTime, ""+ renderingTime, "" +crossings, ""+turns, "" + totalEdgeLength, "" + totalDiagramSize, "" + totalGlyphSize,""+exception };
			
	}

	public int compareTo(Metrics o) {
		return name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return "Metrics [name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Metrics other = (Metrics) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
}
