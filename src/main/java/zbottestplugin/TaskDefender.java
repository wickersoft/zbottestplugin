/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.ArrayList;
import java.util.Collection;
import zedly.zbot.Location;
import zedly.zbot.Material;
import zedly.zbot.entity.Entity;
import zedly.zbot.entity.Monster;
import zedly.zbot.entity.Squid;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class TaskDefender implements Runnable {

    @Override
    public void run() {
        //System.out.println("Running");
        Collection<Monster> enemies = Storage.self.getEnvironment().getEntities(Monster.class);
        if (enemies.isEmpty()) {
            return;
        }
        Entity target = null;
        double c;
        double d = Double.MAX_VALUE;
        for (Entity e : enemies) {
            if (e.getLocation() == null) {
                System.out.println("Fuck this entity: " + e);
            }
            if ((c = Storage.self.getLocation().distanceTo(e.getLocation())) < d) {
                d = c;
                target = e;
            }
        }
        //System.out.println("Closest enemy: " + target + " at " + d);
        if (d < 5) {
            Location loc = Storage.self.getLocation();
            Vector v = loc.vectorTo(target.getLocation());
            Storage.self.moveTo(loc.withYawPitch(v));
            InventoryUtil.findAndSelect((is) -> {
                switch (is.getType()) {
                    case DIAMOND_SWORD:
                    case GOLDEN_SWORD:
                    case IRON_SWORD:
                    case STONE_SWORD:
                    case WOODEN_SWORD:
                        return true;
                    default:
                        return false;
                }
            });
            Storage.self.attackEntity(target);
            Storage.self.swingArm(false);
        }
    }
}
