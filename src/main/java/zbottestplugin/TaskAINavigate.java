/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zbottestplugin.task.Task;
import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class TaskAINavigate extends Task {

    private final Location target;

    public TaskAINavigate(Location target) {
        super(100);
        this.target = target;
    }

    @Override
    public void run() {
        try {
            ai.navigateTo(target);
        } catch (InterruptedException ex) {
        }
    }

}
