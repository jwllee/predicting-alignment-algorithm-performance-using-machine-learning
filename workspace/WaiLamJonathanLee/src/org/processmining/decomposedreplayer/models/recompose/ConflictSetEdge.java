package org.processmining.decomposedreplayer.models.recompose;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

public class ConflictSetEdge implements Comparable<ConflictSetEdge> {

	private final Set<XEventClass> u;
	private final Set<XEventClass> v;
	private final double weight;
	
	public ConflictSetEdge(ConflictSetEdge edge) {
		this.u = edge.u;
		this.v = edge.v;
		this.weight = computeWeight();
	}
	
	public ConflictSetEdge(Set<XEventClass> u, Set<XEventClass> v) {
		this.u = u;
		this.v = v;
		this.weight = computeWeight();
	}
	
	public Set<XEventClass> either() {
		return u;
	}
	
	public Set<XEventClass> other(Set<XEventClass> n) {
		if (u.equals(n)) return v;
		else if (v.equals(n)) return u;
		else throw new IllegalArgumentException("Do not recognize edge");
	}
	
	private double computeWeight() {
		Set<XEventClass> aux = new HashSet<>(u);
		aux.retainAll(v);
		return aux.size();
//		if (u.size() < v.size())
//			return aux.size() * 1.0 / u.size();
//		else
//			return aux.size() * 1.0 / v.size();
	}
	
	@Override
	public String toString() {
		return "{" + u + ", " + v + "}";
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ConflictSetEdge))
			return false;
		ConflictSetEdge other = (ConflictSetEdge) o;
		Set<XEventClass> s = other.either();
		Set<XEventClass> t = other.other(s);
		if ((s.equals(u) && t.equals(v)) || 
				(s.equals(v) && t.equals(u)))
			return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		return u.hashCode() + v.hashCode();
	}

	public double getWeight() {
		return weight;
	}

	public int compareTo(ConflictSetEdge o) {
		if (Double.compare(this.getWeight(), o.getWeight()) < 0)
			return -1;
		else if (Double.compare(this.getWeight(), o.getWeight()) == 0)
			return 0;
		else
			return 1;
	}
}
