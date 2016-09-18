/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import edu.kit.informatik.AStar;
import edu.kit.informatik.GeometricPath;
import java.util.List;
import zedly.zbot.Location;
import zedly.zbot.block.Material;

/**
 *
 * @author Dennis
 */
public class ThreadTaskTest extends Thread {

    private final BlockingAI ai = new BlockingAI();
    private boolean alive = true;

    private final int xStart = -834;
    private final int zStart = 4824;
    private final int xEnd = -819;
    private final int zEnd = 4841;

    public void run() {
        Storage.self.scheduleSyncRepeatingTask(Storage.plugin, ai, 100, 100);
        try {
            for (int xLoc = xStart; xLoc < xEnd; xLoc++) {
                for (int zLoc = zStart; zLoc < zEnd; zLoc++) {
                    Location target = new Location(xLoc, 69, zLoc).centerHorizontally();
                    
                    GeometricPath path = AStar.getPath(target);
                    List<Location> nodes = path.getLocations();
                    for (Location node : nodes) {
                        ai.moveTo(node);
                    }
                    for (int i = 0; i < 19; i++) {
                        Location loc = new Location(xLoc + xOff, 69, 4823 + i).centerHorizontally();
                        if (Storage.self.getEnvironment().getBlockAt(loc).getTypeId() == 17) {
                            harvestTree(loc);
                        } else {
                            clearLeaves(loc);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            StackTraceElement ste = ex.getStackTrace()[0];
            Storage.self.sendChat("rip thread :(       " + ste.getClassName() + ":" + ste.getLineNumber());
        }
    }

    private void harvestTree(Location treeLocation) throws InterruptedException {
        InventoryUtil.findAndSelect(Material.IRON_AXE);
        ai.tick();
        for (int i = 0; i < 5; i++) {
            Location logLocation = treeLocation.getRelative(0, i, 0);
            if (i == 2) {
                Storage.self.moveTo(treeLocation);
                ai.tick();
                if (Storage.self.getEnvironment().getBlockAt(logLocation).getTypeId() != 17) {
                    return;
                }
            }
            ai.breakBlock(logLocation, 500);
            for (int j = 0; j < 5; j++) {
                ai.tick();
            }

        }
    }

    private void clearLeaves(Location treeLocation) throws InterruptedException {
        Storage.self.selectSlot(0);
        ai.tick();
        for (int i = 0; i < 2; i++) {
            Location leafLocation = treeLocation.getRelative(0, i, 0);
            if (Storage.self.getEnvironment().getBlockAt(leafLocation).getTypeId() == Material.LEAVES.getTypeId()) {
                ai.breakBlock(leafLocation, 1000);
                for (int j = 0; j < 5; j++) {
                    ai.tick();
                }
            }
        }
        Storage.self.moveTo(treeLocation);
        ai.tick();
    }

    private synchronized boolean isRunning() {
        return alive;
    }

    public synchronized void kill() {
        alive = false;
    }
}
