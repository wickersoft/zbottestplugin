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
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class BlockingAI implements Runnable {

    private final Object lock = "";

    public void run() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void moveTo(Location loc) throws InterruptedException {
        GeometricPath path = AStar.getPath(loc);
        List<Location> nodes = path.getLocations();
        double yaw;
        for (Location node : nodes) {
            Location oldLoc = Storage.self.getLocation();
            Vector direction = Storage.self.getLocation().vectorTo(loc);
            if (direction.getRadius() != 0) {
                yaw = direction.getYaw();
            }

            int steps = (int) Math.floor(direction.getRadius() / 0.4);
            direction = direction.normalize();
            for (int i = 0; i < steps; i++) {
                Storage.self.moveTo(oldLoc.getRelative(direction.multiply(i * 0.4)));
                tick();
            }
            Storage.self.moveTo(loc);
            tick();
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

    public void tick() throws InterruptedException {
        synchronized (lock) {
            lock.wait();
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
