/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik;

import edu.kit.informatik.moves.Move;
import java.util.PriorityQueue;
import java.util.HashMap;
import zbottestplugin.Storage;
import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class AStar2 {
    private static final int MAX_PATH_WEIGHT = 100;
    private static final int PARTIAL_PATH_THRESHOLD = 200;
    private static PriorityQueue<PathTree> searchPerimeter;
    private static final HashMap<Long, PathTree> BEST_KNOWN_SCORES = new HashMap<>();

    public static final PathTree getPath(Location target, boolean acceptPartialPath) {
        // Init
        searchPerimeter = new PriorityQueue<>((a, b) -> {
            double h = a.getHeuristic(target) - b.getHeuristic(target);
            if (h >= 0) {
                return 1;
            } else {
                return -1;
            }
        });
        BEST_KNOWN_SCORES.clear();
        Location myLocation = Storage.self.getLocation().centerHorizontally();

        // Satisfy loop invariants
        PathTree root = new PathTree(myLocation);
        searchPerimeter.add(root);
        BEST_KNOWN_SCORES.put(myLocation.toLong(), root);

        long consCounter = 0;

        //Search around path with best heuristic until target is reached or length exceeded
        while (true) {
            PathTree bestPath = searchPerimeter.poll();
            if (bestPath == null) {
                return null;
            }

            Location l = bestPath.getEndPoint();
            if (l.distanceTo(target) < 1
                    || (acceptPartialPath && bestPath.getCost() > PARTIAL_PATH_THRESHOLD && l.distanceTo(target) < myLocation.distanceTo(target))) {
                consCounter++;
                return bestPath;
            } else if (bestPath.getCost() > MAX_PATH_WEIGHT) {
                consCounter++;
                return null;
            }

            for (Move move : Move.DIRECTIONS) {
                PathTree testTree = new PathTree(bestPath, move);
                long locLong = testTree.getEndPoint().toLong();
                PathTree oldTree = BEST_KNOWN_SCORES.get(locLong);
                if ((oldTree == null || oldTree.getHeuristic(target) > testTree.getHeuristic(target)) && move.isPossible(l)) {
                    searchPerimeter.remove(oldTree);
                    searchPerimeter.add(testTree);
                    consCounter++;
                    BEST_KNOWN_SCORES.put(locLong, testTree);
                }
            }
        }
    }
}
