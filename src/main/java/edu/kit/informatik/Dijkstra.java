/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 *
 * @author Dennis
 * @version 1.0
 */
public final class Dijkstra {
    
    /**
     * The comparator representing the criterion of shortest time between nodes.
     */
    public static final Comparator<Path> FASTEST = new Comparator<Path>() {
        @Override
        public int compare(Path a, Path b) {
            return a.getTime() - b.getTime();
        }
    };
    
    /**
     * The comparator representing the criterion of smallest distance between
     * nodes.
     */
    public static final Comparator<Path> SHORTEST = new Comparator<Path>() {
        @Override
        public int compare(Path a, Path b) {
            return a.getLength() - b.getLength();
        }
    };

    private Dijkstra() {
    }
    
    /**
     * Find the shortest path between any two points in a contiguous graph, from
     * start to finish. The following kilobyte of code is what this whole
     * assignment comes down to and I'm wasting my time sanitizing input
     * artificially invalidated by a machine.
     *
     * @param graph the graph to search in
     * @param startNodeName the name of the starting point
     * @param endNodeName the name of the destination point
     * @param criterion the criterion to apply to the search
     * @return a list of nodes representing the shortest path from start to
     * finish, inclusive.
     */
    public static Path findPath(Graph graph, String startNodeName, String endNodeName,
            Comparator<Path> criterion) {
        ArrayList<Path> paths = new ArrayList<>();
        HashMap<Node, Path> shortestKnownPaths = new HashMap<>();

        Node startNode = graph.getNode(startNodeName);
        Node endNode = graph.getNode(endNodeName);

        Path startPath = new Path(startNode);
        paths.add(startPath); // Add formal zero-length path to start node
        shortestKnownPaths.put(startNode, startPath);

        do {
            Path nextBestPath = paths.get(0); // Look at shortest unvisited path
            paths.remove(nextBestPath); // Make path visited by removing it

            Node node = nextBestPath.getEndPoint();

            for (Node n : node.getNeighbors()) {
                // Generate a new path continuing the current one to this neighbor
                Path p = new Path(n, nextBestPath);
                if (shortestKnownPaths.containsKey(n)) { // Compare this path's distance to known value
                    Path oldShortestPath = shortestKnownPaths.get(n);
                    if (criterion.compare(oldShortestPath, p) <= 0) {
                        continue; // Discard if old path was better
                    }
                    shortestKnownPaths.remove(n);
                    paths.remove(oldShortestPath); // Remove old path if new path is better
                }
                paths.add(p); // Add this path if node is unvisited or path is an improvement
                shortestKnownPaths.put(n, p);
            }
            Collections.sort(paths, criterion);
        } while (paths.get(0).getEndPoint() != endNode);
            // Exit if shortest remaining path is the path to destination

        // Retrieve solution
        Path deFactoShortestPath = shortestKnownPaths.get(endNode);
        return deFactoShortestPath;
    }

    /**
     * Get all paths between two given nodes
     *
     * @param graph the graph to search in
     * @param startNodeName the name of the starting point
     * @param endNodeName the name of the end point
     * @return a list of all paths from the starting point to the end point
     */
    public static ArrayList<Path> findAllUniquePaths(Graph graph, String startNodeName, String endNodeName) {
        ArrayList<Path> paths = new ArrayList<>();

        Node startNode = graph.getNode(startNodeName);
        Node endNode = graph.getNode(endNodeName);

        Path path = new Path(startNode);

        for (Node n : startNode.getNeighbors()) {
            weNeedToGoDeeper(new Path(n, path), endNode, paths);
        }

        return paths;
    }

    private static void weNeedToGoDeeper(Path p, Node endNode, ArrayList<Path> paths) {
        if (p.getEndPoint() == endNode) {
            paths.add(p);
            return;
        }
        for (Node n : p.getEndPoint().getNeighbors()) {
            if (!p.containsNode(n)) {
                weNeedToGoDeeper(new Path(n, p), endNode, paths);
            }
        }
    }
}
