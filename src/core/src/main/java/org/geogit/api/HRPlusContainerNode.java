/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

/**
 * The core building block of HR+ Trees. 
 * A container node surrounds data envelopes, representing them by their minimum bounding rectangle (MBR).
 * Nodes within a container should be close, spatially, and every sub-container of this container should have an MBR contained within this MBR.
 * <p>
 * We use containers rather than pure nodes as the basic unit within an HR+ Tree to track overflow.
 * Ideally, data will be spread evenly throughout the data structure; containers ensure this property.
 * If, during insert, we add too many nodes to one container, the container is split into two and its nodes repartitioned.
 * The method {@link HRPlusTree#insert} performs the check and repartitioning.
 * 
 * <h4>Notes</h4>
 * <ul>
 * <li>Need to instantiate object id. Currently, it's null.
 * <ul>
 */
public class HRPlusContainerNode implements RevObject {

	/**
	 *  Map of nodes inhabiting this container.
	 *  Keys are the unique {@code ObjectId} for the nodes.
	 */
	private Map<ObjectId, HRPlusNode> nodeMap = new HashMap<ObjectId, HRPlusNode>();

	/**
	 * Unique id describing this container.
	 * TODO never set
	 */
	private ObjectId objectId;

	/**
	 * Unique id describing the parent of this.
	 * Will be null if the node is a root.
	 */
	private ObjectId parentId;

        public HRPlusContainerNode() {
                // TODO: Set objectid
        }

        public HRPlusContainerNode(ObjectId parentId) {
                super();
                this.parentId = parentId;
        }

        /**
         * Gets the objectId of this container.
         * @return this.objectId  not null
         */
	public ObjectId getObjectId() {
		return this.objectId;
	}

	/**
	 * Gets the parentId of this container.
	 * @return this.parentId  may be null
	 */
	public ObjectId getParentId() {
		return this.parentId;
	}

	/**
	 * Counts the number of nodes in this container.
	 * Does not count nodes within sub-containers.
	 * @return the number of direct child nodes of this container
	 */
	public int getNumNodes() {
		return this.nodeMap.size();
	}

	/**
	 * Adds a node to this container.
	 * Simply inserts the node into {@code nodeMap}.
	 * If the objectId of {@code node} is identical to an id existing in {@code nodeMap}, erase the existing node.
	 * 
	 * @param node  the node to insert
	 */
	public void addNode(HRPlusNode node) {
		nodeMap.put(node.getObjectId(), node);
		node.setParentContainerId(this.objectId);
	}

	/**
	 * Removes a node from this container.
	 * Specifically, removes the node whose object id matches {@code objectId}
	 * 
	 * @param objectId
	 * @return the node just removed, null if nothing in {@code nodeMap} matched {@code objectId}
	 */
	public HRPlusNode removeNode(ObjectId objectId) {
		return nodeMap.remove(objectId);
	}

	/**
	 * Create a list of the nodes stored in this container.
	 * 
	 * @return a list containing the direct children of this container
	 */
	public List<HRPlusNode> getNodes() {
		return new ArrayList<HRPlusNode>(this.nodeMap.values());
	}

	/**
	 * Determine whether this container is a leaf. 
	 * A leaf has no sub-containers (a leaf may contain nodes).
	 * <p>
	 * An HR+ tree is balanced by construction, so if one node within this container has no children, none of the nodes within this container do.
	 * 
	 * @return boolean indicating whether this container is a leaf
	 */
	public boolean isLeaf() {
		return this.nodeMap.isEmpty() || this.getNodes().get(0).isLeaf();
	}

	/**
	 * Check if the container does not have any nodes.
	 * 
	 * @return boolean indicating whether this container has any nodes inside.
	 */
	public boolean isEmpty() {
		return (this.nodeMap.isEmpty());
	}

	/**
	 * Check whether any children of this node are leaves.
	 * Used in {@link HRPlusTree#chooseSubtree}.
	 * 
	 * @return boolean indicating whether any sub-container of this container have leaves.
	 */
	public boolean isOneStepAboveLeafLevel() {
		if (this.isLeaf()) {
			return false;
		}
		// Not a leaf, so getNodes will return a non-empty list.
		HRPlusContainerNode nextLevel = this.getNodes().get(0).getChild();
		return nextLevel.isLeaf();
	}
	
	/**
	 * Gets the version ids of each node within this container.
	 * 
	 * @return list of version ids, one for each node in this container
	 */
	public List<ObjectId> getVersionIds(){
	        List<ObjectId> ids = new ArrayList<ObjectId>();
	        for (HRPlusNode node : this.getNodes()){
	                ids.add(node.getVersionId());
	        }
	        return ids;
	}

	/**
	 * Compute the minimum bounding rectangle for nodes in this container.
	 * MBR is empty if the container is empty.
	 * MBR implicitly covers all nodes in sub-containers.
	 * That is, these nodes are not checked to determine the MBR in this function, but we assume that the envelope for a node contains all envelopes of nodes under it.
	 * 
	 * @return minimum bounding rectangle surrounding contained nodes.
	 */
	public Envelope getMBR() {
		Envelope env = new Envelope();
		for (HRPlusNode node : nodeMap.values()) {
			node.expand(env);
		}
		return env;
	}

	/**
	 * Calculates overlap between this container's MBR and the argument envelope.
	 * 
	 * @param env  Envelope to compare to this container's MBR.
	 * @return the envelope obtained by intersecting @param env with this container's MBR
	 */
	public Envelope getOverlap(Envelope env) {
		return this.getMBR().intersection(env);
	}

	/**
	 * Search this container for nodes within the argument envelope and recurse into their containers.
	 * 
	 * @param env
	 * @param matches list of nodes across the entire tree that fit in this envelope
	 */
	public void query(Envelope env, List<HRPlusNode> matches) {
		if (this.getMBR().intersects(env)) {
			for (HRPlusNode n : this.getNodes()) {
				n.query(env, matches);
			}
		}
		return;
	}

	/**
	 * Return all the nodes in this container and all the nodes in the child containers.
	 * 
	 * @return all the nodes at and below this container
	 */
	public List<HRPlusNode> getNodesForContainer() {
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		// add all the nodes in this container
		nodes.addAll(this.getNodes());

		if (this.isLeaf()) // if its a leaf we are done, otherwise recurse
			return nodes;
		else {
			for (HRPlusNode node : this.getNodes()) {
				// get the nodes for child container this node points to
				nodes.addAll(node.getChild().getNodesForContainer());
			}
		}
		return nodes;
	}

	@Override
	public TYPE getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectId getId() {
		// TODO Auto-generated method stub
		return null;
	}

}
