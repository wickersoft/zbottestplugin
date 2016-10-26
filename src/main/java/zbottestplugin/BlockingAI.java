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
import zedly.zbot.Location;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class BlockingAI implements Runnable {

    private final double stepResolution = 0.4;
    private final Object lock = "";

    public void run() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void moveTo(Location target) throws InterruptedException {
        List<Location> nodes;
        Location oldLoc = Storage.self.getLocation();
        if (oldLoc.distanceTo(target) <= 1) {
            nodes = new LinkedList<>();
            nodes.add(target);
        } else {
            GeometricPath path = AStar.getPath(target);
            nodes = path.getLocations();
        }
        followPath(nodes);
    }

    public void followPath(GeometricPath path) throws InterruptedException {
        followPath(path.getLocations());
    }

    public void followPath(List<Location> nodes) throws InterruptedException {
        Location oldLoc = Storage.self.getLocation();
        double yaw = oldLoc.getYaw();
        for (Location loc : nodes) {
            Vector direction = Storage.self.getLocation().vectorTo(loc);
            if (direction.getHorizontalLength() != 0) {
                yaw = direction.getYaw();
            }
            int steps = (int) Math.floor(direction.getLength() / stepResolution);
            direction = direction.normalize();
            for (int i = 0; i < steps; i++) {
                Storage.self.moveTo(oldLoc.getRelative(direction.multiply(i * stepResolution)).withYawPitch(180 / Math.PI * yaw, oldLoc.getPitchTo(loc)));
                tick();
            }
            Storage.self.moveTo(loc.withYawPitch(180 / Math.PI * yaw, oldLoc.getPitchTo(loc)));
            tick();
            oldLoc = loc;
        }
    }

    public void breakBlock(Location loc, int millis) throws InterruptedException {
        CallbackLock cLock = new CallbackLock();
        Storage.self.breakBlock(loc, millis, () -> {
            cLock.finish();
        });
        int i = 0;
        while (true) {
            tick();
            if (++i % 3 == 0) {
                Storage.self.swingArm(false);
            }
            if (cLock.isFinished()) {
                return;
            }
        }
    }

    public void breakBlock(Location loc) throws InterruptedException {
        breakBlock(loc, 1000);
    }
    
    public void clickBlock(Location loc) throws InterruptedException {
        Storage.self.clickBlock(loc);
        tick();
    }

    public void tick() throws InterruptedException {
        synchronized (lock) {
            lock.wait();
        }
    }
    
    public void tick(int ticks) throws InterruptedException {
        for(int i = 0; i < ticks; i++) {
            tick();
        }
    }

    private class CallbackLock {

        private boolean finished = false;

        public synchronized boolean isFinished() {
            return finished;
        }

        public synchronized void finish() {
            finished = true;
        }
    }

}
