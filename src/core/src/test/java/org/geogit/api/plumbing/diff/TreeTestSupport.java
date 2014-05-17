/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing.diff;

import java.util.Random;

import org.geogit.api.Node;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.geogit.api.RevTreeBuilder;
import org.geogit.storage.ObjectDatabase;

public class TreeTestSupport {

    public static RevTreeBuilder createTreesTree(ObjectDatabase source, int numSubTrees,
            int featuresPerSubtre, ObjectId metadataId) {

        RevTreeBuilder builder = new RevTreeBuilder(source);
        for (int treeN = 0; treeN < numSubTrees; treeN++) {
            RevTree subtree = createFeaturesTree(source, "subtree" + treeN, featuresPerSubtre)
                    .build();
            source.put(subtree);
            builder.put(Node.create("subtree" + treeN, subtree.getId(), metadataId, TYPE.TREE, null));
        }
        return builder;
    }

    public static RevTreeBuilder createFeaturesTree(ObjectDatabase source, final String namePrefix,
            final int numEntries) {
        return createFeaturesTree(source, namePrefix, numEntries, 0, false);
    }

    public static RevTreeBuilder createFeaturesTree(ObjectDatabase source, final String namePrefix,
            final int numEntries, final int startIndex, boolean randomIds) {

        RevTreeBuilder tree = new RevTreeBuilder(source);
        for (int i = startIndex; i < startIndex + numEntries; i++) {
            tree.put(featureNode(namePrefix, i, randomIds));
        }
        return tree;
    }

    public static Node featureNode(String namePrefix, int index) {
        return featureNode(namePrefix, index, false);
    }

    public static Node featureNode(String namePrefix, int index, boolean randomIds) {
        String name = namePrefix + String.valueOf(index);
        ObjectId oid;
        if (randomIds) {
            oid = ObjectId.forString(name + index + String.valueOf(new Random(index).nextInt()));
        } else {// predictable id
            oid = ObjectId.forString(name);
        }
        Node ref = Node.create(name, oid, ObjectId.NULL, TYPE.FEATURE, null);
        return ref;
    }

}
