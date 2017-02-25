package org.jacoco.core.internal.flow;

import org.jacoco.core.internal.analysis.utils.AcyclicPathBuilder;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ControlFlowAnalyzer extends MethodProbesVisitor {
    public AcyclicPathBuilder.Node entry;
    private AcyclicPathBuilder.Node currentNode;
    private HashMap<Label, AcyclicPathBuilder.Node> nodes;
    private int lastProbeId;
    private char nodeLabel = 'A';

    public static AcyclicPathBuilder.Node exit = new AcyclicPathBuilder.Node();

    public static AcyclicPathBuilder.Node makeGraph(final MethodNode method, final IProbeIdGenerator idGenerator) {
        // We do not use the accept() method as ASM resets labels after every
        // call to accept()
        final ControlFlowAnalyzer lfa = new ControlFlowAnalyzer();
        LabelFlowAnalyzer.markLabels(method);

        for (int i = method.tryCatchBlocks.size(); --i >= 0;) {
            method.tryCatchBlocks.get(i).accept(lfa);
        }
        method.instructions.accept(new MethodProbesAdapter(lfa, idGenerator));
        exit.label = lfa.nodeLabel;
        return lfa.entry;
    }

    private ControlFlowAnalyzer() {
        entry = new AcyclicPathBuilder.Node();
        entry.label = nodeLabel;
        nodes = new LinkedHashMap<Label, AcyclicPathBuilder.Node>();
        nodeLabel++;
        currentNode = entry;
    }

    @Override
    public void visitJumpInsnWithProbe(final int opcode, final Label label,
                                       final int probeId, final IFrame frame) {
        if( LabelInfo.isMultiTarget(label) ) {
            AcyclicPathBuilder.Node target = addOrGetNode(label);
            AcyclicPathBuilder.Edge edge = AcyclicPathBuilder.Edge.createEdge(currentNode, target);
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
            AcyclicPathBuilder.Node target = addOrGetNode(label);
            AcyclicPathBuilder.Edge edge = AcyclicPathBuilder.Edge.createEdge(currentNode, target);
            edge.numProbe = lastProbeId;

            currentNode = target;
        }
    }

    @Override
    public void visitInsnWithProbe(final int opcode, final int probeId) {
        // EXIT node
        AcyclicPathBuilder.Edge edge = AcyclicPathBuilder.Edge.createEdge(currentNode, exit);
        edge.numProbe = probeId;
    }

    @Override
    public void visitEnd() {

    }


    private AcyclicPathBuilder.Node addOrGetNode(Label label) {
        if( nodes.get(label) == null ) {
            AcyclicPathBuilder.Node node = new AcyclicPathBuilder.Node();
            node.label = nodeLabel;
            nodeLabel++;
            nodes.put(label, node);
        }
        return nodes.get(label);
    }


}

