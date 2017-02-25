package org.jacoco.core.internal.analysis.utils;

import org.jacoco.core.internal.flow.Instruction;

import java.util.*;

/**
 * Created by abuca on 24.02.17.
 */
public class InstructionTreeBuilder {
    public static void buildInstructionsTree(List<Instruction> instructions){
        //Build map
        char label = 'A';
        Map<Instruction, Node> instToNodeMap = new LinkedHashMap<Instruction, Node>();
        for(Instruction instruction : instructions){
            Node node = new Node();
            node.label = label;
            instToNodeMap.put(instruction, node);
            label++;
        }
        //Build tree
        ListIterator<Instruction> li = instructions.listIterator(instructions.size());
        Set<Node> leafs = new HashSet<Node>(instToNodeMap.values());
        Set<Edge> allEdges = new HashSet<Edge>();
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
        //Detect backedges
        Node entry = instToNodeMap.get(instructions.get(0));
        Node exit = new Node();
        exit.label = label;
        for(Node leaf : leafs){
            Edge edge = new Edge();
            edge.from = leaf;
            edge.to = exit;
            edge.from.edges.add(edge);
            edge.to.edges.add(edge);
        }

        Set<Edge> backedges = new HashSet<Edge>();
        detectBackEdges(entry,
                        exit,
                        backedges,
                        new HashSet<Node>());
        //Replace backedges
        Set<Edge> dummpyEdges = new HashSet<Edge>();
        for(Edge backedge : backedges){
            Edge dummyEdgeEntry = new Edge();
            dummyEdgeEntry.from = entry;
            dummyEdgeEntry.to = backedge.to;
            Edge dummyEdgeExit = new Edge();
            dummyEdgeExit.from = backedge.from;
            dummyEdgeExit.to = exit;
            dummpyEdges.add(dummyEdgeEntry);
            dummpyEdges.add(dummyEdgeExit);
            backedge.from.edges.add(dummyEdgeExit);
            backedge.to.edges.add(dummyEdgeEntry);
            backedge.from.edges.remove(backedge);
            backedge.to.edges.remove(backedge);
        }
        //Weights calculation
        li = instructions.listIterator(instructions.size());
        while(li.hasPrevious()){
            Instruction currentInstruction = li.previous();
            final Node currentNode  = instToNodeMap.get(currentInstruction);
            if(leafs.contains(currentNode)){
                currentNode.numPaths = 1;
            }
            else{
                currentNode.numPaths = 0;
                for(Edge edge : currentNode.edges){
                    if (edge.from == currentNode){
                        edge.weight = edge.from.numPaths;
                        edge.from.numPaths = edge.from.numPaths + edge.to.numPaths;
                    }
                }
            }
        }
        //Add exit to entry edge
        Edge exitToEntryEdge = new Edge();
        exitToEntryEdge.from = exit;
        exitToEntryEdge.to = entry;
        exitToEntryEdge.from.edges.add(exitToEntryEdge);
        exitToEntryEdge.to.edges.add(exitToEntryEdge);
        //Build spanning tree
        Set<Edge> spanningTree = new HashSet<Edge>();
        buildSpanningTree(exit,
                          instToNodeMap.size(),
                          spanningTree,
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
            StringBuilder builder = new StringBuilder();
            getPath(i,builder,entry,dummpyEdges,exit);
            paths.add(builder.toString());
        }
        paths.clear();
    }

    private static void getPath(int R, StringBuilder builder, Node currentNode, Set<Edge> dummyEdges, Node exit){
        Edge maxEdge = new Edge();
        maxEdge.weight = - 1000;
        if(currentNode == exit){
            return;
        }
        for(Edge edge : currentNode.edges){
            if(edge.from == currentNode && edge.weight > maxEdge.weight && edge.weight <= R){
                maxEdge = edge;
            }
        }
        if(!dummyEdges.contains(maxEdge)){
            builder.append(currentNode.label);
        }
        getPath(R-maxEdge.weight,builder,maxEdge.to,dummyEdges,exit);
    }

    private static void findCycle(Node start,
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
    private static void buildSpanningTree(Node current,
                                          Integer nodesCount,
                                          Set<Edge> spanningTree,
                                          Set<Node> visitedSet){
        if(visitedSet.size() == nodesCount){
            return;
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
    }

    //Modified DFS
    private static void detectBackEdges(Node current,
                                        Node end,
                                        Set<Edge> backedges,
                                        Set<Node> visitedSet){
        if(current == end){
            return;
        }
        visitedSet.add(current);
        for(Edge edge : current.edges){
            if(edge.from == current){
                if(visitedSet.contains(edge.to)){
                    backedges.add(edge);
                }
                else{
                    detectBackEdges(edge.to,
                                    end,
                                    backedges,
                                    new HashSet<Node>(visitedSet));
                }
            }
        }
    }

    private static class Node {
        char label;
        int numPaths;
        List<Edge> edges = new LinkedList<Edge>();
    }

    private static class Edge {
        Node from;
        Node to;
        int weight = 0;
        int inc = 0;
    }
}
