/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik.moves;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;
import zedly.zbot.BlockFace;
import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class MoveShortJump extends Move {

    private static final Function<Double, Double> jumpUpParabola = (x) -> {
        return x < 0 ? 0 : x > 1 ? 1 : 2.61803398874989 * x * (1 - x) + x;
    };

    private final Function<Double, Double> yFunction;

    private MoveShortJump(int deltaX, int deltaZ) {
        super(deltaX, 1, deltaZ);
        this.yFunction = buildYFunc(deltaX, deltaZ);
    }

    @Override
    public double getCost() {
        double c = getLength();
        if (deltaY == 0) {
            c -= 0.2;
        }
        if (Math.abs(deltaX) <= 1 && Math.abs(deltaZ) <= 1) {
            c -= 0.2;
        }
        return c;
    }

    @Override
    public void build(LinkedList<Location> path, double stepSize) {
        Location start = path.get(path.size() - 1);
        double length = getLength();
        for (double d = stepSize / length; d < 1; d += stepSize / length) {
            path.add(start.getRelative(deltaX * d, yFunction.apply(d), deltaZ * d));
        }
        path.add(getTarget(start));
    }

    private static Function<Double, Double> buildYFunc(int dX, int dZ) {
        return buildJumpFunc(jumpUpParabola, Math.sqrt(dX * dX + dZ * dZ));
    }

    private static Function<Double, Double> buildJumpFunc(Function<Double, Double> interpolation, double moveLen) {
        return (x) -> {
            return interpolation.apply(1.8 * x);
        };
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
        DIRECTIONS.add(new MoveShortJump(rad * radDir.getModX() + tan * tanDir.getModX(), rad * radDir.getModZ() + tan * tanDir.getModZ()));
    }

    public static void registerMoves(Collection<Move> DIRECTIONS) {
        regGeneralizedMove(DIRECTIONS, 1, 0);
        regGeneralizedMove(DIRECTIONS, 1, 1);
    }
}
