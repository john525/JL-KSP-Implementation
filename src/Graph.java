import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;


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
			data[i] = new NodeData(k, i);
		}
		int[] count = new int[number2Name.size()];
		
		//Statistical things
		int itr = 0;
		long timeToReadMem = 0;
		long heapMax = 0;
		if(statistics) {//Technically, should add this to timeToReadMem, but that would change an independent variable.
			heapMax = memoryUsed();
		}
		long timeStart = System.currentTimeMillis();
		
		do {
			for(int i=0; i<edges.length; i++) {
				for(Edge e : edges[i]) {
					e.edgeCount = 0;
				}
			}
			
			for(int i=0; i<data.length; i++) {
				count[i] = 0;
			}
			
			PriorityQueue<Path> pq = new PriorityQueue<Path>();
			
			Path stub = new Path(null, new Edge(source, source, 0));
			data[source].paths[0] = stub;
			pq.add(stub);
			
			int pqIdx = 0;
			int modValue = Math.round( ((float) number2Name.size()*k) / ((float) numReadings) );
			
			while(!pq.isEmpty()) {
				Path path = pq.remove();
//				System.out.println(path);
				//System.out.println(pq.size());
								
				for(Edge edge : edges[path.getFinalHead()]) {
					Path newPath = new Path(path == stub ? null : path, edge);
					if(newPath.numNodes() >= h) break;
					
//					System.out.println("+" + edge + " = " + newPath);
					
					double distance = newPath.getDistance();
					
					if(!path.contains(edge.head) && distance < data[edge.head].paths[k-1].getDistance()) {
						if(count[edge.head] < k) {
							count[edge.head]++;
						}
						else {
							//pq.remove(data[edge.head].paths[k-1]);
						}
						data[edge.head].addToEnd(newPath, lambda);
						pq.add(newPath);
						
						edge.edgeCount++; //Will decrement some edge counts later.
					}
				}
				
				if(statistics) {
					pqIdx++;
					long x = System.currentTimeMillis();
					if(pqIdx % modValue == 0) {
						//System.out.println("reading");
						heapMax = Math.max(heapMax, memoryUsed());
					}
					long y = System.currentTimeMillis();
					timeToReadMem += y-x;
				}
//				System.out.println();
			}

			for(Edge e : edges[source]) {
				e.edgeCount--;
			}
			
			for(int i=0; i<data.length; i++) {
				data[i].diverseFlush(lambda);
			}
			
			itr++;
			if(numNodesCompleted >= data.length) {
				break;
			}
			
			int numDeleted = 0;
			for(int i=0; i<edges.length; i++) {
				Iterator<Edge> edgeIt = edges[i].iterator();
				while(edgeIt.hasNext()) {
					Edge e = edgeIt.next();
					if(e.edgeCount >= 0.5*((float)k)) {
						float prob = ((float)e.edgeCount) / ((float)k);
						if(Math.random() < prob) {
//							System.out.println(e + " deleted");
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
		} while(numNodesCompleted < number2Name.size());
				
		try {
			PrintStream output = new PrintStream(new FileOutputStream(resultFile));
			for(int i=0; i<data.length; i++) {
				output.println("Paths to " + number2Name.get(i));
				int j = 1;
				for(Path p : data[i].diversePaths) {
					output.println(j + ". " + p.toString(number2Name) + " = " + p.getDistance());
					j++;
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
		s.time = (System.currentTimeMillis()-timeStart-timeToReadMem)/1000f;
		
		numNodesCompleted = 0; //Take only photos, leave only footprints.
		
		return s;
	}

	public void addDirectedEdge(int start, int end, double length) {
		int head = end;
		int tail = start;
		edges[tail].add(new Edge(head, tail, length));
		numEdges++;
	}

	public void addUndirectedEdge(int a, int b, double length) {
		addDirectedEdge(a, b, length);
		addDirectedEdge(b, a, length);
	}

	private class NodeData {
		private ArrayList<Path> diversePaths;
		private TreeSet<Edge> diverseEdges;
		
		private Path[] paths;
		private int node;
		
		public static final boolean FAST_MODE = true;
		
		public NodeData(int k, int node) {
			this.node = node;
			paths = new Path[k];
			for(int i=0; i<k; i++) {
				paths[i] = new Path();//All empty paths have MAX_DISTANCE as length.
			}
			diversePaths = new ArrayList<Path>();
			if(FAST_MODE) {
				diverseEdges = new TreeSet<Edge>();
			}
		}
		
		public void diverseFlush(float lambda) {
			if(diversePaths.size() < paths.length && !paths[0].emptyPath() /*We should have at least one path*/) {
				TreeSet<Edge> visitedEdges;
				if(FAST_MODE) {
					visitedEdges = diverseEdges;
				}
				else {
					visitedEdges = new TreeSet<Edge>();//could make two versions: high memory (store visitedEdges) and high time (dynamically compute).
					for(Path p : diversePaths) {
						visitedEdges.addAll(p.toArrayList());
					}
				}
				
				for(Path p : paths) {
					if(p.emptyPath()) break;
					
					int numDivEdgesRequired = (int) Math.ceil(lambda * p.numEdges());
					int numDivEdgesFound = 0;
					
					List<Edge> newEdges = new LinkedList<Edge>();
					boolean addPath = false;
					
					for(Edge e : p.toArrayList()) {
						if(!visitedEdges.contains(e)) {
							numDivEdgesFound++;
							newEdges.add(e);
							
							if(numDivEdgesFound >= numDivEdgesRequired) {
								addPath = true;
								break;
							}
						}
					}
					
					if(addPath) {
						diversePaths.add(p);
						visitedEdges.addAll(newEdges);
					}
					
					if(diversePaths.size() == paths.length) {//paths.length is just where we store the k value
						numNodesCompleted++;
						break;
					}
				}
			}
			
			paths = new Path[paths.length];
			for(int i=0; i<paths.length; i++) {
				paths[i] = new Path();//All empty paths have MAX_DISTANCE as length.
			}
		}
		
		/**
		 * @param p The path to be added.
		 * @param lambda The diversity threshold it must fulfill.
		 * @return Whether or not the path was added.
		 */
		public boolean addToEnd(Path p, float lambda) {
			if(p.getDistance() >= paths[paths.length-1].getDistance()) {
				return false;
			}
			paths[paths.length-1] = p;
			
			for(int i=paths.length-1; i>0; i--) {
				if(paths[i].getDistance() < paths[i-1].getDistance()) {
					paths[i] = paths[i-1];
					paths[i-1] = p;
				}
				else break;
			}
			return true;
		}
	}
}
