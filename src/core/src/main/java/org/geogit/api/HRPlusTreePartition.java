package org.geogit.api;

import java.util.List;

/**
 * Based on the algorithm from
 *     [2] http://dbs.mathematik.uni-marburg.de/publications/myPapers/1990/BKSS90.pdf
 * Create a spatial partition of a set of nodes.
 * 
 * @author jillian
 *
 */
public class HRPlusTreePartition extends HRPlusTreeUtils{

    // Area of shared space between partitions.
    double overlap = Double.MAX_VALUE;
    // Area of both partitions, combined.
    double area = Double.MAX_VALUE;
    // index of the input list where nodes were divided.
    int splitPoint = 0;

    /**
     * Accept a list of nodes sorted by position along a one-dimensional axis.
     * Create a partition of these nodes, split at one point along the axis.
     * 
     * Record the overlap between the two segments of the partition,
     * the total area covered by the nodes in the partition, and
     * the index of the list at which nodes are divided.
     * 
     * @param sortedNodes
     */
    public HRPlusTreePartition(List<HRPlusNode> sortedNodes){
        // Create two equally-sized lists
        List<HRPlusNode> firstGroup = sortedNodes.subList(0, sortedNodes.size()/2);
        List<HRPlusNode> secondGroup = sortedNodes.subList(sortedNodes.size() - firstGroup.size(), sortedNodes.size() -1);
        // Track the overlap and area
        double curOverlap;
        double curArea;
        // Iteratively add elements of second group to the first group.
        while(!secondGroup.isEmpty()){
            firstGroup.add(secondGroup.remove(0));
            curOverlap = getOverlap(firstGroup, secondGroup);
            if(curOverlap < this.overlap) {
                // Keep the smallest overlap
                this.overlap = curOverlap;
                this.splitPoint = firstGroup.size();
            } else if(curOverlap == this.overlap) {
                // Same overlap? Choose the smallest total area. 
                curArea = getTotalAreaOfTwoRegions(firstGroup, secondGroup);
                if(curArea < this.area){
                    this.overlap = curOverlap;
                    this.splitPoint = firstGroup.size();
                    this.area = curArea;
                }
            }
        }
    }

    public double getOverlap() {
        return overlap;
    }

    public double getArea() {
        return area;
    }

    public int getSplitPoint() {
        return splitPoint;
    }

}
