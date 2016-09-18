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
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;
import zedly.zbot.event.SelfTeleportEvent;

/**
 *
 * @author Dennis
 */
public class ThreadTaskTest extends Thread {

    private final BlockingAI ai = new BlockingAI();
    private final BumpDetector bumpDetector = new BumpDetector();
    private final int xStart = -834;
    private final int zStart = 4822;
    private final int floorHeight = 69;
    private final int xEnd = -819;
    private final int zEnd = 4841;

    private boolean alive = true;
    private int aiTaskId = -1;

    public void run() {
        aiTaskId = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, ai, 100, 100);
        Storage.self.registerEvents(bumpDetector);
        try {

            
            
            unregister();
        } catch (Exception ex) {
            ex.printStackTrace();
            StackTraceElement ste = ex.getStackTrace()[0];
            unregister();
            Storage.self.sendChat("rip thread :(       " + ste.getClassName() + ":" + ste.getLineNumber());
        }
    }

    private void harvest() throws InterruptedException {
        for (int xLoc = xStart; xLoc < xEnd; xLoc += 2) {
            for (int zLoc = zStart; zLoc < zEnd; zLoc++) {
                Location target = new Location(xLoc, floorHeight, zLoc).centerHorizontally();

                GeometricPath path = AStar.getPath(target);
                List<Location> nodes = path.getLocations();
                for (Location node : nodes) {
                    if (node.distanceSquareTo(target) < 1) {
                        break;
                    }
                    ai.moveTo(node);
                }

                if (Storage.self.getEnvironment().getBlockAt(target).getTypeId() == 17) {
                    harvestTree(target);
                } else {
                    clearLeaves(target);
                }
            }
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
            ai.breakBlock(logLocation, 600);
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

    private void unregister() {
        Storage.self.cancelTask(aiTaskId);
        Storage.self.unregisterEvents(bumpDetector);
    }

    private synchronized boolean isRunning() {
        return alive;
    }

    public synchronized void kill() {
        alive = false;
    }

    private class BumpDetector implements Listener {

        private boolean bumped = false;

        @EventHandler
        public synchronized void onTeleport(SelfTeleportEvent evt) {
            bumped = true;
        }

        public synchronized boolean hasBumped() {
            if (bumped) {
                bumped = false;
                return true;
            }
            return false;
        }

    }
}
