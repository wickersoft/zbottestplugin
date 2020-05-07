/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.informatik.moves;

import static edu.kit.informatik.moves.Move.DIRECTIONS;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Function;
import zedly.zbot.BlockFace;
import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class MoveSimpleJump extends Move {

    private static final double MAX_JUMP_UP_DISTANCE = 2.0;
    private static final double MAX_JUMP_LEVEL_DISTANCE = 2.0 / 3.0 * Math.sqrt(13);
    private static final double MAX_JUMP_DOWN_DISTANCE = Math.sqrt(10);
    private static final Function<Double, Double> jumpUpParabola = (x) -> {
        return x < 0 ? 0 : x > 1 ? 1 : 2.61803398874989 * x * (1 - x) + x;
    };
    private static final Function<Double, Double> jumpLevelParabola = (x) -> {
        return x < 0 ? 0 : x > 1 ? 0 : 5 * x * (1 - x);
    };
    private static final Function<Double, Double> jumpDownParabola = (x) -> {
        return x < 0 ? 0 : x > 1 ? -1 : 6.85410196624968 * x * (1 - x) - x;
    };

    private final Function<Double, Double> yFunction;

    private MoveSimpleJump(int deltaX, int deltaY, int deltaZ) {
        super(deltaX, deltaY, deltaZ);
        this.yFunction = buildYFunc(deltaX, deltaY, deltaZ);
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

    private static Function<Double, Double> buildYFunc(int dX, int dY, int dZ) {
        switch (dY) {
            case -1:
                if (Math.abs(dX) <= 1 && Math.abs(dZ) <= 1) {
                    return (x) -> { // Fall down one block without jumping
                        return x <= 0.5 ? 1.0 : 4 * x * (1 - x);
                    };
                }
                return buildJumpFunc(jumpDownParabola, Math.sqrt(dX * dX + dZ * dZ), MAX_JUMP_DOWN_DISTANCE);
            case 0:
                return buildJumpFunc(jumpLevelParabola, Math.sqrt(dX * dX + dZ * dZ), MAX_JUMP_LEVEL_DISTANCE);
            case 1:
                return buildJumpFunc(jumpUpParabola, Math.sqrt(dX * dX + dZ * dZ), MAX_JUMP_UP_DISTANCE);
        }
        return null;
    }

    private static Function<Double, Double> buildJumpFunc(Function<Double, Double> interpolation, double moveLen, double maxAirDist) {
        return (x) -> {
            double scale = Math.max(1, moveLen / maxAirDist);
            double offset = 0.5 * (scale - 1);
            return interpolation.apply(scale * x - (offset / scale));
        };
    }

    private static void regGeneralizedMove(Collection<Move> DIRECTIONS, int rad, int alt, int tan) {
        regMove(DIRECTIONS, rad, tan, alt, BlockFace.NORTH, BlockFace.EAST);
        regMove(DIRECTIONS, rad, tan, alt, BlockFace.EAST, BlockFace.SOUTH);
        regMove(DIRECTIONS, rad, tan, alt, BlockFace.SOUTH, BlockFace.WEST);
        regMove(DIRECTIONS, rad, tan, alt, BlockFace.WEST, BlockFace.NORTH);
        if (tan != 0 && tan != rad) {
            regMove(DIRECTIONS, rad, tan, alt, BlockFace.NORTH, BlockFace.WEST);
            regMove(DIRECTIONS, rad, tan, alt, BlockFace.WEST, BlockFace.SOUTH);
            regMove(DIRECTIONS, rad, tan, alt, BlockFace.SOUTH, BlockFace.EAST);
            regMove(DIRECTIONS, rad, tan, alt, BlockFace.EAST, BlockFace.NORTH);
        }
    }

    private static void regMove(Collection<Move> DIRECTIONS, int rad, int tan, int alt, BlockFace radDir, BlockFace tanDir) {
        DIRECTIONS.add(new MoveSimpleJump(rad * radDir.getModX() + tan * tanDir.getModX(), alt, rad * radDir.getModZ() + tan * tanDir.getModZ()));
    }

    public static void registerMoves(Collection<Move> DIRECTIONS) {
        regGeneralizedMove(DIRECTIONS, 2, 1, 0);
        regGeneralizedMove(DIRECTIONS, 2, 1, 1);
        regGeneralizedMove(DIRECTIONS, 2, 1, 2);
        regGeneralizedMove(DIRECTIONS, 3, 1, 0);

        regGeneralizedMove(DIRECTIONS, 2, 0, 0);
        regGeneralizedMove(DIRECTIONS, 2, 0, 1);
        regGeneralizedMove(DIRECTIONS, 2, 0, 2);
        regGeneralizedMove(DIRECTIONS, 3, 0, 0);
        regGeneralizedMove(DIRECTIONS, 3, 0, 1);
        regGeneralizedMove(DIRECTIONS, 3, 0, 2);

        regGeneralizedMove(DIRECTIONS, 1, -1, 0);
        regGeneralizedMove(DIRECTIONS, 1, -1, 1);
        regGeneralizedMove(DIRECTIONS, 2, -1, 0);
        regGeneralizedMove(DIRECTIONS, 2, -1, 1);
        regGeneralizedMove(DIRECTIONS, 2, -1, 2);

        regGeneralizedMove(DIRECTIONS, 2, -1, 0);
        regGeneralizedMove(DIRECTIONS, 2, -1, 1);
        regGeneralizedMove(DIRECTIONS, 2, -1, 2);
        regGeneralizedMove(DIRECTIONS, 2, -1, 3);
        regGeneralizedMove(DIRECTIONS, 3, -1, 0);
        regGeneralizedMove(DIRECTIONS, 3, -1, 1);
        regGeneralizedMove(DIRECTIONS, 3, -1, 2);
        regGeneralizedMove(DIRECTIONS, 3, -1, 3);
    }
}
