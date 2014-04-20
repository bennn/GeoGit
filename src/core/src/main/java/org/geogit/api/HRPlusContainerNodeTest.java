package org.geogit.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusContainerNodeTest {
    
    @Test
    public void testGetLayerIdsEmptyContainer(){
        // Initialize container, call method
        HRPlusContainerNode node = new HRPlusContainerNode();
        List<ObjectId> emptyList = new ArrayList<ObjectId>();
        
        assertEquals(emptyList, node.getLayerIds());
    }
    
    @Test
    public void testGetLayerIdsOneNode(){
        // Create a node
        HRPlusNode child = new HRPlusNode(new ObjectId(), new Envelope());
        // Create a container, add node to container
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        // Check layer id against expectation
        List<ObjectId> layerIds = node.getLayerIds();
        assertEquals(1, layerIds.size());
        assertEquals(child.getFirstLayerId(), layerIds.get(0));
    }
    
    @Test
    public void testGetLayerIdsUniqueLayerIds(){
        // Create nodes
        HRPlusNode childA = new HRPlusNode(ObjectId.forString("zardoz"), new Envelope());
        HRPlusNode childB = new HRPlusNode(ObjectId.forString("zodraz"), new Envelope());
        // Create container
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(childA); node.addNode(childB);

        List<ObjectId> layerIds = node.getLayerIds();
        // Should have two layer ids
        assertEquals(2, layerIds.size());
        // Layer ids should match the children
        assertTrue(layerIds.contains(childA.getFirstLayerId()));
        assertTrue(layerIds.contains(childB.getFirstLayerId()));
    }
    
    @Test
    public void testGetLayerIdsDuplicateLayerIds(){
        // Create nodes
        HRPlusNode childA = new HRPlusNode(ObjectId.forString("zardoz"), new Envelope());
        HRPlusNode childB = new HRPlusNode(ObjectId.forString("zardoz"), new Envelope());
        // Create container
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(childA); node.addNode(childB);
        // Check layer ids
        List<ObjectId> layerIds = node.getLayerIds();
        // Should have one layer ids because the containers @field nodeMap filters duplicates 
        assertEquals(1, layerIds.size());
        // Layer ids should match children
        assertTrue(layerIds.contains(childA.getFirstLayerId()));
        assertTrue(layerIds.contains(childB.getFirstLayerId()));
   }
    
    
    @Test
    public void testGetLayerIdsHiddenDuplicateLayerIds(){
        // try sneaking duplicates past the container's nodeMap 
        // Create two layer ids
        ObjectId idA = ObjectId.forString("zardoz");
        ObjectId idB = ObjectId.forString("zodraz");
        // Create nodes, second node contains two layer ids
        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        childB.addLayerId(idA);
        // Create container
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(childA); node.addNode(childB);
        // Check layer ids
        List<ObjectId> layerIds = node.getLayerIds();
        // Should have three layer ids because the method doesn't check duplicates
        assertEquals(3, layerIds.size());
        // Layer ids should match children
        assertTrue(layerIds.contains(idA));
        assertTrue(layerIds.contains(idB));
        // idA should appear twice
        layerIds.remove(idA);
        assertTrue(layerIds.contains(idA));
   }
    
    @Test
    public void testGetObjectId(){
        // TODO the source method needs an implementation
        HRPlusContainerNode node = new HRPlusContainerNode();
        assertEquals(null, node.getObjectId());
    }
    
    @Test
    public void testGetParentId(){
        // TODO the source method needs an implementation
        HRPlusContainerNode node = new HRPlusContainerNode();
        assertEquals(null, node.getParentId());
    }

    @Test
    public void testGetNumNodesEmptyContainer(){
        // Create a container
        HRPlusContainerNode node = new HRPlusContainerNode();
        assertEquals(0, node.getNumNodes());
    }

    @Test
    public void testGetNumNodesOneNode(){
        // Create a node
        HRPlusNode child = new HRPlusNode(new ObjectId(), new Envelope());
        // Create a container, add node
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        assertEquals(1, node.getNumNodes());
    }
    
    @Test
    public void testGetNumNodesTwoNodesUniqueObjectIds(){
        // Create unique object ids
        ObjectId idA = ObjectId.forString("zardoz");
        ObjectId idB = ObjectId.forString("zodraz");
        // Create two nodes
        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        // Create a container, add node
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(childA); node.addNode(childB);
        assertEquals(2, node.getNumNodes());
    }
    
    @Test
    public void testGetNumNodesTwoNodesDuplicateObjectIds(){
        // Create unique object ids
        ObjectId idA = ObjectId.forString("zardoz");
        ObjectId idB = ObjectId.forString("zardoz");
        // Create two nodes
        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        // Create a container, add node
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(childA); node.addNode(childB);
        // Should have one id because previous node was overwritten
        assertEquals(1, node.getNumNodes());
    }
    
    @Test
    public void testGetNumNodesManyNodes(){
        // Create many unique object ids
        ObjectId idA = ObjectId.forString("a");
        ObjectId idB = ObjectId.forString("b");
        ObjectId idC = ObjectId.forString("c");
        ObjectId idD = ObjectId.forString("d");
        ObjectId idE = ObjectId.forString("e");
        ObjectId idF = ObjectId.forString("f");
        ObjectId idG = ObjectId.forString("g");
        ObjectId idH = ObjectId.forString("h");
        ObjectId idI = ObjectId.forString("i");
        ObjectId idJ = ObjectId.forString("j");
        // Create nodes
        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        HRPlusNode childC = new HRPlusNode(idC, new Envelope());
        HRPlusNode childD = new HRPlusNode(idD, new Envelope());
        HRPlusNode childE = new HRPlusNode(idE, new Envelope());
        HRPlusNode childF = new HRPlusNode(idF, new Envelope());
        HRPlusNode childG = new HRPlusNode(idG, new Envelope());
        HRPlusNode childH = new HRPlusNode(idH, new Envelope());
        HRPlusNode childI = new HRPlusNode(idI, new Envelope());
        HRPlusNode childJ = new HRPlusNode(idJ, new Envelope());
        // Create a container, add nodes
        HRPlusContainerNode node = new HRPlusContainerNode();
        // A funny thing here is that addNode doesn't ever split the container
        node.addNode(childA); node.addNode(childB);
        node.addNode(childC); node.addNode(childD);
        node.addNode(childE); node.addNode(childF);
        node.addNode(childG); node.addNode(childH);
        node.addNode(childI); node.addNode(childJ);

        assertEquals(10, node.getNumNodes());
    }
    
    @Test
    public void testGetNumNodesManyNodesManyContainers(){
        // Euclid only proved things up to 3. What's good enough for Euclid is good enough for me.
        // Create many unique object ids
        ObjectId idA = ObjectId.forString("a");
        ObjectId idB = ObjectId.forString("b");
        ObjectId idC = ObjectId.forString("c");
        ObjectId idD = ObjectId.forString("d");
        ObjectId idE = ObjectId.forString("e");
        ObjectId idF = ObjectId.forString("f");
        ObjectId idG = ObjectId.forString("g");
        ObjectId idH = ObjectId.forString("h");
        ObjectId idI = ObjectId.forString("i");
        ObjectId idJ = ObjectId.forString("j");
        // Create nodes
        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        HRPlusNode childC = new HRPlusNode(idC, new Envelope());
        HRPlusNode childD = new HRPlusNode(idD, new Envelope());
        HRPlusNode childE = new HRPlusNode(idE, new Envelope());
        HRPlusNode childF = new HRPlusNode(idF, new Envelope());
        HRPlusNode childG = new HRPlusNode(idG, new Envelope());
        HRPlusNode childH = new HRPlusNode(idH, new Envelope());
        HRPlusNode childI = new HRPlusNode(idI, new Envelope());
        HRPlusNode childJ = new HRPlusNode(idJ, new Envelope());
        // Create a container, add nodes
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNodeA = new HRPlusContainerNode();
        HRPlusContainerNode subNodeB1 = new HRPlusContainerNode();
        HRPlusContainerNode subNodeB2 = new HRPlusContainerNode();
        // Create a hierarchy with max depth 3
        node.addNode(childA); node.addNode(childB);
        subNodeA.addNode(childC); subNodeA.addNode(childD);
        subNodeA.addNode(childE);
        subNodeB1.addNode(childF); subNodeB1.addNode(childG);
        subNodeB1.addNode(childH);
        subNodeB1.addNode(childI); subNodeB1.addNode(childJ);
        childA.setChild(subNodeA);
        childB.setChild(subNodeB1);
        childF.setChild(subNodeB2);
        // Method is not recursive, only checks the number of nodes in one container
        // This is because `getNumNodes` is used to check if we should split
        assertEquals(2, node.getNumNodes());
    }
 
    @Test
    public void testAddNode(){
        // Hopefully this works, otherwise the above tests are questionable...
        HRPlusContainerNode node = new HRPlusContainerNode();
        
        assertEquals(0, node.getNumNodes());
        
        HRPlusNode child = new HRPlusNode(new ObjectId(), new Envelope());
        node.addNode(child);
        
        assertEquals(1, node.getNumNodes());
    }
    
    @Test
    public void testRemoveNodeEmptyContainer(){
        HRPlusContainerNode node = new HRPlusContainerNode();
        
        assertEquals(null, node.removeNode(new ObjectId()));
    }
    
    @Test
    public void testRemoveNodeNonEmptyContainer(){
        ObjectId id = ObjectId.forString("objectid");
        HRPlusNode child = new HRPlusNode(id, new Envelope());

        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        assertEquals(child, node.removeNode(id));
    }
    
    
    @Test
    public void testRemoveNodeFromSubContainer(){
        ObjectId idA = ObjectId.forString("objectid");
        ObjectId idB = ObjectId.forString("anotherobjectid");

        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());

        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();

        node.addNode(childA);
        childA.setChild(subNode);
        subNode.addNode(childB);
        // Shouldn't remove anything. Remove doesn't act recursively on containers
        assertEquals(null, node.removeNode(idB));
        // Should remove, just a sanity check
        assertEquals(childA, node.removeNode(idA));
    }
    
    @Test
    public void testGetNodesEmptyContainer(){
        HRPlusContainerNode node = new HRPlusContainerNode();
        // empty list
        List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
        assertEquals(nodes, node.getNodes());
    }

    @Test
    public void testGetNodesNonEmptyContainer(){
        HRPlusNode child = new HRPlusNode(new ObjectId(), new Envelope());
        
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);

        List<HRPlusNode> nodes = node.getNodes();

        assertEquals(1, nodes.size());
        assertTrue(nodes.contains(child));
    }

    @Test
    public void testGetNodesManyNodes(){
        // Create many unique object ids
        ObjectId idA = ObjectId.forString("a");
        ObjectId idB = ObjectId.forString("b");
        ObjectId idC = ObjectId.forString("c");
        ObjectId idD = ObjectId.forString("d");
        ObjectId idE = ObjectId.forString("e");
        ObjectId idF = ObjectId.forString("f");
        ObjectId idG = ObjectId.forString("g");
        ObjectId idH = ObjectId.forString("h");
        ObjectId idI = ObjectId.forString("i");
        ObjectId idJ = ObjectId.forString("j");
        // Create many nodes
        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        HRPlusNode childC = new HRPlusNode(idC, new Envelope());
        HRPlusNode childD = new HRPlusNode(idD, new Envelope());
        HRPlusNode childE = new HRPlusNode(idE, new Envelope());
        HRPlusNode childF = new HRPlusNode(idF, new Envelope());
        HRPlusNode childG = new HRPlusNode(idG, new Envelope());
        HRPlusNode childH = new HRPlusNode(idH, new Envelope());
        HRPlusNode childI = new HRPlusNode(idI, new Envelope());
        HRPlusNode childJ = new HRPlusNode(idJ, new Envelope());
        // Create a container, add node
        HRPlusContainerNode node = new HRPlusContainerNode();

        node.addNode(childA); node.addNode(childB);
        node.addNode(childC); node.addNode(childD);
        node.addNode(childE); node.addNode(childF);
        node.addNode(childG); node.addNode(childH);
        node.addNode(childI); node.addNode(childJ);
        
        List<HRPlusNode> nodes = node.getNodes();
        assertEquals(10, nodes.size());
        List<HRPlusNode> expected = new ArrayList<HRPlusNode>();
        expected.add(childA); expected.add(childB);
        expected.add(childC); expected.add(childD);
        expected.add(childE); expected.add(childF);
        expected.add(childG); expected.add(childH);
        expected.add(childI); expected.add(childJ);
        assertTrue(nodes.containsAll(expected));
    }

    @Test
    public void testGetNodesSubContainer(){
        // Ignores nodes in sub-containers
        ObjectId idA = ObjectId.forString("idA");
        ObjectId idB = ObjectId.forString("idB");

        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(childA);
        subNode.addNode(childB);
        childA.setChild(subNode);
        
        List<HRPlusNode> nodes = node.getNodes();
        assertEquals(1, nodes.size());
        assertTrue(nodes.contains(childA));
    }
    
    @Test
    public void testAllNodesContainLayerIdEmptyContainer(){
        // vacuously true
        HRPlusContainerNode node = new HRPlusContainerNode();
        assertTrue(node.allNodesContainLayerId(new ObjectId()));
    }
    
    @Test
    public void testAllNodesContainLayerIdNonEmptyContainerPass(){
        ObjectId id = ObjectId.forString("zardoz");
        HRPlusNode child = new HRPlusNode(id, new Envelope());
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        assertTrue(node.allNodesContainLayerId(id));
    }

    @Test
    public void testAllNodesContainLayerIdNonEmptyContainerFail(){
        ObjectId badId = ObjectId.forString("stringtohash");

        ObjectId id = ObjectId.forString("zardoz");
        HRPlusNode child = new HRPlusNode(id, new Envelope());
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        assertFalse(node.allNodesContainLayerId(badId));
    }

    @Test 
    public void testAllNodesContainLayerIdManyNodesPass(){
        // Create many unique object ids
        ObjectId idA = ObjectId.forString("a");
        ObjectId idB = ObjectId.forString("b");
        ObjectId idC = ObjectId.forString("c");
        ObjectId idD = ObjectId.forString("d");
        ObjectId idE = ObjectId.forString("e");
        ObjectId idF = ObjectId.forString("f");
        ObjectId idG = ObjectId.forString("g");
        ObjectId idH = ObjectId.forString("h");
        ObjectId idI = ObjectId.forString("i");
        ObjectId idJ = ObjectId.forString("j");
        // Create many nodes
        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        HRPlusNode childC = new HRPlusNode(idC, new Envelope());
        HRPlusNode childD = new HRPlusNode(idD, new Envelope());
        HRPlusNode childE = new HRPlusNode(idE, new Envelope());
        HRPlusNode childF = new HRPlusNode(idF, new Envelope());
        HRPlusNode childG = new HRPlusNode(idG, new Envelope());
        HRPlusNode childH = new HRPlusNode(idH, new Envelope());
        HRPlusNode childI = new HRPlusNode(idI, new Envelope());
        HRPlusNode childJ = new HRPlusNode(idJ, new Envelope());
        // Create a container, add node
        HRPlusContainerNode node = new HRPlusContainerNode();

        node.addNode(childA); node.addNode(childB);
        node.addNode(childC); node.addNode(childD);
        node.addNode(childE); node.addNode(childF);
        node.addNode(childG); node.addNode(childH);
        node.addNode(childI); node.addNode(childJ);
        
        assertFalse(node.allNodesContainLayerId(idA));
        assertFalse(node.allNodesContainLayerId(idB));
        assertFalse(node.allNodesContainLayerId(idC));
        assertFalse(node.allNodesContainLayerId(idD));
        assertFalse(node.allNodesContainLayerId(idE));
        assertFalse(node.allNodesContainLayerId(idF));
        assertFalse(node.allNodesContainLayerId(idG));
        assertFalse(node.allNodesContainLayerId(idH));
        assertFalse(node.allNodesContainLayerId(idI));
        assertFalse(node.allNodesContainLayerId(idJ));
    }
    
    @Test 
    public void testAllNodesContainLayerIdManyNodesFail(){
        // Create many unique object ids
        ObjectId sharedId = ObjectId.forString("zed");
        ObjectId idA = ObjectId.forString("a");
        ObjectId idB = ObjectId.forString("b");
        ObjectId idC = ObjectId.forString("c");
        ObjectId idD = ObjectId.forString("d");
        ObjectId idE = ObjectId.forString("e");
        ObjectId idF = ObjectId.forString("f");
        ObjectId idG = ObjectId.forString("g");
        ObjectId idH = ObjectId.forString("h");
        ObjectId idI = ObjectId.forString("i");
        ObjectId idJ = ObjectId.forString("j");
        // Create many nodes
        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        HRPlusNode childC = new HRPlusNode(idC, new Envelope());
        HRPlusNode childD = new HRPlusNode(idD, new Envelope());
        HRPlusNode childE = new HRPlusNode(idE, new Envelope());
        HRPlusNode childF = new HRPlusNode(idF, new Envelope());
        HRPlusNode childG = new HRPlusNode(idG, new Envelope());
        HRPlusNode childH = new HRPlusNode(idH, new Envelope());
        HRPlusNode childI = new HRPlusNode(idI, new Envelope());
        HRPlusNode childJ = new HRPlusNode(idJ, new Envelope());
        // Add shared id to each node
        childA.addLayerId(sharedId); childB.addLayerId(sharedId);
        childC.addLayerId(sharedId); childD.addLayerId(sharedId);
        childE.addLayerId(sharedId); childF.addLayerId(sharedId);
        childG.addLayerId(sharedId); childH.addLayerId(sharedId);
        childI.addLayerId(sharedId); childJ.addLayerId(sharedId);
        // Create a container, add node
        HRPlusContainerNode node = new HRPlusContainerNode();

        node.addNode(childA); node.addNode(childB);
        node.addNode(childC); node.addNode(childD);
        node.addNode(childE); node.addNode(childF);
        node.addNode(childG); node.addNode(childH);
        node.addNode(childI); node.addNode(childJ);
        
        assertFalse(node.allNodesContainLayerId(idA));
        assertFalse(node.allNodesContainLayerId(idB));
        assertFalse(node.allNodesContainLayerId(idC));
        assertFalse(node.allNodesContainLayerId(idD));
        assertFalse(node.allNodesContainLayerId(idE));
        assertFalse(node.allNodesContainLayerId(idF));
        assertFalse(node.allNodesContainLayerId(idG));
        assertFalse(node.allNodesContainLayerId(idH));
        assertFalse(node.allNodesContainLayerId(idI));
        assertFalse(node.allNodesContainLayerId(idJ));
        assertTrue(node.allNodesContainLayerId(sharedId));
    }
    
    @Test
    public void testAllNodesContainLayerIdSubContainerPass(){
        ObjectId idA = ObjectId.forString("idA");

        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idA, new Envelope());
        
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(childA);
        subNode.addNode(childB);
        childA.setChild(subNode);
        
        assertTrue(node.allNodesContainLayerId(idA));
    }
  
    @Test
    public void testAllNodesContainLayerIdSubContainerFail(){
        ObjectId idA = ObjectId.forString("idA");
        ObjectId idB = ObjectId.forString("idB");

        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(childA);
        subNode.addNode(childB);
        childA.setChild(subNode);
        // The search is not recursive
        assertTrue(node.allNodesContainLayerId(idA));
        assertFalse(node.allNodesContainLayerId(idB));
    }
    
    @Test
    public void testGetNodesForLayerEmptyContainer(){
        HRPlusContainerNode node = new HRPlusContainerNode();
        List<HRPlusNode> emptyList = new ArrayList<HRPlusNode>();
        assertEquals(emptyList, node.getNodesForLayer(new ObjectId()));
    }
    
    @Test
    public void testGetNodesForLayerNonEmptyContainerNoMatch(){
        ObjectId id = ObjectId.forString("zardoz");
        HRPlusNode child = new HRPlusNode(id, new Envelope());
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        List<HRPlusNode> expected = new ArrayList<HRPlusNode>();
        ObjectId badId = ObjectId.forString("asillyid");

        assertEquals(expected, node.getNodesForLayer(badId));
    }    
    
    @Test
    public void testGetNodesForLayerNonEmptyContainerHasMatch(){
        ObjectId id = ObjectId.forString("zardoz");
        HRPlusNode child = new HRPlusNode(id, new Envelope());
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        List<HRPlusNode> expected = new ArrayList<HRPlusNode>();
        expected.add(child);
        
        assertEquals(expected, node.getNodesForLayer(id));
    }
    
    @Test 
    public void testGetNodesForLayerManyNodes(){
        // Create many unique object ids
        ObjectId sharedId = ObjectId.forString("zed");
        ObjectId idA = ObjectId.forString("a");
        ObjectId idB = ObjectId.forString("b");
        ObjectId idC = ObjectId.forString("c");
        ObjectId idD = ObjectId.forString("d");
        ObjectId idE = ObjectId.forString("e");
        ObjectId idF = ObjectId.forString("f");
        ObjectId idG = ObjectId.forString("g");
        ObjectId idH = ObjectId.forString("h");
        ObjectId idI = ObjectId.forString("i");
        ObjectId idJ = ObjectId.forString("j");
        // Create many nodes
        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idB, new Envelope());
        HRPlusNode childC = new HRPlusNode(idC, new Envelope());
        HRPlusNode childD = new HRPlusNode(idD, new Envelope());
        HRPlusNode childE = new HRPlusNode(idE, new Envelope());
        HRPlusNode childF = new HRPlusNode(idF, new Envelope());
        HRPlusNode childG = new HRPlusNode(idG, new Envelope());
        HRPlusNode childH = new HRPlusNode(idH, new Envelope());
        HRPlusNode childI = new HRPlusNode(idI, new Envelope());
        HRPlusNode childJ = new HRPlusNode(idJ, new Envelope());
        // Add shared id to each node
        childA.addLayerId(sharedId); childB.addLayerId(sharedId);
        childC.addLayerId(sharedId); childD.addLayerId(sharedId);
        childE.addLayerId(sharedId); childF.addLayerId(sharedId);
        childG.addLayerId(sharedId); childH.addLayerId(sharedId);
        childI.addLayerId(sharedId); childJ.addLayerId(sharedId);
        // Create a container, add node
        HRPlusContainerNode node = new HRPlusContainerNode();

        node.addNode(childA); node.addNode(childB);
        node.addNode(childC); node.addNode(childD);
        node.addNode(childE); node.addNode(childF);
        node.addNode(childG); node.addNode(childH);
        node.addNode(childI); node.addNode(childJ);
        
        assertEquals(1, node.getNodesForLayer(idA).size());
        assertEquals(1, node.getNodesForLayer(idA).size());
        assertEquals(1, node.getNodesForLayer(idA).size());
        assertEquals(1, node.getNodesForLayer(idA).size());
        assertEquals(1, node.getNodesForLayer(idA).size());
        assertEquals(1, node.getNodesForLayer(idA).size());
        assertEquals(1, node.getNodesForLayer(idA).size());
        assertEquals(1, node.getNodesForLayer(idA).size());
        assertEquals(1, node.getNodesForLayer(idA).size());
        assertEquals(1, node.getNodesForLayer(idA).size());

        assertEquals(10, node.getNodesForLayer(sharedId).size());
    }
    
    @Test
    public void testGetNodesForLayerSubContainer(){
        ObjectId idA = ObjectId.forString("idA");

        HRPlusNode childA = new HRPlusNode(idA, new Envelope());
        HRPlusNode childB = new HRPlusNode(idA, new Envelope());
        
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(childA);
        subNode.addNode(childB);
        childA.setChild(subNode);
        // Does not search recursively
        assertEquals(1, node.getNodesForLayer(idA).size());
    }
    
    @Test
    public void testIsLeafNonEmptyLeafChild(){
        HRPlusNode child = new HRPlusNode(new ObjectId(), new Envelope());
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        assertTrue(node.isLeaf());
    }
    
    @Test
    public void testIsLeafNonEmptyNonLeafChild(){
        HRPlusNode child = new HRPlusNode(new ObjectId(), new Envelope());
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(child);
        child.setChild(subNode);
        
        assertFalse(node.isLeaf());
        assertTrue(subNode.isLeaf());
    }
    
    @Test
    public void testIsLeafEmptyContainer(){
        HRPlusContainerNode node = new HRPlusContainerNode();
        assertTrue(node.isLeaf());
    }
    
    @Test
    public void testIsEmptyPass(){
        HRPlusContainerNode node = new HRPlusContainerNode();
        assertTrue(node.isEmpty());
    }
    
    @Test
    public void testIsEmptyFail(){
        HRPlusNode child = new HRPlusNode(new ObjectId(), new Envelope());
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        assertFalse(node.isEmpty());
    }
    
    @Test
    public void testIsOneStepAboveLeafLevelEmptyContainer(){
        HRPlusContainerNode node = new HRPlusContainerNode();
        assertFalse(node.isOneStepAboveLeafLevel());
    }
    
    @Test
    public void testIsOneStepAboveLeafLevelNonEmptyContainer(){
        HRPlusNode child = new HRPlusNode(new ObjectId(), new Envelope());
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        assertFalse(node.isOneStepAboveLeafLevel());
    }
    
    @Test
    public void testIsOneStepAboveLeafLevelHasEmptySubContainer(){
        HRPlusNode child = new HRPlusNode(new ObjectId(), new Envelope());
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(child);
        child.setChild(subNode);
        
        assertTrue(node.isOneStepAboveLeafLevel());
    }
    
    @Test
    public void testIsOneStepAboveLeafLevelHasNonEmptySubContainer(){
        HRPlusNode childA = new HRPlusNode(new ObjectId(), new Envelope());
        HRPlusNode childB = new HRPlusNode(new ObjectId(), new Envelope());
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(childA);
        childA.setChild(subNode);
        subNode.addNode(childB);
        // Still a leaf provided nodes in sub container are leaves
        assertTrue(node.isOneStepAboveLeafLevel());
    }
    
    @Test
    public void testIsOneStepAboveLeafLevelHasSubSubContainer(){
        HRPlusNode childA = new HRPlusNode(new ObjectId(), new Envelope());
        HRPlusNode childB = new HRPlusNode(new ObjectId(), new Envelope());

        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        HRPlusContainerNode subSubNode = new HRPlusContainerNode();

        node.addNode(childA);
        childA.setChild(subNode);
        subNode.addNode(childB);
        childB.setChild(subSubNode);
        // Sub-sub means nodes in sub container are not leaves
        assertFalse(node.isOneStepAboveLeafLevel());
    }
    
    @Test
    public void testGetMBREmptyContainer(){
        HRPlusContainerNode node = new HRPlusContainerNode();

        assertEquals(new Envelope(), node.getMBR());
    }
    
    @Test
    public void testGetMBRNonEmptyContainer(){
        Envelope env = new Envelope(-10,10,-10,10);
        HRPlusNode childA = new HRPlusNode(new ObjectId(), env);
        HRPlusContainerNode node = new HRPlusContainerNode();
        
        node.addNode(childA);
        
        assertEquals(env, node.getMBR());
    }
    
    @Test
    public void testGetMBRManyNodesDisjointEnvelopes(){
        ObjectId idA = ObjectId.forString("a");
        ObjectId idB = ObjectId.forString("b");
        ObjectId idC = ObjectId.forString("c");
        ObjectId idD = ObjectId.forString("d");
        ObjectId idE = ObjectId.forString("e");
        ObjectId idF = ObjectId.forString("f");
        ObjectId idG = ObjectId.forString("g");
        ObjectId idH = ObjectId.forString("h");
        ObjectId idI = ObjectId.forString("i");
        ObjectId idJ = ObjectId.forString("j");
        // Create many nodes
        HRPlusNode childA = new HRPlusNode(idA, new Envelope(-10,-9,0,1));
        HRPlusNode childB = new HRPlusNode(idB, new Envelope(-9,-8,1,2));
        HRPlusNode childC = new HRPlusNode(idC, new Envelope(-8,-7,2,3));
        HRPlusNode childD = new HRPlusNode(idD, new Envelope(-7,-6,3,4));
        HRPlusNode childE = new HRPlusNode(idE, new Envelope(-6,-5,4,5));
        HRPlusNode childF = new HRPlusNode(idF, new Envelope(-5,-4,5,6));
        HRPlusNode childG = new HRPlusNode(idG, new Envelope(-4,-3,6,7));
        HRPlusNode childH = new HRPlusNode(idH, new Envelope(-3,-2,7,8));
        HRPlusNode childI = new HRPlusNode(idI, new Envelope(-2,-1,8,9));
        HRPlusNode childJ = new HRPlusNode(idJ, new Envelope(-1,0,9,10));
        // Create a container, add nodes
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(childA); node.addNode(childB);
        node.addNode(childC); node.addNode(childD);
        node.addNode(childE); node.addNode(childF);
        node.addNode(childG); node.addNode(childH);
        node.addNode(childI); node.addNode(childJ);
        
        assertEquals(new Envelope(-10,0,0,10), node.getMBR());
    }
    
    @Test
    public void testGetMBRManyNodesOverlapEnvelopes(){
        ObjectId idA = ObjectId.forString("a");
        ObjectId idB = ObjectId.forString("b");
        ObjectId idC = ObjectId.forString("c");
        ObjectId idD = ObjectId.forString("d");
        ObjectId idE = ObjectId.forString("e");
        ObjectId idF = ObjectId.forString("f");
        ObjectId idG = ObjectId.forString("g");
        ObjectId idH = ObjectId.forString("h");
        ObjectId idI = ObjectId.forString("i");
        ObjectId idJ = ObjectId.forString("j");
        // Keep same bounds as overlap test, different envelopes inside
        HRPlusNode childA = new HRPlusNode(idA, new Envelope(-10,0,0,1));
        HRPlusNode childB = new HRPlusNode(idB, new Envelope(-9,-7,0,10));
        HRPlusNode childC = new HRPlusNode(idC, new Envelope(-8,-6,0,10));
        HRPlusNode childD = new HRPlusNode(idD, new Envelope(-7,-5,0,10));
        HRPlusNode childE = new HRPlusNode(idE, new Envelope(-4,-2,0,10));
        HRPlusNode childF = new HRPlusNode(idF, new Envelope(-3,-1,0,10));
        HRPlusNode childG = new HRPlusNode(idG, new Envelope(-2,0,0,10));
        HRPlusNode childH = new HRPlusNode(idH, new Envelope(-5,-2,5,9));
        HRPlusNode childI = new HRPlusNode(idI, new Envelope(-8,-4,3,6));
        HRPlusNode childJ = new HRPlusNode(idJ, new Envelope(-1,0,0,1));
        // Create a container, add nodes
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(childA); node.addNode(childB);
        node.addNode(childC); node.addNode(childD);
        node.addNode(childE); node.addNode(childF);
        node.addNode(childG); node.addNode(childH);
        node.addNode(childI); node.addNode(childJ);
        
        assertEquals(new Envelope(-10,0,0,10), node.getMBR());
    }
    
    @Test
    public void testGetMBRSubContainer(){
        ObjectId idA = ObjectId.forString("idA");

        Envelope envA = new Envelope(5,6,5,6);
        Envelope envB = new Envelope(-5,-6,-5,-6);
        
        HRPlusNode childA = new HRPlusNode(idA, envA);
        HRPlusNode childB = new HRPlusNode(idA, envB);
        
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(childA);
        subNode.addNode(childB);
        childA.setChild(subNode);
        // Sub container is ignored
        assertEquals(envA, node.getMBR());
    }
    
    @Test
    public void testGetOverlapEmptyContainer(){
        HRPlusContainerNode node = new HRPlusContainerNode();
        
        Envelope env1 = new Envelope();
        Envelope env2 = new Envelope(-9000, 9000, -9000, 9000);
        
        assertEquals(env1, node.getOverlap(env1));
        assertEquals(env1, node.getOverlap(env2));
    }    
    
    @Test
    public void testGetOverlapNonEmptyContainerPassNodeOverlapped(){
        Envelope envA = new Envelope(0,1,0,1);
        HRPlusNode child = new HRPlusNode(new ObjectId(), envA);
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        Envelope envB = new Envelope(-9000, 9000, -9000, 9000);
        
        assertEquals(envA, node.getOverlap(envB));
    }      
    
    @Test
    public void testGetOverlapNonEmptyContainerPassEnvOverlapped(){
        Envelope envA = new Envelope(-9000, 9000, -9000, 9000);
        HRPlusNode child = new HRPlusNode(new ObjectId(), envA);
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        Envelope envB = new Envelope(0,1,0,1);
        
        assertEquals(envB, node.getOverlap(envB));
    }
    
    @Test
    public void testGetOverlapNonEmptyContainerFail(){
        Envelope envA = new Envelope(0,1,0,1);
        HRPlusNode child = new HRPlusNode(new ObjectId(), envA);
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        Envelope envC = new Envelope(0,1,-2,-1);
        assertEquals(new Envelope(), node.getOverlap(envC));
    }
    
    public void testGetOverlapSubContainer(){
        ObjectId idA = ObjectId.forString("idA");

        Envelope envA = new Envelope(5,6,5,6);
        Envelope envB = new Envelope(-5,-6,-5,-6);
        
        HRPlusNode childA = new HRPlusNode(idA, envA);
        HRPlusNode childB = new HRPlusNode(idA, envB);
        
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(childA);
        subNode.addNode(childB);
        childA.setChild(subNode);
        // Sub container is ignored
        assertEquals(new Envelope(), node.getOverlap(envB));
        assertEquals(envA, node.getOverlap(new Envelope(-100,100,-100,100)));
    }
    
    @Test
    public void testQueryEmptyContainer(){
        HRPlusContainerNode node = new HRPlusContainerNode();

        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        
        node.query(new Envelope(), matches);
        assertEquals(0, matches.size());
        node.query(new Envelope(-9000,9000,-9000,9000), matches);
        assertEquals(0, matches.size());
    }
    
    @Test
    public void testQueryNonEmptyContainerPassFullOverlap(){
        Envelope envA = new Envelope(-9000, 9000, -9000, 9000);
        HRPlusNode child = new HRPlusNode(new ObjectId(), envA);
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        
        node.query(new Envelope(5,34,-432,-20), matches);
        assertEquals(1, matches.size());
        assertEquals(child, matches.get(0));
    }
    
    @Test
    public void testQueryNonEmptyContainerPassPartialOverlap(){
        Envelope envA = new Envelope(-9000, 0, -9000, 0);
        HRPlusNode child = new HRPlusNode(new ObjectId(), envA);
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        
        node.query(new Envelope(0, 9000, 0, 9000), matches);
        // Still matches even if they intersect only at one point
        assertEquals(1, matches.size());
        assertEquals(child, matches.get(0));
    }
    
    @Test
    public void testQueryNonEmptyContainerFail(){
        Envelope envA = new Envelope(0,1,0,1);
        HRPlusNode child = new HRPlusNode(new ObjectId(), envA);
        HRPlusContainerNode node = new HRPlusContainerNode();
        node.addNode(child);
        
        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        
        node.query(new Envelope(-10,-9,-10,-9), matches);
        assertEquals(0, matches.size());
    }
    
    @Test
    public void testQueryManyNodes(){
        // Create many unique object ids
        ObjectId sharedId = ObjectId.forString("zed");
        ObjectId idA = ObjectId.forString("a");
        ObjectId idB = ObjectId.forString("b");
        ObjectId idC = ObjectId.forString("c");
        ObjectId idD = ObjectId.forString("d");
        ObjectId idE = ObjectId.forString("e");
        ObjectId idF = ObjectId.forString("f");
        ObjectId idG = ObjectId.forString("g");
        ObjectId idH = ObjectId.forString("h");
        ObjectId idI = ObjectId.forString("i");
        ObjectId idJ = ObjectId.forString("j");
        // Create many nodes
        HRPlusNode childA = new HRPlusNode(idA, new Envelope(10, 15, 3,4));
        HRPlusNode childB = new HRPlusNode(idB, new Envelope(100,101,2000,2011));
        HRPlusNode childC = new HRPlusNode(idC, new Envelope(0,1,0,1));
        HRPlusNode childD = new HRPlusNode(idD, new Envelope(30,100,30,31));
        HRPlusNode childE = new HRPlusNode(idE, new Envelope(42,43,5,77));
        HRPlusNode childF = new HRPlusNode(idF, new Envelope(-1,-2,-1,-2));
        HRPlusNode childG = new HRPlusNode(idG, new Envelope(-302,302,-10,-43));
        HRPlusNode childH = new HRPlusNode(idH, new Envelope(-5,-6,-5,-6));
        HRPlusNode childI = new HRPlusNode(idI, new Envelope(-5,-90,-10,10));
        HRPlusNode childJ = new HRPlusNode(idJ, new Envelope(-3,-90,-3,-99));
        // Add shared id to each node
        childA.addLayerId(sharedId); childB.addLayerId(sharedId);
        childC.addLayerId(sharedId); childD.addLayerId(sharedId);
        childE.addLayerId(sharedId); childF.addLayerId(sharedId);
        childG.addLayerId(sharedId); childH.addLayerId(sharedId);
        childI.addLayerId(sharedId); childJ.addLayerId(sharedId);
        // Create a container, add node
        HRPlusContainerNode node = new HRPlusContainerNode();

        node.addNode(childA); node.addNode(childB);
        node.addNode(childC); node.addNode(childD);
        node.addNode(childE); node.addNode(childF);
        node.addNode(childG); node.addNode(childH);
        node.addNode(childI); node.addNode(childJ);
        
        // Nodes in first quadrant will match
        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        node.query(new Envelope(0, 9000,0, 9000), matches);
        assertEquals(5, matches.size());
        assertTrue(matches.contains(childA));
        assertTrue(matches.contains(childB));
        assertTrue(matches.contains(childC));
        assertTrue(matches.contains(childD));
        assertTrue(matches.contains(childE));
    }
    
    @Test
    public void testQuerySubContainerFail(){
        ObjectId idA = ObjectId.forString("idA");

        Envelope envA = new Envelope(5,6,5,6);
        Envelope envB = new Envelope(-5,-6,-5,-6);
            
        HRPlusNode childA = new HRPlusNode(idA, envA);
        HRPlusNode childB = new HRPlusNode(idA, envB);
            
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(childA);
        subNode.addNode(childB);
        childA.setChild(subNode);
        
        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        // Sub container is ignored if container's envelope has no intersect with query
        node.query(envB, matches);
        assertEquals(0, matches.size());
    }
    
    @Test
    public void testQuerySubContainerPass(){
        ObjectId idA = ObjectId.forString("idA");

        Envelope envA = new Envelope(5,6,5,6);
        Envelope envB = new Envelope(-5,-6,-5,-6);
            
        HRPlusNode childA = new HRPlusNode(idA, envA);
        HRPlusNode childB = new HRPlusNode(idA, envB);
            
        HRPlusContainerNode node = new HRPlusContainerNode();
        HRPlusContainerNode subNode = new HRPlusContainerNode();
        node.addNode(childA);
        subNode.addNode(childB);
        childA.setChild(subNode);
        
        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        // Sub container is NOT ignored if node's envelope matches
        node.query(new Envelope(-10,10,-10,10), matches);
        assertEquals(2, matches.size());
        assertTrue(matches.contains(childA));
        assertTrue(matches.contains(childB));
    }
    
    @Test
    public void testGetType(){
        // TODO implement that method!
        HRPlusContainerNode node = new HRPlusContainerNode();
        assertEquals(null, node.getType());
    }

    @Test
    public void testGetId(){
        // TODO implement that method!
        HRPlusContainerNode node = new HRPlusContainerNode();
        assertEquals(null, node.getId());
    }
}
