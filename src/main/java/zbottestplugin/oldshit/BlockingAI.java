/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.oldshit;

import edu.kit.informatik.AStar;
import edu.kit.informatik.GeometricPath;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import zbottestplugin.InventoryUtil;
import zbottestplugin.Storage;
import zedly.zbot.event.Event;
import zedly.zbot.Location;
import zedly.zbot.BlockFace;
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;
import zedly.zbot.event.SlotUpdateEvent;
import zedly.zbot.event.TransactionResponseEvent;
import zedly.zbot.event.WindowOpenFinishEvent;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class BlockingAI implements Runnable {

    private final double stepResolution = 0.4;
    private final Object lock = new Object();
    private final Object timestopLock = new Object();
    private boolean timeStop = false;

    public void run() {
        synchronized (lock) {
            lock.notifyAll();
        }
        if (timeStop) {
            try {
                synchronized (timestopLock) {
                    timestopLock.wait();
                }
            } catch (InterruptedException ex) {
            }
        }
    }

    public boolean moveTo(Location target) throws InterruptedException {
        List<Location> nodes;
        Location oldLoc = Storage.self.getLocation();
        if (oldLoc.distanceTo(target) <= 1) {
            nodes = new LinkedList<>();
            nodes.add(target);
        } else {
            GeometricPath path = AStar.getPath(target);
            if (path == null) {
                return false;
            }
            nodes = path.getLocations();
        }
        followPath(nodes);
        return true;
    }

    public boolean navigateTo(Location target) throws InterruptedException {
        List<Location> nodes;
        while (Storage.self.getLocation().distanceSquareTo(target) > 1) {
            Location oldLoc = Storage.self.getLocation();
            GeometricPath path = AStar.getPath(target, true);
            if (path == null) {
                return false;
            }
            nodes = path.getLocations();
            followPath(nodes);
        }
        Location oldLoc = Storage.self.getLocation();
        nodes = new LinkedList<>();
        nodes.add(target);
        followPath(nodes);
        return true;
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
        timeStop = false;
        synchronized (timestopLock) {
            timestopLock.notifyAll();
        }
        synchronized (lock) {
            lock.wait();
        }
    }

    public void tick(int ticks) throws InterruptedException {
        for (int i = 0; i < ticks; i++) {
            tick();
        }
    }

    public void timeStop() throws InterruptedException {
        timeStop = true;
        tick();
    }

    public <T extends Event> boolean waitForEvent(final Class<T> eventClass, Predicate<Event> eventFilter, int timeoutMillis) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        final BlockingAI ai = this;
        final AtomicBoolean detected = new AtomicBoolean();
        detected.set(false);

        Storage.self.registerEvents(new Listener() {
            @EventHandler
            public void listen(Event hue) {
                if (eventClass.isInstance(hue) && eventFilter.test(hue)) {
                    detected.set(true);
                    Storage.self.unregisterEvents(this);
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            }
        });

        while (true) {
            synchronized (lock) {
                lock.wait();
            }
            if (detected.get()) {
                return true;
            } else if (System.currentTimeMillis() - startTime > timeoutMillis) {
                return false;
            }
        }
    }

    public boolean openContainer(int x, int y, int z) throws InterruptedException {
        Storage.self.placeBlock(x, y, z, BlockFace.NORTH);
        if (!waitForEvent(WindowOpenFinishEvent.class, 5000)) {
            return false;
        }
        while (waitForEvent(SlotUpdateEvent.class, 500)) {
        }
        return true;
    }

    public boolean openContainer(Location loc) throws InterruptedException {
        return openContainer(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public boolean closeContainer() throws InterruptedException {
        //final int expectedSlot = Storage.self.getInventory().getSelectedSlot() + 9;
        Storage.self.closeWindow();
        if (Storage.self.getInventory().changed()) {
            boolean closed = waitForEvent(SlotUpdateEvent.class, 5000);
            return closed;
        }
        tick();
        return true;
    }

    public int withdrawSlot(int sourceSlot) throws InterruptedException {
        int destSlot = InventoryUtil.findFreeStorageSlot(true);
        if (destSlot == -1) {
            return 3;
        }
        return transferItem(sourceSlot, destSlot);
    }

    public int depositSlot(int sourceSlot) throws InterruptedException {
        int destSlot = InventoryUtil.findFreeStorageSlot(false);
        if (destSlot == -1) {
            return 3;
        }
        return transferItem(sourceSlot, destSlot);
    }

    public int transferItem(int sourceSlot, int destSlot) throws InterruptedException {
        if (!clickSlot(sourceSlot, 0, 0)) {
            return 1;
        }
        if (!clickSlot(destSlot, 0, 0)) {
            return 2;
        }
        return 0;
    }

    public boolean clickSlot(int slot, int mode, int button) throws InterruptedException {
        AtomicBoolean confirm = new AtomicBoolean(false);
        Storage.self.getInventory().click(slot, mode, button);
        waitForEvent(TransactionResponseEvent.class, (e) -> {
            confirm.set(((TransactionResponseEvent) e).getStatus() == 1);
            return true;
        }, 15000);
        return confirm.get();
    }

    public <T extends Event> boolean waitForEvent(final Class<T> eventClass, int timeoutMillis) throws InterruptedException {
        return waitForEvent(eventClass, (e) -> true, timeoutMillis);
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
