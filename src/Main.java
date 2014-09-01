import java.io.FileInputStream;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class Main {

	public static final int[] sizes = {10000, 50000, 250000, 1000000};
	public static final int[] avgDegrees = {5, 10, 20};
	public static final Random rand = new Random();

	public static void main(String[] args) {
		doTesting();
	}

	public static void doTesting() {
		for(int i=0; i<sizes.length; i++) {
			for(int j=0; j<avgDegrees.length; j++) {
				int size = sizes[i];
				int deg = avgDegrees[j];

				long heapStart = Graph.memoryUsed();
				
				HashMap<Integer, String> nameMapping = new HashMap<Integer, String>();
				try {
					File nodeList = new File(new File("test_data", "n="+size+", deg="+deg), "allProteins");
					Scanner nodeListReader = new Scanner(new FileInputStream(nodeList), "UTF-8");
					int z=0;

					while(nodeListReader.hasNextLine()){
						String geneName = nodeListReader.nextLine();
						nameMapping.put(z , geneName);
						z++;
					}
					nodeListReader.close();
				} catch(IOException e) {
					e.printStackTrace();
				}

//				for(int node=0; node<size; node++) {
//					nameMapping.put(node, "G"+node);
//				}
				
				Graph g = new Graph(nameMapping);
				try {
					File edgeList = new File(new File("test_data", "n="+size+", deg="+deg), "G0_G1");
					Scanner edgeListReader = new Scanner(new FileInputStream(edgeList), "UTF-8");
					edgeListReader.nextLine();//throw away header
					while(edgeListReader.hasNextLine()) {
						String[] edge = edgeListReader.nextLine().split(" ");
						g.addDirectedEdge(Integer.parseInt(edge[0]), Integer.parseInt(edge[1]), Double.parseDouble(edge[2]));
					}
					edgeListReader.close();
				} catch(IOException e) {
					e.printStackTrace();
				}

				//			for(int node=0; node<g.edges.length; node++) {
				//				g.edges[i] = new ArrayList<Edge>();
				//			}
				//			long totalNumEdges = 0;
				//			while (((float)2*totalNumEdges)/((float)size) < deg) {
				//				int a = rand.nextInt(size);
				//				int b = rand.nextInt(size-1);
				//				if(b >= a) b++;
				//				if(g.edges[a].contains(b)) {
				//					continue;
				//				}
				//				else {
				//					g.addDirectedEdge(a, b, Math.random());
				//					totalNumEdges++;
				//				}
				//			}

				System.out.println("Running n="+size+" and deg="+deg);
				Statistics s = g.runAlgorithm(0, 5, 0.5F, 23, new File("res.txt"));
				System.out.println("Memory: " + (s.maxHeap - heapStart)/1e6 + "MB");
				System.out.println("Time: " + s.time + "s");
				System.out.println();
			}
		}
	}

	public static void runOnSmallGraph() {
		HashMap<Integer, String> nameMapping = new HashMap<Integer, String>();
		nameMapping.put(0, "A");
		nameMapping.put(1, "B");
		nameMapping.put(2, "C");
		nameMapping.put(3, "D");
		nameMapping.put(4, "E");
		nameMapping.put(5, "F");
		nameMapping.put(6, "G");
		nameMapping.put(7, "H");
		nameMapping.put(8, "I");
		nameMapping.put(9, "J");
		Graph g = new Graph(nameMapping);
		g.addUndirectedEdge(0, 2, 3);
		g.addUndirectedEdge(0, 3, 6);
		g.addUndirectedEdge(2, 4, 9);
		g.addUndirectedEdge(1, 2, 7);
		g.addUndirectedEdge(3, 5, 2);
		g.addUndirectedEdge(5, 4, 3);
		g.addUndirectedEdge(1, 6, 2);
		g.addUndirectedEdge(5, 6, 1);
		g.addUndirectedEdge(4, 6, 7);
		g.addUndirectedEdge(4, 9, 3);
		g.addUndirectedEdge(6, 8, 5);
		g.addUndirectedEdge(6, 7, 3);
		g.addUndirectedEdge(7, 8, 0.5);
		g.runAlgorithm(0, 3, 0.5F, 23, new File("res.txt"));
		System.out.println("done");
	}

}
