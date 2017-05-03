package main;

import core.State;
import core.VF2;
import graph.Graph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Ruby on 2017/5/3.
 */

public class App {

    public static void main(String[] args) throws IOException {
		Path graphPath = Paths.get("E:/Subgraph Isomorphism/vf2/graphDB", "Test1.my");
		Path queryPath = Paths.get("E:/Subgraph Isomorphism/vf2/graphDB", "QTest1.my");
		Path outPath = Paths.get("E:/Subgraph Isomorphism/vf2/graphDB", "res1.my");

/*        Path graphPath = Paths.get("/home/ada/ruby/data", "newGraph.txt");
        Path queryPath = Paths.get("/home/ada/ruby/data", "newQuery.txt");
        Path outPath = Paths.get("/home/ada/ruby/data", "resNew.txt");*/

        if (args.length == 0) {
            printUsage();
            System.out.println();
            System.out.println("Warning: no arguments given, using default arguments");
            System.out.println();
        }

        for (int i = 0; i < args.length; i++){
            if (args[i].equals("-t")) {
                graphPath = Paths.get(args[i+1]);
                i++;
            } else if (args[i].equals("-q")) {
                queryPath = Paths.get(args[i+1]);
                i++;
            } else if (args[i].equals("-o")) {
                outPath = Paths.get(args[i+1]);
                i++;
            } else {
                printUsage();
                System.exit(1);
            }
        }

        System.out.println("Target Graph Path: " + graphPath.toString());
        System.out.println("Query Graph Path: " + queryPath.toString());
        System.out.println("Output Path: " + outPath.toString());
        System.out.println();


        long startMilli = System.currentTimeMillis();

        PrintWriter writer = new PrintWriter(outPath.toFile());

        Graph graph = loadGraphSetFromFile(graphPath, "Graph ");
        Graph query = loadGraphSetFromFile(queryPath, "Query ");

        VF2 vf2= new VF2();

        System.out.println("Loading Done!");
        printTimeFlapse(startMilli);
        startMilli = System.currentTimeMillis();
        System.out.println();
        State state = vf2.matchGraphPair(graph, query);
        //System.out.println("state.size() = " + state.Sum.size());
        if (state.Sum.size() == 0) {
            System.out.println("Cannot find a map for the query");
            printTimeFlapse(startMilli);
            //printAverageMatchingTime(startMilli, queryCnt);
            System.out.println();

            writer.write("Cannot find a map for the query" + "\n\n");
            writer.flush();
        }else {
            System.out.println("Found " + state.Sum.size() + " maps for the query");
            printTimeFlapse(startMilli);
            System.out.println();
            state.writeMapping(writer);
            writer.flush();
        }

        printTimeFlapse(startMilli);
    }

    /**
     * Load graph set from file
     * @param inpath	Input path
     * @param namePrefix	The prefix of the names of graphs
     * @return	Graph Set
     * @throws FileNotFoundException
     */
    private static Graph loadGraphSetFromFile(Path inpath, String namePrefix) throws FileNotFoundException{
        Graph graph = new Graph();
        Scanner scanner = new Scanner(inpath.toFile());
        while (scanner.hasNextLine()){
            String line = scanner.nextLine().trim();
            if (line.equals("")){
                continue;
            }else if (line.startsWith("v")) {
                int i = 2;
                String[] lineSplit = line.split(" ");
                int nodeId = Integer.parseInt(lineSplit[1]);
                ArrayList<Integer> nodeLabel = new ArrayList<Integer>();
                while(i < lineSplit.length) {
                    nodeLabel.add(Integer.parseInt(lineSplit[i]));
                    i++;
                }
                graph.addNode(nodeId, nodeLabel);
            } else if (line.startsWith("e")) {
                String[] lineSplit = line.split(" ");
                int sourceId = Integer.parseInt(lineSplit[1]);
                int targetId = Integer.parseInt(lineSplit[2]);
                graph.addEdge(sourceId, targetId);
            }
        }
        scanner.close();
        //graph.printGraph();
        return graph;
    }

    private static void printTimeFlapse(long startMilli){
        long currentMili=System.currentTimeMillis();
        System.out.println(((currentMili - startMilli) / 1000) + " seconds elapsed");
    }

    private static void printAverageMatchingTime(long startMilli, int queryCnt){
        long currentMili=System.currentTimeMillis();
        System.out.println(((currentMili - startMilli) / queryCnt) + " milliseconds per graph in average.");
    }

    private static void printUsage(){
        System.out.println("Usage: -t target_graph_path -q query_graph_path -o output_path");
    }
}