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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Implementation of the HRPlus tree described in [1]
 * {@link http://www.cs.ust.hk/faculty/dimitris/PAPERS/ssdbm01.pdf}
 * We chose the HRPlus tree for the spatial index because it provides
 *<p><ul>
 *<li>Partial persistence. Can access to past versions.
 *<li>Multiple roots. (Entry points to past versions)
 *<li>Trees share branches when data doesn't change
 *<li>Space-efficient
 *<li>Improved query performance over regular R-Tree or Historical R-Tree
 *<ul><p>
 * TODO build HR trees for different layers. Currently only have one tree for
 * one layer/feature
 *</p>
 *<p>
 * TODO add data sharing between versions. An important part of HR+ trees
 * is that data for old versions may appear under the root of a new version
 * these nodes are invisible to queries made on the newer version, but exist
 * nonetheless to save space. However this complicates lookups and splits so
 * we implement the more space-inefficient technique of keeping all versions
 * separate first.
 *</p>
 */
public class HRPlusTree extends HRPlusTreeUtils {

        /**
         * Connection to geogit database
         */
	private ObjectDatabase db;
	/**
	 * Unique id for this tree. Required by object database.
	 */
	private ObjectId objectId;

	/**
	 * Map from versionId to tree root list.
	 * The versionId denotes a timestamp, a historical copy of this data structure
	 * The roots are stored in a list because insert can potentially split a root into two.
	 */
	private Map<ObjectId, List<HRPlusContainerNode>> rootMap = 
			new HashMap<ObjectId, List<HRPlusContainerNode>>();
	
	public HRPlusTree() {
	    // TODO properly set these fields
	    this.objectId = null; 
	    this.db = null;
	}
	
	public HRPlusTree(RevTreeImpl revTree, RevFeatureType featureType) {
	    super();
	    // Create an HR+ tree from an existing rev tree
            ObjectId objectId;
            Envelope env;
	    
	    if (revTree.features().isPresent()){
	        for (Node featureNode : revTree.features().get()){
	            // Copy node's objectid, envelope, and versionid
	            objectId = featureNode.getObjectId();
	            // Copy envelope by creating a new one and expanding it
	            env = new Envelope();
	            featureNode.expand(env);
	            // TODO: How do we get the versionId?
	            ObjectId versionId = ObjectId.NULL;
	            
	            this.insert(objectId, env, versionId);
	        }
	    }
	}
	
	/**
	 * Insert a node into this tree.
	 * <p>
	 * Insertion algorithm, roughly:
	 * <ul>
	 * <li>Create an HRPlusNode from {@param layerId} and {@param bounds}
	 * <li>Find the correct container node to insert into (@method chooseSubtree). Insert node into container.
	 * <li>Check for degree overflow, if so, rebalance the tree.
	 * <li>Re-organize tree among roots, add any new roots to the @field rootMap.
	 * <ul>
	 * 
	 * @param objectId  Denotes the object id of the node. Passing in for now, since dont know how to generate.
	 * @param bounds  The data itself. A region of a map.
	 * @param versionId  Timestamp. Associates this node with a particular version of the tree
	 */
	public void insert(final ObjectId objectId, Envelope bounds,
			final ObjectId versionId) {
		// Create node from params
		HRPlusNode newNode = new HRPlusNode(objectId, bounds, versionId);
		// Find appropriate container to insert into
		HRPlusContainerNode containerNode = chooseSubtree(newNode, versionId);
		// Check for edge condition: root map didn't contain this @param versionId
		if (containerNode == null) {
			// adding a new container node to the tree
			containerNode = new HRPlusContainerNode(versionId);
			containerNode.addNode(newNode);
			this.addRootTableEntry(containerNode);
			return;
		}
		// Perform insert
		containerNode.addNode(newNode);
		// Now check if we have a degree overflow.
		HRPlusContainerNode newContainerNode = null;
		if (containerNode.getNumNodes() > this.getMaxDegree()) {
			// Shoot, we have overflow. Split the old container.
			newContainerNode = treatOverflow(containerNode, versionId);
		}
		// Balance the tree among roots.
		HRPlusContainerNode newRoot = adjustTree(containerNode, newContainerNode, versionId);
		// Add new root to the table of entry points
		if(newRoot!=null){
			this.addRootTableEntry(newRoot);
		}
		return;
	}

	/**
	 * Search this tree for nodes contained within a bounding box.
	 * <p>
	 * Recursively search the entire tree for nodes within the given envelope.
         *
	 * @param env  The bounding box we restrict results to.
	 * @return A list of nodes within {@param env}
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
	 * Query, but restrict search to nodes of a particular version id.
	 * <p>
         * Returns null if {@param versionId} does not appear as a key in {@param rootMap}.
         *
         * @param versionId  The version of the tree we wish to search
         * @param env  Bounding box to search
         * @return A list of nodes within {@param env} belonging to the version with id {@param versionId}
	 */
	public List<HRPlusNode> queryHistorical(ObjectId versionId, Envelope env) {
		// Give up if version doesn't exist
		if (!this.hasVersion(versionId)) {
			return null;
		}
		List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
		// Search all container nodes in @field rootMap
		for (HRPlusContainerNode root : this.rootMap.get(versionId)) {
				root.query(env, matches);
		}
		return matches;
	}

	/**
	 * Determines whether @param versionId points to a root in this tree.
	 * 
	 * @param versionId  A timestamp, may match a root of this tree
	 * @return boolean indicating whether there is a subtree associated with @param versionId
	 */
	public boolean hasVersion(ObjectId versionId) {
		return this.rootMap.containsKey(versionId);
	}

	/**
	 * Add a new root to the overall table of entry points. 
	 * Either add to an existing entry or create a new one.
	 * 
	 * @param newRoot  The node to insert into {@field rootTable}
	 */
	private void addRootTableEntry(HRPlusContainerNode newRoot) {
                for (ObjectId versionId : newRoot.getVersionIds()){
                        if (!this.hasVersion(versionId)) {
                	        // Adding a brand new root to tree 
                                List<HRPlusContainerNode> roots = new ArrayList<HRPlusContainerNode>();
                                roots.add(newRoot);
                                this.rootMap.put(versionId, roots);
                        }else{
                                // Adding a new root to an existing set
                                List<HRPlusContainerNode> existing = this.rootMap.get(versionId);
                                existing.add(newRoot);
                        }
                }
	}

	/**
	 * Distribute the children of one container into two containers.
         * <p>
	 * TODO: do a version split. This comes after we share nodes between versions of the tree.
         * For now, the split is only spatial.
         * 
	 * @param containerNode  Node we want to split due to overflow
	 * @param versionId  The version of the tree we're working with. Momentarily unused.
	 * @return the new container node (the old one is modified in keySplit)
	 */
	private HRPlusContainerNode treatOverflow(HRPlusContainerNode containerNode, ObjectId versionId) {
		return keySplitContainerNode(containerNode);
	}

	/**
	 * Spatially divide one container node.
	 * Minimize the perimeter/margin and overlap of subsets of nodes.
         * This heuristic helps eliminate search paths during queries.
         *<p>
         * Algorithm computes and compares the 'goodness' of margin (perimeter) values.
         * See @method sumOfMargins for details.
         *
	 * @param containerNode  The container to split
	 * @return The newly-created container node
	 */
	public HRPlusContainerNode keySplitContainerNode(HRPlusContainerNode containerNode) {
	    int numNodesExpected = this.getMaxDegree()+1;
	        Preconditions.checkArgument(containerNode != null && containerNode.getNumNodes() == numNodesExpected,
	            "keySplitContainerNode must be called on a non-null container with [%d] nodes",
	            numNodesExpected);
		// Uses R* splitting algorithm
		List<HRPlusNode> minXSort = minXSort(containerNode.getNodes());
		List<HRPlusNode> maxXSort = maxXSort(containerNode.getNodes());
		List<HRPlusNode> minYSort = minYSort(containerNode.getNodes());
		List<HRPlusNode> maxYSort = maxYSort(containerNode.getNodes());
		// Get total perimeters
		double xMarginSum = sumOfMargins(minXSort) + sumOfMargins(maxXSort);
		double yMarginSum = sumOfMargins(minYSort) + sumOfMargins(maxYSort);
		// partition is a subset of nodes inside the container. A
		// spatially-close subset.
		List<HRPlusNode> partition;
		// choose the split axis based on the min margin sum (aka smallest perimeter)
		// after choosing axis, choose distribution with the minimum overlap value
		if (xMarginSum <= yMarginSum) {
			partition = partitionByMinOverlap(minXSort, maxXSort);
		} else {
			partition = partitionByMinOverlap(minYSort, maxYSort);
		}
		// Create new container, move each node in partition from old container
		// to new one.
		// New container has same versionId as old one, for now. (Should maybe get a brand new timestamp.)
		HRPlusContainerNode newContainerNode = new HRPlusContainerNode();
		HRPlusNode transferNode;
		for (HRPlusNode node : partition) {
			transferNode = containerNode.removeNode(node.getObjectId());
			newContainerNode.addNode(transferNode);
		}
		return newContainerNode;
	}

	/**
	 * Determine whether {@param containerNode} is a root.
	 * Assumes the container is already part of the tree.
	 * 
	 * @param containerNode  check if is root 
	 * @return true if {@param containerNode} is a root
	 */
	private boolean isRoot(HRPlusContainerNode containerNode) {
		if (containerNode == null || this.rootMap == null) {
			// Edge case: the container is empty.
			return false;
		}
		return this.hasVersion(containerNode.getVersionIds().get(0));
	}

	/**
	 * Re-distribute a tree's nodes among roots.
	 * This may happen after a regular insert or after an insert where an old container node was split.
	 * 
	 * @param containerNode  node we begin normalizing at
	 * @param siblingContainerNodes  siblings of {@param containerNode}
	 * @param versionId  possibly unnecessary, the version of the tree we're working in
	 * @return The newly-adjusted container
	 */
	private HRPlusContainerNode adjustTree(HRPlusContainerNode containerNode,
			HRPlusContainerNode newContainerNode, ObjectId versionId) {
	        Preconditions.checkNotNull(containerNode);
		// Loop variables. Parents of current node.
		HRPlusNode parent;
		HRPlusContainerNode parentContainer;
		Envelope containerMBR;
		// Loop until we hit a root.
		while (!this.isRoot(containerNode)) {
			// get info about containerNode
			parent = lookupHRPlusNode(containerNode.getParentId());
			parentContainer = lookupHRPlusContainerNode(parent.getParentContainerId());
			containerMBR = containerNode.getMBR();
			parent.setBounds(containerMBR);
			// Siblings might be propagated upwards.
			// TODO: why getObjectId()? Because we have nothing better for it yet. Need Gabriel.
			// And then do newContainer.getLayerId?
			HRPlusNode newNode = new HRPlusNode(newContainerNode.getObjectId(),newContainerNode.getMBR(), versionId);
			newNode.setChild(newContainerNode);
			parentContainer.addNode(newNode);
		
			// We may have to split. Create a list in case.
			HRPlusContainerNode secondSplitContainerNode = null;
			if (parentContainer.getNumNodes() > this.getMaxDegree()) {
				// A split!
				secondSplitContainerNode = treatOverflow(parentContainer, versionId);
			}
			// Update loop vars for next iteration
			containerNode = parentContainer;
			newContainerNode = secondSplitContainerNode;
		}
                return newContainerNode;
	}

	/**
	 * Gets the container associated with parameter
	 * 
	 * @param objectId  unique id for a container node
	 * @return the container associated with {@param objectId}
	 */
	public HRPlusContainerNode lookupHRPlusContainerNode(ObjectId objectId) {
		// TODO unguarded cast
		return (HRPlusContainerNode) this.db.get(objectId);
	}

	/**
	 * Gets the node associated with parameter
	 * 
	 * @param objectId
	 * @return the node associated with {@param objectId}
	 */
	public HRPlusNode lookupHRPlusNode(ObjectId objectId) {
		// TODO unguarded cast
		return (HRPlusNode) this.db.get(objectId);
	}

	/**
	 * Gets all roots for {@param layerId}
	 * 
	 * @param layerId  id to find roots for
	 * @return entry points associated with {@param layerId}
	 */
	private List<HRPlusContainerNode> getRootsForLayerId(ObjectId layerId) {
		return rootMap.get(layerId);
	}

	/**
	 * Determines the leaf container node to add the new node to.
         *<p>
         * The algorithm as described in Section 3.1.1 of [1].  
         *<ul>
         *<li>Get container list for root given by the current
	 * version (time stamp) of the node being inserted. We don't care about layer
	 * ids as everything in the tree has the same layer id (feature type id)
         *<li>Find the container with the max overlap with @param newNode
         *<li>If the container is a leaf, return it.
	 *<li>Else, if is one level above leaf then choose sub-container with the max overlap
	 *<li>Else, Pick the sub-container with the minimum area enlargement
	 *</ul>
	 *
	 * @param newNode  node to find a subtree for
	 * @return best place to store the new node
	 */
	private HRPlusContainerNode chooseSubtree(final HRPlusNode newNode,
			ObjectId versionId) {
		// First, find an entry point for this node.
		List<HRPlusContainerNode> containerNodes = getRootsForLayerId(versionId);
		// TODO: Can we avoid using null?
		if (containerNodes == null) {
			// Just return null, since we check for null in insert()
			return null;
		}
		// Gotta search for a place to insert.
		// Choose the container node with the largest intersection area with the new node.
		double maxIntersectionArea = Double.MIN_VALUE;
		double intersectionArea;
		HRPlusContainerNode maxIntersectionContainerNode = containerNodes.get(0);
		for (HRPlusContainerNode containerNode : containerNodes) {
			intersectionArea = containerNode.getMBR().intersection(newNode.getBounds()).getArea();
			if (intersectionArea > maxIntersectionArea) {
				maxIntersectionArea = intersectionArea;
				maxIntersectionContainerNode = containerNode;
			}
		}
		// Found a container node to insert into.
		// Now find the exact place for this node within container.
		HRPlusContainerNode containerNode = maxIntersectionContainerNode;
		// TODO: adding a check for null here. What if containerNode passed here is null
		while (!containerNode.isLeaf()) {
			List<HRPlusNode> nodesForContainer = containerNode.getNodes();
			double minOverlap = Double.MAX_VALUE;
			double newOverlap;
			if (containerNode.isOneStepAboveLeafLevel()) {
				// Find the sub-container with the minimum overlap enlargement
				for (HRPlusNode node : nodesForContainer) {
					newOverlap = node.getBounds().intersection(newNode.getBounds()).getArea();
					if (newOverlap < minOverlap) {
						minOverlap = newOverlap;
						containerNode = node.getChild();
					}
				}
			} else {
				// Find the sub-container with the minimum area enlargement
				double minArea = Double.MAX_VALUE;
				double newArea;
				for (HRPlusNode node : nodesForContainer) {
					Envelope currentEnvelope = new Envelope();
					node.expand(currentEnvelope);
					newNode.expand(currentEnvelope);
					newArea = currentEnvelope.getArea() - node.getBounds().getArea();
					if (newArea < minArea) {
						minArea = newArea;
						containerNode = node.getChild();
					}
				}

			}
		}
		return containerNode;
	}

	/**
	 * Gets all nodes under the param root.
	 * 
	 * @param containerList  containers corresponding to the root we want to scrape  
	 * @return list of nodes associated with a specific root(Version)
	 */
	private List<HRPlusNode> getNodesForRoot(List<HRPlusContainerNode> containerList) {
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		for (HRPlusContainerNode c : containerList) {
			nodes.addAll(c.getNodesForContainer());
		}
		return nodes;
	}

	/**
	 * Gets all nodes within this tree.
	 * 
	 * @return all the nodes in this HRPlusTree
	 */
	public List<HRPlusNode> getNodes() {
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		for (List<HRPlusContainerNode> root : this.rootMap.values()) {
			nodes.addAll(getNodesForRoot(root));
		}
		return nodes;
	}

	/**
	 * Gets the number of roots of this tree
	 * 
	 * @return the size of the root-map of this HRPlusTree
	 */
	public int getNumRoots() {
		int numRoots = 0;
		for (List<HRPlusContainerNode> roots : this.rootMap.values()){
		    numRoots += roots.size();
		}
		return numRoots;
	}

	/**
	 * Gets the container nodes associated with the parameter id.
	 * Used for testing. 
	 * 
	 * @param rootId  key to search {@field rootMap} with
	 * @return containers associated with {@param rootId}, null if {@param rootId} is not in {@field rootMap}
	 */
	public List<HRPlusContainerNode> getContainersForRoot(ObjectId rootId) {
		return this.rootMap.get(rootId);
	}
	
	/**
	 * Gets the nodes associated with the parameter version id.
	 * Used for testing.
	 * 
	 * @param versionId  key to search for nodes with
	 * @return list of nodes belonging to {@param versionId}
	 */
	public List<HRPlusNode> getNodes(ObjectId versionId){
		List<HRPlusNode> result = new ArrayList<HRPlusNode>();
		if(this.rootMap.containsKey(versionId)){
			result = this.getNodesForRoot(this.rootMap.get(versionId));
		}
		return result;
	}

}
