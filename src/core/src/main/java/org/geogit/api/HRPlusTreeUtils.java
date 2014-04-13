package org.geogit.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Base class for HRPlus tree implementation of the spatial index.
 * This class contains utility methods for manipulating HRPlus trees.
 * Utilities are factored out because we distinguish
 * between a plain HRPlus tree and an HRPlus tree partition. The latter is
 * used to speed up insertions.
 * 
 * For the original idea, see
 *     [1] http://www.cs.ust.hk/faculty/dimitris/PAPERS/ssdbm01.pdf
 * and for details on insertion, see Section 4 of:
 *     [2] http://dbs.mathematik.uni-marburg.de/publications/myPapers/1990/BKSS90.pdf
 * 
 * @author jillian
 *
 */
public class HRPlusTreeUtils {

    /**
     * Create an envelope covering all the points in @param nodes. Used to 
     * create a container node for the HRPlus tree.
     * @param nodes
     * @return
     */
    protected static Envelope boundingBoxOf(List<HRPlusNode> nodes){
        Envelope envelope = new Envelope();
        // Iterate over nodes, expand envelope to include each.
        // We don't care about the order of iteration. Nothing fancy here.
        for(HRPlusNode node : nodes){
            node.expand(envelope);
        }
        return envelope;
    }

    /**
     * Find the overlap between bounding boxes surrounding two groups of nodes.
     * 
     * @param firstGroup
     * @param secondGroup
     * @return the area of the enclosing bounding box
     */
    protected static double getOverlap(List<HRPlusNode> firstGroup,
            List<HRPlusNode> secondGroup){
        return boundingBoxOf(firstGroup).intersection(boundingBoxOf(secondGroup)).getArea();
    }

    /**
     * Get total area of the envelopes covering two groups of nodes.
     * 
     * @param firstGroup
     * @param secondGroup
     * @return combined area of the two envelopes
     */
    protected static double getTotalAreaOfTwoRegions(List<HRPlusNode> firstGroup, List<HRPlusNode> secondGroup){
        return boundingBoxOf(firstGroup).getArea() + boundingBoxOf(secondGroup).getArea();
    }

    /**
     * margin = perimeter
     * @param envelope
     * @return the perimeter of the envelope
     */
    protected static double marginOf(Envelope envelope){
        double height = envelope.getHeight();
        double width = envelope.getWidth(); 
        return height + height + width + width;
    }

    /**
     * Sum many perimeters. I do not entirely understand the implementation.
     * It's based off the algorithm presented in [2], and that paper suggests
     * minimizing perimeters as a useful way to optimize an index.
     * @param nodes
     * @return
     */
    protected static double sumOfMargins(List<HRPlusNode> nodes){
        // Divide nodes into two even groups.
        List<HRPlusNode> firstGroup = nodes.subList(0, nodes.size()/2);
        List<HRPlusNode> secondGroup = nodes.subList(nodes.size() - firstGroup.size(), nodes.size() -1);

        double marginValueSum = 0;
        // Iteratively add one element of the second group to the first. Add 
        while(!secondGroup.isEmpty()){
            marginValueSum +=  marginOf(boundingBoxOf(firstGroup)) + marginOf(boundingBoxOf(secondGroup));
            firstGroup.add(secondGroup.remove(0));
        }
        return marginValueSum;
    }

    /**
     * Take a list of nodes, sorted by position on a one-dimensional axis.
     * Return a subset of these nodes. Specifically, the subset of nodes
     * that is closest together based on the one-dimensional axis and the
     * total perimeter and area of their enclosing envelope.
     * 
     * @param minSort nodes sorted by minimum position along some axis 
     * @param maxSort same nodes, sorted by maximum position along the same axis.
     * @return sublist containing the nodes closest together
     */
    protected static List<HRPlusNode> partitionByMinOverlap(List<HRPlusNode>minSort, 
            List<HRPlusNode>maxSort){
        // Create two partitions corresponding to the two arguments.
        // This determines the minimum area/perimeter split of the nodes.
        HRPlusTreePartition minPartition = new HRPlusTreePartition(minSort);
        HRPlusTreePartition maxPartition = new HRPlusTreePartition(maxSort);
        // Extract fields from the partitions 
        double overlapMinSort = minPartition.getOverlap();
        double areaValueMinSort = minPartition.getArea();
        double overlapMaxSort = maxPartition.getOverlap();
        double areaValueMaxSort = maxPartition.getArea();
        // Choose the partition with the smallest overlap.
        // If overlaps are equal, choose the partition with the smallest area.
        if (overlapMinSort < overlapMaxSort || 
                (overlapMinSort == overlapMaxSort && 
                areaValueMinSort <= areaValueMaxSort)) {
            int splitPointMinSort = minPartition.getSplitPoint();
            // Get the spatially smallest partition of nodes from the minsort list
            return minSort.subList(0, splitPointMinSort - 1);
        } else {
            int splitPointMaxSort = maxPartition.getSplitPoint();
            // Get the smallest (least perimeter/area) partition of nodes from the maxsort list
            return maxSort.subList(0, splitPointMaxSort - 1);
        }
    }

    /**
     * Sort a list of nodes by their minimum x coordinate.
     * @param nodes
     * @return
     */
    protected static List<HRPlusNode> minXSort(List<HRPlusNode> nodes){
        List<HRPlusNode> minXSort = new ArrayList<HRPlusNode>(nodes);
        Collections.sort(minXSort, new Comparator<HRPlusNode>() {
            public int compare(HRPlusNode n1, HRPlusNode n2) {
                return Double.compare(n1.getMinX(), n1.getMinX());
            }
        });
        return minXSort;
    }

    /**
     * Sort a list of nodes by their minimum y coordinate.
     * @param nodes
     * @return
     */
    protected static List<HRPlusNode> minYSort(List<HRPlusNode> nodes){
        List<HRPlusNode> minYSort = new ArrayList<HRPlusNode>(nodes);
        Collections.sort(minYSort, new Comparator<HRPlusNode>() {
            public int compare(HRPlusNode n1, HRPlusNode n2) {
                return Double.compare(n1.getMinY(), n1.getMinY());
            }
        });
        return minYSort;
    }

    /**
     * Sort a list of nodes by their maximum x coordinate
     * @param nodes
     * @return
     */
    protected static List<HRPlusNode> maxXSort(List<HRPlusNode> nodes){
        List<HRPlusNode> maxXSort = new ArrayList<HRPlusNode>(nodes);
        Collections.sort(maxXSort, new Comparator<HRPlusNode>() {
            public int compare(HRPlusNode n1, HRPlusNode n2) {
                return Double.compare(n1.getMaxX(), n1.getMaxX());
            }
        });
        return maxXSort;
    }

    /**
     * Sort a list of nodes by their maximum y coordinate.
     * @param nodes
     * @return
     */
    protected static List<HRPlusNode> maxYSort(List<HRPlusNode> nodes){
        List<HRPlusNode> maxYSort = new ArrayList<HRPlusNode>(nodes);
        Collections.sort(maxYSort, new Comparator<HRPlusNode>() {
            public int compare(HRPlusNode n1, HRPlusNode n2) {
                return Double.compare(n1.getMaxY(), n1.getMaxY());
            }
        });
        return maxYSort;
    }

}
