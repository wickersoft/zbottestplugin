/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import edu.kit.informatik.Graph;
import edu.kit.informatik.Node;
import zedly.zbot.Location;
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;
import zedly.zbot.event.entity.EntityBaseEvent;
import zedly.zbot.event.entity.EntityMoveEvent;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class MotionRecorder implements Listener {

    private int entityId = 0;
    private Graph graph = new Graph();
    private boolean running = false;
    private boolean following = false;
    private int followDistance = 4;
    private Node lastTrackNode;
    private final ConcurrentLinkedQueue<Node> trackingQueue = new ConcurrentLinkedQueue<>();

    @EventHandler
    public void onEntityMove(EntityMoveEvent evt) {
        if (running && evt.getEntity().getEntityId() == entityId && (lastTrackNode == null || lastTrackNode.getLocation().distanceTo(evt.getNewLocation()) > 0.1)) {
            Node node = new Node(graph.getNextNodeId() + "", evt.getNewLocation().clone());
            trackingQueue.enq(node);
            graph.addNode(node);
            if (lastTrackNode != null) {
                graph.connectNodes(node, lastTrackNode, 1, 1);
            }
            while (following && trackingQueue.size() > followDistance) {
                Location l = trackingQueue.tryDeq().getLocation();
                Vector v = Storage.self.getLocation().vectorTo(l).toSpherical();
                Storage.self.moveTo(new Location(l.getX(), l.getY(), l.getZ(), 180.0 / Math.PI * v.getYaw(),  180.0 / Math.PI * v.getPitch()));
            }
            lastTrackNode = node;
        }
    }

    @EventHandler
    public void onEntityMetadata(EntityBaseEvent evt) {
        if (evt.getEntity().getEntityId() == entityId) {
            if (evt.isSneaking()) {
                if (lastTrackNode == null) {
                    Storage.self.sendChat("You are not at a recorded waypoint!");
                } else {
                    Storage.self.sendChat("You are at waypoint " + lastTrackNode.getName() + " / " + graph.getSize());
                }
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public void saveAs(String filename) {
        graph.writeToFile(filename);
        Storage.self.sendChat(filename + " saved");
    }

    public void loadFrom(String filename) {
        graph = Graph.readFromFile(filename);
        Storage.self.sendChat(filename + " loaded");
    }

    public void record() {
        running = true;
    }

    public void clearQueue() {
        trackingQueue.clear();
    }

    public void stop() {
        running = false;
    }

    public void reset() {
        graph.clear();
        trackingQueue.clear();
        Storage.self.sendChat("Waypoints reset");
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public int getFollowDistance() {
        return followDistance;
    }

    public void setFollowDistance(int followDistance) {
        this.followDistance = followDistance;
    }

    public int getEntityId() {
        return entityId;
    }

    public Graph getGraph() {
        return graph;
    }

    public Node getLastTrackNode() {
        return lastTrackNode;
    }

    public void setLastTrackNode(Node lastTrackNode) {
        this.lastTrackNode = lastTrackNode;
    }
}
