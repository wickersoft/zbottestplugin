/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.oldshit;

import edu.kit.informatik.Dijkstra;
import edu.kit.informatik.Graph;
import edu.kit.informatik.Node;
import java.util.List;
import zbottestplugin.Storage;
import zbottestplugin.TaskNavigate;
import zedly.zbot.Location;
import zedly.zbot.entity.Entity;
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class DijkstraCommands implements Listener {

    @EventHandler
    public static void onCommand(String player, String command, boolean pm) {
        String[] args = command.split(" ");
        String respondTo = null;
        if (pm) {
            respondTo = player;
        }
        switch (args[0]) {
            case "navigate":
                if (Storage.roamer.isRoaming()) {
                    respond(respondTo, "Stop the roaming process first");
                    return;
                }
                Node node = null;
                if (args.length >= 4) {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    Location loc = new Location(x, y, z);
                    node = Storage.recorder.getGraph().getClosestNodeTo(loc);
                    if (node.getLocation().distanceTo(loc) > 10) {
                        respond(respondTo, "I'm not sure how to get there...");
                        return;
                    }
                } else if (args.length >= 2) {
                    node = Storage.recorder.getGraph().getNode(args[1]);
                    if (node == null) {
                        respond(respondTo, "This node does not exist");
                        return;
                    }
                }
                Node selfNode = Storage.recorder.getGraph().getClosestNodeTo(Storage.self.getLocation());
                if (Storage.self.getLocation().distanceTo(selfNode.getLocation()) > 5) {
                    respond(respondTo, "I am not at a suitable starting point!");
                    return;
                }
                List<Node> nodes = Dijkstra.findPath(Storage.recorder.getGraph(), selfNode.getName(), node.getName(), Dijkstra.SHORTEST).getNodes();
                respond(respondTo, "Navigating along path of length " + nodes.size());
                TaskNavigate tn = new TaskNavigate(nodes);
                tn.identify(Storage.self.scheduleSyncRepeatingTask(Storage.plugin, tn, 100, 100));
                break;
            case "prune":
                if (args.length == 3) {
                    int firstNode = Integer.parseInt(args[1]);
                    int lastNode = Integer.parseInt(args[2]);
                    Graph graph = Storage.recorder.getGraph();
                    for (int i = lastNode; i >= firstNode; i--) {
                        Node n = graph.getNode(i + "");
                        graph.removeNode(n);
                    }
                    respond(respondTo, "Removed " + (lastNode - firstNode + 1) + " nodes. Size now " + graph.getSize());
                }
                break;
            case "record":
                if (Storage.recorder.isRunning()) {
                    respond(respondTo, "Already recording!");
                    return;
                }
                if (Storage.recorder.getEntityId() == 0) {
                    respond(respondTo, "Not focused on an entity!");
                    return;
                }
                if (Storage.recorder.getEntityId() == Storage.self.getEntityId()) {
                    respond(respondTo, "Record myself? What are you a perv?");
                    return;
                }
                if (args.length == 2) {
                    try {
                        int followDistance = Integer.parseInt(args[1]);
                        Entity p = Storage.self.getEnvironment().getEntityById(Storage.recorder.getEntityId());
                        if (p.getLocation().distanceTo(Storage.self.getLocation()) > 3) {
                            Location myLoc = Storage.self.getLocation();
                            respond(respondTo, "You are too far away from me! Come to " + myLoc.getBlockX() + " " + myLoc.getBlockY() + " " + myLoc.getBlockZ() + "!");
                            return;
                        }
                        Storage.recorder.setFollowDistance(followDistance);
                        Storage.recorder.setFollowing(true);
                    } catch (NumberFormatException ex) {
                    }
                }
                Storage.recorder.record();
                break;
            case "distfrom":
                if (args.length == 2) {
                    try {
                        node = Storage.recorder.getGraph().getNode(args[1]);
                        if (node == null) {
                            respond(respondTo, "This node does not exist");
                            return;
                        } else {
                            if (Storage.recorder.getEntityId() == 0 || Storage.recorder.getEntityId() == Storage.self.getEntityId()) {
                                respond(respondTo, node.getLocation().distanceTo(Storage.self.getLocation()) + "");
                                return;
                            }
                            respond(respondTo, node.getLocation().distanceTo(Storage.self.getEnvironment().getEntityById(Storage.recorder.getEntityId()).getLocation()) + "");
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
                break;
            case "closest":
                Location loc;
                if (Storage.recorder.getEntityId() == 0 || Storage.recorder.getEntityId() == Storage.self.getEntityId()) {
                    loc = Storage.self.getLocation();
                } else {
                    loc = Storage.self.getEnvironment().getEntityById(Storage.recorder.getEntityId()).getLocation();
                }

                node = Storage.recorder.getGraph().getClosestNodeTo(loc);
                if (node == null) {
                    respond(respondTo, "No graph loaded!");
                    return;
                }
                respond(respondTo, "Node " + node.getName() + ": "
                        + node.getLocation().distanceTo(loc) + "");
                break;
            case "attach":
                if (args.length >= 2) {
                    try {
                        Node targetNode = Storage.recorder.getGraph().getNode(args[1]);
                        if (targetNode == null) {
                            respond(respondTo, "This node does not exist");
                            return;
                        } else {
                            Node currentNode = Storage.recorder.getLastTrackNode();
                            double distance = targetNode.getLocation().distanceTo(currentNode.getLocation());
                            if (distance > 5) {
                                respond(respondTo, "You are too far away from the node! Go near " + targetNode.getLocation().toString());
                                return;
                            }
                            Graph graph = Storage.recorder.getGraph();
                            Vector v = currentNode.getLocation().vectorTo(targetNode.getLocation()).normalize(0.5);
                            Location currentLoc = currentNode.getLocation();
                            int newNodes = (int) (distance * 2);
                            for (int i = 0; i < newNodes - 1; i++) {
                                Location newLoc = currentLoc.getRelative(v);
                                Node newNode = new Node(graph.getNextNodeId() + "", newLoc);
                                graph.addNode(newNode);
                                graph.connectNodes(currentNode, newNode, 1, 1);
                                currentNode = newNode;
                                currentLoc = newLoc;
                            }
                            graph.connectNodes(currentNode, targetNode, 1, 1);
                            respond(respondTo, "Attached to " + Storage.recorder.getLastTrackNode().getName() + " via " + newNodes + " intermediate nodes.");
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
                break;
            case "startfrom":
                if (args.length == 2) {
                    try {
                        node = Storage.recorder.getGraph().getNode(args[1]);
                        if (node == null) {
                            respond(respondTo, "This node does not exist");
                            return;
                        } else {
                            double distance = node.getLocation().distanceTo(Storage.self.getLocation());
                            if (distance > 3) {
                                respond(respondTo, "I am too far away from the node! Go to " + node.getLocation().toString());
                                return;
                            }
                            Storage.recorder.setLastTrackNode(node);
                            respond(respondTo, "Starting next recording at Node " + Storage.recorder.getLastTrackNode().getName() + ".");
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
                break;
            case "clearqueue":
                Storage.recorder.clearQueue();
                respond(respondTo, "Cleared following queue");
                break;
            case "moveto":
                if (args.length == 2) {
                    try {
                        node = Storage.recorder.getGraph().getNode(args[1]);
                        if (node == null) {
                            respond(respondTo, "This node does not exist");
                            return;
                        } else {
                            double distance = node.getLocation().distanceTo(Storage.self.getLocation());
                            if (distance > 8) {
                                respond(respondTo, "I am too far away from the node! Go to " + node.getLocation().toString());
                                return;
                            }
                            Storage.self.moveTo(node.getLocation());
                            respond(respondTo, "Relocated to " + node.getName() + ".");
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
                break;
            case "neighbors":
                if (args.length == 2) {
                    try {
                        node = Storage.recorder.getGraph().getNode(args[1]);
                        if (node == null) {
                            respond(respondTo, "This node does not exist");
                            return;
                        } else {
                            List<Node> neighbors = node.getNeighbors();
                            StringBuilder sb = new StringBuilder();
                            sb.append(neighbors.size()).append(" neighbors:");
                            for (Node n : neighbors) {
                                sb.append(n.getName()).append(" ");
                            }
                            respond(respondTo, sb.toString());
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
                break;
            case "stop":
                if (!Storage.recorder.isRunning()) {
                    respond(respondTo, "Already stopped!");
                    return;
                }
                Storage.recorder.stop();
                break;
            case "save":
                if (Storage.recorder.isRunning()) {
                    respond(respondTo, "Cannot save while recording");
                    return;
                }
                Storage.recorder.saveAs(args[1]);
                return;
            case "load":
                if (Storage.recorder.isRunning()) {
                    respond(respondTo, "Cannot load while recording");
                    return;
                }
                Storage.recorder.loadFrom(args[1]);
                return;
            case "reset":
                if (Storage.recorder.isRunning()) {
                    respond(respondTo, "Cannot reset while recording");
                    return;
                }
                Storage.recorder.reset();
                return;
            case "play":
                if (args.length == 4) {
                    TaskPlay tp = new TaskPlay(args[1], Integer.parseInt(args[2]), Boolean.parseBoolean(args[3]));
                    //Storage.self.scheduleTask(Storage.plugin, tp);
                }
                break;
            case "roam":
                if (Storage.graph != null) {
                    Node n = Storage.graph.getClosestNodeTo(Storage.self.getLocation());
                    if (n.getLocation().distanceTo(Storage.self.getLocation()) < 5) {
                        Storage.roamer.setCurrentNode(n);
                        Storage.roamer.setRoaming(true);
                        respond(respondTo, "Now roaming!");
                    } else {
                        Storage.roamer.setRoaming(false);
                        respond(respondTo, "I am not at a suitable starting point!");
                    }
                }
                break;
            case "roamstop":
                Storage.roamer.setRoaming(false);
                respond(respondTo, "Stopped!");
                break;
            case "roamload":
                if (args.length == 2) {
                    if (Storage.roamer.isRoaming()) {
                        respond(respondTo, "Cannot load while roaming!");
                    } else {
                        respond(respondTo, "Loading " + args[1] + "..");
                        Storage.roamer.loadGraph(args[1]);
                    }
                }
                break;
        }

    }
    
    private static void respond(String user, String message) {
        if (user == null) {
            Storage.self.sendChat(message);
        } else {
            Storage.self.sendChat("/msg " + user + " " + message);
        }
    }

}
