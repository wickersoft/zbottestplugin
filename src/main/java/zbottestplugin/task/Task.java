/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.task;

import zbottestplugin.BlockingAI;

/**
 *
 * @author Dennis
 */
public abstract class Task extends Thread {
    
    protected final BlockingAI ai = new BlockingAI();
    private int aiTaskId;

    
    public void exec() {
        
    }
    
}
