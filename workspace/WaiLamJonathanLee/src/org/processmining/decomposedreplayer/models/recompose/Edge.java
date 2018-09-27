package org.processmining.decomposedreplayer.models.recompose;

public class Edge {

	private String u;
	private String v;
	
	public Edge(Edge edge) {
		this.u = edge.u;
		this.v = edge.v;
	}
	
	public Edge(String u, String v) {
		this.u = u;
		this.v = v;
	}
	
	public String either() {
		return u;
	}
	
	public String other(String n) {
		if (u.equals(n)) return v;
		else if (v.equals(n)) return u;
		else throw new IllegalArgumentException("Do not recognize edge");
	}
	
	@Override
	public String toString() {
		return u + ", " + v;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Edge))
			return false;
		Edge other = (Edge) o;
		String s = other.either();
		String t = other.other(s);
		if ((s.equals(u) && t.equals(v)) || 
				(s.equals(v) && t.equals(u)))
			return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		return u.hashCode() + v.hashCode();
	}
}
