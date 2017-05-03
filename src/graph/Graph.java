package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ruby on 2017/5/3.
 */
public class Graph {

    //public String name; // name of the graph
    public Map<Integer, Node> nodes = new HashMap<Integer, Node>(); // list of all nodes<index,node>
    public Map<Integer, Integer> nodeList = new HashMap<Integer, Integer>();//<id,index>
    public ArrayList<Edge> edges = new ArrayList<Edge>(); // list of all edges
    int index = 0;

    private int[][] adjacencyMatrix; // stores graph structure as adjacency matrix (-1: not adjacent, >=0: the edge label)
    private boolean adjacencyMatrixUpdateNeeded = true; // indicates if the adjacency matrix needs an update

    public void addNode(int id, ArrayList<Integer> label) {
        nodes.put(index, new Node(this, id, label));
        nodeList.put(id, index);
        this.adjacencyMatrixUpdateNeeded = true;
        index++;
    }

    public void addEdge(Node source, Node target) {
        edges.add(new Edge(this, source, target));
        this.adjacencyMatrixUpdateNeeded = true;
    }

    public void addEdge(int sourceId, int targetId) {
        this.addEdge(this.nodes.get(this.nodeList.get(sourceId)), this.nodes.get(this.nodeList.get(targetId)));
    }


    /**
     * Get the adjacency matrix
     * Reconstruct it if it needs an update
     *
     * @return Adjacency Matrix
     */
    public int[][] getAdjacencyMatrix() {

        if (this.adjacencyMatrixUpdateNeeded) {
            int k = this.nodes.size();
            this.adjacencyMatrix = new int[k][k];    // node size may have changed
            for (int i = 0; i < k; i++)            // initialize entries to -1
                for (int j = 0; j < k; j++)
                    this.adjacencyMatrix[i][j] = -1;

            for (Edge e : this.edges) {
                this.adjacencyMatrix[this.nodeList.get(e.source.id)][this.nodeList.get(e.target.id)] = 1; // label must bigger than -1
            }
            this.adjacencyMatrixUpdateNeeded = false;
        }
        return this.adjacencyMatrix;
    }
}
