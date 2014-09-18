import java.util.ArrayList;
import java.util.HashMap;

public class Path implements Comparable<Path> {

	private ArrayList<Edge> edges;
	private double distance;
	private int pathID;
	
	private static int numPaths;
	static {
		numPaths = 0;
	}

	public Path(Path prefix, Edge edge) {
		if(prefix == null) edges = new ArrayList<Edge>();
		else edges = new ArrayList<Edge>(prefix.edges);
		edges.add(edge);
		distance = (prefix != null ? prefix.distance : 0) + edge.length;
		
		pathID = numPaths;
		numPaths++;
	}

	//empty path constructor
	public Path() {
		edges = new ArrayList<Edge>();
		distance = Double.MAX_VALUE;
	}

	public double getDistance() {
		return distance;
	}

	public ArrayList<Edge> toArrayList() {
		return edges;
	}

	public int numEdges() {
		return edges.size();
	}

	public boolean emptyPath() {
		return numEdges() == 0;//This would be suboptimal in a recursive formulation
	}

	public int numNodes() {
		return numEdges() + 1;
	}

	public boolean contains(int node) {
		if(edges.get(0).tail == node) return true;
		for(Edge e : edges) {
			if(e.head == node) return true;
		}
		return false;
	}

	public boolean contains(Edge e) {
		return edges.contains(e);
	}

	public int getFinalHead() {
		return edges.get(edges.size() - 1).head;
	}

	@Override
	public String toString() {
		if(edges == null || edges.size() == 0) return "[Empty Path]";

		StringBuilder builder = new StringBuilder(Integer.toString(edges.get(0).tail));
		for(Edge e : edges) {
			builder.append(">" + e.head);
		}
		return builder.toString();
	}
	
	public String toString(HashMap<Integer,String> number2Name) {
		if(edges == null || edges.size() == 0) return "[Empty Path]";

		StringBuilder builder = new StringBuilder(number2Name.get(edges.get(0).tail));
		for(Edge e : edges) {
			builder.append(">" + number2Name.get(e.head));
		}
		return builder.toString();
	}

	@Override
	public int compareTo(Path p) {
		return new Double(this.getDistance()).compareTo(new Double(p.getDistance()));
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Path) return ((Path)o).edges.equals(this.edges);
		else return false;
	}

}