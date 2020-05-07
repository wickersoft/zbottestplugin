/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik.moves;

import java.util.Collection;
import java.util.LinkedList;
import zbottestplugin.Storage;
import zedly.zbot.BlockFace;
import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class MoveLevelWalk extends Move {

    public MoveLevelWalk(int deltaX, int deltaZ) {
        super(deltaX, 0, deltaZ);
    }

    @Override
    public double getCost() {
        return getLength() * 0.8;
    }

    @Override
    public boolean isPossible(Location l) {
        for (double d = 0; d <= 1; d += 0.2 / getLength()) {
            Location minL = l.getRelative(-.3 + d * deltaX, -1, -.3 + d * deltaZ);
            Location maxL = l.getRelative(.3 + d * deltaX, -1, .3 + d * deltaZ);
            int minX = minL.getBlockX();
            int maxX = maxL.getBlockX();
            int minZ = minL.getBlockZ();
            int maxZ = maxL.getBlockZ();
            int y = minL.getBlockY();
            boolean canStand = false;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    
                    if(x == 127 && z == 374) {
                        int k = 8732;
                    }
                    
                    if (Storage.self.getEnvironment().getBlockAt(x, y, z).getType().isWalkable()) {
                        canStand = true;
                    }
                }
            }
            if (!canStand) {
                return false;
            }
        }
        return super.isPossible(l);
    }

    @Override
    public void build(LinkedList<Location> path, double stepSize) {
        Location start = path.get(path.size() - 1);
        double length = getLength();
        for (double d = stepSize / length; d < 1; d += stepSize / length) {
            path.add(start.getRelative(deltaX * d, 0, deltaZ * d));
        }
        path.add(getTarget(start));
    }

    private static void regGeneralizedMove(Collection<Move> DIRECTIONS, int rad, int tan) {
        regMove(DIRECTIONS, rad, tan, BlockFace.NORTH, BlockFace.EAST);
        regMove(DIRECTIONS, rad, tan, BlockFace.EAST, BlockFace.SOUTH);
        regMove(DIRECTIONS, rad, tan, BlockFace.SOUTH, BlockFace.WEST);
        regMove(DIRECTIONS, rad, tan, BlockFace.WEST, BlockFace.NORTH);
        if (tan != 0 && tan != rad) {
            regMove(DIRECTIONS, rad, tan, BlockFace.NORTH, BlockFace.WEST);
            regMove(DIRECTIONS, rad, tan, BlockFace.WEST, BlockFace.SOUTH);
            regMove(DIRECTIONS, rad, tan, BlockFace.SOUTH, BlockFace.EAST);
            regMove(DIRECTIONS, rad, tan, BlockFace.EAST, BlockFace.NORTH);
        }
    }

    private static void regMove(Collection<Move> DIRECTIONS, int rad, int tan, BlockFace radDir, BlockFace tanDir) {
        DIRECTIONS.add(new MoveLevelWalk(rad * radDir.getModX() + tan * tanDir.getModX(), rad * radDir.getModZ() + tan * tanDir.getModZ()));
    }

    public static void registerMoves(Collection<Move> DIRECTIONS) {
        for(int i = 1; i < 5; i++) {
            for(int j = 0; j <= i; j++) {
                regGeneralizedMove(DIRECTIONS, i, j);
            }
        }
    }
}
