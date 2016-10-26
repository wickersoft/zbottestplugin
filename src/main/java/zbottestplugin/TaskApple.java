/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.ArrayList;
import zedly.zbot.Location;
import zedly.zbot.block.Material;
import zedly.zbot.entity.Entity;
import zedly.zbot.entity.Monster;
import zedly.zbot.entity.Zombie;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class TaskApple extends Thread {

    private final ArrayList<Entity> villagerZombies = new ArrayList<>();
    private final BlockingAI ai = new BlockingAI();
    private int aiTaskId;

    @Override
    public void run() {
        try {
            aiTaskId = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, ai, 50, 50);
            //System.out.println("Running");
            for (Entity e : Storage.self.getEnvironment().getEntities()) {
                if (e instanceof Monster) {
                    if (e instanceof Zombie) {
                        Zombie z = (Zombie) e;
                        if (((Zombie) e).isVillager()) {
                            villagerZombies.add(e);
                        }
                    }
                }
            }
            if (villagerZombies.isEmpty()) {
                Storage.self.sendChat("Can't see any villager zombies here");
                return;
            }
            int apples = InventoryUtil.count(Material.GOLDEN_APPLE);
            if (villagerZombies.size() > apples) {
                Storage.self.sendChat("Need " + (villagerZombies.size() - apples) + " more apples to do that reasonably");
                return;
            }
            InventoryUtil.findAndSelect(Material.GOLDEN_APPLE);
            ai.tick(20);
            Location loc = Storage.self.getLocation();
            for (Entity e : villagerZombies) {
                if (e.getLocation() == null) {
                    System.out.println("Fuck this entity: " + e.getEntityId());
                }
                if (Storage.self.getLocation().distanceTo(e.getLocation()) < 5.0) {
                    Vector v = loc.vectorTo(e.getLocation()).toSpherical();
                    Storage.self.moveTo(new Location(loc.getX(), loc.getY(), loc.getZ(), 180 / Math.PI * v.getYaw(), 180 / Math.PI * v.getPitch()));
                    Storage.self.swingArm(false);
                    Storage.self.interactWithEntity(e, false);
                    ai.tick(1);
                }
            }
            Storage.self.sendChat("Cured " + villagerZombies.size() + " zombies");
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
