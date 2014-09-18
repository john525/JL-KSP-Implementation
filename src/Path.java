import java.util.ArrayList;
import java.util.HashMap;

public class Path implements Comparable<Path> {

		Path prefix;
		Edge edge;
		
		int size;
		double distance;

		public Path(Path prefix, Edge edge) {
			this.prefix = prefix;
			this.edge = edge;
			size = -1;
			distance = -1;
		}

		//empty path constructor
		public Path() {
			this(null, null);
		}

		public double getDistance() {
			if(distance != -1) return distance;
			
			if(prefix == null && edge == null) {
				return Double.MAX_VALUE;
			}

			double result = 0;
			if(prefix != null) result += prefix.getDistance();
			if(edge != null) result += edge.length;
			distance = result;
			return result;
		}

		public ArrayList<Edge> toArrayList() {
			ArrayList<Edge> result = new ArrayList<Edge>();
			if(prefix != null) {
				result.addAll(prefix.toArrayList());
			}
			if(edge != null) {
				result.add(edge);
			}
			return result;
		}

		public int numEdges() {
			return numNodes() - 1;
		}

		public boolean emptyPath() {
			return numNodes() == 0;
		}

		public int numNodes() {
			if(size != -1) return size;
			
			int result = 0;
			if(prefix != null) {
				result += prefix.numNodes();
			}
			else if(prefix == null && edge != null) {
				result++;//add on the tail
			}

			if(edge != null) {
				if(edge.head == edge.tail) {
					return 1; //stub (see main algorithm)
				}
				else {
					result++;//add on this head.
				}
			}
			size = result;
			return result;
		}
		
		public boolean contains(int node) {
			if(prefix == null) {
				return edge.head == node || edge.tail == node;//Base case.
			}
			else if(edge != null) {
				if(edge.head == node) {
					return true;
				}
			}
			
			//We know the prefix isn't null because of the first if clause.
			return prefix.contains(node);
		}

		public boolean contains(Edge e) {
			if(edge != null) {
				if(edge.equals(e)) {
					return true;
				}
			}
			if(prefix != null) {
				return prefix.contains(e);
			}
			return false;
		}

		public String toString(HashMap<Integer, String> number2Name) {
			if(edge != null && prefix == null) {
				return number2Name.get(edge.tail) + ">" + number2Name.get(edge.head);
			}
			else if(edge != null && prefix != null) {
				return prefix.toString(number2Name) + ">" + number2Name.get(edge.head);
			}
			else if(edge == null && prefix != null) {
				return prefix.toString(number2Name);
			}
			else {
				return "[empty path]";
			}
		}

		@Override
		public int compareTo(Path p) {
			return new Double(this.getDistance()).compareTo(new Double(p.getDistance()));
		}
		
		public int getFinalHead() {
			if(edge != null) {
				return edge.head;
			}
			else {
				return prefix.getFinalHead();
			}
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof Path) {
				Path p = (Path) o;
				if(edge != null && p.edge != null) {
					if(edge.head != p.edge.head) return false;
				}
				if(prefix == null && p.prefix == null) {
					if(edge != null && p.edge != null) {
						if(edge.equals(p.edge)) return true;
						else return false;
					}
					else if (edge == null && p.edge == null) return true;
					else if(edge == null || p.edge == null) return false;
				}
				else if(prefix == null || p.prefix == null) return false;//They're not both null, so if either is null we've found a discrepancy.
				else if(!prefix.equals(p.prefix)) return false;
				return true;
			}
			else return false;
		}

	}