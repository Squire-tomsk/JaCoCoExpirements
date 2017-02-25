package org.jacoco.core.internal.analysis.utils;

import org.jacoco.core.internal.flow.Instruction;

import java.util.*;

/**
 * Created by abuca on 24.02.17.
 */
public class InstructionTreeBuilder {

    private Set<Edge> allEdges;
    private List<Node> allNodes;
    private LinkedHashMap<Instruction, Node> instToNodeMap;
    private Node entry;
    private Node exit;

    private void buildInstructionToNodeMap(List<Instruction> instructions) {
        char label = 'A';
        this.allNodes = new LinkedList<Node>();
        this.instToNodeMap = new LinkedHashMap<Instruction, Node>();
        for(Instruction instruction : instructions){
            Node node = new Node();
            node.label = label;
            instToNodeMap.put(instruction, node);
            label++;
            allNodes.add(node);
        }
    }

    private Set<Node> buildTestNodes() {
        Set<Node> leafs = new HashSet<Node>();
        Map<Character, Node> labelsMap = new HashMap<Character, Node>();
        char label = 'A';
        this.allNodes = new LinkedList<Node>();
        this.instToNodeMap = new LinkedHashMap<Instruction, Node>();
        this.allEdges = new HashSet<Edge>();
        for(int i =0;i<9;i++){
            Node node = new Node();
            node.label = label;
            this.allNodes.add(node);
            labelsMap.put(label,node);
            label++;
        }
        this.allEdges.add(Edge.createEdge(labelsMap.get('A'), labelsMap.get('B')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('A'), labelsMap.get('F')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('B'), labelsMap.get('C')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('B'), labelsMap.get('D')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('C'), labelsMap.get('E')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('D'), labelsMap.get('E')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('E'), labelsMap.get('F')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('E'), labelsMap.get('B')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('F'), labelsMap.get('G')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('F'), labelsMap.get('H')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('G'), labelsMap.get('I')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('H'), labelsMap.get('I')));
        leafs.add(this.allNodes.get(this.allNodes.size()-1));
        return leafs;
    }

    private Set<Node> buildTestNodes2() {
        Set<Node> leafs = new HashSet<Node>();
        Map<Character, Node> labelsMap = new HashMap<Character, Node>();
        char label = 'A';
        this.allNodes = new LinkedList<Node>();
        this.instToNodeMap = new LinkedHashMap<Instruction, Node>();
        this.allEdges = new HashSet<Edge>();
        for(int i =0;i<8;i++){
            Node node = new Node();
            node.label = label;
            this.allNodes.add(node);
            labelsMap.put(label,node);
            label++;
        }
        this.allEdges.add(Edge.createEdge(labelsMap.get('A'), labelsMap.get('B')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('A'), labelsMap.get('C')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('B'), labelsMap.get('D')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('C'), labelsMap.get('D')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('D'), labelsMap.get('E')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('D'), labelsMap.get('F')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('E'), labelsMap.get('G')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('F'), labelsMap.get('G')));
        this.allEdges.add(Edge.createEdge(labelsMap.get('F'), labelsMap.get('H')));
        leafs.add(this.allNodes.get(this.allNodes.size()-1));
        leafs.add(this.allNodes.get(this.allNodes.size()-2));
        return leafs;
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

    public List<String> getAcyclicPaths(List<Instruction> instructions){
       /* //Build map
        buildInstructionToNodeMap(instructions);
        //Build tree
        this.allEdges = new HashSet<Edge>();
        Set<Node> leafs = initialiseEdges(instructions);*/
        //Detect backedges
        Set<Node> leafs = buildTestNodes2();
        this.entry = this.allNodes.get(0);
        if(leafs.size() > 1){
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
        //Add exit to entry edge
        Edge exitToEntryEdge = new Edge();
        exitToEntryEdge.from = exit;
        exitToEntryEdge.to = entry;
        exitToEntryEdge.from.edges.add(exitToEntryEdge);
        exitToEntryEdge.to.edges.add(exitToEntryEdge);
        //Build spanning tree
        Set<Edge> spanningTree = buildSpanningTree(exit,
                                                   allNodes.size(),
                                                   new HashSet<Edge>(),
                                                   new HashSet<Node>());
        //compute inc values
        Set<Edge> chords = new HashSet<Edge>(allEdges);
        chords.removeAll(spanningTree);
        for (Edge chord : chords){
            Queue<Edge> cycle = new LinkedList<Edge>();
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
            Edge dummyEdgeEntry = new Edge();
            dummyEdgeEntry.from = entry;
            dummyEdgeEntry.to = backedge.to;
            this.entry.edges.add(dummyEdgeEntry);
            Edge dummyEdgeExit = new Edge();
            dummyEdgeExit.from = backedge.from;
            dummyEdgeExit.to = exit;
            this.exit.edges.add(dummyEdgeExit);
            dummpyEdges.add(dummyEdgeEntry);
            dummpyEdges.add(dummyEdgeExit);
            backedge.from.edges.add(dummyEdgeExit);
            backedge.to.edges.add(dummyEdgeEntry);
            backedge.from.edges.remove(backedge);
            backedge.to.edges.remove(backedge);
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

    private void findCycle(Node start,
                           Node current,
                           Set<Edge> spanningTree,
                           Queue<Edge> cycle){
        if(current == start){
            return;
        }
        for(Edge edge : current.edges){
            if(spanningTree.contains(edge)){
                cycle.add(edge);
                spanningTree.remove(edge);
                Node next = edge.from == current ? edge.to : edge.from;
                findCycle(start, next, spanningTree, cycle);
            }
        }
        cycle.poll();
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



    private static class Node {
        char label;
        int numPaths;
        List<Edge> edges = new LinkedList<Edge>();
    }

    private static class Edge {
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

        Node from;
        Node to;
        int weight = 0;
        int inc = 0;
    }
}
