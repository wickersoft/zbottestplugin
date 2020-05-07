/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik;

import edu.kit.informatik.moves.Move;
import java.util.LinkedList;
import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class PathTree {

    private final Move move;
    private final double cost;
    private final double length;
    private final PathTree parent;
    private final Location target;
    private int moveDirectionsIndex = 0;

    public PathTree(PathTree parent, Move move) {
        this.parent = parent;
        this.move = move;
        this.cost = parent.getCost() + move.getCost();
        this.length = parent.getLength() + move.getLength();
        this.target = parent.target.getRelative(move.getDisplacement());
    }

    public PathTree(Location origin) {
        this.parent = null;
        this.move = null;
        this.cost = 0;
        this.length = 0;
        this.target = origin;
    }

    public LinkedList<Move> build() {
        if (parent == null) {
            return new LinkedList<>();
        }
        LinkedList<Move> moves = parent.build();
        moves.add(move);
        return moves;
    }

    public Location getEndPoint() {
        return target;
    }

    public double getCost() {
        return cost;
    }
    
    public double getHeuristic(Location target) {
        return getCost() + getEndPoint().distanceTo(target);
    }
    
    // Return the best heuristic value the next move could possibly have
    public double getBestNextCost(Location target) {
        Move move = Move.DIRECTIONS.get(moveDirectionsIndex);
        return cost + move.getCost() + getEndPoint().distanceTo(target) - move.getLength();
    }

    // Return the best heuristic value the next move could possibly have
    public double getWorstNextCost(Location target) {
        Move move = Move.DIRECTIONS.get(moveDirectionsIndex);
        return cost + move.getCost() + getEndPoint().distanceTo(target) + move.getLength();
    }
    
    public double getLength() {
        return length;
    }
    
    public Move tryAMove() {
        if(moveDirectionsIndex == Move.DIRECTIONS.size()) {
            return null;
        }
        return Move.DIRECTIONS.get(moveDirectionsIndex++);
    }

}
