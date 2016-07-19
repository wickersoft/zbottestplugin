/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik;

import java.util.LinkedList;

/**
 *
 * @author Dennis
 * @version 1.0
 *
 * Let's do some recursion too, because why not!
 */
public class Path {

    private final Path origin;
    private final Node node;
    private final int length;
    private final int time;
    private final int spaceTimeStuff;

    /**
     * Create a starting point for a path. This is formally a path of length 0.
     *
     * @param node the starting point of this path
     */
    public Path(Node node) {
        origin = null;
        this.node = node;
        this.length = 0;
        this.time = 0;
        this.spaceTimeStuff = 0;
    }

    /**
     * Create a new Path that continues the given one to the specified node. The
     * created path will reach from the starting point of the given path to the
     * specified node. Its weights will be greater than the previous path's by
     * the weights of the edge from the previous node's endpoint to the new
     * node.
     *
     * @param node the node to continue this path to
     * @param path the path to extend
     */
    public Path(Node node, Path path) {
        origin = path;
        this.node = node;
        this.length = origin.getLength() + origin.getEndPoint().getDistanceTo(node);
        this.time = origin.getTime() + origin.getEndPoint().getTimeTo(node);
        this.spaceTimeStuff = origin.getSpaceTimeDistance()
                + (origin.getEndPoint().getDistanceTo(node) * origin.getEndPoint().getDistanceTo(node))
                + (origin.getEndPoint().getTimeTo(node) * origin.getEndPoint().getTimeTo(node));
    }

    /**
     * Check whether a given node has already been visited in this path.
     *
     * @param n the node to look for
     * @return true or false, duh.
     */
    public boolean containsNode(Node n) {
        if (n == node) {
            return true;
        } else if (origin == null) {
            return false;
        } else {
            return origin.containsNode(n);
        }
    }

    /**
     * Get the nodes this path passes, from start to finish
     *
     * @return a List of nodes constituting this path
     */
    public LinkedList<Node> getNodes() {
        LinkedList<Node> nodes;
        if (origin == null) {
            nodes = new LinkedList<>();
            nodes.add(node);
            return nodes;
        } else {
            nodes = origin.getNodes();
            nodes.add(node);
            return nodes;
        }
    }

    /**
     * Get the endpoint of this path
     *
     * @return the last node in this path
     */
    public Node getEndPoint() {
        return node;
    }

    /**
     * Get the distance-weight of this path, the sum of all constituent edges.
     *
     * @return an integer representing this path's distance-weight
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the time-weight of this path, the sum of all constituent edges.
     *
     * @return an integer representing this path's time-weight
     */
    public int getTime() {
        return time;
    }

    /**
     * Get the total squared space-time distance of this path.
     * I mean what the hell am I even doing at this point
     *
     * @return an integer representing the total squared space-time distance of
     * this path
     */
    public int getSpaceTimeDistance() {
        return spaceTimeStuff;
    }

    /**
     * Get a string representation of this path with all its constituent nodes.
     * Nodes are separated by single spaces.
     *
     * @return a string representing this path
     */
    @Override
    public String toString() {
        LinkedList<Node> path = origin.getNodes();
        StringBuilder sb = new StringBuilder();
        for (Node n : path) {
            sb.append(n.getName()).append(" ");
        }
        sb.append(node.getName());
        return sb.toString();
    }
}
