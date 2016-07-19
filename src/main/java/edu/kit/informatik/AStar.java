/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik;

import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.HashSet;
import zbottestplugin.Storage;
import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class AStar {

    private static final HashSet<Integer> NONSOLID_BLOCKS;
    private static final int MAX_PATH_WEIGHT = 1000;
    private static PriorityQueue<GeometricPath> searchPerimeter;
    private static HashSet<Long> visited;
    private static int runtimeCounter = 0;

    private static final int[][] RELATIVE_VECTORS = {
        {1, 0}, {0, 1}, {-1, 0}, {0, -1}
    };

    public static final GeometricPath getPath(Location target) {
        runtimeCounter = 0;
        EuclideanHeuristic heuristic = new EuclideanHeuristic(target);
        searchPerimeter = new PriorityQueue<>(heuristic);
        visited = new HashSet<>();
        Location myLocation = Storage.self.getLocation().centerHorizontally();

        GeometricPath root = new GeometricPath(myLocation);
        searchPerimeter.add(root);
        visited.add(Storage.self.getLocation().toLong());

        //Search around path with best heuristic until target is reached or length exceeded
        while (true) {
            GeometricPath bestPath = searchPerimeter.poll();
            if (bestPath == null) {
                return null;
            }
            //System.out.println(bestPath.getEndPoint());
            Location l = bestPath.getEndPoint();
            if (l.distanceTo(target) < 1) {
                return new GeometricPath(target, bestPath);
            } else if (bestPath.getLength() > MAX_PATH_WEIGHT) {
                return null;
            }
            runtimeCounter++;
            int x = l.getBlockX();
            int y = l.getBlockY();
            int z = l.getBlockZ();
            for (int i = 0; i < 4; i++) {
                int dx = RELATIVE_VECTORS[i][0];
                int dz = RELATIVE_VECTORS[i][1];
                if (!isBlockFree(x + dx, y + 1, z + dz)) {
                    /*
                    Block in front of face is not empty or path has been visited.
                    No possible paths in this direction
                     */
                    continue;
                }

                if (isBlockFree(x + dx, y, z + dz)) {
                    /*
                    Blocks in front of face and feet are free. Consider some paths
                     */
                    if (!isVisited(x + dx, y, z + dz) && !isBlockFree(x + dx, y - 1, z + dz)) {
                        /*
                        Terrain ahead has solid ground. Consider this cardinal direction
                         */
                        searchPerimeter.add(new GeometricPath(l.getRelative(dx, 0, dz).centerHorizontally(), bestPath));
                        markVisited(x + dx, y, z + dz);
                    } else if (!isVisited(x + dx, y - 1, z + dz)
                            && isBlockFree(x + dx, y - 1, z + dz)
                            && !isBlockFree(x + dx, y - 2, z + dz)) {
                        /*
                        Terrain in this direction drops exactly one block. Consider a step down
                         */
                        GeometricPath temp = new GeometricPath(l.getRelative(dx, 0, dz).centerHorizontally(), bestPath);
                        searchPerimeter.add(new GeometricPath(l.getRelative(dx, -1, dz).centerHorizontally(), temp));
                        markVisited(x + dx, y - 1, z + dz);
                    }

                    int ddx = RELATIVE_VECTORS[(i + 1) % 4][0];
                    int ddz = RELATIVE_VECTORS[(i + 1) % 4][1];
                    if (!isVisited(x + dx + ddx, y, z + dz + ddz)
                            && isBlockFree(x + dx + ddx, y + 1, z + dz + ddz)
                            && isBlockFree(x + ddx, y + 1, z + ddz)
                            && isBlockFree(x + dx + ddx, y, z + dz + ddz)
                            && isBlockFree(x + ddx, y, z + ddz)
                            && !isBlockFree(x + dx + ddx, y - 1, z + dz + ddz)) {
                        /*
                        Diagonal path is free.
                         */
                        searchPerimeter.add(new GeometricPath(l.getRelative(dx + ddx, 0, dz + ddz).centerHorizontally(), bestPath));
                        markVisited(x + dx + ddx, y, z + dz + ddz);
                    }
                } else if (!isVisited(x + dx, y + 1, z + dz)
                        && isBlockFree(x, y + 2, z)
                        && isBlockFree(x + dx, y + 2, z + dx)) {
                    /*
                    Terrain ahead goes up by one block. Consider a step up
                     */
                    GeometricPath temp = new GeometricPath(l.getRelative(0, 1, 0).centerHorizontally(), bestPath);
                    searchPerimeter.add(new GeometricPath(l.getRelative(dx, 1, dz).centerHorizontally(), temp));
                    markVisited(x + dx, y + 1, z + dz);
                }
            }
        }
    }
    
    public static final int getRuntimeCounter() {
        return runtimeCounter;
    }

    private static final void consider(int x, int y, int z, GeometricPath pathToExtend) {
        markVisited(x, y, z);
        searchPerimeter.add(new GeometricPath(new Location(x, y, z).centerHorizontally(), pathToExtend));
    }

    private static final boolean isBlockFree(int x, int y, int z) {
        return NONSOLID_BLOCKS.contains(Storage.self.getEnvironment().getBlockAt(x, y, z).getTypeId());
    }

    private static final void resetVisited() {
        visited.clear();
    }

    private static final void markVisited(int x, int y, int z) {
        visited.add(posToLong(x, y, z));
    }

    private static final boolean isVisited(int x, int y, int z) {
        return visited.contains(posToLong(x, y, z));
    }

    private static final long posToLong(int x, int y, int z) {
        return ((long) x & 0x3FFFFFF) << 38 | (((long) y & 0xFFF) << 26) | ((long) z & 0x3FFFFFF);
    }

    private static final class EuclideanHeuristic implements Comparator<GeometricPath> {

        private final Location targetLoc;
        private final double aggressiveness = 2;
        
        public EuclideanHeuristic(Location targetLoc) {
            this.targetLoc = targetLoc;
        }

        public int compare(GeometricPath a, GeometricPath b) {
            double h = aggressiveness * a.getEndPoint().distanceTo(targetLoc)
                    + a.getLength()
                    - aggressiveness * b.getEndPoint().distanceTo(targetLoc)
                    - b.getLength();
            if (h >= 0) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    static {
        NONSOLID_BLOCKS = new HashSet<>();
        NONSOLID_BLOCKS.add(0);
        NONSOLID_BLOCKS.add(6);
        NONSOLID_BLOCKS.add(31);
        NONSOLID_BLOCKS.add(37);
        NONSOLID_BLOCKS.add(38);
        NONSOLID_BLOCKS.add(50);
        NONSOLID_BLOCKS.add(175);
    }
}
