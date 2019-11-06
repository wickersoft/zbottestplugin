/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.ArrayList;
import zedly.zbot.Location;
import zedly.zbot.entity.Entity;
import zedly.zbot.entity.Monster;
import zedly.zbot.entity.Zombie;
import zedly.zbot.entity.ZombieVillager;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class TaskZombieFilter implements Runnable {

    public final ArrayList<Entity> enemies = new ArrayList<>();

    @Override
    public void run() {
        //System.out.println("Running");
        enemies.clear();
        for (Entity e : Storage.self.getEnvironment().getEntities()) {
            if (e instanceof Monster) {
                if(e instanceof ZombieVillager) {
                    continue;
                }
                enemies.add(e);
            }
        }
        if (enemies.isEmpty()) {
            return;
        }
        Entity target = null;
        double c;
        double d = Double.MAX_VALUE;
        for (Entity e : enemies) {
            if(e.getLocation() == null) {
                System.out.println("Fuck this entity: " + e.getEntityId());
            }
            if ((c = Storage.self.getLocation().distanceTo(e.getLocation())) < d) {
                d = c;
                target = e;
            }
        }
        //System.out.println("Closest enemy: " + target + " at " + d);
        if (d < 5) {
            Location loc = Storage.self.getLocation();
            Vector v = loc.vectorTo(target.getLocation()).toSpherical();
            Storage.self.moveTo(new Location(loc.getX(), loc.getY(), loc.getZ(), 180 / Math.PI * v.getYaw(), 180 / Math.PI * v.getPitch()));
            Storage.self.swingArm(false);
            Storage.self.attackEntity(target);
        }
    }

}
