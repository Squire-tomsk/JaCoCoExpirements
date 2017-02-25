package org.jacoco.core.internal.flow;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by iplus.
 */
public class ControlFlowAnalyzerTest implements IProbeIdGenerator {
    private MethodNode method;
    private int nextProbeId;

    @Before
    public void setup() {
        method = new MethodNode();
        method.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
    }

    @SuppressWarnings("Duplicates")
    private void createLinearSequence() {
        final Label l0 = new Label();
        method.visitLabel(l0);
        method.visitLineNumber(1001, l0);
        method.visitInsn(Opcodes.NOP);
        final Label l1 = new Label();
        method.visitLabel(l1);
        method.visitLineNumber(1002, l1);
        method.visitInsn(Opcodes.RETURN);
    }

    @SuppressWarnings("Duplicates")
    private void createIfBranch() {
        final Label l0 = new Label();
        method.visitLabel(l0);
        method.visitLineNumber(1001, l0);
        method.visitVarInsn(Opcodes.ILOAD, 1);
        Label l1 = new Label();
        method.visitJumpInsn(Opcodes.IFEQ, l1);
        final Label l2 = new Label();
        method.visitLabel(l2);
        method.visitLineNumber(1002, l2);
        method.visitLdcInsn("a");
        method.visitInsn(Opcodes.ARETURN);
        method.visitLabel(l1);
        method.visitLineNumber(1003, l1);
        method.visitLdcInsn("b");
        method.visitInsn(Opcodes.ARETURN);
    }

    @Test
    public void testInDebugger() {
        createIfBranch();
        ControlFlowAnalyzer.Node entry = ControlFlowAnalyzer.makeGraph(method, this);

        assertTrue(true);
    }

    public int nextId() {
        return nextProbeId++;
    }
}
