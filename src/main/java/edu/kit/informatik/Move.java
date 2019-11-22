/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import zbottestplugin.Storage;
import zedly.zbot.Location;
import zedly.zbot.Material;
import zedly.zbot.util.CartesianVector;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class Move {

    public static final Collection<Move> DIRECTIONS = new ArrayList<>(); 
    
    private static final double MAX_JUMP_UP_DISTANCE = 2.0;
    private static final double MAX_JUMP_LEVEL_DISTANCE = 2.0 / 3.0 * Math.sqrt(13);
    private static final double MAX_JUMP_DOWN_DISTANCE = Math.sqrt(10);

    private final CartesianVector relativeTarget;
    private final List<Vector> requiredSpace;
    private final BiFunction<Double, Double, Double> interpolator;

    private Move(CartesianVector vector, List<Vector> requiredSpace, BiFunction<Double, Double, Double> builder) {
        this.relativeTarget = vector;
        this.requiredSpace = requiredSpace;
        this.interpolator = builder;
    }

    public Vector getDisplacement() {
        return relativeTarget;
    }

    public Location getTarget(Location start) {
        return start.getRelative(relativeTarget);
    }

    public boolean isPossible(Location start) {
        for (Vector v : requiredSpace) {
            if (!Storage.self.getEnvironment().getBlockAt(start.getRelative(v)).isSolid()) {
                return false;
            }
        }
        return true;
    }

    public double getCost() {
        return relativeTarget.getHorizontalLength();
    }

    public void build(List<Location> path, double stepSize) {
        Location start = path.get(path.size() - 1);
        CartesianVector dir = new CartesianVector(relativeTarget.getX(), 0, relativeTarget.getZ()).normalize().toCartesian();
        double cost = relativeTarget.getHorizontalLength();
        double fracSteps = (cost / stepSize);
        int steps = (int) fracSteps;
        if (fracSteps - steps < stepSize / 2) {
            steps--;
        }

        for (double d = 0; d < cost; d += stepSize) {
            path.add(start.getRelative(dir.getX() * d, interpolator.apply(d, cost), dir.getZ() * d));
        }
        path.add(getTarget(start));
    }

    private static final boolean isBlockWalkable(int x, int y, int z) {
        if (Storage.self.getEnvironment().getBlockAt(x, y, z).getType() == Material.LADDER) {
            return true;
        }
        return !isBlockFree(x, y, z);// && !FORBIDDEN_BLOCKS.contains(Storage.self.getEnvironment().getBlockAt(x, y, z).getTypeId());
    }

    private static final boolean isBlockFree(int x, int y, int z) {
        return !Storage.self.getEnvironment().getBlockAt(x, y, z).getType().isSolid(); //NONSOLID_BLOCKS.contains(Storage.self.getEnvironment().getBlockAt(x, y, z).getTypeId());
    }

    private static double jumpUp(double x, double t) {
        return max(max(min((x - (max(0, t - MAX_JUMP_UP_DISTANCE) / 2)) / min(t, MAX_JUMP_UP_DISTANCE), 1), 0), ((x - (max(0, t - MAX_JUMP_UP_DISTANCE) / 2)) / min(t, MAX_JUMP_UP_DISTANCE)) + (1.2 / 0.5 / min(t, MAX_JUMP_UP_DISTANCE) * min(t, MAX_JUMP_UP_DISTANCE)) * (x - (max(0, t - MAX_JUMP_UP_DISTANCE) / 2)) * (min(t, MAX_JUMP_UP_DISTANCE) - (x - (max(0, t - MAX_JUMP_UP_DISTANCE) / 2))));
    }

    private static double jumpDown(double x, double t) {
        return max(max(min(((max(0, t - MAX_JUMP_UP_DISTANCE) / 2) - x) / min(t, MAX_JUMP_UP_DISTANCE), 0), -1), (((max(0, t - MAX_JUMP_UP_DISTANCE) / 2) - x) / min(t, MAX_JUMP_UP_DISTANCE)) + (3.2 / 0.5 / min(t, MAX_JUMP_UP_DISTANCE) * min(t, MAX_JUMP_UP_DISTANCE)) * (x - (max(0, t - MAX_JUMP_UP_DISTANCE) / 2)) * (min(t, MAX_JUMP_UP_DISTANCE) - (x - (max(0, t - MAX_JUMP_UP_DISTANCE) / 2))));
    }

    private static double jumpLevel(double x, double t) {
        return max(0, (2.2 / 0.5 / min(t, MAX_JUMP_UP_DISTANCE) * min(t, MAX_JUMP_UP_DISTANCE)) * (x - (max(0, t - MAX_JUMP_UP_DISTANCE) / 2)) * (min(t, MAX_JUMP_UP_DISTANCE) - (x - (max(0, t - MAX_JUMP_UP_DISTANCE) / 2))));
    }

    private static double fallDown(double x, double t) {
        return 0;
    }

    private static double walkLevel(double x, double t) {
        return 0;
    }

    private static double max(double a, double b) {
        return Math.max(a, b);
    }

    private static double min(double a, double b) {
        return Math.min(a, b);
    }
    
    static {
        //DIRECTIONS.add(new Move(new CartesianVector(1, 0, 0), ))
        
        
        
        
    }
    
    
    
}
