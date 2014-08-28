import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;


public class Graph {

	public ArrayList<Edge>[] edges;
	private HashMap<Integer, String> number2Name;
	private int numEdges;
	private int numNodesCompleted;
	
	public static final boolean statistics = true;
	public static final int numReadings = 4;//number of memory readings

	public Graph(HashMap<Integer, String> nameMapping) {
		number2Name = nameMapping;
		edges = new ArrayList[nameMapping.size()];
		numEdges = 0;
		for(int i=0; i<edges.length; i++) {
			edges[i] = new ArrayList<Edge>();
		}
	}
	
	public static long memoryUsed() {
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		return rt.totalMemory() - rt.freeMemory();
	}

	/**
	 * Run the single source k diverse short paths algorithm.
	 * @param source Where the algorithm starts from.
	 * @param k The number of paths to find.
	 * @param lambda Diversity threshold.
	 * @param h Max number of nodes in a path.
	 * @param resultFile Text file to output the results to.
	 */
	public Statistics runAlgorithm(int source, int k, float lambda, int h, File resultFile) {
		numNodesCompleted = 0;

		NodeData[] data = new NodeData[number2Name.size()];
		for(int i=0; i<data.length; i++) {
			data[i] = new NodeData(k);
		}
		int[] count = new int[number2Name.size()];
		
		//Statistical things
		int itr = 0, numBefore = 0, numAfter = 0;
		long heapMax = 0;
		if(statistics) {
			heapMax = memoryUsed();
		}
		long timeStart = System.currentTimeMillis();
		long timeToReadMem = 0;
		
		do {
//			System.out.println("Number of Edges: " + numEdges);

			for(int i=0; i<edges.length; i++) {
				for(Edge e : edges[i]) {
					e.edgeCount = 0;
				}
			}

			PriorityQueue<Path> pq = new PriorityQueue<Path>();

//			for(int i=0; i<data.length; i++) {
//				count[i] = 0;
//			}
			Path stub = new Path(null, new Edge(source, source, 0));
			data[source].paths[0] = stub;
			pq.add(stub);
			//			for(Edge edge : edges[source]) {
			//				Path initial = new Path(null, edge);
			////				System.out.println(initial.getDistance());
			//				pq.add(initial);
			//				data[edge.head].addToEnd(initial);
			//				//Don't add to edgeCount, since we don't want top level of pseudo tree to get deleted.
			//				//That's what they do in the source code.
			//			}
			
			int pqIdx = 0;
			int modValue = Math.round( ((float) number2Name.size()) / ((float) numReadings) );
			
			while(!pq.isEmpty()) {
				Path path = pq.remove();

				for(Edge edge : edges[path.edge.head]) {
					Path newPath = new Path(path == stub ? null : path, edge);
					if(newPath.numNodes() > h) break;

					double distance = newPath.getDistance();
					if(!path.contains(edge.head) && distance < data[edge.head].paths[k-1].getDistance()) {
						boolean increasedCount = false;
						if(count[edge.head] < k) {
							count[edge.head]++;
							pq.add(newPath);
							if(edge.tail != source) edge.edgeCount++;
							increasedCount = true;
						}
						else {
							pq.remove(data[edge.head].paths[k-1]);
							pq.add(newPath);
						}
						boolean saved = data[edge.head].addToEnd(newPath, lambda);
						if(increasedCount && !saved) {
							edge.edgeCount--;
						}
						if(saved) {
							if(itr == 0) numBefore++;
							else numAfter++;
						}
					}
				}
				
				if(statistics) {
					pqIdx++;
					long x = System.currentTimeMillis();
					if(pqIdx % modValue == 0) {
						heapMax = Math.max(heapMax, memoryUsed());
					}
					long y = System.currentTimeMillis();
					timeToReadMem += y-x;
				}
			}

			for(Edge e : edges[source]) {
				e.edgeCount--;
			}
			
//			for(int i=0; i<data.length; i++) {
//				data[i].diversityCheck(lambda);
//			}
			
			if(numNodesCompleted >= data.length) {
				break;
			}
			
			int numDeleted = 0;
			for(int i=0; i<edges.length; i++) {
				Iterator<Edge> edgeIt = edges[i].iterator();
				while(edgeIt.hasNext()) {
					Edge e = edgeIt.next();
//					System.out.println(e + " " + e.edgeCount);
					if(e.edgeCount >= 0.5*((float)k)) {
						float prob = ((float)e.edgeCount) / ((float)k);
						if(Math.random() < prob) {
							//								System.out.println(e + " deleted");
							edgeIt.remove();
							numDeleted++;
							numEdges--;
						}
					}
				}
			}
//			System.out.println("num deleted: "+ numDeleted);
			if(numDeleted < ((float)numEdges) / ((float)number2Name.size())) {
//				System.out.println("Terminating due to m/n clause.");
				break;
			}
			itr++;
		} while(numNodesCompleted < number2Name.size());
		
//		System.out.println("Before " + numBefore);
//		System.out.println("After " + numAfter);
		
//		System.out.println("Time: " + (System.currentTimeMillis()-timeStart-timeToReadMem)/1000F + "s");
//		System.out.println("Memory: " + (heapMax-heapStart)/1e6 + "MB");
		
		try {
			PrintStream output = new PrintStream(new FileOutputStream(resultFile));
			for(int i=0; i<data.length; i++) {
				output.println("Paths to " + number2Name.get(i));
				for(int j=0; j<k; j++) {
					output.println((j+1) + ". " + data[i].paths[j] + " = " + data[i].paths[j].getDistance() + " n=" + data[i].paths[j].numNodes());
				}
				output.println();
			}
			output.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		Statistics s = new Statistics();
		s.maxHeap = heapMax;
		s.time = (System.currentTimeMillis()-timeStart-timeToReadMem)/1000F;
		return s;
	}

	public void addDirectedEdge(int start, int end, double length) {
		int head = end;
		int tail = start;
		edges[tail].add(new Edge(head, tail, length));
		numEdges++;
	}

	public void addUndirectedEdge(int a, int b, double length) {
		edges[b].add(new Edge(a, b, length));
		edges[a].add(new Edge(b, a, length));
		numEdges += 2;
	}

	private class NodeData {
		Path[] paths;

		public NodeData(int k) {
			paths = new Path[k];
			for(int i=0; i<k; i++) {
				paths[i] = new Path();//All have MAX_DISTANCE as length.
			}
		}
		
		public void diversityCheck(float lambda) {
			//1st path is automatically diverse.
			int numDiversePaths = 1;
			for(int pathIdx=1; pathIdx<paths.length; pathIdx++) {
				if(paths[pathIdx].emptyPath()) {
					numNodesCompleted++;//If we ran out of paths, we won't find any more by deleting edges.
					//Or if this is the source, it's completed by default.
					return;
				}
				else numDiversePaths++;
			}
			if(numDiversePaths == paths.length) {
				numNodesCompleted++;
			}
		}
		
		/**
		 * @return Diversity of a path assumed with respect to all elements currently stored in paths.
		 */
		public float computeDiversity(Path p) {
			int numDivEdges = 0;
			ArrayList<Edge> edgeList = p.toArrayList();
			for(Edge e : edgeList) {
				boolean first = true;
				for(int prevPathIdx = 0; prevPathIdx < paths.length; prevPathIdx++) {
					if(paths[prevPathIdx].contains(e)) {
						first = false;
						break;
					}
					if(paths[prevPathIdx].emptyPath()) {
						break;
					}
				}
				if(first) {
					numDivEdges++;
				}
			}
			float diversity = ((float)numDivEdges) / ((float)p.numEdges());
			return diversity;
		}

		public boolean addToEnd(Path p, float lambda) {
			float div = computeDiversity(p);
			if(div < lambda) return false;
			if(div == 0) return false; //Shouldn't include any duplicate paths.
			
			if(p.getDistance() < paths[paths.length-1].getDistance()) {
				paths[paths.length-1] = p;
			}
			else return false;
			
			for(int i=paths.length-2; i>=0; i--) {
				if(paths[i].getDistance() < p.getDistance()) {
					break;
				}
				else {
					paths[i+1] = paths[i];
					paths[i] = p;
				}
			}
			System.out.println(p);
			System.out.println(lambda);
			return true;
		}
	}

	private class Path implements Comparable<Path> {

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

		@Override
		public String toString() {
			if(edge != null && prefix == null) {
				return number2Name.get(edge.tail) + ">" + number2Name.get(edge.head);
			}
			else if(edge != null && prefix != null) {
				return prefix.toString() + ">" + number2Name.get(edge.head);
			}
			else if(edge == null && prefix != null) {
				return prefix.toString();
			}
			else {
				return "empty path";
			}
		}

		@Override
		public int compareTo(Path p) {
			return new Double(this.getDistance()).compareTo(new Double(p.getDistance()));
		}

	}

}
