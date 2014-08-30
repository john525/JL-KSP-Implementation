
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
			else {
				return 0;
			}
		}
	}
}
