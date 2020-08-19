/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.task;

import zbottestplugin.oldshit.BlockingAI;
import zbottestplugin.task.Task;

/**
 *
 * @author Dennis
 */
public abstract class Routine extends Task {
    
    public Routine(BlockingAI ai) {
        super(ai);
    }
    
    @Override
    public abstract void work() throws InterruptedException;
        
}
