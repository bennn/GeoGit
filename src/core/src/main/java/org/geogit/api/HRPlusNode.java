package org.geogit.api;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusNode implements RevObject {

    private ObjectId objectId;
    private ObjectId parentContainerId;

    // Bounds are either 2 values, for one-dimensional envelopes,
    // or 4 values, for two-dimensional envelopes
    protected double[] bounds;
    private HRPlusContainerNode child;
    private List<ObjectId> layerIds = new ArrayList<ObjectId>();
    
    public HRPlusNode(ObjectId layerId, Envelope bounds) {
        super();
        this.layerIds.add(layerId);
        this.setBounds(bounds);
    }

    public HRPlusNode(List<ObjectId> layerIds, Envelope bounds) {
        super();
        this.layerIds.addAll(layerIds);
        this.setBounds(bounds);
    }

    public ObjectId getObjectId(){
        return this.objectId;
    }

    public double getMinX(){
        return this.bounds[0];
    }

    public double getMinY(){
        return this.bounds[1];
    }

    public double getMaxX(){
        return this.bounds[2];
    }

    public double getMaxY(){
        return this.bounds[3];
    }

    /**
     * Increase @param env to include the bounds of this node.
     * @param env
     */
    public void expand(Envelope env) {
        env.expandToInclude(bounds[0], bounds[1]);
        if (bounds.length > 2) {
            env.expandToInclude(bounds[2], bounds[3]);
        }
    } 

    public HRPlusContainerNode getChild(){
        return this.child;
    }

    public void setChild(HRPlusContainerNode child){
        this.child = child;
    }

    public List<ObjectId> getLayerIds() {
        return this.layerIds;
    }

    public void addLayerId(ObjectId layerId){
        this.layerIds.add(layerId);
    }

    public ObjectId getFirstLayerId(){
        if(this.layerIds == null){
            return null;
        }
        return this.layerIds.get(0);
    }

    public boolean isLeaf(){
        return this.child == null;
    }

    public ObjectId getParentContainerId(){
        return this.parentContainerId;
    }

    public Envelope getBounds(){
        Envelope env = new Envelope();
        this.expand(env);
        return env;
    }

    public void setBounds(Envelope env){
        this.bounds[0] = env.getMinX();
        this.bounds[1] = env.getMinY();
        this.bounds[2] = env.getMaxX();
        this.bounds[3] = env.getMaxY();
    }

    public void getOverlap(Envelope env){
        if(isLeaf()){
            expand(env);
        }
        this.child.getOverlap(env);
    }
    
    /**
     * Bounding box query. If this node fits in container, add it to list 
     * and recurse on its child (if it has a child). Else end search.
     * @param env
     * @param matches
     */
    public void query(Envelope env, List<HRPlusNode> matches) {
        // TODO okay to stop if there's no intersection?
        if (this.getBounds().intersects(env)) {
            // A match!
            matches.add(this);
            if (!this.isLeaf()) {
                // Not a leaf, continue searching child
                this.getChild().query(env, matches);
            }
        }
        return;
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
