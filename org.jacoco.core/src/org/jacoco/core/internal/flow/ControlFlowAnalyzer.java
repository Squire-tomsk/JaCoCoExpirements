package org.jacoco.core.internal.flow;

import org.jacoco.core.internal.analysis.utils.InstructionTreeBuilder;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ControlFlowAnalyzer extends MethodProbesVisitor {
    public InstructionTreeBuilder.Node entry;
    private InstructionTreeBuilder.Node currentNode;
    private HashMap<Label, InstructionTreeBuilder.Node> nodes;
    private int lastProbeId;
    private char nodeLabel = 'A';

    public static InstructionTreeBuilder.Node exit = new InstructionTreeBuilder.Node();

    public static InstructionTreeBuilder.Node makeGraph(final MethodNode method, final IProbeIdGenerator idGenerator) {
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
        entry = new InstructionTreeBuilder.Node();
        entry.label = nodeLabel;
        nodes = new LinkedHashMap<Label, InstructionTreeBuilder.Node>();
        nodeLabel++;
        currentNode = entry;
    }

    @Override
    public void visitJumpInsnWithProbe(final int opcode, final Label label,
                                       final int probeId, final IFrame frame) {
        if( LabelInfo.isMultiTarget(label) ) {
            InstructionTreeBuilder.Node target = addOrGetNode(label);
            InstructionTreeBuilder.Edge edge = InstructionTreeBuilder.Edge.createEdge(currentNode, target);
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
            InstructionTreeBuilder.Node target = addOrGetNode(label);
            InstructionTreeBuilder.Edge edge = InstructionTreeBuilder.Edge.createEdge(currentNode, target);
            edge.numProbe = lastProbeId;

            currentNode = target;
        }
    }

    @Override
    public void visitInsnWithProbe(final int opcode, final int probeId) {
        // EXIT node
        InstructionTreeBuilder.Edge edge = InstructionTreeBuilder.Edge.createEdge(currentNode, exit);
        edge.numProbe = probeId;
    }

    @Override
    public void visitEnd() {

    }


    private InstructionTreeBuilder.Node addOrGetNode(Label label) {
        if( nodes.get(label) == null ) {
            InstructionTreeBuilder.Node node = new InstructionTreeBuilder.Node();
            node.label = nodeLabel;
            nodeLabel++;
            nodes.put(label, node);
        }
        return nodes.get(label);
    }


}

