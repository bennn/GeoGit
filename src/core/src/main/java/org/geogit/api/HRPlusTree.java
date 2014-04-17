package org.geogit.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geogit.storage.ObjectDatabase;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Implementation of the HRPlus tree described in 
 *     [1] http://www.cs.ust.hk/faculty/dimitris/PAPERS/ssdbm01.pdf
 * We chose the HRPlus tree for the spatial index because it provides
 * - Provides access to past versions. Keeps a history.
 * - Provides multiple roots. Multiple entry points.
 * - Trees share branches when data doesn't change
 * - Space-efficient
 * - Improved query performance over regular R-Tree or Historical R-Tree
 * @author jillian
 */
public class HRPlusTree extends HRPlusTreeUtils {
    
    // Connection to geogit database
    private ObjectDatabase db;
    // Id for this tree
    private ObjectId objectId;
    // Map of Lists of roots. The lists contain Nodes with the same objectId,
    // and are possibly added to during inserts.
    private Map<ObjectId, List<HRPlusContainerNode>> rootMap = new HashMap<ObjectId, List<HRPlusContainerNode>>();
    
    /**
     * Insertion algorithm, roughly:
     * - Create an HRPlus node from @param layerId and @param bounds
     * - Find the correct container node to insert into. Insert node into container.
     * - Check for degree overflow, if so, rebalance the tree.
     * - Re-organize tree among roots, add any new roots to the @field rootMap.
     * @param layerId
     * @param bounds
     */
    public void insert(final ObjectId layerId, Envelope bounds){
        // Create node from params
        HRPlusNode newNode = new HRPlusNode(layerId, bounds);
        // Find appropriate container to insert into
        HRPlusContainerNode containerNode = chooseSubtree(newNode);
        
        if(containerNode == null)
        {
        	//adding a new container node to the tree
            containerNode = new HRPlusContainerNode();
            containerNode.addNode(newNode);
            this.addRootTableEntry(containerNode);
            return;
        }
        // Perform insert
        containerNode.addNode(newNode);
        // Check if we have a degree overflow. Did adding the new node increase the degree of its
        // parent beyond @field DEGREE ?
        List<HRPlusContainerNode> newContainerNodes = null;
        if(containerNode.getNumNodes() >= this.getMaxDegree()){
            // Shoot, we have overflow. Split the old container.
            newContainerNodes = treatOverflow(containerNode, layerId);
        }
        // Balance the tree among roots. 
        List<HRPlusContainerNode> newRoots = adjustTree(containerNode, newContainerNodes, layerId);
        // Add each new root to the table of entry points
        for(HRPlusContainerNode newRoot : newRoots){
            this.addRootTableEntry(newRoot);
        }
    }
    
    /**
     * Bounding box query.
     * Search the tree for nodes within the given envelope.
     * 
     * Start at the roots, search all those containers. In each container, search 
     * the nodes contained within and their respective containers.
     * 
     * @param env
     * @return
     */
    public List<HRPlusNode> query(Envelope env) {
        // HRPlusNode has a getBounds and a getChild (returns container)
        // Containers have a getNodes and a getMBR (expensive)
        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        // Search all container nodes in @field rootMap
        for (List<HRPlusContainerNode> roots : this.rootMap.values()) {
            for (HRPlusContainerNode root : roots) {
                root.query(env, matches);
            }
        }
        return matches;
    }

    /**
     * Add a new root to the overall table of entry points.
     * Either add to an existing entry or create a new one.
     * @param newRoot
     */
    private void addRootTableEntry(HRPlusContainerNode newRoot){
        // Temp value: the existing list of roots corresponding to the current  
        List<HRPlusContainerNode> roots = null;
        // Iterate over layers contained in the param
        for(ObjectId layerId : newRoot.getLayerIds()){
            // Find the existing entry for this layer id
            roots = this.rootMap.get(layerId);
            if(roots == null){
                // No existing entry. Make a new one.
                roots = new ArrayList<HRPlusContainerNode>();
            }
            // Add this new root, update the @field rootMap
            roots.add(newRoot);
            this.rootMap.put(layerId, roots);
        }
        
    }

    /**
     * Distribute the children of one container into two containers.
     * 
     * @param containerNode
     * @param layerId
     * @return
     */
    private List<HRPlusContainerNode> treatOverflow(HRPlusContainerNode containerNode, 
            ObjectId layerId) {
        // 
        List<HRPlusContainerNode> newContainerNodes = new ArrayList<HRPlusContainerNode>();
        if(containerNode.allNodesContainLayerId(layerId)){
            // If all nodes contain the same layerId, split containerNode spatially 
            HRPlusContainerNode newContainerNode = keySplitContainerNode(containerNode);
            newContainerNodes.add(newContainerNode);
        } else {
            // Else, create a new container, move all nodes associated with @param layerId
            // to the new container.
            HRPlusContainerNode newContainerNode = new HRPlusContainerNode();
            List<HRPlusNode> nodesForLayer = containerNode.getNodesForLayer(layerId);
            HRPlusNode transferNode;
            for(HRPlusNode node : nodesForLayer){
                transferNode = containerNode.removeNode(node.getObjectId());
                newContainerNode.addNode(transferNode);
            }
            // If new container is over-full, split it spatially. This is legal because
            // all nodes in the new container belong to the same layer id.
            if(newContainerNode.getNumNodes() > this.getMaxDegree()){
                HRPlusContainerNode secondNewContainerNode = keySplitContainerNode(newContainerNode);
                newContainerNodes.add(secondNewContainerNode);
            }
            newContainerNodes.add(newContainerNode);
        }
        return newContainerNodes;
    }

    /**
     * Divide one container into two by minimizing the perimeter/margin and overlap
     * of subsets of nodes contained within it.
     * 
     * @param containerNode
     * @return
     */
    private HRPlusContainerNode keySplitContainerNode(HRPlusContainerNode containerNode){
        // Uses R* splitting algorithm
        List<HRPlusNode> minXSort = minXSort(containerNode.getNodes());
        List<HRPlusNode> maxXSort = maxXSort(containerNode.getNodes());
        List<HRPlusNode> minYSort = minYSort(containerNode.getNodes());
        List<HRPlusNode> maxYSort = maxYSort(containerNode.getNodes());
        // Get total perimeters
        double xMarginSum = sumOfMargins(minXSort) + sumOfMargins(maxXSort);
        double yMarginSum = sumOfMargins(minYSort) + sumOfMargins(maxYSort);
        // partition is a subset of nodes inside the container. A spatially-close subset.
        List<HRPlusNode> partition;
        // choose the split axis based on the min margin sum (aka smallest perimeter)
        // after choosing axis, choose distribution with the minimum overlap value
        if(xMarginSum <= yMarginSum){
            partition = partitionByMinOverlap(minXSort, maxXSort);
        } else {
            partition = partitionByMinOverlap(minYSort, maxYSort);
        }
        // Create new container, move each node in partition from old container to new one.
        HRPlusContainerNode newContainerNode = new HRPlusContainerNode();
        HRPlusNode transferNode;
        for(HRPlusNode node : partition){
            transferNode = containerNode.removeNode(node.getObjectId());
            newContainerNode.addNode(transferNode);
        }
        return newContainerNode;
    }

    /**
     * Determine whether @param containerNode is a root.
     * 
     * @param containerNode
     * @return true if @param containerNode is contained in @field rootMap
     */
    private boolean isRoot(HRPlusContainerNode containerNode){
        if(containerNode == null || this.rootMap == null){
            // Edge case: the container is empty.
            return false;
        }
        return this.rootMap.values().contains(containerNode.getObjectId());
    }

    /**
     * Re-distribute a tree's nodes among roots. This may happen after a regular 
     * insert or after an insert where an old container node was split.
     * @param containerNode node we begin normalizing at
     * @param siblingContainerNodes siblings of @param containerNode
     * @param layerId
     * @return
     */
    private List<HRPlusContainerNode> adjustTree(HRPlusContainerNode containerNode, 
            List<HRPlusContainerNode> siblingContainerNodes, ObjectId layerId){
        // Loop variables. Parents of current node.
        HRPlusNode parent;
        HRPlusContainerNode parentContainer;
        Envelope containerMBR;
        // Loop until we hit a root.
        while(!this.isRoot(containerNode)){
            // get info about containerNode
            parent = lookupHRPlusNode(containerNode.getParentId());
            parentContainer = lookupHRPlusContainerNode(parent.getParentContainerId());
            containerMBR = containerNode.getMBR();
            parent.setBounds(containerMBR);
            // Siblings might be propagated upwards.
            for(HRPlusContainerNode siblingContainerNode : siblingContainerNodes){
                HRPlusNode newNode = new HRPlusNode(siblingContainerNode.getLayerIds(), siblingContainerNode.getMBR());
                newNode.setChild(siblingContainerNode);
                parentContainer.addNode(newNode);				
            }
            // We may have to split. Create a list in case.
            List<HRPlusContainerNode> newContainerNodes = null;
            if(parentContainer.getNumNodes() > this.getMaxDegree()){
                // A split!
                newContainerNodes = treatOverflow(parentContainer, layerId);
            }
            // Update loop vars for next iteration
            containerNode = parentContainer;
            siblingContainerNodes = newContainerNodes;
        }
        return siblingContainerNodes;
    }

    /**
     * @param objectId
     * @return the container associated with @param objectId
     */
    public HRPlusContainerNode lookupHRPlusContainerNode(ObjectId objectId) {
        // TODO unguarded cast
        return (HRPlusContainerNode) this.db.get(objectId);
    }

    public HRPlusNode lookupHRPlusNode(ObjectId objectId){
        // TODO unguarded cast
        return (HRPlusNode) this.db.get(objectId);
    }
    
    /**
     * @param layerId
     * @return entry points associated with @param layerId
     */
    private List<HRPlusContainerNode> getRootsForLayerId(ObjectId layerId) {
        return rootMap.get(layerId);
    }

    /**
     * Choose the subtree to insert the new node at.
     * Traverse the tree, search for
     *  
     * @param newNode
     * @return 
     */
    private HRPlusContainerNode chooseSubtree(final HRPlusNode newNode){
        // First, find an entry point for this node.
        List<HRPlusContainerNode> containerNodes = getRootsForLayerId(newNode.getFirstLayerId()); 
        // TODO: Can we avoid using null?
        if(containerNodes==null){
            // Just return null, since we check for null in insert()
            return null;
        }
        // Gotta search for a place to insert.
        // Choose the container node with the largest intersection area with the new node.
        double maxIntersectionArea = Double.MIN_VALUE;
        double intersectionArea;
        HRPlusContainerNode maxIntersectionContainerNode = containerNodes.get(0);
        for(HRPlusContainerNode containerNode: containerNodes){
            intersectionArea = containerNode.getMBR().intersection(newNode.getBounds()).getArea();
            if (intersectionArea > maxIntersectionArea) {
                maxIntersectionArea = intersectionArea;
                maxIntersectionContainerNode = containerNode;
            }
        }
        // Found a container node to insert into.
        // Now find the exact place for this node within container.
        HRPlusContainerNode containerNode = maxIntersectionContainerNode;
        while (containerNode.isLeaf()) {
            // Find nodes in this layer. We'll pick the spatially-best one
            List<HRPlusNode> nodesForLayer = containerNode.getNodesForLayer(newNode.getFirstLayerId());
            double minEnlargement = Double.MAX_VALUE;
            double enlargement;
            Envelope currentEnvelope = new Envelope();
            Envelope newEnvelope = new Envelope();
            HRPlusNode insertionNode = nodesForLayer.get(0);
            // Check if we're almost at a leaf.  
            if (containerNodes.get(0).isOneStepAboveLeafLevel()) {
                // Almost leaf. Time to insert. 
                // Find the node such that inserting newNode causes the minimum overlap enlargement
                for(HRPlusNode node : nodesForLayer){
                    node.getOverlap(currentEnvelope);
                    newNode.expand(newEnvelope);
                    enlargement = currentEnvelope.intersection(newEnvelope).getArea() - currentEnvelope.getArea();
                    if(enlargement < minEnlargement){
                        insertionNode = node;
                    }	
                }
            } else {
                // Find the node such that inserting newNode causes the minimum area enlargement
                for(HRPlusNode node : nodesForLayer){
                    node.expand(currentEnvelope);
                    node.expand(newEnvelope);
                    newNode.expand(newEnvelope);

                    enlargement = newEnvelope.getArea() - currentEnvelope.getArea();
                    if(enlargement < minEnlargement){
                        insertionNode = node;
                    }	
                }
            }
            insertionNode.addLayerId(newNode.getFirstLayerId());
            containerNode = insertionNode.getChild();
        }
        return containerNode;
    }

}
