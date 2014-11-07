import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

import jxl.CellType;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.Number;

public class Main {
	
	public static void main(String[] args) {
		System.out.println("JL-KSP-Implementation");
		//runOnSmallGraph();
		//benchmarkMax(new File("/home/jlhota/Documents/shortestpaths/Fast_ENTS/results/"));
		//doTesting(false);
		
		ENTS(Integer.parseInt(args[0]), Float.parseFloat(args[1]), Integer.parseInt(args[2]));
		
		System.out.println("Main method is done.");
	}
	
	public static void ENTS(int k, float lambda, int h) {
		System.out.println("John Lhota ENTS.");
		
		ArrayList<File> graphFiles = null;
		int num = 0, tot=0;
		
		File graphs = new File("/storage/db/SCOP/RANKPROP/fatcat_hhblist_e0.0");
		if(!graphs.exists()) {
			graphs = new File("/work/db/SCOP/RANKPROP/fatcat_hhblist_e0.0");
		}
		if(!graphs.exists()) {
			graphs = new File("D:\\hh_graphs");
		}
		
		try {
			graphFiles = new ArrayList<File>();
			BufferedReader reader = new BufferedReader(new FileReader(new File(graphs, "edge_files.list")));
			while(reader.ready()) {
				File file = new File(graphs, reader.readLine());
				graphFiles.add(file);
			}
			reader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		
		Date programStart = new Date();
		
		for(int i = 0; i<graphFiles.size(); ) {
			File f = graphFiles.get(i);
			long startTime = System.currentTimeMillis();
			try {
				runProteinFile(k, lambda, f);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Program failed on " + format.format(new Date()));
			}
			i++;
			System.out.print(i + "/" + graphFiles.size());
			System.out.print(" ("+f.getName()+", "+(System.currentTimeMillis()-startTime)/1000F+"s)");
			System.out.println();
		}
		System.out.println("Fast ENTS is finished.");
		System.out.println("Program ended successfully on " + format.format(new Date()));
	}
	
	public static void runProteinFile(int k, float lambda, File f) {
		HashMap<Integer, String> number2Name = new HashMap<Integer, String>();
		HashMap<String, Integer> name2Number = new HashMap<String, Integer>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(f));
			
			String line;
			int n = 0;

			while(reader.ready()) {
				String[] info = reader.readLine().split(" ");
				if(!name2Number.containsKey(info[0])) {
					name2Number.put(info[0], n);
					number2Name.put(n, info[0]);
					n++;
				}
				if(!name2Number.containsKey(info[1])) {
					name2Number.put(info[1], n);
					number2Name.put(n, info[1]);
					n++;
				}
			}
			
			Graph g = new Graph(number2Name);
			
			reader = new BufferedReader(new FileReader(f));
			
			while(reader.ready()) {
				String[] info = reader.readLine().split(" ");
				g.addUndirectedEdge(name2Number.get(info[0]), name2Number.get(info[1]), 1.0-Math.log(Double.valueOf(info[2]))/Math.log(10.0));
			}
			
			int source = name2Number.get(f.getName().replace(".pair", ""));
			name2Number = null;
			
			File resultDir = new File("D:\\ENTS_results_logged+1_edges_prob", "K="+k+"_"+"lambda="+lambda);
			resultDir.mkdir();
			File finalResult = new File(resultDir, number2Name.get(source)+".txt");
			g.runAlgorithm(source, k, lambda, 23, finalResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void benchmarkMax(File resultDir) {
		HashMap<String, String> classifications = new HashMap<String, String>(25000);
		
		Workbook workbook = null;
		WritableWorkbook temp = null;
		WritableSheet sheet = null;
		try {
			File spreadsheet = new File("/home/jlhota/Documents/shortestpaths/Fast_ENTS/algorithm_evaluation/empty.xls");
			workbook = Workbook.getWorkbook(spreadsheet);
			temp = Workbook.createWorkbook(new File("/home/jlhota/Documents/shortestpaths/Fast_ENTS/algorithm_evaluation/data.xls"), workbook);
			
			sheet = temp.getSheet(0);
			
			Label topN = new Label(0, 0, "Top N Hits");
			sheet.addCell(topN);
			
			for(int i = 0; i < 2000; i++) {
				Number n = new Number(0, i+1, i+1);
				sheet.addCell(n);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			File scopFile = new File("/storage/db/SCOP/dir.scop_PDP.txt");
			BufferedReader reader = new BufferedReader(new FileReader(scopFile));
			while(reader.ready()) {
				String[] classification = reader.readLine().split("\\s");
				for(String s : classification) {
					System.out.print(s + "___");
				}
				System.out.println();
				String[] scop = classification[2].split("\\.");
				if(!classification[3].equals("-")) {
					classifications.put(classification[3], scop[0] + "." + scop[1]);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		TreeSet<Hit> hits = new TreeSet<Hit>();
				
		File[] folders = resultDir.listFiles();
		int paramSetID = 0;
		for(File folder : folders) {
			System.out.println("Working on " + (paramSetID+1) + "/" + folders.length);
			
			try {
				File benchmarkFolder = new File(folder, "benchmark_testing");
				benchmarkFolder.mkdir();
				
				File[] resultFiles = folder.listFiles();
				
				for(File file : resultFiles) {
					if(file.isDirectory()) continue;
					
					//System.out.println(paramSetID + "/" + folders.length + " - Working on " + file.getName());
					
					String query = file.getName().replace(".txt", "");
					
					BufferedReader reader = new BufferedReader(new FileReader(file));
					HashSet<String> foldsAdded = new HashSet<String>();
					while(reader.ready()) {
						if(foldsAdded.size() >= 2000) break;
						
						String line = reader.readLine();
						if(line.contains("path") || line.isEmpty()) continue;
						
						String[] summary = line.split(" ");
						String proteinName = summary[0];
						
						if(!summary[1].equals("Infinity")) {
							double importance = Double.valueOf(summary[1]);
							String foldGuess = classifications.get(proteinName);
							
							if(classifications.containsKey(proteinName)) {
								if(!foldsAdded.contains(foldGuess)) {//We start with the highest importance values so no need to check that.
									hits.add(new Hit(query, foldGuess, importance));
									foldsAdded.add(foldGuess);
								}
							}
							else {
								//System.out.println("Highest noninfinite ranking is unclassified.");
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("Hits: " + hits.size());
			
			try {				
				Label top = new Label(paramSetID+1, 0, folder.getName());
				sheet.addCell(top);
				
				int checked = 0, correct = 0;
				for(int i = 0; i < Math.min(hits.size(), 2000); i++) {
					Hit hit = hits.pollLast();
					if(hit.foldGuess.equals(classifications.get(hit.proteinName))) {
						correct++;
					}
					checked++;
					
					Number fraction = new Number(paramSetID+1, checked, ((double)correct)/885.0);
					sheet.addCell(fraction);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				paramSetID++;
			}
		}
		
		try {
			temp.write();
			temp.close();
			workbook.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void doTesting(boolean newData) {
		if(newData) new TestDataGenerator().go();
		
		for(int i=0; i<TestDataGenerator.sizes.length; i++) {
			for(int j=0; j<TestDataGenerator.avgDegrees.length; j++) {
				int size = TestDataGenerator.sizes[i];
				int deg = TestDataGenerator.avgDegrees[j];

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
		g.runAlgorithm(0, 3, 0.5F, 23, new File("res_smalltest.txt"));
		System.out.println("done");
	}

}
