/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.List;
import java.util.Random;
import zedly.zbot.EntityType;
import zedly.zbot.Location;
import zedly.zbot.entity.Entity;
import zedly.zbot.entity.Player;
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;
import zedly.zbot.event.entity.EntityAnimationEvent;
import zedly.zbot.event.entity.EntityMoveEvent;
import zedly.zbot.event.entity.PlayerSneakEvent;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class TaskHeadFollow implements Listener, Runnable {

    private final Random rnd = new Random();
    private final Location idleLocation = new Location(Storage.plugin.getConfig().getInt("spawn.x", 0), 
            Storage.plugin.getConfig().getInt("spawn.y", 64), 
            Storage.plugin.getConfig().getInt("spawn.z", 0));
    private final int trackingDistance = 15;
    private final boolean warpSpam;
    private int closestPlayerId = -1;
    private double closestPlayerDistance = Double.MAX_VALUE;
    
    public TaskHeadFollow(boolean warpSpam) {
        this.warpSpam = warpSpam;
    }
    
    private final List<String> messages = Storage.plugin.getConfig().getList("follow.msgs", String.class);

    public void run() {
        Entity e = Storage.self.getEnvironment().getEntityById(closestPlayerId);
        if (e == null || e.getLocation().distanceTo(Storage.self.getLocation()) > trackingDistance) {
            closestPlayerId = -1;
            closestPlayerDistance = Double.MAX_VALUE;
            lookAt(idleLocation);
            Storage.self.sneak(false);
        } else if (warpSpam) {
            Player p = (Player) e;
            String playerName = Storage.self.getEnvironment().getPlayerNameByUUID(p.getUUID());
            Storage.self.sendChat("/msg " + playerName + " " + messages.get(rnd.nextInt(messages.size())));
        }
    }

    @EventHandler
    public void onPlayerMove(EntityMoveEvent evt) {
        if (evt.getEntity().getType() == EntityType.PLAYER) {
            double newDistance = evt.getNewLocation().distanceTo(Storage.self.getLocation());
            if (evt.getEntity().getEntityId() == closestPlayerId) {
                closestPlayerDistance = newDistance;
                lookAt(evt.getEntity().getLocation());
            } else if (newDistance < trackingDistance) {
                if (closestPlayerId == -1 || newDistance < closestPlayerDistance) {
                    closestPlayerId = evt.getEntity().getEntityId();
                    closestPlayerDistance = newDistance;
                    lookAt(evt.getEntity().getLocation());
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerSneak(PlayerSneakEvent evt) {
        if(evt.getPlayer().getEntityId() == closestPlayerId) {
            Storage.self.sneak(evt.isSneaking());
        }
    }
    
    @EventHandler
    public void onSwing(EntityAnimationEvent evt) {
        if(evt.getEntity().getEntityId() == closestPlayerId && (evt.getAnimationId() == 0 || evt.getAnimationId() == 3)) {
            Storage.self.swingArm(false);
        }
    }

    private void lookAt(Location loc) {
        Vector v = Storage.self.getLocation().vectorTo(loc);
        Storage.self.lookAt(180 / Math.PI * v.getYaw(), 180 / Math.PI * v.getPitch());
    }
}
