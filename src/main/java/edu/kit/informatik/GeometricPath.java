/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik;

import java.util.LinkedList;
import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class GeometricPath {

    private final GeometricPath origin;
    private final Location target;
    private final double length;

    /**
     * Create a starting point for a path. This is formally a path of length 0.
     *
     * @param node the starting point of this path
     */
    public GeometricPath(Location node) {
        origin = null;
        this.target = node;
        this.length = 0;
    }

    /**
     * Create a new GeometricPath that continues the given one to the specified
     * node. The created path will reach from the starting point of the given
     * path to the specified node. Its weights will be greater than the previous
     * path's by the weights of the edge from the previous node's endpoint to
     * the new node.
     *
     * @param node the node to continue this path to
     * @param path the path to extend
     */
    public GeometricPath(Location node, GeometricPath path) {
        origin = path;
        this.target = node;
        this.length = origin.getLength() + origin.getEndPoint().distanceTo(node);
    }

    /**
     * Check whether a given node has already been visited in this path.
     *
     * @param n the node to look for
     * @return true or false, duh.
     */
    public boolean containsLocation(Location n) {
        if (n == target) {
            return true;
        } else if (origin == null) {
            return false;
        } else {
            return origin.containsLocation(n);
        }
    }

    /**
     * Get the nodes this path passes, from start to finish
     *
     * @return a List of nodes constituting this path
     */
    public LinkedList<Location> getLocations() {
        LinkedList<Location> nodes;
        if (origin == null) {
            nodes = new LinkedList<>();
            nodes.add(target);
            return nodes;
        } else {
            nodes = origin.getLocations();
            nodes.add(target);
            return nodes;
        }
    }

    /**
     * Get the endpoint of this path
     *
     * @return the last node in this path
     */
    public Location getEndPoint() {
        return target;
    }

    /**
     * Get the distance-weight of this path, the sum of all constituent edges.
     *
     * @return an integer representing this path's distance-weight
     */
    public double getLength() {
        return length;
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
            nodes.add(new Node(target));
            return nodes;
        } else {
            nodes = origin.getNodes();
            nodes.add(new Node(target));
            return nodes;
        }
    }
}
