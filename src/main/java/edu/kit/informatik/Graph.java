/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import zedly.zbot.Location;
import zedly.zbot.self.Self;

/**
 *
 * @author Dennis
 * @version 1.0
 */
public class Graph {

    private final HashMap<String, Node> nodes = new HashMap<>();
    private int nextNodeId = 0;

    /**
     * Create a new graph
     */
    public Graph() {
    }

    /**
     * Populate this graph with data from a text file
     *
     * @param filename the file to parse
     * @return the graph represented by the contents of the text file
     */
    public static Graph readFromFile(String filename) {
        Graph graph = new Graph();
        String[] serializedMapData;

        try {
            File serializedGraphFile = new File(filename);
            byte[] bin = new byte[(int) serializedGraphFile.length()];
            FileInputStream fis = new FileInputStream(serializedGraphFile);
            fis.read(bin);
            serializedMapData = new String(bin).replace("\r", "").split("\n");
        } catch (IOException ex) {
            return graph;
        }
        int i = 0;

        for (; i < serializedMapData.length && !serializedMapData[i].equals("--"); i++) {
            String[] serializedNodeData = serializedMapData[i].split(" ");
            if (graph.containsNode(serializedNodeData[0])) {
                System.err.println("Error, duplicate node declaration");
                continue;
            }
            graph.addNode(serializedNodeData);
        }
        for (i++; i < serializedMapData.length; i++) {
            String[] edges = serializedMapData[i].split(" ");
            if (edges.length != 4) {
                System.err.println("Error, malformed edge definition");
                continue;
            }
            if (!graph.containsNode(edges[0]) || !graph.containsNode(edges[1])) {
                System.err.println("Error, malformed edge definition");
                continue;
            }
            int distance = 0;
            int time = 0;
            try {
                distance = Integer.parseInt(edges[2]);
                time = Integer.parseInt(edges[3]);
            } catch (NumberFormatException ex) {
                System.err.println("Error, malformed edge definition");
                continue;
            }
            if (distance <= 0 || time <= 0) {
                System.err.println("Error, malformed edge definition");
                continue;
            }

            if (graph.getNode(edges[0]).getNeighbors().contains(graph.getNode(edges[1]))) {
                System.err.println("Error, duplicate edge definition");
                continue;
            }
            graph.connectNodes(graph.getNode(edges[0]), graph.getNode(edges[1]), Integer.parseInt(edges[2]), Integer.parseInt(edges[3]));
        }
        return graph;
    }

    /**
     * Serialize this graph into a text file.
     *
     * @param filename the file name to save the graph to
     */
    public void writeToFile(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            fos.write(serialize().getBytes());
            fos.close();
        } catch (IOException ex) {
        }
    }

    /**
     * Produces a string of text in the same format as the serialized graph
     * file.
     *
     * @return a string representing the current state of the graph
     */
    public String serialize() {
        HashSet<Node> checkedNodes = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (Node n : nodes.values()) {
            sb.append(toSerializedNode(n)).append("\n");
        }
        sb.append("--\n");
        for (Node n : nodes.values()) {
            checkedNodes.add(n);
            for (Node n1 : n.getNeighbors()) {
                if (!checkedNodes.contains(n1)) {
                    sb.append(n.getName()).append(" ").append(n1.getName()).append(" ").append(n.getDistanceTo(n1)).append(" ").append(n.getTimeTo(n1)).append("\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Add a node to the graph
     *
     * @param serializedNodeData the data of the node to add
     */
    public void addNode(String[] serializedNodeData) {
        Location loc = locationFromSerializedNode(serializedNodeData);
        if (serializedNodeData[0].matches("\\d+")) {
            try {
                int id = Integer.parseInt(serializedNodeData[0]);
                if (id >= nextNodeId) {
                    nextNodeId = id + 1;
                }
            } catch (NumberFormatException ex) {
            }
        }
        Node node = new Node(serializedNodeData[0], loc);
        nodes.put(serializedNodeData[0], node);
    }

    /**
     * Add a node to the graph
     *
     * @param node the node to add
     */
    public void addNode(Node node) {
        nodes.put(node.getName(), node);
    }

    /**
     * Remove a node from the graph, disconnecting all neighbors
     *
     * @param node the node to remove
     */
    public void removeNode(Node node) {
        if (!nodes.containsKey(node.getName())) {
            return;
        }
        node.disconnect();
        nodes.remove(node.getName());
    }

    /**
     * Create a new connection between given nodes, with given distance and time
     * weights
     *
     * @param node1 the first node to connect
     * @param node2 the secodn node to connect
     * @param distance the distance weight to apply to the new edge
     * @param time the time weight to apply to the new edge
     */
    public void connectNodes(Node node1, Node node2, int distance, int time) {
        node1.connectTo(node2, distance, time);
        node2.connectTo(node1, distance, time);
    }

    /**
     * Disconnect two nodes.
     *
     * @param node1 the first node to disconnect
     * @param node2 the second node to disconnect
     */
    public void disconnectNodes(Node node1, Node node2) {
        node1.disconnectFrom(node2);
        node2.disconnectFrom(node1);
    }

    /**
     * Get a node by its anme
     *
     * @param name the name to look for
     * @return the node with the given name
     */
    public Node getNode(String name) {
        return nodes.get(name.toLowerCase());
    }

    /**
     * Check if a node by the given name is already present in the graph
     *
     * @param name the name to look for
     * @return whether or not the node exists
     */
    public boolean containsNode(String name) {
        return nodes.containsKey(name.toLowerCase());
    }

    /**
     * Get the number of nodes contained in this graph
     *
     * @return an integer representing the number of nodes
     */
    public int getSize() {
        return nodes.size();
    }

    /**
     * Remove all nodes from this graph.
     */
    public void clear() {
        nodes.clear();
    }

    /**
     * Get all the nodes in this graph
     *
     * @return a collection of nodes contained in this graph
     */
    public ArrayList<Node> getNodes() {
        ArrayList<Node> newNodes = new ArrayList<>();
        newNodes.addAll(nodes.values());
        return newNodes;
    }

    /**
     * Check whether this graph is contiguous
     *
     * @param startingPoint any node in the graph to start checking
     * @return whether the graph is contiguous
     */
    public boolean isContiguous(Node startingPoint) {
        if (nodes.size() <= 1) {
            return true;
        }
        HashSet<Node> reachableNodes = new HashSet<>();
        recurseContiguityCheck(startingPoint, reachableNodes);
        return reachableNodes.size() == nodes.size();
    }

    /**
     * Get the closest node to this location
     *
     * @param loc the location to look for nodes
     * @return the closest node, or null
     */
    public Node getClosestNodeTo(Location loc) {
        double distance = Double.MAX_VALUE;
        Node node = null;
        for (Node n : nodes.values()) {
            double d = n.getLocation().distanceTo(loc);
            if (d < distance) {
                distance = d;
                node = n;
            }
        }
        return node;
    }

    /**
     * Get an ID that is guaranteed safe to use to label a new node (hopefully)
     * @return the ID for the new node
     */
    public int getNextNodeId() {
        return nextNodeId++;
    }

    private void recurseContiguityCheck(Node node, HashSet<Node> reachableNodes) {
        for (Node n : node.getNeighbors()) {
            if (!reachableNodes.contains(n)) {
                reachableNodes.add(n);
                recurseContiguityCheck(n, reachableNodes);
            }
        }
    }

    private Location locationFromSerializedNode(String[] serializedNodeData) {
        return new Location(Double.parseDouble(serializedNodeData[1]),
                Double.parseDouble(serializedNodeData[2]),
                Double.parseDouble(serializedNodeData[3]),
                Double.parseDouble(serializedNodeData[4]),
                Double.parseDouble(serializedNodeData[5]));
    }

    private String toSerializedNode(Node node) {
        Location loc = node.getLocation();
        return node.getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getYaw() + " " + loc.getPitch();
    }

}
