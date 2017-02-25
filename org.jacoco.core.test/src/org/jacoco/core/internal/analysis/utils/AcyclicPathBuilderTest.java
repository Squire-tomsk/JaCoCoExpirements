package org.jacoco.core.internal.analysis.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by abuca on 25.02.17.
 */
public class AcyclicPathBuilderTest {
    private AcyclicPathBuilder acyclicPathBuilder;

    @Before
    public void setup() {
        this.acyclicPathBuilder = new AcyclicPathBuilder();
    }

    @Test
    public void pathTestFromPaper(){ //test from example on figure 10
        buildTestNodes();
        acyclicPathBuilder.createAllAcyclicPaths();
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("AFGI"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("AFHI"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ABCEFGI"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ABCEFHI"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ABDEFGI"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ABDEFHI"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ABCE"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ABDE"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("BCE"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("BDE"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("BCEFGI"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("BCEFHI"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("BDEFGI"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("BDEFHI"));
    }

    @Test
/*
        A
       / \
      |   \
      |    |
      v    v
      B    C
      |    |
      |    |
       \  /
        D
       / \
      |   \
      |    |
      v    v
      E    F
      |    |
      |    |
       \  / \
        G    H
      *
 */
    public void pathTestByLike8Graph(){
        buildTestNodes2();
        acyclicPathBuilder.createAllAcyclicPaths();
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ABDEG"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ABDFG"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ABDFH"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ACDEG"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ACDFG"));
        Assert.assertTrue(acyclicPathBuilder.getPaths().contains("ACDFH"));
    }

    @Test
    public void incValuesTestFromPaper(){ //test from example on figure 7
        buildTestNodesWithCustomSpanningTree();
        acyclicPathBuilder.createAllAcyclicPaths();
        for(AcyclicPathBuilder.Edge edgeWithProbe : acyclicPathBuilder.getChords()){
            if(edgeWithProbe.from.label == 'A' && edgeWithProbe.to.label == 'C'){
                Assert.assertEquals(0,edgeWithProbe.inc);
            }
            else if(edgeWithProbe.from.label == 'B' && edgeWithProbe.to.label == 'C'){
                Assert.assertEquals(2,edgeWithProbe.inc);
            }
            else if(edgeWithProbe.from.label == 'B' && edgeWithProbe.to.label == 'D'){
                Assert.assertEquals(4,edgeWithProbe.inc);
            }
            else if(edgeWithProbe.from.label == 'D' && edgeWithProbe.to.label == 'E'){
                Assert.assertEquals(1,edgeWithProbe.inc);
            }
            else {
                Assert.assertTrue(false);
            }
        }
    }

    private void buildTestNodes() {
        Set<AcyclicPathBuilder.Node> leafs = new HashSet<AcyclicPathBuilder.Node>();
        Map<Character, AcyclicPathBuilder.Node> labelsMap = new HashMap<Character, AcyclicPathBuilder.Node>();
        char label = 'A';

        List<AcyclicPathBuilder.Node> allNodes = new LinkedList<AcyclicPathBuilder.Node>();
        Set<AcyclicPathBuilder.Edge> allEdges = new HashSet<AcyclicPathBuilder.Edge>();
        for(int i =0;i<9;i++){
            AcyclicPathBuilder.Node node = new AcyclicPathBuilder.Node();
            node.label = label;
            allNodes.add(node);
            labelsMap.put(label,node);
            label++;
        }
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('B')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('F')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('B'), labelsMap.get('C')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('B'), labelsMap.get('D')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('C'), labelsMap.get('E')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('D'), labelsMap.get('E')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('E'), labelsMap.get('B')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('F'), labelsMap.get('G')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('F'), labelsMap.get('H')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('G'), labelsMap.get('I')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('H'), labelsMap.get('I')));
        leafs.add(allNodes.get(allNodes.size()-1));

        //this.acyclicPathBuilder.setLeafs(leafs);
        //this.acyclicPathBuilder.setAllEdges(allEdges);
        this.acyclicPathBuilder.setAllNodes(allNodes);
    }

    private void buildTestNodes2() {
        Set<AcyclicPathBuilder.Node> leafs = new HashSet<AcyclicPathBuilder.Node>();
        Map<Character, AcyclicPathBuilder.Node> labelsMap = new HashMap<Character, AcyclicPathBuilder.Node>();
        char label = 'A';
        List<AcyclicPathBuilder.Node> allNodes = new LinkedList<AcyclicPathBuilder.Node>();
        Set<AcyclicPathBuilder.Edge> allEdges = new HashSet<AcyclicPathBuilder.Edge>();
        for(int i =0;i<8;i++){
            AcyclicPathBuilder.Node node = new AcyclicPathBuilder.Node();
            node.label = label;
            allNodes.add(node);
            labelsMap.put(label,node);
            label++;
        }
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('B')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('C')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('B'), labelsMap.get('D')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('C'), labelsMap.get('D')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('D'), labelsMap.get('E')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('D'), labelsMap.get('F')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('E'), labelsMap.get('G')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('F'), labelsMap.get('G')));
        allEdges.add(AcyclicPathBuilder.Edge.createEdge(labelsMap.get('F'), labelsMap.get('H')));

        //acyclicPathBuilder.setLeafs(leafs);
        //acyclicPathBuilder.setAllEdges(allEdges);
        acyclicPathBuilder.setAllNodes(allNodes);
    }

    private void buildTestNodesWithCustomSpanningTree() {
        Set<AcyclicPathBuilder.Node> leafs = new HashSet<AcyclicPathBuilder.Node>();
        Map<Character, AcyclicPathBuilder.Node> labelsMap = new HashMap<Character, AcyclicPathBuilder.Node>();
        Set<AcyclicPathBuilder.Edge> customSpanningTree = new HashSet<AcyclicPathBuilder.Edge>();
        char label = 'A';
        List<AcyclicPathBuilder.Node> allNodes = new LinkedList<AcyclicPathBuilder.Node>();
        Set<AcyclicPathBuilder.Edge> allEdges = new HashSet<AcyclicPathBuilder.Edge>();
        for(int i =0;i<6;i++){
            AcyclicPathBuilder.Node node = new AcyclicPathBuilder.Node();
            node.label = label;
            allNodes.add(node);
            labelsMap.put(label,node);
            label++;
        }
        AcyclicPathBuilder.Edge edge;
        edge = AcyclicPathBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('B'));
        edge.weight = 2;
        allEdges.add(edge);
        customSpanningTree.add(edge);
        edge = AcyclicPathBuilder.Edge.createEdge(labelsMap.get('A'), labelsMap.get('C'));
        edge.weight = 0;
        allEdges.add(edge);
        edge = AcyclicPathBuilder.Edge.createEdge(labelsMap.get('B'), labelsMap.get('C'));
        edge.weight = 0;
        allEdges.add(edge);
        edge = AcyclicPathBuilder.Edge.createEdge(labelsMap.get('B'), labelsMap.get('D'));
        edge.weight = 2;
        allEdges.add(edge);
        edge = AcyclicPathBuilder.Edge.createEdge(labelsMap.get('C'), labelsMap.get('D'));
        edge.weight = 0;
        allEdges.add(edge);
        customSpanningTree.add(edge);
        edge = AcyclicPathBuilder.Edge.createEdge(labelsMap.get('D'), labelsMap.get('E'));
        edge.weight = 1;
        allEdges.add(edge);
        edge = AcyclicPathBuilder.Edge.createEdge(labelsMap.get('D'), labelsMap.get('F'));
        edge.weight = 0;
        allEdges.add(edge);
        customSpanningTree.add(edge);
        edge = AcyclicPathBuilder.Edge.createEdge(labelsMap.get('E'), labelsMap.get('F'));
        edge.weight = 0;
        allEdges.add(edge);
        customSpanningTree.add(edge);
//        edge = AcyclicPathBuilder.Edge.createEdge(labelsMap.get('F'), labelsMap.get('A'));
//        edge.weight = 0;
//        allEdges.add(edge);
//        customSpanningTree.add(edge);
        leafs.add(allNodes.get(allNodes.size()-1));

        ///acyclicPathBuilder.setLeafs(leafs);
        //acyclicPathBuilder.setAllEdges(allEdges);
        acyclicPathBuilder.setAllNodes(allNodes);
        acyclicPathBuilder.setCustomSpanningTree(customSpanningTree);
    }
}
