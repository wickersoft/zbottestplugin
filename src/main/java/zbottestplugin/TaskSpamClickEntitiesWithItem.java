/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import zedly.zbot.Location;
import zedly.zbot.entity.Entity;
import zedly.zbot.inventory.ItemStack;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class TaskSpamClickEntitiesWithItem extends Thread {

    private final ArrayList<Entity> targetEntities = new ArrayList<>();
    private final BlockingAI ai = new BlockingAI();
    private int aiTaskId;

    private final Predicate<Entity> entityFilter;
    private final Predicate<ItemStack> itemFilter;
    private final double maxDistance;

    public TaskSpamClickEntitiesWithItem(Predicate<Entity> entityFilter, Predicate<ItemStack> materialFilter, double maxDistance) {
        this.entityFilter = entityFilter;
        this.itemFilter = materialFilter;
        this.maxDistance = maxDistance;
    }

    @Override
    public void run() {
        boolean sort = true;
        try {
            aiTaskId = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, ai, 100, 100);
            for (Entity e : Storage.self.getEnvironment().getEntities()) {
                if (e.getLocation().distanceTo(Storage.self.getLocation()) < maxDistance && entityFilter.test(e)) {
                    targetEntities.add(e);
                }
            }
            if (targetEntities.isEmpty()) {
                Storage.self.sendChat("Can't see any targets here");
                return;
            }
            ai.tick();
            int i = 0;
            while (!targetEntities.isEmpty()) {
                Location loc = Storage.self.getLocation();
                if (sort) {
                    Collections.sort(targetEntities, (a, b) -> {
                        return (a.getLocation().distanceSquareTo(loc) < b.getLocation().distanceSquareTo(loc)) ? 1 : -1;
                    });
                    sort = false;
                }
                Entity e = targetEntities.remove(targetEntities.size() - 1);
                if (e.getLocation() == null) {
                    //System.out.println("Fuck this entity: " + e.getEntityId());
                    continue;
                }
                if (!entityFilter.test(e)) {
                    //System.out.println("Nevermind this entity: " + e.getEntityId());
                    continue;
                }
                System.out.println("Progress: " + i + "/" + targetEntities.size());
                if (!InventoryUtil.findAndSelect(itemFilter)) {
                    Storage.self.sendChat("Ran out of item/tool");
                    unregister();
                    return;
                }
                while (Storage.self.getLocation().distanceTo(e.getLocation()) > 4
                        && ai.moveTo(e.getLocation())) {
                    sort = true;
                }

                Vector v = loc.vectorTo(e.getLocation()).toSpherical();
                Storage.self.lookAt(180 / Math.PI * v.getYaw(), 180 / Math.PI * v.getPitch());
                Storage.self.interactWithEntity(e, false);
                ai.tick();
            }
            System.out.println("Done");
            Storage.self.sneak(true);
            ai.tick(10);
            Storage.self.sneak(false);
            unregister();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            StackTraceElement ste = ex.getStackTrace()[0];
            unregister();
            Storage.self.sendChat("rip thread :(       " + ste.getClassName() + ":" + ste.getLineNumber());
        }
    }

    private void unregister() {
        Storage.self.cancelTask(aiTaskId);
    }

}
