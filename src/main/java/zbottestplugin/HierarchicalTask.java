/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zedly.zbot.event.Listener;

/**
 *
 * @author Dennis
 */
public class HierarchicalTask implements Runnable, Listener {

    private HierarchicalTask subTask;
    private boolean running = true;

    public final void run() {
        if (subTask != null && subTask.isRunning()) {
            subTask.run();
        } else if (isRunning()) {
            tick();
        }
    }

    public void goSub(HierarchicalTask task) {
        subTask = task;
    }

    public boolean isRunning() {
        return running;
    }

    public void tick() {
    }

    public void finish() {
        running = false;
    }

}
