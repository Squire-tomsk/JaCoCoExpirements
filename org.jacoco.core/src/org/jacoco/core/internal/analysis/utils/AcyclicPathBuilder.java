package org.jacoco.core.internal.analysis.utils;

import org.jacoco.core.internal.flow.Instruction;

import java.util.*;

/**
 * Created by abuca on 24.02.17.
 */
public class AcyclicPathBuilder {

    private Set<Edge> allEdges;
    private List<Node> allNodes;
    private Set<Edge> customSpanningTree;
    private Set<Node> leafs;
    private LinkedHashMap<Instruction, Node> instToNodeMap;
    private Node entry;
    private Node exit;
    private Set<Edge> chords;
    private List<String> paths;

    public Set<Edge> getChords() {
        return chords;
    }

    public List<String> getPaths() {
        return paths;
    }

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

    public void setEntry(Node entry) {
        this.entry = entry;
    }

    public void build(){
        if (this.allNodes == null && this.entry == null){
            throw new RuntimeException("Three is not defined");
        }
        if (this.allNodes == null){
            this.allNodes = getAllNodes();
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
        if(this.customSpanningTree == null){
            calculateWeights();
        }
        else{
            restoreNumPaths();
        }
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
        chords = new HashSet<Edge>(allEdges);
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
        Set <String> allPaths = new HashSet<String>();
        for(int i = 0; i < entry.numPaths; i++){
            StringBuilder builder = getPath(i,new StringBuilder(),entry,dummyEdges,exit);
            allPaths.add(builder.toString().replace("@",""));
        }
        paths = new ArrayList<String>(allPaths);
    }

    private Set<Node> detectLeafs(){
        Set<Node> leafs = new HashSet<Node>(this.allNodes);
        for(Edge edge : this.allEdges){
            leafs.remove(edge.from);
        }
        return leafs;
    }

    //BFS based
    private List<Node> getAllNodes(){
        Set<Node> nodes = new HashSet<Node>();
        Queue<Node> queue = new LinkedList<Node>();
        queue.add(this.entry);
        while (!queue.isEmpty()){
            Node currentNode = queue.poll();
            if(!nodes.contains(currentNode)){
                nodes.add(currentNode);
                for(Edge edge : currentNode.edges){
                    if(edge.from == currentNode && edge.to != currentNode){
                        queue.add(edge.to);
                    }
                }
            }
        }
        Set<Edge> allEdges = new HashSet<Edge>();
        List<Node> allNodes = new ArrayList<Node>();
        for(Node node : nodes){
            for(Edge edge : node.edges){
                allEdges.add(edge);
            }
        }
        Set<Edge> backEdges = detectBackEdges(this.entry,
                                              new HashSet<Edge>(),
                                              new HashSet<Node>());
        allEdges.removeAll(backEdges);
        while(!nodes.isEmpty()){
            Set<Node> heads = new HashSet<Node>(nodes);
            for(Edge edge : allEdges){
                heads.remove(edge.to);
            }
            nodes.removeAll(heads);
            for(Node node : heads){
                allNodes.add(node);
                Iterator<Edge> iterator = allEdges.iterator();
                while (iterator.hasNext()){
                    Edge edge = iterator.next();
                    if(edge.from == node){
                        iterator.remove();
                    }
                }
            }

        }
        return allNodes;
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
                        currentNode.numPaths += edge.to.numPaths;
                    }
                }
            }
        }
    }

    private void restoreNumPaths() {
        ListIterator<Node> li = this.allNodes.listIterator(this.allNodes.size());
        while(li.hasPrevious()){
            final Node currentNode = li.previous();
            currentNode.numPaths = 0;
            for (Edge edge : currentNode.edges) {
                if (edge.from == currentNode) {
                    if (currentNode.numPaths < edge.weight) {
                        currentNode.numPaths = edge.weight;
                        currentNode.numPaths += edge.to.numPaths;
                    }
                }
            }
        }
        for(Node currentNode : this.allNodes){
            currentNode.numPaths++;
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
            backedge.from.edges.remove(backedge);
            backedge.to.edges.remove(backedge);
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

        public int numProbe;
        public Node from;
        public Node to;
        public int weight = 0;
        public int inc = 0;
    }
}
