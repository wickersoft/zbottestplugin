/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

/**
 *
 * @author Dennis
 */
public class Synchronizer implements Runnable {

    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public void run() {
        while (queue.size() != 0) {
            Runnable r = queue.tryDeq();
            r.run();
        }
    }

    public void synchronize(Runnable r) {
        queue.enq(r);
    }

}
