/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import edu.kit.informatik.Node;
import java.util.Iterator;
import java.util.List;
import zedly.zbot.Location;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class TaskNavigate implements Runnable {

    private final Iterator<Node> it;
    private int i;
    
    public TaskNavigate(List<Node> nodes) {
        this.it = nodes.iterator();
        if(it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    public void run() {
        if (it.hasNext()) {
            Location loc = Storage.self.getLocation();
            Location next = it.next().getLocation();
            Vector v = loc.vectorTo(next);
            Vector w = v.toSpherical();
            Storage.self.moveTo(next.getX(), next.getY(), next.getZ(), 180.0 / Math.PI * w.getYaw(), 0);
        } else {
            //Storage.self.sendChat("Here I am!");
            Storage.self.cancelTask(i);
        }
    }

    public void identify(int i) {
        this.i = i;
    }
}
