package org.geogit.api;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.geogit.api.RevObject.TYPE;
import org.junit.Test;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Class for testing the the HRPlusTree 
 * 
 * @author ben 
 * @author neelesh
 *
 */


public class HRPlusTreeTest {
	
	
	// assuming insert works - TODO: test insert(going into same container+diff container)
	private HRPlusTree initHRPlusTree(){
		HRPlusTree tree = new HRPlusTree();
		
		 Envelope a  = new Envelope(-12,-10,-2,2);
		 Envelope b  = new Envelope(-8,-6,-2,2);
		 Envelope c  = new Envelope(-4,-2,-2,2);
		 tree.insert(new ObjectId(),a);
		 tree.insert(new ObjectId(),b);
		 tree.insert(new ObjectId(),c);
		return tree;
	}
	
	@Test
	public void testInsert(){
		HRPlusTree tree = new HRPlusTree();
		Envelope a = new Envelope(-5,5,-5,5);
		ObjectId a_id = new ObjectId();
		tree.insert(a_id, a);
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(new HRPlusNode(a_id,a));
		Envelope test = new Envelope(-50,50,-50,50);
		List<HRPlusNode> result = tree.query(test);
		
		//assertEquals(a_id,result.get(0).getId());
		assertEquals(a,result.get(0).getBounds());
	}
	
	/*
	@Test 
	
    public void testQuery() {
		HRPlusTree tree = new HRPlusTree();
		 Envelope a  = new Envelope(-12,-10,-2,2);
		 Envelope b  = new Envelope(-8,-6,-2,2);
		 Envelope c  = new Envelope(-4,-2,-2,2);
		 tree.insert(new ObjectId(),a);
		 tree.insert(new ObjectId(),b);
		 tree.insert(new ObjectId(),c);
		 
		 //TODO: Need a method which returns all the nodes of the tree!
		 
		  List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		  nodes.add(new HRPlusNode(new ObjectId(),a));
		  nodes.add(new HRPlusNode(new ObjectId(),b));
		  nodes.add(new HRPlusNode(new ObjectId(),c));
		  
		  assertEquals(nodes, tree.query(new Envelope(-50,-50,50,50)));
	}
    */
	
	

}
