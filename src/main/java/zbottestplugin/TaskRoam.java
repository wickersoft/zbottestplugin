/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import edu.kit.informatik.Graph;
import edu.kit.informatik.Node;
import java.util.List;
import zedly.zbot.Location;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class TaskRoam implements Runnable {

    private boolean roaming = false;
    private Node currentNode;
    private Node previousNode;

    public void run() {
        if (!roaming) {
            return;
        }

        List<Node> neighbors = currentNode.getNeighbors();
        if (neighbors.size() > 1) {
            double totalDistance = 0;
            do {
                int randomNeighbor = Storage.rnd.nextInt(neighbors.size() - 1);
                for (int i = 0; i <= randomNeighbor; i++) {
                    if (previousNode != null && neighbors.get(i).equals(previousNode)) {
                        randomNeighbor++;
                        break;
                    }
                }
                previousNode = currentNode;
                currentNode = neighbors.get(randomNeighbor);
                totalDistance += previousNode.getLocation().distanceTo(currentNode.getLocation());
                neighbors = currentNode.getNeighbors();
            } while (totalDistance < 0.25 && neighbors.size() == 2);
        } else {
            previousNode = currentNode;
            currentNode = neighbors.get(0);
        }
        Location loc = currentNode.getLocation();
        Vector v = previousNode.getLocation().vectorTo(loc);
        Vector w = v.toSpherical();
        Storage.self.moveTo(loc.getX(), loc.getY(), loc.getZ(), 180.0 / Math.PI * w.getYaw(), 0);
    }

    public void setRoaming(boolean roaming) {
        this.roaming = roaming;
    }

    public boolean isRoaming() {
        return roaming;
    }

    public void loadGraph(String filename) {
        Storage.graph = Graph.readFromFile(filename);
    }
    
    public boolean isAtSuitableStartingPoint() {
        currentNode = Storage.graph.getClosestNodeTo(Storage.self.getLocation());
        return currentNode.getLocation().distanceTo(Storage.self.getLocation()) < 5;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }
    
    
}
