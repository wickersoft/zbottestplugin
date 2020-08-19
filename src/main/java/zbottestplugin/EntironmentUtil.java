/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Predicate;
import zedly.zbot.BlockFace;
import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class EntironmentUtil {

    public static Location BFSScan(Predicate<Location> returnPred, Predicate<Location> searchPred, int x, int y, int z, int viewrange) {
        Location origin = new Location(x, y, z);
        return BFSScan(returnPred, searchPred, origin, viewrange);
    }

    public static Location BFSScan(Predicate<Location> returnPred, Predicate<Location> searchPred, Location origin, int viewrange) {
        HashSet<Location> searched_blocks = new HashSet<>();
        final BlockFace[] searchdirections = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

        LinkedList<Location> scanborder = new LinkedList<>();

        searched_blocks.add(origin);
        scanborder.add(origin);

        while (!scanborder.isEmpty()) {
            Location loc = scanborder.removeFirst();

            if (returnPred.test(loc)) {
                return loc;
            }
            for (BlockFace face : searchdirections) {
                Location relatives = loc.getRelative(face.getDirection());
                if (searched_blocks.contains(relatives)) {
                    continue;
                }
                if (relatives.distanceTo(origin) > viewrange) {
                    continue;
                }
                if (!searchPred.test(relatives)) {
                    continue;
                }

                searched_blocks.add(relatives);
                scanborder.add(relatives);
            }
        }
        return null;
    }

}
