package org.jacoco.core.internal.flow;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class ControlFlowAnalyzer extends MethodProbesVisitor {
    public Node entry;
    private Node currentNode;
    private HashMap<Label, Node> nodes;
    private int lastProbeId;

    public  static Node exit = new Node();

    public static Node makeGraph(final MethodNode method, final IProbeIdGenerator idGenerator) {
        // We do not use the accept() method as ASM resets labels after every
        // call to accept()
        final ControlFlowAnalyzer lfa = new ControlFlowAnalyzer();

        for (int i = method.tryCatchBlocks.size(); --i >= 0;) {
            method.tryCatchBlocks.get(i).accept(lfa);
        }
        method.instructions.accept(new MethodProbesAdapter(lfa, idGenerator));
        return lfa.entry;
    }

    private ControlFlowAnalyzer() {
        entry = new Node();
        currentNode = entry;
    }

    @Override
    public void visitJumpInsnWithProbe(final int opcode, final Label label,
                                       final int probeId, final IFrame frame) {
        if( LabelInfo.isMultiTarget(label) ) {
            Node target = addOrGetNode(label);
            Edge edge = Edge.createEdge(currentNode, target);
            edge.numProbe = probeId;
        }
    }

    @Override
    public void visitProbe(final int probeId) {
        // called just before Label with probe
        // next Label will be new Node
        lastProbeId = probeId;
    }

    @Override
    public void visitLabel(final Label label) {
        if( LabelInfo.needsProbe(label) ) {
            Node target = addOrGetNode(label);
            Edge edge = Edge.createEdge(currentNode, target);
            edge.numProbe = lastProbeId;

            currentNode = target;
        }
    }

    @Override
    public void visitInsnWithProbe(final int opcode, final int probeId) {
        // EXIT node
        Edge edge = Edge.createEdge(currentNode, exit);
        edge.numProbe = probeId;
    }

    @Override
    public void visitEnd() {

    }


    private Node addOrGetNode(Label label) {
        if( nodes.get(label) == null ) {
            Node node = new Node();
            nodes.put(label, node);
        }
        return nodes.get(label);
    }


    public static class Node {
        int numPaths;
        List<Edge> edges = new LinkedList<Edge>();
    }

    public static class Edge {
        public static Edge createEdge(Node from, Node to){
            Edge edge = new Edge();
            edge.from = from;
            edge.from.edges.add(edge);
            edge.to = to;
            //edge.to.edges.add(edge);
            return edge;
        }

        Node from;
        Node to;
        int weight = 0;
        int inc = 0;
        int numProbe;
    }
}

