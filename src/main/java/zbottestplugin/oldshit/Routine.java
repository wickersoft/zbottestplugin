/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.oldshit;

/**
 *
 * @author Dennis
 */
public abstract class Routine {
    
    protected final BlockingAI ai;
    
    public Routine(BlockingAI ai) {
        this.ai = ai;
    }
    
    public abstract void run() throws InterruptedException;
        
}
