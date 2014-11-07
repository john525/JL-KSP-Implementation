import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Set;
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
	 * Runs the nondiverse algorithm.
	 * @param source
	 * @param k
	 * @param resultFile
	 * @return
	 */
	public Statistics runAlgorithm(int source, int k, File resultFile) {
		return runAlgorithm(source, k, 0f, resultFile);
	}
	
	/**
	 * Runs the algorithm with h=23, as this is the maximum length of a gene regulatory pathway.
	 * (Gene regulatory pathway discover was this algorithm's initial application.
	 * @param source
	 * @param k
	 * @param lambda
	 * @param resultFile
	 * @return
	 */
	public Statistics runAlgorithm(int source, int k, float lambda, File resultFile) {
		return runAlgorithm(source, k, lambda, 23, resultFile);
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
			data[i] = new NodeData(k, h, i);
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
			data[source].addToEnd(stub, k);
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
					
					if(!path.contains(edge.head) && distance < data[edge.head].longestPathDistance(k)) {
						if(count[edge.head] < k) {
							count[edge.head]++;
						}
						else {
							//pq.remove(data[edge.head].paths[k-1]);
						}

						/*if(!data[edge.head].complete(k))*/ data[edge.head].addToEnd(newPath, k);
						
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
				data[i].diverseFlush(k, lambda);
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
				
		Arrays.sort(data);
		try {
			PrintStream output = new PrintStream(new FileOutputStream(resultFile));
			for(int i=0; i<data.length; i++) {
				NodeData node = data[i];
				if(node.getNumber() == source) continue;
				output.println(number2Name.get(node.getNumber()) + " " + node.importance());
				int j = 1;
				for(Path p : node.diversePaths) {
					output.println(j + ". " + p.toString(number2Name) + " = " + p.getDistance());
					j++;
				}
				output.println();
			}
			output.close();
		} catch(IOException e) {
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

	private class NodeData implements Comparable<NodeData> {
		private List<Path> diversePaths;
		private Set<Edge> diverseEdges;
		
		private LinkedList<Path> paths;
		private int node;
		
		public static final boolean STORAGE = true;
		
		public NodeData(int k, int h, int node) {
			this.node = node;
			paths = new LinkedList<Path>();
			diversePaths = new ArrayList<Path>();
			if(STORAGE) {
				diverseEdges = new HashSet<Edge>(k*h);
			}
		}
		
		public int getNumber() {
			return node;
		}

		public void diverseFlush(int k, float lambda) {
			if(diversePaths.size() < paths.size() && paths.size() > 0 /*We should have at least one path*/) {
				Set<Edge> visitedEdges;
				if(STORAGE) {
					visitedEdges = diverseEdges;
				}
				else {
					visitedEdges = new TreeSet<Edge>();
					for(Path p : diversePaths) {
						visitedEdges.addAll(p.toArrayList());
					}
				}
				
				for(Path p : paths) {			
					int numDivEdgesRequired = (int) Math.ceil(lambda * p.numEdges());
					int numDivEdgesFound = 0;
					
					boolean addPath = false;
					
					List<Edge> edgeList = p.toArrayList();
					List<Edge> newEdgesSoFar = new LinkedList<Edge>();
					int lastCheckedIndex = -1;
					for(Edge e : edgeList) {
						lastCheckedIndex++;
						if(!visitedEdges.contains(e)) {
							numDivEdgesFound++;
							newEdgesSoFar.add(e);
							
							if(numDivEdgesFound >= numDivEdgesRequired) {
								addPath = true;
								break;
							}
						}
					}
					
					if(addPath) {
						diversePaths.add(p);
						visitedEdges.addAll(newEdgesSoFar);
						for(int i = lastCheckedIndex + 1; i < edgeList.size(); i++) {
							visitedEdges.add(edgeList.get(i));
						}
					}
					
					if(diversePaths.size() == k) {//paths.length is just where we store the k value
						numNodesCompleted++;
						break;
					}
				}
			}
			
			paths.clear();
		}
		
		public boolean complete(int k) {
			return diversePaths.size() == k;
		}
		
		public double longestPathDistance(int k) {
			if(paths.size() < k) return Double.MAX_VALUE;
			else return ((LinkedList<Path>)paths).getLast().getDistance();
		}
		
		/**
		 * @param p The path to be added.
		 * @param lambda The diversity threshold it must fulfill.
		 * @return Whether or not the path was added.
		 */
		public boolean addToEnd(Path p, int k) {
			if(paths.size() == k && p.getDistance() < longestPathDistance(k)) {
				paths.remove(paths.size()-1);
			}
			else if(paths.size() == k && p.getDistance() >= longestPathDistance(k)) {
				return false;
			}
			
			ListIterator<Path> li = paths.listIterator(paths.size());
			do {
				if(!li.hasPrevious()) {
					li.add(p);
					return true;
				}
			} while(li.previous().getDistance() > p.getDistance());
			li.next();
			li.add(p);
			return true;
		}
		
		public double importance() {
			double probAllWrong = 1.0;
			for(Path p : diversePaths) {
				double pathImp = p.getDistance() - ((double)p.numEdges());
				double probRight = Math.exp(-pathImp);
				probAllWrong *= (1.0 - probRight);
			}
			double probAnyRight = 1.0 - probAllWrong;
			return probAnyRight;
		}

		@Override
		public int compareTo(NodeData other) {
			return -(new Double(importance()).compareTo(other.importance()));
		}
	}
}
