package graph;

import java.util.ArrayList;

/**
 * Created by Ruby on 2017/5/3.
 */
public class Node {

    public Graph graph; // the graph to which the node belongs

    public int id; // a unique id - running number
    //public int label; // for semantic feasibility checks
    public ArrayList<Integer> label = new ArrayList<Integer>();
    public ArrayList<Edge> outEdges = new ArrayList<Edge>(); // edges of which this node is the origin
    public ArrayList<Edge> inEdges = new ArrayList<Edge>(); // edges of which this node is the destination

    public Node(Graph g, int id, ArrayList<Integer> label) {
        this.graph = g;
        this.id = id;
        for(int i = 0; i < label.size(); i ++)
            this.label.add(label.get(i));
    }
}
