/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.test.integration;

import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.api.plumbing.FindCommonAncestor;
import org.geogit.api.porcelain.BranchCreateOp;
import org.geogit.api.porcelain.CheckoutOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.ConfigOp;
import org.geogit.api.porcelain.ConfigOp.ConfigAction;
import org.geogit.api.porcelain.MergeOp;
import org.geogit.api.porcelain.MergeOp.MergeReport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;

public class FindCommonAncestorTest extends RepositoryTestCase {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
        // These values should be used during a commit to set author/committer
        // TODO: author/committer roles need to be defined better, but for
        // now they are the same thing.
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET).setName("user.name")
                .setValue("groldan").call();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET).setName("user.email")
                .setValue("groldan@opengeo.org").call();
    }

    @Test
    public void testFindCommonAncestorCase1() throws Exception {
        // Create the following revision graph
        // o
        // |
        // o - Points 1 added
        // |\
        // | o - branch1 - Points 2 added
        // |
        // o - Points 3 added
        // |
        // o - master - HEAD - Lines 1 added
        insertAndAdd(points1);
        final RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();

        // create branch1 and checkout
        geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch1").call();
        insertAndAdd(points2);
        final RevCommit left = geogit.command(CommitOp.class).setMessage("commit for " + idP2)
                .call();

        // checkout master
        geogit.command(CheckoutOp.class).setSource("master").call();
        insertAndAdd(points3);
        geogit.command(CommitOp.class).setMessage("commit for " + idP3).call();
        insertAndAdd(lines1);
        final RevCommit right = geogit.command(CommitOp.class).setMessage("commit for " + idL1)
                .call();

        Optional<ObjectId> commonAncestor = geogit.command(FindCommonAncestor.class).setLeft(left)
                .setRight(right).call();

        assertTrue(commonAncestor.isPresent());
        assertEquals(commonAncestor.get(), c1.getId());

    }

    @Test
    public void testFindCommonAncestorCase2() throws Exception {
        // Create the following revision graph
        // o
        // |
        // o - Points 1 added
        // |\
        // | o - Points 2 added
        // | |
        // | o - Points 3 added
        // | |
        // | o - Lines 2 added - branch1
        // |
        // o - master - HEAD - Lines 1 added
        insertAndAdd(points1);
        final RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();

        // create branch1 and checkout
        geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch1").call();
        insertAndAdd(points2);
        geogit.command(CommitOp.class).setMessage("commit for " + idP2).call();
        insertAndAdd(points3);
        geogit.command(CommitOp.class).setMessage("commit for " + idP3).call();
        insertAndAdd(lines2);
        final RevCommit left = geogit.command(CommitOp.class).setMessage("commit for " + idL2)
                .call();

        // checkout master
        geogit.command(CheckoutOp.class).setSource("master").call();
        insertAndAdd(lines1);
        final RevCommit right = geogit.command(CommitOp.class).setMessage("commit for " + idL1)
                .call();

        Optional<ObjectId> commonAncestor = geogit.command(FindCommonAncestor.class).setLeft(left)
                .setRight(right).call();

        assertTrue(commonAncestor.isPresent());
        assertEquals(commonAncestor.get(), c1.getId());

    }

    @Test
    public void testFindCommonAncestorCase3() throws Exception {
        // Create the following revision graph
        // o
        // |
        // o - Points 1 added
        // |\
        // | o - Points 2 added
        // | |
        // | o - Points 3 added
        // | |\
        // | | o - Lines 1 added - branch2
        // | |
        // o | - Lines 2 added
        // | |
        // | o - Lines 3 added - branch1
        // |/
        // o - master - HEAD - Merge Commit
        insertAndAdd(points1);
        geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();

        // create branch1 and checkout
        geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch1").call();
        insertAndAdd(points2);
        geogit.command(CommitOp.class).setMessage("commit for " + idP2).call();
        insertAndAdd(points3);
        final RevCommit ancestor = geogit.command(CommitOp.class).setMessage("commit for " + idP3)
                .call();
        geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch2").call();
        insertAndAdd(lines1);
        final RevCommit branch2 = geogit.command(CommitOp.class).setMessage("commit for " + idL1)
                .call();
        geogit.command(CheckoutOp.class).setSource("branch1").call();
        insertAndAdd(lines3);
        final RevCommit left = geogit.command(CommitOp.class).setMessage("commit for " + idL3)
                .call();

        // checkout master
        geogit.command(CheckoutOp.class).setSource("master").call();
        insertAndAdd(lines2);
        geogit.command(CommitOp.class).setMessage("commit for " + idL2).call();

        final MergeReport mergeReport = geogit.command(MergeOp.class)
                .addCommit(Suppliers.ofInstance(left.getId())).call();

        Optional<ObjectId> commonAncestor = geogit.command(FindCommonAncestor.class)
                .setLeft(mergeReport.getMergeCommit()).setRight(branch2).call();

        assertTrue(commonAncestor.isPresent());
        assertEquals(commonAncestor.get(), ancestor.getId());

    }

    @Test
    public void testFindCommonAncestorCase4() throws Exception {
        // Create the following revision graph
        // o
        // |
        // o - Points 1 added
        // |\
        // | o - Points 2 added
        // | |
        // | o - Points 3 added (Ancestor of branch2 and master)
        // | |\
        // | | o - Lines 1 added
        // | | |\
        // | | | o - Polygon 1 added - branch3
        // | | | |
        // | | o | - Polygon 2 added
        // | | |/
        // | | o - Merge Commit - branch2
        // | |
        // o | - Lines 2 added
        // | |
        // | o - Lines 3 added - branch1
        // |/
        // o - master - HEAD - Merge Commit
        insertAndAdd(points1);
        geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();

        // create branch1 and checkout
        geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch1").call();
        insertAndAdd(points2);
        geogit.command(CommitOp.class).setMessage("commit for " + idP2).call();
        insertAndAdd(points3);
        final RevCommit ancestor = geogit.command(CommitOp.class).setMessage("commit for " + idP3)
                .call();
        geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch2").call();
        insertAndAdd(lines1);
        geogit.command(CommitOp.class).setMessage("commit for " + idL1).call();
        geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch3").call();
        insertAndAdd(poly1);
        RevCommit branch3 = geogit.command(CommitOp.class).setMessage("commit for " + idPG1).call();
        geogit.command(CheckoutOp.class).setSource("branch2").call();
        insertAndAdd(poly2);
        geogit.command(CommitOp.class).setMessage("commit for " + idPG2).call();

        MergeReport mergeReport = geogit.command(MergeOp.class)
                .addCommit(Suppliers.ofInstance(branch3.getId())).call();

        RevCommit branch2 = mergeReport.getMergeCommit();

        geogit.command(CheckoutOp.class).setSource("branch1").call();
        insertAndAdd(lines3);
        final RevCommit left = geogit.command(CommitOp.class).setMessage("commit for " + idL3)
                .call();

        // checkout master
        geogit.command(CheckoutOp.class).setSource("master").call();
        insertAndAdd(lines2);
        geogit.command(CommitOp.class).setMessage("commit for " + idL2).call();

        mergeReport = geogit.command(MergeOp.class).addCommit(Suppliers.ofInstance(left.getId()))
                .call();

        Optional<ObjectId> commonAncestor = geogit.command(FindCommonAncestor.class)
                .setLeft(mergeReport.getMergeCommit()).setRight(branch2).call();

        assertTrue(commonAncestor.isPresent());
        assertEquals(commonAncestor.get(), ancestor.getId());

    }

    @Test
    public void testFindCommonAncestorCase5() throws Exception {
        // Create the following revision graph
        // o - root commit Add Points 1
        // |\
        // | o - commit1 Add Points 2
        // | |\
        // o | | - commit2 Add Points 3
        // | | |
        // | | o - commit3 Modify Points 1
        // | | |
        // | o | - commit4 Add Lines 1
        // | |\|
        // | | o - commit5 Merge commit
        // | | |
        // | o | - commit6 Add Lines 2
        // | | |
        // | o | - commit7 Add Lines 3
        // | | |
        // | o | - commit8 Add Polygon 1
        // | | |
        // | o | - commit9 Add Polygon 2
        // | | |
        // | | o - commit10 Add Polygon 3
        // |/
        // o - commit11 Merge commit

        // root commit
        insertAndAdd(points1);
        geogit.command(CommitOp.class).setMessage("root commit").call();

        // commit1
        geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch1").call();
        insertAndAdd(points2);
        geogit.command(CommitOp.class).setMessage("commit1").call();
        geogit.command(BranchCreateOp.class).setAutoCheckout(false).setName("branch2").call();

        // commit2
        geogit.command(CheckoutOp.class).setSource("master").call();
        insertAndAdd(points3);
        geogit.command(CommitOp.class).setMessage("commit2").call();

        // commit3
        geogit.command(CheckoutOp.class).setSource("branch2").call();
        insertAndAdd(points1_modified);
        geogit.command(CommitOp.class).setMessage("commit3").call();

        // commit4
        geogit.command(CheckoutOp.class).setSource("branch1").call();
        insertAndAdd(lines1);
        ObjectId commit4 = geogit.command(CommitOp.class).setMessage("commit4").call().getId();

        // commit5
        geogit.command(CheckoutOp.class).setSource("branch2").call();
        geogit.command(MergeOp.class).setMessage("commit3")
                .addCommit(Suppliers.ofInstance(commit4)).call();

        // commit6
        geogit.command(CheckoutOp.class).setSource("branch1").call();
        insertAndAdd(lines2);
        geogit.command(CommitOp.class).setMessage("commit6").call();

        // commit7
        insertAndAdd(lines3);
        geogit.command(CommitOp.class).setMessage("commit7").call();

        // commit8
        insertAndAdd(poly1);
        geogit.command(CommitOp.class).setMessage("commit8").call();

        // commit9
        insertAndAdd(poly2);
        ObjectId commit9 = geogit.command(CommitOp.class).setMessage("commit9").call().getId();

        // commit10
        geogit.command(CheckoutOp.class).setSource("branch2").call();
        insertAndAdd(poly3);
        RevCommit commit10 = geogit.command(CommitOp.class).setMessage("commit10").call();

        // commit11
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeReport report = geogit.command(MergeOp.class).setMessage("commit11")
                .addCommit(Suppliers.ofInstance(commit9)).call();

        Optional<ObjectId> commonAncestor = geogit.command(FindCommonAncestor.class)
                .setLeft(report.getMergeCommit()).setRight(commit10).call();

        assertTrue(commonAncestor.isPresent());
        assertEquals(commonAncestor.get(), commit4);
    }
}
