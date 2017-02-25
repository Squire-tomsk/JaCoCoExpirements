package org.jacoco.core.internal.analysis.Utils;

import org.jacoco.core.internal.analysis.utils.InstructionTreeBuilder;
import org.jacoco.core.internal.flow.Instruction;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by abuca on 25.02.17.
 */
public class InstructionTreeBuilderTest {
    private InstructionTreeBuilder instructionTreeBuilder;

    @Before
    public void setup() {
        this.instructionTreeBuilder = new InstructionTreeBuilder();
    }

    @Test
    public void treeFromPaperTest(){
        buildTestNodes();
        instructionTreeBuilder.getAcyclicPaths();
    }

    @Test
    public void treeFromPaperTest2(){
        buildTestNodes2();
        instructionTreeBuilder.getAcyclicPaths();
    }

    @Test
    public void treeFromPaperWithCustomSpanningTree(){
        buildTestNodesWithCustomSpanningTree();
        instructionTreeBuilder.getAcyclicPaths();
    }

    private void buildTestNodes() {
        Set<InstructionTreeBuilder.Node> leafs = new HashSet<InstructionTreeBuilder.Node>();
        Map<Character, InstructionTreeBuilder.Node> labelsMap = new HashMap<Character, InstructionTreeBuilder.Node>();
        char label = 'A';

        List<InstructionTreeBuilder.Node> allNodes = new LinkedList<InstructionTreeBuilder.Node>();
        Set<InstructionTreeBuilder.Edge> allEdges = new HashSet<InstructionTreeBuilder.Edge>();
        for(int i =0;i<9;i++){
            InstructionTreeBuilder.Node node = new InstructionTreeBuilder.Node();
            node.label = label;
            allNodes.add(node);
            labelsMap.put(label,node);
            label++;
        }
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('B')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('F')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('B'), labelsMap.get('C')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('B'), labelsMap.get('D')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('C'), labelsMap.get('E')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('D'), labelsMap.get('E')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('E'), labelsMap.get('F')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('E'), labelsMap.get('B')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('F'), labelsMap.get('G')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('F'), labelsMap.get('H')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('G'), labelsMap.get('I')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('H'), labelsMap.get('I')));
        leafs.add(allNodes.get(allNodes.size()-1));

        this.instructionTreeBuilder.setLeafs(leafs);
        this.instructionTreeBuilder.setAllEdges(allEdges);
        this.instructionTreeBuilder.setAllNodes(allNodes);
    }

    private void buildTestNodes2() {
        Set<InstructionTreeBuilder.Node> leafs = new HashSet<InstructionTreeBuilder.Node>();
        Map<Character, InstructionTreeBuilder.Node> labelsMap = new HashMap<Character, InstructionTreeBuilder.Node>();
        char label = 'A';
        List<InstructionTreeBuilder.Node> allNodes = new LinkedList<InstructionTreeBuilder.Node>();
        Set<InstructionTreeBuilder.Edge> allEdges = new HashSet<InstructionTreeBuilder.Edge>();
        for(int i =0;i<8;i++){
            InstructionTreeBuilder.Node node = new InstructionTreeBuilder.Node();
            node.label = label;
            allNodes.add(node);
            labelsMap.put(label,node);
            label++;
        }
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('B')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('C')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('B'), labelsMap.get('D')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('C'), labelsMap.get('D')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('D'), labelsMap.get('E')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('D'), labelsMap.get('F')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('E'), labelsMap.get('G')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('F'), labelsMap.get('G')));
        allEdges.add(InstructionTreeBuilder.Edge.createEdge(labelsMap.get('F'), labelsMap.get('H')));
        leafs.add(allNodes.get(allNodes.size()-1));
        leafs.add(allNodes.get(allNodes.size()-2));

        instructionTreeBuilder.setLeafs(leafs);
        instructionTreeBuilder.setAllEdges(allEdges);
        instructionTreeBuilder.setAllNodes(allNodes);
    }

    private void buildTestNodesWithCustomSpanningTree() {
        Set<InstructionTreeBuilder.Node> leafs = new HashSet<InstructionTreeBuilder.Node>();
        Map<Character, InstructionTreeBuilder.Node> labelsMap = new HashMap<Character, InstructionTreeBuilder.Node>();
        Set<InstructionTreeBuilder.Edge> customSpanningTree = new HashSet<InstructionTreeBuilder.Edge>();
        char label = 'A';
        List<InstructionTreeBuilder.Node> allNodes = new LinkedList<InstructionTreeBuilder.Node>();
        Set<InstructionTreeBuilder.Edge> allEdges = new HashSet<InstructionTreeBuilder.Edge>();
        for(int i =0;i<6;i++){
            InstructionTreeBuilder.Node node = new InstructionTreeBuilder.Node();
            node.label = label;
            allNodes.add(node);
            labelsMap.put(label,node);
            label++;
        }
        InstructionTreeBuilder.Edge edge;
        edge = InstructionTreeBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('B'));
        edge.weight = 2;
        allEdges.add(edge);
        customSpanningTree.add(edge);
        edge = InstructionTreeBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('C'));
        edge.weight = 0;
        allEdges.add(edge);
        edge = InstructionTreeBuilder.Edge.createEdge(labelsMap.get('B'), labelsMap.get('C'));
        edge.weight = 0;
        allEdges.add(edge);
        edge = InstructionTreeBuilder.Edge.createEdge(labelsMap.get('B'), labelsMap.get('D'));
        edge.weight = 2;
        allEdges.add(edge);
        edge = InstructionTreeBuilder.Edge.createEdge(labelsMap.get('C'), labelsMap.get('D'));
        edge.weight = 0;
        allEdges.add(edge);
        customSpanningTree.add(edge);
        edge = InstructionTreeBuilder.Edge.createEdge(labelsMap.get('D'), labelsMap.get('E'));
        edge.weight = 1;
        allEdges.add(edge);
        edge = InstructionTreeBuilder.Edge.createEdge(labelsMap.get('D'), labelsMap.get('F'));
        edge.weight = 0;
        allEdges.add(edge);
        customSpanningTree.add(edge);
        edge = InstructionTreeBuilder.Edge.createEdge(labelsMap.get('E'), labelsMap.get('F'));
        edge.weight = 0;
        allEdges.add(edge);
        customSpanningTree.add(edge);
        edge = InstructionTreeBuilder.Edge.createEdge(labelsMap.get('F'), labelsMap.get('A'));
        edge.weight = 0;
        allEdges.add(edge);
        customSpanningTree.add(edge);
        leafs.add(allNodes.get(allNodes.size()-1));

        instructionTreeBuilder.setLeafs(leafs);
        instructionTreeBuilder.setAllEdges(allEdges);
        instructionTreeBuilder.setAllNodes(allNodes);
        instructionTreeBuilder.setCustomSpanningTree(customSpanningTree);
    }
}
