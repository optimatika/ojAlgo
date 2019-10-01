/*
 * Copyright 1997-2019 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.type.tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

final class TreeNode implements BinaryTree, BinaryNode {

    private TreeNode myLeftChild;
    private final TreeNode myParent;
    private TreeNode myRightChild;

    TreeNode() {
        super();
        myParent = null;
        myLeftChild = null;
        myRightChild = null;
    }

    TreeNode(TreeNode parent) {
        super();
        myParent = parent;
        myLeftChild = null;
        myRightChild = null;
    }

    TreeNode(TreeNode parent, TreeNode leftChild, TreeNode rightChild) {
        super();
        myParent = parent;
        myLeftChild = leftChild;
        myRightChild = rightChild;
    }

    @Override
    public List<BinaryNode> getChildren() {
        if ((myLeftChild != null) && (myRightChild != null)) {
            return Collections.unmodifiableList(Arrays.asList(myLeftChild, myRightChild));
        } else {
            return Collections.emptyList();
        }

    }

    @Override
    public Optional<BinaryNode> getLeftChild() {
        return Optional.ofNullable(myLeftChild);
    }

    @Override
    public Optional<BinaryNode> getParent() {
        return Optional.ofNullable(myParent);
    }

    @Override
    public Optional<BinaryNode> getRightChild() {
        return Optional.ofNullable(myRightChild);
    }

    @Override
    public BinaryNode getRoot() {
        if (myParent == null) {
            return this;
        } else {
            return myParent.getRoot();
        }
    }

    @Override
    public BinaryTree getTree() {
        return this;
    }

    @Override
    public boolean isLeaf() {
        return (myLeftChild == null) && (myRightChild == null);
    }

    @Override
    public boolean isRoot() {
        return myParent == null;
    }

    @Override
    public void split() {
        myLeftChild = new TreeNode(this);
        myRightChild = new TreeNode(this);
    }

}
