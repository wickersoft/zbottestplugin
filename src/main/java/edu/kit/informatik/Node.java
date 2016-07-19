package edu.kit.informatik;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import zedly.zbot.Location;
/**
 *
 * @author Dennis
 * @version 1.0
 * 
 */
public class Node {

    private final ArrayList<Node> neighbors = new ArrayList<>();
    private final HashMap<Node, Integer> distanceWeights = new HashMap<>();
    private final HashMap<Node, Integer> timeWeights = new HashMap<>();
    private final String name;
    private final Location location;
    
    /**
     * Create a node with this name
     * @param name the name to give to this nodes
     */
    public Node(String name, Location location) {
        this.name = name;
        this.location = location;
    }
    
    /**
     * Create a node with no name at this location
     * @param location the location this node represents
     */
    public Node(Location location) {
        this.name = null;
        this.location = location;
    }
    
    /**
     * Adds an edge to the given node with given time and distance weights
     * @param node the node to connect to
     * @param distance the distance-weight of the new edge
     * @param time the time-weight of the new edge
     */
    public void connectTo(Node node, int distance, int time) {
        neighbors.add(node);
        distanceWeights.put(node, distance);
        timeWeights.put(node, time);
    }

    /**
     * Removes the edge to the given node
     * @param node the node to disconnect from
     */
    public void disconnectFrom(Node node) {
        neighbors.remove(node);
        distanceWeights.remove(node);
        timeWeights.remove(node);
    }
    
    /**
     * Disconnect this node from all its neighbors
     */
    public void disconnect() {
        for(Node n : neighbors) {
            n.disconnectFrom(this);
        }
        neighbors.clear();
        distanceWeights.clear();
        timeWeights.clear();
    }

    /**
      * Get the distance-weight of the edge to the given neighbor
     * @param node the neighbor to find the distance to
     * @return the distance-weight of the edge to the given neighbor
     */
    public int getDistanceTo(Node node) {
        return distanceWeights.get(node);
    }

    /**
     * Get the time-weight of the edge to the given neighbor
     * @param node the neighbor to find the distance to
     * @return the time-weight of the edge to the given neighbor
     */
    public int getTimeTo(Node node) {
        return timeWeights.get(node);
    }
    
    /**
     * Get all the neighbors of this node
     * @return a List of nodes adjacent to this node
     */
    public List<Node> getNeighbors() {
        return neighbors;
    }
    
    /**
     * Get the name of this node
     * @return the name of this node
     */
    public String getName() {
        return name;
    }
    
    /**
     * For elegent in-line printing
     * @return the name of this node
     */
    @Override
    public String toString() {
        return name;
    }

    public Location getLocation() {
        return location;
    }
}
