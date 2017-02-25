package org.jacoco.core.internal.analysis.utils;

import org.jacoco.core.internal.flow.Instruction;

import java.util.*;

/**
 * Created by abuca on 24.02.17.
 */
public class InstructionTreeBuilder {

    private Set<Edge> allEdges = null;
    private List<Node> allNodes = null;
    private Set<Edge> customSpanningTree = null;
    private Set<Node> leafs = null;
    private LinkedHashMap<Instruction, Node> instToNodeMap;
    private Node entry;
    private Node exit;

    public void setAllEdges(Set<Edge> allEdges) {
        this.allEdges = allEdges;
    }

    public void setAllNodes(List<Node> allNodes) {
        this.allNodes = allNodes;
    }

    public void setCustomSpanningTree(Set<Edge> customSpanningTree) {
        this.customSpanningTree = customSpanningTree;
    }

    public void setLeafs(Set<Node> leafs) {
        this.leafs = leafs;
    }

    public List<String> getAcyclicPaths(){
        if (this.allNodes == null){
            throw new RuntimeException("Three is not defined");
        }
        //Get all edges
        if (this.allEdges == null){
            this.allEdges = getAllEdges();
        }
        //Detect leafs
        if(this.leafs == null){
            this.leafs = detectLeafs();
        }
        //Detect backedges
        this.entry = this.allNodes.get(0);
        if(this.leafs.size() > 1){
            this.exit = getEndNode(leafs);
            this.allNodes.add(this.exit);
        }
        else{
            this.exit = this.allNodes.get(this.allNodes.size()-1);
        }

        Set<Edge> backedges = detectBackEdges(this.entry,
                new HashSet<Edge>(),
                new HashSet<Node>());
        //Replace backedges
        Set<Edge> dummyEdges = replaceBackedges(backedges);
        //Weights calculation
        calculateWeights();
        //Build spanning tree
        Set<Edge> spanningTree;
        //Add exit to entry edge
        Edge exitToEntryEdge = Edge.createEdge(exit,entry);
        this.allEdges.add(exitToEntryEdge);
        if (this.customSpanningTree == null){
            spanningTree = buildSpanningTree(exit,
                    allNodes.size(),
                    new HashSet<Edge>(),
                    new HashSet<Node>());
        }
        else {
            spanningTree = this.customSpanningTree;
            spanningTree.add(exitToEntryEdge);
        }
        //compute inc values
        Set<Edge> chords = new HashSet<Edge>(allEdges);
        chords.removeAll(spanningTree);
        for (Edge chord : chords){
            Stack<Edge> cycle = new Stack<Edge>();
            findCycle(chord.from,chord.to,new HashSet<Edge>(spanningTree),cycle);
            chord.inc += chord.weight;
            for(Edge cycleEdge : cycle){
                chord.inc += cycleEdge.weight;
            }
        }
        //paths regeneration
        List<String> paths = new ArrayList<String>();
        for(int i = 0; i < entry.numPaths; i++){
            StringBuilder builder = getPath(i,new StringBuilder(),entry,dummyEdges,exit);
            paths.add(builder.toString().replace("@",""));
        }
        return paths;
    }

    private Set<Node> detectLeafs(){
        Set<Node> leafs = new HashSet<Node>(this.allNodes);
        for(Edge edge : this.allEdges){
            leafs.remove(edge.from);
        }
        return leafs;
    }

    private Set<Edge> getAllEdges(){
        Set<Edge> allEdges = new HashSet<Edge>();
        for(Node node : this.allNodes){
            for(Edge edge : node.edges){
                allEdges.add(edge);
            }
        }
        return allEdges;
    }

    //Modified DFS
    private Set<Edge> detectBackEdges(Node current,
                                     Set<Edge> backedges,
                                     Set<Node> visitedSet){
        if(current == this.exit){
            return backedges;
        }
        visitedSet.add(current);
        for(Edge edge : current.edges){
            if(edge.from == current){
                if(visitedSet.contains(edge.to)){
                    backedges.add(edge);
                }
                else{
                    backedges = detectBackEdges(edge.to,
                                                backedges,
                                                new HashSet<Node>(visitedSet));
                }
            }
        }
        return backedges;
    }



    private void calculateWeights() {
        ListIterator<Node> li = this.allNodes.listIterator(this.allNodes.size());
        while(li.hasPrevious()){
            final Node currentNode = li.previous();
            if(currentNode == this.exit){
                currentNode.numPaths = 1;
            }
            else{
                currentNode.numPaths = 0;
                for(Edge edge : currentNode.edges){
                    if (edge.from == currentNode){
                        edge.weight = edge.from.numPaths;
                        currentNode.numPaths = currentNode.numPaths + edge.to.numPaths;
                    }
                }
            }
            int a = 1;
        }
    }

    private Set<Edge> replaceBackedges(Set<Edge> backedges) {
        Set<Edge> dummpyEdges = new HashSet<Edge>();
        for(Edge backedge : backedges){
            Edge dummyEdgeEntry = Edge.createEdge(entry,backedge.to);
            this.allEdges.add(dummyEdgeEntry);
            Edge dummyEdgeExit = Edge.createEdge(backedge.from,exit);
            this.allEdges.add(dummyEdgeExit);
            dummpyEdges.add(dummyEdgeEntry);
            dummpyEdges.add(dummyEdgeExit);
            this.allEdges.remove(backedge);
        }
        return dummpyEdges;
    }

    private Node getEndNode(Set<Node> leafs) {
        Node exit = new Node();
        exit.label = '@';
        for(Node leaf : leafs){
            Edge edge = new Edge();
            edge.from = leaf;
            edge.to = exit;
            edge.from.edges.add(edge);
            edge.to.edges.add(edge);
        }
        return exit;
    }

    private Set<Node> initialiseEdges(List<Instruction> instructions) {
        Set<Node> leafs = new HashSet<Node>(instToNodeMap.values());
        ListIterator<Instruction> li = instructions.listIterator(instructions.size());
        while(li.hasPrevious()){
            Instruction currentInstruction = li.previous();
            Node currentNode  = instToNodeMap.get(currentInstruction);
            if(currentInstruction.getPredecessor() != null){
                Edge edge = new Edge();
                edge.from = instToNodeMap.get(currentInstruction.getPredecessor());
                edge.to = currentNode;
                edge.from.edges.add(edge);
                edge.to.edges.add(edge);
                leafs.remove(edge.from);
                allEdges.add(edge);
            }
        }
        return leafs;
    }

    private StringBuilder getPath(int R, StringBuilder builder, Node currentNode, Set<Edge> dummyEdges, Node exit){
        Edge maxEdge = new Edge();
        maxEdge.weight = - 1000;
        if(currentNode == exit){
            return builder;
        }
        for(Edge edge : currentNode.edges){
            if(edge.from == currentNode && edge.weight > maxEdge.weight && edge.weight <= R){
                maxEdge = edge;
            }
        }
        if(!dummyEdges.contains(maxEdge) || maxEdge.to == this.exit){
            builder.append(currentNode.label);
        }
        if(!dummyEdges.contains(maxEdge) && maxEdge.to == exit){
            builder.append(maxEdge.to.label);
        }
        return getPath(R-maxEdge.weight,builder,maxEdge.to,dummyEdges,exit);
    }

    private Stack<Edge> findCycle(Node start,
                           Node current,
                           Set<Edge> spanningTree,
                           Stack<Edge> cycle){
        if(current == start){
            return cycle;
        }
        for(Edge edge : current.edges){
            if(spanningTree.contains(edge)){
                cycle.add(edge);
                spanningTree.remove(edge);
                Node next = edge.from == current ? edge.to : edge.from;
                Stack<Edge> stack = findCycle(start, next, spanningTree, cycle);
                if(stack != null){
                    return stack;
                }
            }
        }
        cycle.pop();
        return null;
    }

    //Modified DFS
    private Set<Edge> buildSpanningTree(Node current,
                                  Integer nodesCount,
                                  Set<Edge> spanningTree,
                                  Set<Node> visitedSet){
        if(visitedSet.size() == nodesCount){
            return spanningTree;
        }
        visitedSet.add(current);
        for(Edge edge : current.edges){
            if(edge.from == current){
                if(!visitedSet.contains(edge.to)){
                    spanningTree.add(edge);
                    buildSpanningTree(edge.to,
                                      nodesCount,
                                      spanningTree,
                                      visitedSet);
                }
            }
        }
        return spanningTree;
    }



    public static class Node {
        public char label;
        public int numPaths;
        public List<Edge> edges = new LinkedList<Edge>();
    }

    public static class Edge {
        public Edge() {
        }

        public static Edge createEdge(Node from, Node to){
            Edge edge = new Edge();
            edge.from = from;
            edge.from.edges.add(edge);
            edge.to = to;
            edge.to.edges.add(edge);
            return edge;
        }

        public Node from;
        public Node to;
        public int weight = 0;
        public int inc = 0;
    }
}
