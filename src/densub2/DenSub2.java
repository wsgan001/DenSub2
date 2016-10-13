package densub2;
import java.io.*;
import java.util.*;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.*;

public class DenSub2 {
	public static void main(String[] args) {
		double time = System.currentTimeMillis();
		Scanner sc = new Scanner(System.in);
		
		int maxR = 100;
		String data ="youtube";
		
		// 주어진 그래프
		UndirectedGraph<String, DefaultEdge> graphG = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

//		System.out.print("시작 노드 입력: ");
//		String startNode = sc.nextLine();
//		String startNode = "0";
		
		try {
			String line;
			BufferedReader reader = new BufferedReader(new FileReader("./"+data+"/"+data+" edges.txt"));
			while((line = reader.readLine()) != null) {
				String[] edges = line.split("\t"); // 엔론, 유투브 데이터 디리미터
//				String[] edges = line.split(" "); // 페이스북 데이터용 디리미터
				graphG.addVertex(edges[0]);
				graphG.addVertex(edges[1]);
				graphG.addEdge(edges[0], edges[1]);
//				System.out.println(edges[0] + " " + edges[1]);
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		UndirectedSubgraph<String, DefaultEdge> pruneG = new UndirectedSubgraph<String, DefaultEdge>(graphG, graphG.vertexSet(), graphG.edgeSet());
		
//		System.out.println(graphG.toString());


		
//		// 리스트 V
//		HashMap<String, Integer> listV = new HashMap<String, Integer>();
//		Set<String> nodeSet = graphG.vertexSet();
//		Iterator<String> it = nodeSet.iterator();
//		while(it.hasNext()) {
//			String node = it.next();
//			listV.put(node, 1);
////			System.out.println(node);
//		}
		
		String[] startNodes = new String[1];
		for(int i = 0; i < startNodes.length; i++) {
//			startNodes[i] = Integer.toString(i+1);
			startNodes[i] = "1";
			
//			System.out.println(startNodes[i]);
//			System.out.println(graphG.containsVertex(startNodes[i]));
//			System.out.println(graphG.degreeOf(startNodes[i]));
		}
		
//		System.out.println(graphG.toString());
//		
//		System.out.println(graphG.vertexSet().size());
//		System.out.println(graphG.edgeSet().size());
		
		int countMax = maxR;

		for(int index = 0; index < startNodes.length; index++) {
//			if(true)break;
			
			String startNode = startNodes[index];
		
			// 서브 그래프 S
			UndirectedGraph<String, DefaultEdge> subS = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
			subS.addVertex(startNode);
			
//			System.out.println(subS.toString());
			
			// 리스트 V
			HashMap<String, Integer> listV = new HashMap<String, Integer>();
			Set<String> nodeSet = graphG.vertexSet();
			Iterator<String> it = nodeSet.iterator();
			while(it.hasNext()) {
				String node = it.next();
				listV.put(node, 1);
//				System.out.println(node);
			}
		
			listV.remove(startNode);
		
			double maxDensity = 0d;
		
			// log.txt에 프로그램 로그 남김
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(startNode+"log.csv"));
				
				int countBreak = 0;
				while(!listV.isEmpty()) {
					countBreak++;
					if (countBreak > 30) break;
//					System.out.println("while");
		
					ArrayList<String> cV = new ArrayList<String>();
					Iterator<DefaultEdge> iE = graphG.edgeSet().iterator();
					HashMap<String,String> edgePrune = new HashMap<String,String>();
					while(iE.hasNext()) {
						DefaultEdge edge = iE.next();
						StringTokenizer st = new StringTokenizer(edge.toString(), "()[]: ");
						String v1 = st.nextToken();
						String v2 = st.nextToken();
//						System.out.println(v1 + " " + v2);
						
						if(subS.containsVertex(v1) && subS.containsVertex(v2)) {
							edgePrune.put(v1, v2);
						}
						else if(subS.containsVertex(v1) && !subS.containsVertex(v2)) {
							cV.add(v2);
//							System.out.println(v2);
						}
						else if(!subS.containsVertex(v1) && subS.containsVertex(v2)) {
							cV.add(v1);
//							System.out.println(v1);
						}
					}
					
					Iterator<String> iP = edgePrune.keySet().iterator();
					while(iP.hasNext()) {
						String key = iP.next();
						pruneG.removeEdge(key, edgePrune.get(key));
					}
					
					for(int i = 0; i < cV.size(); i++) {
						subS.addVertex(cV.get(i));
					}
					
					subS = new UndirectedSubgraph<String, DefaultEdge>(graphG, subS.vertexSet(), graphG.edgeSet());
	
					int maxDegree = 0;
					String maxV = null;
					for(int i = 0; i < cV.size(); i++) {
						if(maxDegree < subS.degreeOf(cV.get(i))) {
							maxDegree = subS.degreeOf(cV.get(i));
							maxV = cV.get(i);
	//						System.out.println(maxV + " " + maxDegree);
						}
					}
					
					for(int i = 0; i < cV.size(); i++) {
						if(!cV.get(i).equals(maxV)) {
							subS.removeVertex(cV.get(i));
						}
					}
					
	//				System.out.println(cV.size());break;
	//				System.out.println(maxV);
//					System.out.println(subS.toString());
					
					listV.remove(maxV);
					
					double numEdges = subS.edgeSet().size();
					double numVertices = subS.vertexSet().size();			
					double density = numEdges / numVertices;
					
//					System.out.println(numEdges + " / " + numVertices);
					System.out.print(index+1 + ":\t" + listV.size() + "\t");
					System.out.println(density);

					bw.write(listV.size()+","+density+"\n");
					
					if(maxDensity < density) {
						maxDensity = density;
						countMax = maxR;
					}
					countMax--;
					
//					// 현재 덴서티가 최대 덴서티의 1/2보다 작으면 종료
//					if(density < maxDensity/2d) {
//						break;
//					}
					
					// 50회 연속 최대 덴서티 갱신이 없으면 알고리즘 종료
					if(countMax <= 1) {
						break;
					}
				}
				bw.flush();
				bw.close();
			} catch (IOException e) {
				System.err.println(e);
				System.exit(1);
			}
			
		}
		
		System.out.println("operation time: " + (System.currentTimeMillis() - time));
	}
}
