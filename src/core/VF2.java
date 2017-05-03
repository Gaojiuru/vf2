package core;

import java.io.IOException;
import java.util.ArrayList;

import graph.Node;
import graph.Edge;
import graph.Graph;

/**
 * Created by Ruby on 2017/5/3.
 */
public class VF2 {

    /**
     * Find matches given a query graph and a set of target graphs
     * @param graphSet		Target graph set
     * @param queryGraph	Query graph
     * @return				The state set containing the mappings
     */
/*    public ArrayList<State> matchGraphSetWithQuery(Graph graph, Graph queryGraph){
        ArrayList<State> stateSet = new ArrayList<State>();
        matchGraphPair(graph, queryGraph);
        System.out.println("Found " + stateSet.size() + " maps for: " + queryGraph);
        System.out.println();
        return stateSet;
    }*/

    /**
     * Figure out if the target graph contains query graph
     * @param targetGraph	Big Graph
     * @param queryGraph	Small Graph
     * @param state			The state to store the result mapping
     * @return				Match or not
     */
    public State matchGraphPair(Graph targetGraph, Graph queryGraph) throws IOException {
        State state = new State(targetGraph, queryGraph);
        int t = matchRecursive(state, targetGraph, queryGraph);
        if(t > 0)
            System.out.println(t);
        return state;
    }

    /**
     * Recursively figure out if the target graph contains query graph
     * @param state			VF2 State
     * @param targetGraph	Big Graph
     * @param queryGraph	Small Graph
     * @return	Match or not
     */
    private int matchRecursive(State state, Graph targetGraph, Graph queryGraph){
        int sum = 0;
        if (state.depth == queryGraph.nodes.size()) {    // Found a match
            state.matched = true;
            state.addMapping();
            return 1;
        } else {    // Extend the state
            ArrayList<Pair<Integer, Integer>> candidatePairs = genCandidatePairs(state, targetGraph, queryGraph);
            for (Pair<Integer, Integer> entry : candidatePairs) {
                if (checkFeasibility(state, entry.getKey(), entry.getValue())) {
                    state.extendMatch(entry.getKey(), entry.getValue()); // extend mapping
                    sum += matchRecursive(state, targetGraph, queryGraph); // Found a match
                    //System.out.println(sum);
                    state.backtrack(entry.getKey(), entry.getValue()); // remove the match added before
                }
            }
            return sum;
        }
    }

    /**
     * Generate all candidate pairs given current state
     * @param state			VF2 State
     * @param targetGraph	Big Graph
     * @param queryGraph	Small Graph
     * @return				Candidate Pairs
     */
    private ArrayList<Pair<Integer,Integer>> genCandidatePairs(State state, Graph targetGraph, Graph queryGraph) {
        ArrayList<Pair<Integer,Integer>> pairList = new ArrayList<Pair<Integer,Integer>>();

        if (!state.T1out.isEmpty() && !state.T2out.isEmpty()){
            // Generate candidates from T1out and T2out if they are not empty

            // Faster Version
            // Since every node should be matched in query graph
            // Therefore we can only extend one node of query graph (with biggest id)
            // instead of generate the whole Cartesian product of the target and query
            int queryNodeIndex = -1;
            for (int i : state.T2out) {
                queryNodeIndex = Math.max(i, queryNodeIndex);
            }
            for (int i : state.T1out) {
                pairList.add(new Pair<Integer,Integer>(i, queryNodeIndex));
            }

            return pairList;
        } else if (!state.T1in.isEmpty() && !state.T2in.isEmpty()){
            int queryNodeIndex = -1;
            for (int i : state.T2in) {
                queryNodeIndex = Math.max(i, queryNodeIndex);
            }
            for (int i : state.T1in) {
                pairList.add(new Pair<Integer,Integer>(i, queryNodeIndex));
            }
            return pairList;
        } else {
            int queryNodeIndex = -1;
            for (int i : state.unmapped2) {
                queryNodeIndex = Math.max(i, queryNodeIndex);
            }
            for (int i : state.unmapped1) {
                pairList.add(new Pair<Integer,Integer>(i, queryNodeIndex));
            }
            return pairList;
        }
    }

    /**
     * Check the feasibility of adding this match
     * @param state				VF2 State
     * @param targetNodeIndex	Target Graph Node Index
     * @param queryNodeIndex	Query Graph Node Index
     * @return					Feasible or not
     */
    private Boolean checkFeasibility(State state , int targetNodeIndex , int queryNodeIndex) {
        // Node Label Rule
        // The two nodes must have the same label
        //System.out.println("targetNodeIndex = " + targetNodeIndex + "\t" + "queryNodeIndex = " + queryNodeIndex);
        boolean fease = true;
        for (int i = 0; i < state.queryGraph.nodes.get(queryNodeIndex).label.size(); i++){
            int j = 0;
            while(j < state.targetGraph.nodes.get(targetNodeIndex).label.size()) {
                if (state.queryGraph.nodes.get(queryNodeIndex).label.get(i) != state.targetGraph.nodes.get(targetNodeIndex).label.get(j))
                    j++;
                else break;
            }
            if(j == state.targetGraph.nodes.get(targetNodeIndex).label.size()) {
                fease = false;
                break;
            }
        }

        // Predecessor Rule and Successor Rule
        if (!checkPredAndSucc(state, targetNodeIndex, queryNodeIndex)){
            fease = false;
        }

        // In Rule and Out Rule
        if (!checkInAndOut(state, targetNodeIndex, queryNodeIndex)){
            fease = false;
        }

        // New Rule
        if (!checkNew(state, targetNodeIndex, queryNodeIndex)){
            fease =  false;
        }
        return fease;
    }

    /**
     * Check the predecessor rule and successor rule
     * It ensures the consistency of the partial matching
     * @param state				VF2 State
     * @param targetNodeIndex	Target Graph Node Index
     * @param queryNodeIndex	Query Graph Node Index
     * @return					Feasible or not
     */
    private Boolean checkPredAndSucc(State state, int targetNodeIndex , int queryNodeIndex) {
        Node targetNode = state.targetGraph.nodes.get(targetNodeIndex);
        Node queryNode = state.queryGraph.nodes.get(queryNodeIndex);
        int[][] targetAdjacency = state.targetGraph.getAdjacencyMatrix();
        int[][] queryAdjacency = state.queryGraph.getAdjacencyMatrix();


        // Predecessor Rule
        // For all mapped predecessors of the query node,
        // there must exist corresponding predecessors of target node.
        // Vice Versa
        for (Edge e : targetNode.inEdges) {
            int i = state.targetGraph.nodeList.get(e.source.id);
            if (state.core_1[i] > -1) {
                if (queryAdjacency[state.core_1[i]][queryNodeIndex] == -1){
                    return false;	// not such edge in target graph
                } else if (queryAdjacency[state.core_1[i]][queryNodeIndex] != 1) {
                    return false;	// label doesn't match
                }
            }
        }

        for (Edge e : queryNode.inEdges) {
            int i = state.queryGraph.nodeList.get(e.source.id);
            if (state.core_2[i] > -1) {
                if (targetAdjacency[state.core_2[i]][targetNodeIndex] == -1){
                    return false;	// not such edge in target graph
                } else if (targetAdjacency[state.core_2[i]][targetNodeIndex] != 1){
                    return false;	// label doesn't match
                }
            }
        }

        // Successsor Rule
        // For all mapped successors of the query node,
        // there must exist corresponding successors of the target node
        // Vice Versa
        for (Edge e : targetNode.outEdges) {
            int i = state.targetGraph.nodeList.get(e.target.id);
            if (state.core_1[i] > -1) {
                if (queryAdjacency[queryNodeIndex][state.core_1[i]] == -1){
                    return false;	// not such edge in target graph
                } else if (queryAdjacency[queryNodeIndex][state.core_1[i]] != 1) {
                    return false;	// label doesn't match
                }
            }
        }

        for (Edge e : queryNode.outEdges) {
            int i = state.queryGraph.nodeList.get(e.target.id);
            if (state.core_2[i] > -1) {
                if (targetAdjacency[targetNodeIndex][state.core_2[i]] == -1){
                    return false;	// not such edge in target graph
                } else if (targetAdjacency[targetNodeIndex][state.core_2[i]] != 1) {
                    return false;	// label doesn't match
                }
            }
        }

        return true;
    }

    /**
     * Check the in rule and out rule
     * This prunes the search tree using 1-look-ahead
     * @param state				VF2 State
     * @param targetNodeIndex	Target Graph Node Index
     * @param queryNodeIndex	Query Graph Node Index
     * @return					Feasible or not
     */
    private boolean checkInAndOut(State state, int targetNodeIndex , int queryNodeIndex) {

        Node targetNode = state.targetGraph.nodes.get(targetNodeIndex);
        Node queryNode = state.queryGraph.nodes.get(queryNodeIndex);

        int targetPredCnt = 0, targetSucCnt = 0;
        int queryPredCnt = 0, querySucCnt = 0;

        // In Rule
        // The number predecessors/successors of the target node that are in T1in
        // must be larger than or equal to those of the query node that are in T2in
        for (Edge e : targetNode.inEdges){
            int i = state.targetGraph.nodeList.get(e.source.id);
            if (state.inT1in(i)){
                targetPredCnt++;
            }
        }
        for (Edge e : targetNode.outEdges){
            int i = state.targetGraph.nodeList.get(e.target.id);
            if (state.inT1in(i)){
                targetSucCnt++;
            }
        }
        for (Edge e : queryNode.inEdges){
            int i = state.queryGraph.nodeList.get(e.source.id);
            if (state.inT2in(i)){
                queryPredCnt++;
            }
        }
        for (Edge e : queryNode.outEdges){
            int i = state.queryGraph.nodeList.get(e.target.id);
            if (state.inT2in(i)){
                queryPredCnt++;
            }
        }
        if (targetPredCnt < queryPredCnt || targetSucCnt < querySucCnt){
            return false;
        }

        // Out Rule
        // The number predecessors/successors of the target node that are in T1out
        // must be larger than or equal to those of the query node that are in T2out
        targetPredCnt = 0; targetSucCnt = 0;
        queryPredCnt = 0; querySucCnt = 0;
        for (Edge e : targetNode.inEdges){
            int i = state.targetGraph.nodeList.get(e.source.id);
            if (state.inT1out(i)){
                targetPredCnt++;
            }
        }
        for (Edge e : targetNode.outEdges){
            int i = state.targetGraph.nodeList.get(e.target.id);
            if (state.inT1out(i)){
                targetSucCnt++;
            }
        }
        for (Edge e : queryNode.inEdges){
            int i = state.queryGraph.nodeList.get(e.source.id);
            if (state.inT2out(i)){
                queryPredCnt++;
            }
        }
        for (Edge e : queryNode.outEdges){
            int i = state.queryGraph.nodeList.get(e.target.id);
            if (state.inT2out(i)){
                queryPredCnt++;
            }
        }
        if (targetPredCnt < queryPredCnt || targetSucCnt < querySucCnt){
            return false;
        }

        return true;
    }

    /**
     * Check the new rule
     * This prunes the search tree using 2-look-ahead
     * @param state				VF2 State
     * @param targetNodeIndex	Target Graph Node Index
     * @param queryNodeIndex	Query Graph Node Index
     * @return					Feasible or not
     */
    private boolean checkNew(State state, int targetNodeIndex , int queryNodeIndex){

        Node targetNode = state.targetGraph.nodes.get(targetNodeIndex);
        Node queryNode = state.queryGraph.nodes.get(queryNodeIndex);

        int targetPredCnt = 0, targetSucCnt = 0;
        int queryPredCnt = 0, querySucCnt = 0;

        // In Rule
        // The number predecessors/successors of the target node that are in T1in
        // must be larger than or equal to those of the query node that are in T2in
        for (Edge e : targetNode.inEdges){
            int i = state.targetGraph.nodeList.get(e.source.id);
            if (state.inN1Tilde(i)){
                targetPredCnt++;
            }
        }
        for (Edge e : targetNode.outEdges){
            int i = state.targetGraph.nodeList.get(e.target.id);
            if (state.inN1Tilde(i)){
                targetSucCnt++;
            }
        }
        for (Edge e : queryNode.inEdges){
            int i = state.queryGraph.nodeList.get(e.source.id);
            if (state.inN2Tilde(i)){
                queryPredCnt++;
            }
        }
        for (Edge e : queryNode.outEdges){
            int i = state.queryGraph.nodeList.get(e.target.id);
            if (state.inN2Tilde(i)){
                queryPredCnt++;
            }
        }
        if (targetPredCnt < queryPredCnt || targetSucCnt < querySucCnt){
            return false;
        }

        return true;
    }
}
