/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.ArrayList;
import java.util.Collections;
import zedly.zbot.Location;
import org.bukkit.Material;
import zedly.zbot.entity.Entity;
import zedly.zbot.entity.Sheep;
import zedly.zbot.environment.BlockFace;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class TaskInfiniShear extends Thread {

    private final ArrayList<Sheep> shearableSheep = new ArrayList<>();
    private final BlockingAI ai = new BlockingAI();
    private int aiTaskId;
    private final Location deliverLoc;
    private final Location[] tesseracts;
    private boolean enabled = true;

    public TaskInfiniShear(Location deliverLoc, Location... tesseracts) {
        this.deliverLoc = deliverLoc;
        this.tesseracts = tesseracts;
    }

    @Override
    public void run() {
        aiTaskId = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, ai, 100, 100);
        try {
            while (enabled()) {
                boolean sort = true;

                // Find nearby sheep
                for (Entity e : Storage.self.getEnvironment().getEntities()) {
                    if (e instanceof Sheep) {
                        shearableSheep.add((Sheep) e);
                    }
                }
                if (shearableSheep.isEmpty()) {
                    Storage.self.sendChat("Can't see any targets here");
                    unregister();
                    return;
                }

                ai.tick();

                int i = 0;
                while (!shearableSheep.isEmpty() && enabled) {
                    Location loc = Storage.self.getLocation();
                    if (sort) {
                        Collections.sort(shearableSheep, (a, b) -> {
                            double dA = a.getLocation().distanceSquareTo(loc);
                            double dB = b.getLocation().distanceSquareTo(loc);
                            return (dA < dB) ? 1
                                    : (dA == dB) ? 0 : -1;
                        });
                        sort = false;
                    }

                    // Deplete set of sheep until empty
                    Sheep sheep = shearableSheep.remove(shearableSheep.size() - 1);
                    if (sheep.getLocation() == null) {
                        continue;
                    }
                    if (sheep.isBaby() || sheep.isSheared()) {
                        continue;
                    }
                    System.out.println("Progress: " + i + "/" + shearableSheep.size());

                    while (!InventoryUtil.findAndSelect((is) -> {
                        return is.getType() == Material.SHEARS && is.getData() < 200;
                    })) {
                        System.out.println("Ran out of tool/item");
                        Storage.self.sneak(true);
                        ai.tick(10);
                        Storage.self.sneak(false);
                        ai.tick(10);
                    }

                    // If nearest sheep is out of reach, walk to it and re-sort
                    while (Storage.self.getLocation().distanceTo(sheep.getLocation()) > 4
                            && ai.moveTo(sheep.getLocation())) {
                        sort = true;
                    }

                    Vector v = loc.vectorTo(sheep.getLocation()).toSpherical();
                    Storage.self.lookAt(180 / Math.PI * v.getYaw(), 180 / Math.PI * v.getPitch());
                    Storage.self.interactWithEntity(sheep, false);
                    ai.tick();
                }

                System.out.println("Finished round");
                Storage.self.sneak(true);
                ai.tick(10);
                Storage.self.sneak(false);

                InventoryUtil.findAndSelect((is) -> {
                    return is.getType() == Material.WOOL;
                });
                // Spam wool into tesseracts
                ai.moveTo(deliverLoc);
                ai.tick(10);
                for (Location loc : tesseracts) {
                    Storage.self.placeBlock(loc, BlockFace.UP);
                    ai.tick(5);
                    Storage.self.placeBlock(loc, BlockFace.UP);
                    ai.tick(5);
                }
                ai.tick(1200);
            }
            unregister();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            StackTraceElement ste = ex.getStackTrace()[0];
            unregister();
            Storage.self.sendChat("rip thread :(       " + ste.getClassName() + ":" + ste.getLineNumber());
        }
    }

    private synchronized boolean enabled() {
        return enabled;
    }

    public synchronized void disable() {
        enabled = false;
    }

    private void unregister() {
        Storage.self.cancelTask(aiTaskId);
    }

}
