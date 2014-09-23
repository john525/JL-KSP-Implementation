import java.util.HashMap;


public class Edge implements Comparable<Edge> {
	int head, tail;
	double length;
	
	int edgeCount;
	
	public Edge(int head, int tail, double length) {
		this.head = head;
		this.tail = tail;
		this.length = length;
		edgeCount = 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Edge) {
			Edge e = (Edge) o;
			return this.head == e.head && this.tail == e.tail;//Assume this isn't a multigraph.
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return this.tail + ">" + this.head;
	}
	
	public String toString(HashMap<Integer, String> number2Name) {
		return number2Name.get(tail) + ">" + number2Name.get(head);	}
	
	@Override
	public int compareTo(Edge other) {
		if(head > other.head) {
			return 1;
		}
		else if(head < other.head) {
			return -1;
		}
		else {
			if(tail > other.tail) {
				return 1;
			}
			else if(tail < other.tail) {
				return -1;
			}
		}
		
		//Theoretically, this worst-case event should never be called
		return new Double(length).compareTo(other.length);
	}
}
