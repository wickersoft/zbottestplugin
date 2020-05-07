/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik.moves;

import java.util.ArrayList;
import java.util.LinkedList;
import zbottestplugin.Storage;
import zedly.zbot.BlockFace;
import zedly.zbot.Location;
import zedly.zbot.util.CartesianVector;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public abstract class Move {

    public static final ArrayList<Move> DIRECTIONS = new ArrayList<>();
    protected final double deltaX, deltaZ, deltaY;

    protected Move(int deltaX, int deltaY, int deltaZ) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
    }

    public Vector getDisplacement() {
        return new CartesianVector(deltaX, deltaY, deltaZ);
    }

    public Location getTarget(Location start) {
        return start.getRelative(deltaX, deltaY, deltaZ);
    }

    public boolean isPossible(Location start) {
        LinkedList<Location> trajectory = new LinkedList<>();
        trajectory.add(start);
        build(trajectory, 0.2);

        Location dest = getTarget(start);
        if (!Storage.self.getEnvironment().getBlockAt(dest.getRelative(BlockFace.DOWN.getDirection())).getType().isWalkable()) {
            return false;
        }

        for (Location l : trajectory) {
            // Test for collisions of solid blocks with player AABB,
            // which is 0.6x0.6x1.5 referenced to center of bottom face
            Location minL = l.getRelative(-.3, 0, -.3);
            Location maxL = l.getRelative(.3, 1.5, .3);
            int minX = minL.getBlockX();
            int maxX = maxL.getBlockX();
            int minY = minL.getBlockY();
            int maxY = maxL.getBlockY();
            int minZ = minL.getBlockZ();
            int maxZ = maxL.getBlockZ();

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (Storage.self.getEnvironment().getBlockAt(x, y, z).getType().isSolid()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public abstract double getCost();

    public double getLength() {
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public abstract void build(LinkedList<Location> path, double stepSize);
    
    static {
        MoveSimpleJump.registerMoves(DIRECTIONS);
        MoveShortJump.registerMoves(DIRECTIONS);
        MoveLevelWalk.registerMoves(DIRECTIONS);
        MoveUseLadder.registerMoves(DIRECTIONS);
    }
}
