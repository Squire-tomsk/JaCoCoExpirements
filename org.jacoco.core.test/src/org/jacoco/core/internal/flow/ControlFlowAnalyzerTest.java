package org.jacoco.core.internal.flow;

import org.jacoco.core.internal.analysis.utils.AcyclicPathBuilder;
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

    private void createLoop() {
        //TODO: create loop
        //simple empty loop
        Label l0 = new Label();
        method.visitLabel(l0);
        method.visitLineNumber(1001, l0);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitVarInsn(Opcodes.ISTORE,1); //int i = 0;
        Label l2 = new Label();
        Label l1 = new Label();
        method.visitLabel(l1);
        method.visitLineNumber(1002, l1);
        method.visitIntInsn(Opcodes.BIPUSH,10); //loop;
        method.visitJumpInsn(Opcodes.IF_ICMPGE, l2); //i<10
        method.visitIincInsn(1,1); //i++
        method.visitJumpInsn(Opcodes.GOTO, l1);
        method.visitLabel(l2);
        method.visitLineNumber(1003, l2);
        method.visitInsn(Opcodes.RETURN);
    }

    @Test
    public void testInDebugger() {
        createLoop();
        /*
        CODE           INP  NODE
        L0:             0   A
        CONST 0             A
        ISTORE 1            A
            PROBE(0)
        L1:             2   B
        BIPUSH 10           B
        IF_ICMPGE L2    1   B
        INC 1 1             B
            PROBE(1)
        GOTO L1         1   B
        L2:             1   B
            PROBE(2)
        RETURN              C

        A       PROBE NUM
        |       0
        v
        B <--   1
       / \  /
      |   \/
      |
      v         2
      C         EXIT
      * */
        AcyclicPathBuilder.Node entry = ControlFlowAnalyzer.makeGraph(method, this);

        assertTrue(true);
    }

    public int nextId() {
        return nextProbeId++;
    }
}
