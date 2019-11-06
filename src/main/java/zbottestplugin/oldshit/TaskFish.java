/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.oldshit;

import zedly.zbot.Material;
import zbottestplugin.FishWatcher;
import zbottestplugin.InventoryUtil;
import zbottestplugin.Storage;

/**
 *
 * @author Dennis
 */
public class TaskFish extends Thread {

    private FishWatcher watcher;
    private boolean running = true;
    private BlockingAI ai;
    private int aiId;

    public void run() {
        ai = new BlockingAI();
        watcher = new FishWatcher(this);
        aiId = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, ai, 1, 1);
        Storage.self.registerEvents(watcher);
        InventoryUtil.findAndSelect(Material.FISHING_ROD, 1);
        try {
            ai.tick(10);
            while (isRunning()) {
                Storage.self.useItem(false);
                synchronized(this) {
                    wait();
                }
                Storage.self.useItem(false);
                ai.tick(5);
            }
        } catch (InterruptedException ex) {

        }
        unload();
    }

    private void unload() {
        Storage.self.unregisterEvents(watcher);
        Storage.self.cancelTask(aiId);
    }
    
    private synchronized boolean isRunning() {
        return running;
    }

    public synchronized void close() {
        running = false;
        notify();
    }
}
