/*package edu.vt.ece.locks;

import edu.vt.ece.bench.ThreadId;
import edu.vt.ece.util.Tree;

import java.util.List;

*/
package edu.vt.ece.locks;

import edu.vt.ece.bench.ThreadId;
import edu.vt.ece.util.Tree;

import java.util.Stack;

public class TreePeterson implements Lock {
    private final Tree<Peterson> locks;

    public TreePeterson() {
        this(2);
    }

    public TreePeterson(int n) {
        // Create a complete binary tree with n leaves
        locks = new Tree<>(new Peterson()); // Root lock
        createTree(locks, n - 1); // n-1 because we already have the root
    }


    private void createTree(Tree<Peterson> node, int remainingLocks) {
        if (remainingLocks > 0) {
            // Create left child and continue to create its subtree
            Tree<Peterson> leftChild = new Tree<>(new Peterson());
            node.add(leftChild.getData()); // Add the left child to the current node

            // Recursively create the left subtree
            createTree(leftChild, remainingLocks / 2);

            // Create right child and continue to create its subtree
            Tree<Peterson> rightChild = new Tree<>(new Peterson());
            node.add(rightChild.getData()); // Add the right child to the current node

            // Recursively create the right subtree
            createTree(rightChild, remainingLocks / 2);
        }
    }

    @Override
    public void lock() {
        // Get the thread ID
        int threadId = ((ThreadId) Thread.currentThread()).getThreadId();
        // Traverse to the leaves and acquire locks
        Tree<Peterson> current = locks;
        while (current != null) {
            current.getData().lock(); // Acquire lock at this node
            current = (threadId % 2 == 0) ? current.getLeftChild() : current.getRightChild();
            threadId /= 2; // Move up the tree
        }
    }

    @Override
    public void unlock() {
        // Get the thread ID
        int threadId = ((ThreadId) Thread.currentThread()).getThreadId();
        // Traverse back to the root and release locks
        Tree<Peterson> current = locks;
        // Use a stack or an array to store the path to release in reverse
        Stack<Tree<Peterson>> path = new Stack<>();

        while (current != null) {
            path.push(current); // Store the current lock to release later
            current = (threadId % 2 == 0) ? current.getLeftChild() : current.getRightChild();
            threadId /= 2; // Move up the tree
        }

        // Release locks in reverse order
        while (!path.isEmpty()) {
            path.pop().getData().unlock(); // Release lock at this node
        }
    }
}
