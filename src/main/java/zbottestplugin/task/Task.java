/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.task;

import zbottestplugin.Storage;
import zbottestplugin.oldshit.BlockingAI;

/**
 *
 * @author Dennis
 */
public abstract class Task extends Thread {
    
    protected final BlockingAI ai;
    protected int aiTaskId;
    
    public Task(int interval) {
        this.ai = new BlockingAI();
        aiTaskId = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, ai, interval);
    }
    
    protected void unregister() {
        Storage.self.cancelTask(aiTaskId);
    }
    
    protected void setTickSpeed(int newInterval) {
        Storage.self.cancelTask(aiTaskId);
        aiTaskId = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, ai, newInterval);    
    }
}
