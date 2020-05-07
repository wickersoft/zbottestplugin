/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik.moves;

import java.util.Collection;
import java.util.LinkedList;
import zbottestplugin.Storage;
import zedly.zbot.Location;
import zedly.zbot.Material;

/**
 *
 * @author Dennis
 */
public class MoveUseLadder extends Move {

    public MoveUseLadder(int deltaY) {
        super(0, deltaY, 0);
    }

    @Override
    public double getCost() {
        return 1.0;
    }

    @Override
    public boolean isPossible(Location origin) {
        if (deltaY == -1) {
            return (Storage.self.getEnvironment().getBlockAt(origin).getType() == Material.LADDER
                    || Storage.self.getEnvironment().getBlockAt(origin.getRelative(0, deltaY, 0)).getType() == Material.LADDER)
                    && !Storage.self.getEnvironment().getBlockAt(origin.getRelative(0, deltaY, 0)).getType().isSolid();
        } else if(deltaY == 1) {
            return (Storage.self.getEnvironment().getBlockAt(origin).getType() == Material.LADDER
                    || Storage.self.getEnvironment().getBlockAt(origin.getRelative(0, deltaY, 0)).getType() == Material.LADDER)
                    && !Storage.self.getEnvironment().getBlockAt(origin.getRelative(0, deltaY + 1, 0)).getType().isSolid();
        }
        return false;
    }

    @Override
    public void build(LinkedList<Location> path, double stepSize) {
        Location start = path.get(path.size() - 1);
        for (double d = 0; d < 1; d += stepSize) {
            path.add(start.getRelative(0, d * deltaY, 0));
        }
        path.add(getTarget(start));
    }

    public static void registerMoves(Collection<Move> DIRECTIONS) {
        DIRECTIONS.add(new MoveUseLadder(-1));
        DIRECTIONS.add(new MoveUseLadder(1));
    }

}
