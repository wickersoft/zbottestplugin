/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class Menger {

    // North-Western corner
    // Cube expands towards +X and +Z
    private static final Location ROOT_BLOCK = new Location(0, 0, 0);

    
    public static boolean isBlock(Location loc) {
        int x = loc.getBlockX() - ROOT_BLOCK.getBlockX();
        int y = loc.getBlockY() - ROOT_BLOCK.getBlockY();
        int z = loc.getBlockZ() - ROOT_BLOCK.getBlockZ();
        return isAbstractBlock(x, y, z);
    }
    
    public static boolean isBlock(int x, int y, int z) {
        return isAbstractBlock(x - ROOT_BLOCK.getBlockX(), y - ROOT_BLOCK.getBlockY(), z - ROOT_BLOCK.getBlockZ());
    }

    private static boolean isAbstractBlock(int x, int y, int z) {
        return isPlaneBlock(x, y) && isPlaneBlock(x, z) && isPlaneBlock(y, z);
    }

    private static boolean isPlaneBlock(int x, int z) {
        if (x == 0 && z == 0) {
            return true;
        } else if (x % 3 == 1 && z % 3 == 1) {
            return false;
        } else {
            return isPlaneBlock(x / 3, z / 3);
        }
    }
}
