package org.geogit.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.geogit.api.RevObject.TYPE;
import org.junit.Test;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Class for testing the the HRPlusTree
 * 
 * @author neelesh
 * 
 */

public class HRPlusTreeTest {

	// assuming insert works - TODO: test insert(going into same container+diff
	// container)
	private HRPlusTree initHRPlusTree() {
		return new HRPlusTree();
	}
	

	@Test
	public void testGetNodes() {
		HRPlusTree tree = new HRPlusTree();
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		ObjectId id1    = ObjectId.forString("building1");
		ObjectId id2    = ObjectId.forString("building2");
		ObjectId id3    = ObjectId.forString("building3");
		ObjectId versionId1 = ObjectId.forString("Version1");
		// Feature Nodes in Version 1
		Envelope a1 = new Envelope(-12, -10, -2, 2);
		Envelope b1 = new Envelope(12, -10, -2, 2);
		Envelope c1 = new Envelope(-10, 12, -2, 2);

		HRPlusNode nodeA1 = new HRPlusNode(id1, a1, versionId1);
		HRPlusNode nodeB1 = new HRPlusNode(id2, b1, versionId1);;
		HRPlusNode nodeC1 = new HRPlusNode(id3, c1, versionId1);;
		

		tree.insert(id1, a1,versionId1);
		tree.insert(id2, b1,versionId1);
		tree.insert(id3, c1,versionId1);
		nodes.add(nodeA1);
		nodes.add(nodeB1);
		nodes.add(nodeC1);
		
		List<HRPlusNode> result = tree.getNodes();
		assertEquals(nodes.size(), result.size());
		assertTrue(result.containsAll(nodes));
		
	

	}

	@Test
	public void testgetNumRoots() {
		HRPlusTree tree = new HRPlusTree();
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		// 1 Feature Node per Version (4 versions)
		Envelope a1 = new Envelope(-12, -10, -2, 2);
		Envelope a2 = new Envelope(12, -10, -2, 2);
		Envelope a3 = new Envelope(-10, 12, -2, 2);
		Envelope a4 = new Envelope(12, 10, -2, 2);
		ObjectId id1    = ObjectId.forString("building1");
		ObjectId id2    = ObjectId.forString("building2");
		ObjectId id3    = ObjectId.forString("building3");
		ObjectId id4    = ObjectId.forString("building4");
		ObjectId versionId1 = ObjectId.forString("Version1");
		ObjectId versionId2 = ObjectId.forString("Version2");
		
		
		HRPlusNode nodeA1 = new HRPlusNode(id1, a1,versionId1);
		HRPlusNode nodeB1 = new HRPlusNode(id2, a1,versionId1);
		HRPlusNode nodeA2 = new HRPlusNode(id3, a1,versionId2);
		HRPlusNode nodeB2 = new HRPlusNode(id4, a1,versionId2);

		tree.insert(id1, a1,versionId1);
		tree.insert(id2, a1,versionId1);
		tree.insert(id3, a1,versionId2);
		tree.insert(id4, a1,versionId2);

		nodes.add(nodeA1);
		nodes.add(nodeB1);
		nodes.add(nodeA2);
		nodes.add(nodeB2);
		
		assertEquals(2, tree.getNumRoots());

	}

	
	@Test
	public void testInsertNodesDiffVersion() {

		// Simulate two different commits with 3 feature nodes each.
		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId1 = ObjectId.forString("Version1");
		ObjectId versionId2 = ObjectId.forString("Version2");
		
		// Feature Nodes in Version 1
		Envelope a1 = new Envelope(-12, -10, -2, 2);
		Envelope b1 = new Envelope(-8, -6, -2, 2);
		Envelope c1 = new Envelope(-4, -2, -2, 2);
		ObjectId id1_v1    = ObjectId.forString("building1V1");
		ObjectId id2_v1    = ObjectId.forString("building2V1");
		ObjectId id3_v1    = ObjectId.forString("building3V1");
		HRPlusNode nodeA1 = new HRPlusNode(id1_v1, a1,versionId1);
		HRPlusNode nodeB1 = new HRPlusNode(id2_v1, b1,versionId1);
		HRPlusNode nodeC1 = new HRPlusNode(id3_v1, c1,versionId1);

		// Feature Nodes in Version 2
		Envelope a2 = new Envelope(-14, -12, -2, 2);
		Envelope b2 = new Envelope(-10, -8, -2, 2);
		Envelope c2 = new Envelope(-6, -4, -2, 2);
		ObjectId id1_v2    = ObjectId.forString("building1V2");
		ObjectId id2_v2    = ObjectId.forString("building2V2");
		ObjectId id3_v2    = ObjectId.forString("building3V2");
		HRPlusNode nodeA2 = new HRPlusNode(id1_v2, a2,versionId2);
		HRPlusNode nodeB2 = new HRPlusNode(id2_v2, b2,versionId2);
		HRPlusNode nodeC2 = new HRPlusNode(id3_v2, c2,versionId2);

		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(nodeA1); nodes.add(nodeB1);nodes.add(nodeC1);
		nodes.add(nodeA2); nodes.add(nodeB2);nodes.add(nodeC2);

		tree.insert(id1_v1, a1,versionId1);
		tree.insert(id2_v1, b1,versionId1);
		tree.insert(id3_v1, c1,versionId1);
		tree.insert(id1_v2, a2,versionId2);
		tree.insert(id2_v2, b2,versionId2);
		tree.insert(id3_v2, c2,versionId2);

		// Test: Check if tree has all nodes that are inserted.
		List<HRPlusNode> result = tree.getNodes();
		assertEquals(nodes.size(), result.size());
		assertTrue(result.containsAll(nodes));

		// Test: Check if no of entry points = no of versions of the feature
		// type(no of commits)
		assertEquals(2, tree.getNumRoots());

		// Test: Check if nodes have been put into correct version
		List<HRPlusNode> result1 = tree.getNodes(versionId1);
		assertTrue(tree.getNodes(versionId1).containsAll(nodes.subList(0, 3)));
		assertTrue(tree.getNodes(versionId2).containsAll(nodes.subList(3, 6)));
	}

	
	@Test
	public void insertNodesSameVersion() {
		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId1 = ObjectId.forString("Version1");
		
		Envelope a = new Envelope(-12, -10, -2, 2);
		Envelope b = new Envelope(-8, -6, -2, 2);
		Envelope c = new Envelope(-4, -2, -2, 2);
		
		ObjectId id1_v1    = ObjectId.forString("building1V1");
		ObjectId id2_v1    = ObjectId.forString("building2V1");
		ObjectId id3_v1    = ObjectId.forString("building3V1");
		HRPlusNode nodeA = new HRPlusNode(id1_v1, a,versionId1);
		HRPlusNode nodeB = new HRPlusNode(id2_v1, b,versionId1);
		HRPlusNode nodeC = new HRPlusNode(id3_v1, c,versionId1);

		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(nodeA);nodes.add(nodeB);nodes.add(nodeC);

		tree.insert(id1_v1, a,versionId1);
		tree.insert(id2_v1, b,versionId1);
		tree.insert(id3_v1, c,versionId1);

		// Test: Check if tree has all nodes that are inserted.
		List<HRPlusNode> result = tree.getNodes();
		assertEquals(nodes.size(), result.size());
		assertTrue(result.containsAll(nodes));

		// Test: Check if no of entry points = no of versions of the feature
		// type(no of commits)
		assertEquals(1, tree.getNumRoots());
	}
	
	
	@Test
	public void testOverflow(){
		//Idea - Keep adding nodes belonging to the same version which are very close to each other.
		
		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId1 = ObjectId.forString("Version1");
		ObjectId id1    	= ObjectId.forString("building1");
		ObjectId id2    	= ObjectId.forString("building2");
		ObjectId id3	    = ObjectId.forString("building3");
		ObjectId id4	    = ObjectId.forString("building4");
		Envelope a = new Envelope(-12, -10, -2, 2);
		Envelope b = new Envelope(-11, -9, -2, 2);
		Envelope c = new Envelope(-10, -8, -2, 2);
		Envelope d = new Envelope(-9, -7, -2, 2);
		
		HRPlusNode nodeA = new HRPlusNode(versionId1, a, id1);
		HRPlusNode nodeB = new HRPlusNode(versionId1, b, id2);
		HRPlusNode nodeC = new HRPlusNode(versionId1, c, id3);
		HRPlusNode nodeD = new HRPlusNode(versionId1, d, id4);
		
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(nodeA);nodes.add(nodeB);nodes.add(nodeC);nodes.add(nodeD);
		
		tree.insert(id1, a,versionId1);
		tree.insert(id2, b,versionId1);
		tree.insert(id3, c,versionId1);
		tree.insert(id4, d,versionId1);
		
		assertEquals(2,tree.getContainersForRoot(versionId1).size());
		
		
	}

	@Test
	public void testQueries() {
		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId1 = ObjectId.forString("Version1");
		
		Envelope a = new Envelope(-12, -10, -2, 2);
		Envelope b = new Envelope(-8, -6, -2, 2);
		Envelope c = new Envelope(-4, -2, -2, 2);

		ObjectId id1_v1    = ObjectId.forString("building1V1");
		ObjectId id2_v1    = ObjectId.forString("building2V1");
		ObjectId id3_v1    = ObjectId.forString("building3V1");
		HRPlusNode nodeA = new HRPlusNode(id1_v1, a,versionId1);
		HRPlusNode nodeB = new HRPlusNode(id2_v1, b,versionId1);
		HRPlusNode nodeC = new HRPlusNode(id3_v1, c,versionId1);
		
		tree.insert(id1_v1, a,versionId1);
		tree.insert(id2_v1, b,versionId1);
		tree.insert(id3_v1, c,versionId1);

		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(nodeA);nodes.add(nodeB);nodes.add(nodeC);

		
		List<HRPlusNode> result = tree.query(new Envelope(-50, 50, -50, 50));
		assertTrue(result.containsAll(nodes));
		assertEquals(nodeA, tree.query(a).get(0));
		assertEquals(nodeB, tree.query(b).get(0));
		assertEquals(nodeC, tree.query(c).get(0));
		
	}
	
	  
    @Test(expected=NullPointerException.class)
    public void testKeySplitContainerNodeNull(){
        HRPlusTree hr = new HRPlusTree();
        hr.keySplitContainerNode(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testKeySplitContainerNodeTooFewNodes(){
        HRPlusNode nodeA = new HRPlusNode(new ObjectId(), new Envelope(0,1,0,1), new ObjectId());
        HRPlusContainerNode cont = new HRPlusContainerNode(new ObjectId());
        cont.addNode(nodeA);

        HRPlusTree hr = new HRPlusTree();
        hr.keySplitContainerNode(cont);
    }
        
    @Test(expected=IllegalArgumentException.class)
    public void testKeySplitContainerNodeTooManyNodes(){
        HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"), new Envelope(0,1,0,1), new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"), new Envelope(0,1,0,1), new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"), new Envelope(0,1,0,1), new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"), new Envelope(0,1,0,1), new ObjectId());
        HRPlusNode nodeE = new HRPlusNode(ObjectId.forString("E"), new Envelope(0,1,0,1), new ObjectId());

        HRPlusContainerNode cont = new HRPlusContainerNode(new ObjectId());
        cont.addNode(nodeA); cont.addNode(nodeB); cont.addNode(nodeC); 
        cont.addNode(nodeD); cont.addNode(nodeE); 

        HRPlusTree hr = new HRPlusTree();
        hr.keySplitContainerNode(cont);
    }
    
    @Test
    public void testKeySplitContainerNodeNonIntersectingSquare(){
        HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"), new Envelope(1,2,1,2), new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"), new Envelope(-1,-2,1,2), new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"), new Envelope(-1,-2,-1,-2), new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"), new Envelope(1,2,-1,-2), new ObjectId());

        HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
        contA.addNode(nodeA); contA.addNode(nodeB);
        contA.addNode(nodeC); contA.addNode(nodeD);
        
        HRPlusTree hr = new HRPlusTree();
        HRPlusContainerNode contB = hr.keySplitContainerNode(contA);
        
        assertEquals(2, contA.getNumNodes());
        assertEquals(2, contB.getNumNodes());
        assertEquals(new Envelope(1,2,-2,2), contA.getMBR());
        assertEquals(new Envelope(-1,-2,-2,2), contB.getMBR());
    }
    
    @Test
    public void testKeySplitContainerNodeNonIntersectingLine(){
        HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"), new Envelope(1,2,1,2), new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"), new Envelope(3,4,1,2), new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"), new Envelope(5,6,1,2), new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"), new Envelope(7,8,1,2), new ObjectId());

        HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
        contA.addNode(nodeA); contA.addNode(nodeB);
        contA.addNode(nodeC); contA.addNode(nodeD);
        
        HRPlusTree hr = new HRPlusTree();
        HRPlusContainerNode contB = hr.keySplitContainerNode(contA);
        
        assertEquals(3, contA.getNumNodes());
        assertEquals(1, contB.getNumNodes());
        assertEquals(new Envelope(3,8,1,2), contA.getMBR());
        assertEquals(new Envelope(1,2,1,2), contB.getMBR());
    }
    
    @Test
    public void testKeySplitContainerNodeFullyOverlappingSquare(){
        HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"), new Envelope(1,2,1,2), new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"), new Envelope(1,2,1,2), new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"), new Envelope(1,2,1,2), new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"), new Envelope(1,2,1,2), new ObjectId());

        HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
        contA.addNode(nodeA); contA.addNode(nodeB);
        contA.addNode(nodeC); contA.addNode(nodeD);
        
        HRPlusTree hr = new HRPlusTree();
        HRPlusContainerNode contB = hr.keySplitContainerNode(contA);
        
        assertEquals(3, contA.getNumNodes());
        assertEquals(1, contB.getNumNodes());
        assertEquals(new Envelope(1,2,1,2), contA.getMBR());
        assertEquals(new Envelope(1,2,1,2), contB.getMBR());
    }

    @Test
    public void testKeySplitContainerNodeParallelOverlapOne(){
        HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"), new Envelope(-10,-4,-10,10), new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"), new Envelope(-3,3,-10,10), new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"), new Envelope(4,10,-10,10), new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"), new Envelope(-11,-5,8,10), new ObjectId());

        HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
        contA.addNode(nodeA); contA.addNode(nodeB);
        contA.addNode(nodeC); contA.addNode(nodeD);
        
        HRPlusTree hr = new HRPlusTree();
        HRPlusContainerNode contB = hr.keySplitContainerNode(contA);
        
        assertEquals(2, contA.getNumNodes());
        assertEquals(2, contB.getNumNodes());
        assertEquals(new Envelope(-3,10,-10,10), contA.getMBR());
        assertEquals(new Envelope(-11,-4,-10,10), contB.getMBR());
    }

    @Test
    public void testKeySplitContainerNodeParallelOverlapTwo(){
        HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"), new Envelope(-10,-4,-10,10), new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"), new Envelope(-3,3,-10,10), new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"), new Envelope(4,10,-10,10), new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"), new Envelope(-11,4,8,10), new ObjectId());

        HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
        contA.addNode(nodeA); contA.addNode(nodeB);
        contA.addNode(nodeC); contA.addNode(nodeD);
        
        HRPlusTree hr = new HRPlusTree();
        HRPlusContainerNode contB = hr.keySplitContainerNode(contA);
        
        assertEquals(1, contA.getNumNodes());
        assertEquals(3, contB.getNumNodes());
        assertEquals(new Envelope(4,10,-10,10), contA.getMBR());
        assertEquals(new Envelope(-11,4,-10,10), contB.getMBR());
    }

    @Test
    public void testKeySplitContainerNodeParallelOverlapThree(){
        HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"), new Envelope(-10,-4,-10,10), new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"), new Envelope(-3,3,-10,10), new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"), new Envelope(4,10,-10,10), new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"), new Envelope(-11,11,8,10), new ObjectId());

        HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
        contA.addNode(nodeA); contA.addNode(nodeB);
        contA.addNode(nodeC); contA.addNode(nodeD);
        
        HRPlusTree hr = new HRPlusTree();
        HRPlusContainerNode contB = hr.keySplitContainerNode(contA);
        
        assertEquals(3, contA.getNumNodes());
        assertEquals(1, contB.getNumNodes());
        assertEquals(new Envelope(-10,10,-10,10), contA.getMBR());
        assertEquals(new Envelope(-11,11,8,10), contB.getMBR());
    }
    
    @Test
    public void testKeySplitContainerNodePairsOfIntersectingSquares(){
        HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"), new Envelope(-10,-8,6,8), new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"), new Envelope(-9,-7,5,7), new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"), new Envelope(10,8,-6,-8), new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"), new Envelope(9,7,-5,-7), new ObjectId());

        HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
        contA.addNode(nodeA); contA.addNode(nodeB);
        contA.addNode(nodeC); contA.addNode(nodeD);
        
        HRPlusTree hr = new HRPlusTree();
        HRPlusContainerNode contB = hr.keySplitContainerNode(contA);
        
        assertEquals(2, contA.getNumNodes());
        assertEquals(2, contB.getNumNodes());
        assertEquals(new Envelope(7,10,-5,-8), contA.getMBR());
        assertEquals(new Envelope(-10,-7,5,8), contB.getMBR());
    }
  

	
	

}
