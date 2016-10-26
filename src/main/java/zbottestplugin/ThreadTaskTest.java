/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import edu.kit.informatik.AStar;
import edu.kit.informatik.GeometricPath;
import java.util.LinkedList;
import java.util.List;
import zedly.zbot.EntityType;
import zedly.zbot.Location;
import zedly.zbot.block.Material;
import zedly.zbot.entity.Entity;
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;
import zedly.zbot.event.SelfTeleportEvent;
import zedly.zbot.self.Self;

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
    private final Self self = Storage.self;

    private final Location stopLocation = new Location(-837, 69, 4822);
    private final Location axeTesseractLocation = new Location(-837, 69, 4825);
    private final Location saplingTesseractLocation = new Location(-837, 69, 4828);
    private final Location logTesseractLocation = new Location(-837, 69, 4825);

    private boolean alive = true;
    private int aiTaskId = -1;

    public void run() {
        aiTaskId = self.scheduleSyncRepeatingTask(Storage.plugin, ai, 100, 100);
        self.registerEvents(bumpDetector);
        int rowCount = 0;
        try {
            int requiredAxes = 4 - InventoryUtil.count(Material.IRON_AXE);
            if (requiredAxes >= 1) {
                ai.moveTo(axeTesseractLocation);
                self.sneak(true);
                ai.tick();
                for (int i = 0; i < requiredAxes; i++) {
                    ai.clickBlock(axeTesseractLocation);
                    ai.tick();
                }
                self.sneak(false);
                ai.tick();
            }
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

                    if (self.getEnvironment().getBlockAt(target).getTypeId() == 17) {
                        harvestTree(target);
                    } else {
                        clearLeaves(target);
                    }
                }
                if (++rowCount % 2 == 0) {
                    collectItems();
                }
            }

            ai.tick(1200);
            collectItems();
            ai.tick(1200);
            collectItems();

            replant();

            ai.moveTo(saplingTesseractLocation);
            InventoryUtil.findAndSelect(Material.SAPLING);
            ai.tick();
            self.placeBlock(saplingTesseractLocation);
            ai.tick();
            self.placeBlock(saplingTesseractLocation);

            ai.moveTo(logTesseractLocation);
            InventoryUtil.findAndSelect(Material.LOG);
            ai.tick();
            self.placeBlock(logTesseractLocation.getRelative(0, 1, 0));
            ai.tick();
            self.placeBlock(logTesseractLocation.getRelative(0, 1, 0));

            ai.moveTo(stopLocation);
            unregister();
        } catch (Exception ex) {
            ex.printStackTrace();
            StackTraceElement ste = ex.getStackTrace()[0];
            unregister();
            self.sendChat("rip thread :(       " + ste.getClassName() + ":" + ste.getLineNumber());
        }
    }

    private void replant() throws InterruptedException {
        ai.moveTo(saplingTesseractLocation);
        for (int i = 0; i < 5; i++) {
            ai.clickBlock(saplingTesseractLocation);
            ai.tick();
        }
        ai.tick(10);

        int numberOfSaplings = InventoryUtil.count(Material.SAPLING, (short) 0) + 1;

        int sparsity = (int) Math.ceil(17.0 * 8.0 / (double) numberOfSaplings);
        System.out.println("Sparsity " + sparsity + " / " + numberOfSaplings + " saplings");
        for (int xLoc = xStart; xLoc < xEnd; xLoc += 2) {
            for (int zLoc = zStart; zLoc < zEnd; zLoc += sparsity) {
                Location target = new Location(xLoc, floorHeight, zLoc).centerHorizontally();
                ai.moveTo(target);
                InventoryUtil.findAndSelect(Material.SAPLING);
                ai.tick();
                self.placeBlock(target.getRelative(0, -1, 0));
            }
        }
        InventoryUtil.findAndSelect(Material.SAPLING);
    }

    private void collectItems() throws InterruptedException {
        boolean foundCollectableItem;
        do {
            LinkedList<Location> nearbyItems = new LinkedList<>();
            for (Entity ent : self.getEnvironment().getEntities()) {
                if (ent.getType() == EntityType.ITEM
                        && ent.getLocation().distanceSquareTo(self.getLocation()) < 600) {
                    nearbyItems.add(ent.getLocation());
                }
            }
            foundCollectableItem = false;
            for (Location loc : nearbyItems) {
                if (loc.getY() > floorHeight - 1 && loc.getY() < floorHeight + 2) {
                    GeometricPath path = AStar.getPath(loc);
                    if (path != null) {
                        foundCollectableItem = true;
                        ai.followPath(path);
                        break;
                    }
                }
            }

        } while (foundCollectableItem);
    }

    private void harvestTree(Location treeLocation) throws InterruptedException {
        InventoryUtil.findAndSelect(Material.IRON_AXE);
        ai.tick();
        for (int i = 0; i < 5; i++) {
            Location logLocation = treeLocation.getRelative(0, i, 0);
            if (i == 2) {
                self.moveTo(treeLocation);
                ai.tick();
                if (self.getEnvironment().getBlockAt(logLocation).getTypeId() != 17) {
                    return;
                }
            }
            ai.breakBlock(logLocation, 600);
            ai.tick(5);

        }
    }

    private void clearLeaves(Location treeLocation) throws InterruptedException {
        self.selectSlot(0);
        ai.tick();
        for (int i = 0; i < 2; i++) {
            Location leafLocation = treeLocation.getRelative(0, i, 0);
            if (self.getEnvironment().getBlockAt(leafLocation).getTypeId() != 0) {
                ai.breakBlock(leafLocation, 1000);
                ai.tick(5);
            }
        }
        self.moveTo(treeLocation);
        ai.tick();
    }

    private void unregister() {
        self.cancelTask(aiTaskId);
        self.unregisterEvents(bumpDetector);
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
