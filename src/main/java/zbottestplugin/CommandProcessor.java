/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import edu.kit.informatik.AStar;
import edu.kit.informatik.Dijkstra;
import edu.kit.informatik.GeometricPath;
import edu.kit.informatik.Graph;
import edu.kit.informatik.Node;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import zbottestplugin.HTTP.HTTPResponse;
import zedly.zbot.ClientSettings;
import zedly.zbot.entity.Entity;
import zedly.zbot.entity.Player;
import zedly.zbot.Location;
import zedly.zbot.inventory.ItemStack;
import zedly.zbot.block.Block;
import zedly.zbot.block.Material;
import zedly.zbot.entity.FallingBlock;
import zedly.zbot.entity.Item;
import zedly.zbot.entity.Sheep;
import zedly.zbot.entity.Unknown;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class CommandProcessor {

    public static void onCommand(String player, String command, boolean pm) throws Exception {
        String[] args = command.split(" ");
        String respondTo = null;
        if (pm) {
            respondTo = player;
        }
        switch (args[0]) {
            case "hi":
                respond(respondTo, "Hello " + player + "!");
                break;
            case "clock":
                respond(respondTo, "" + System.currentTimeMillis());
            case "google":
                if (args.length == 1) {
                    respond(respondTo, "google {query}");
                    return;
                }
                try {
                    int offset = 0;
                    HTTPResponse http = HTTP.https("https://www.google.com/search?q=" + URLEncoder.encode(command.substring(7), "UTF-8"));
                    String html = new String(http.getContent());
                    String[] links = StringUtil.extractAll(html, "<h3 class=\"r\"><a href=\"", "\"");
                    for (String s : links) {
                        System.out.println(s);

                        //TODO: tinyurl
                        respond(respondTo, s);
                        return;
                    }
                    respond(respondTo, "No results! :(");
                    return;
                } catch (IOException ex) {
                    respond(respondTo, "IO Error! o.O");
                    ex.printStackTrace();
                    return;
                }
            case "calc":
                if (args.length <= 1) {
                    respond(respondTo, "calc {expression}");
                    return;
                }
                Expression exp = new ExpressionBuilder(command.substring(5)).build();
                respond(respondTo, exp.evaluate() + "");
                break;
            case "trans":
            case "translate":
                if (args.length < 2) {
                    respond(respondTo, "translate [player] ([source language] ([your language]))");
                    return;
                }
                if (args.length == 2) {
                    if (args[1].equals("off")) {
                        TranslationService.disableAllTranslationsFor(player);
                        respond(respondTo, "Disabled all translations");
                        return;
                    }
                    TranslationService.removeTranslationFor(args[1], player);
                    respond(respondTo, "Translation of " + args[1] + " disabled");
                    return;
                }
                String sourceLanguage = "";
                String targetLanguage = "en";
                if (args.length >= 3) {
                    if (Storage.languageCodes.contains(args[2])) {
                        sourceLanguage = args[2];
                    } else if (Storage.languageAliases.containsKey(args[2])) {
                        sourceLanguage = Storage.languageAliases.get(args[2]);
                    } else {
                        respond(respondTo, "Unknown language!");
                        return;
                    }
                }
                if (args.length >= 4) {
                    if (Storage.languageCodes.contains(args[3])) {
                        targetLanguage = args[3];
                    } else if (Storage.languageAliases.containsKey(args[3])) {
                        targetLanguage = Storage.languageAliases.get(args[3]);
                    } else {
                        respond(respondTo, "Unknown language!");
                        return;
                    }
                }
                TranslationService.addTranslationFor(args[1], player, sourceLanguage + "|" + targetLanguage);
                respond(respondTo, "Translating " + args[1] + " from " + sourceLanguage + " to " + targetLanguage + "!");
                break;
        }
        if (player.equals("brainiac94") || player.equals("Veresen")) {
            switch (args[0]) {
                case "entity":
                    Entity ent = null;
                    if (args.length == 2) {
                        ent = Storage.self.getEnvironment().getEntityById(Integer.parseInt(args[1]));
                    } else {
                        double d = Double.MAX_VALUE;
                        double c;
                        Collection<Entity> ents = Storage.self.getEnvironment().getEntities();
                        System.out.println(ents.size() + " entities visible");
                        for (Entity e : ents) {
                            c = e.getLocation().distanceTo(Storage.self.getLocation());
                            System.out.println(e.getType() + " at " + e.getLocation());
                            if (c < d) {
                                d = c;
                                ent = e;
                            }
                        }
                    }
                    if (ent == null) {
                        Storage.self.sendChat("That entity is not visible :(");
                        return;
                    }

                    if (ent instanceof Sheep) {
                        Sheep s = (Sheep) ent;
                        Storage.self.sendChat("That is " + (s.isSheared() ? "sheared" : "") + s.getColor() + " SHEEP" + ent.getEntityId());
                    } else if (ent instanceof Unknown) {
                        Storage.self.sendChat("That is unidentified entity " + +ent.getEntityId() + " (Type " + ((Unknown) ent).getEntityTypeId() + ")");
                    } else if (ent instanceof Item) {
                        Item item = (Item) ent;
                        ItemStack is = item.getItemStack();
                        Storage.self.sendChat("That is ITEM " + ent.getEntityId() + " consisting of " + is.getAmount() + "x" + is.getType() + ":" + is.getDamageValue());
                    } else if (ent instanceof Player) {
                        Storage.self.sendChat("That is PLAYER " + ent.getEntityId() + ", " + ((Player) ent).getName());
                    } else if (ent instanceof FallingBlock) {
                        FallingBlock fb = (FallingBlock) ent;
                        Storage.self.sendChat("That is FALLING_BLOCK " + ent.getEntityId() + " consisting of " + fb.getBlockType() + ":" + fb.getBlockData());
                    } else {
                        Storage.self.sendChat("That is " + ent.getType() + " " + ent.getEntityId());
                    }
                    break;
                case "respawn":
                    Storage.self.respawn();
                    break;
                case "say":
                    Storage.self.sendChat(command.substring(4));
                    break;
                case "block":
                    Block block;
                    if (args.length >= 4) {
                        double x = Double.parseDouble(args[1]);
                        double y = Double.parseDouble(args[2]);
                        double z = Double.parseDouble(args[3]);
                        Location loc = new Location(x, y, z).centerHorizontally();
                        block = Storage.self.getEnvironment().getBlockAt(loc);
                    } else {
                        ent = Storage.self.getEnvironment().getEntityById(Storage.recorder.getEntityId());
                        block = Storage.self.getEnvironment().getBlockAt(ent.getLocation());
                    }
                    if (block == null) {
                        respond(respondTo, "This block is not loaded :(");
                    } else {
                        respond(respondTo, "That block is " + block.getTypeId() + ":" + block.getData());
                    }
                    break;
                case "skin":
                    ClientSettings cs = Storage.self.getClientSettings();
                    if (args.length == 2) {
                        switch (args[1]) {
                            case "normal":
                                cs.setSkinFlags(0x40);
                                break;
                            case "magic":
                                cs.setSkinFlags(0x7F);
                                break;
                        }
                    }
                    Storage.self.setClientSettings(cs);
                    break;
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
                case "goto":
                    if (Storage.roamer.isRoaming()) {
                        respond(respondTo, "Stop the roaming process first");
                        return;
                    }
                    Location loc;
                    if (args.length >= 4) {
                        double x = Double.parseDouble(args[1]);
                        double y = Double.parseDouble(args[2]);
                        double z = Double.parseDouble(args[3]);
                        loc = new Location(x, y, z).centerHorizontally();
                    } else {
                        ent = Storage.self.getEnvironment().getEntityById(Storage.recorder.getEntityId());
                        loc = ent.getLocation().centerHorizontally();
                    }
                    GeometricPath path = AStar.getPath(loc);
                    if (path == null) {
                        respond(respondTo, "Could not find a path to that location");
                        return;
                    }
                    nodes = path.getNodes();
                    //respond(respondTo, "Navigating along path of length " + nodes.size() + " (" + AStar.getRuntimeCounter() + " iterations)");
                    tn = new TaskNavigate(nodes);
                    tn.identify(Storage.self.scheduleSyncRepeatingTask(Storage.plugin, tn, 250, 250));
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
                case "debug":
                    Storage.debugEntity = Integer.parseInt(args[1]);
                    Storage.self.sendChat("Debugging " + Storage.debugEntity);
                    break;
                case "focus":
                    if (Storage.recorder.isRunning()) {
                        respond(respondTo, "Cannot change focus while recording");
                        return;
                    }
                    int entityId = Integer.parseInt(args[1]);
                    if (entityId == 0) {
                        Storage.recorder.setEntityId(Storage.self.getEntityId());
                        respond(respondTo, "Now focusing on myself");
                        return;
                    }
                    ent = Storage.self.getEnvironment().getEntityById(entityId);
                    if (ent == null) {
                        respond(respondTo, "Unrecognized entity ID");
                    } else if (ent instanceof Player) {
                        Storage.recorder.setEntityId(Integer.parseInt(args[1]));
                        respond(respondTo, "Now focusing on " + Storage.self.getEnvironment().getPlayerNameByUUID(((Player) ent).getUUID()));
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
                case "sudo":
                    StringBuilder sb = new StringBuilder();
                    for (int i = 2; i < args.length - 1; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    sb.append(args[args.length - 1]);
                    onCommand(args[1], sb.toString(), false);
                    break;
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
                case "select":
                    if (args.length == 2) {
                        int slot = Integer.parseInt(args[1]);
                        Storage.self.selectSlot(slot);
                    }
                    break;
                case "slot":
                    if (args.length == 2) {
                        ItemStack is = Storage.self.getInventory().getSlot(Integer.parseInt(args[1]));
                        respond(respondTo, is.getAmount() + "x " + is.getTypeId() + ":" + is.getDamageValue());
                    }
                    break;
                case "break":
                    if (args.length == 4) {
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        int z = Integer.parseInt(args[3]);
                        Storage.self.breakBlock(x, y, z);
                    } else if (args.length == 5) {
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        int z = Integer.parseInt(args[3]);
                        int ms = Integer.parseInt(args[4]);
                        Storage.self.breakBlock(x, y, z, ms, ()->{});
                    }
                    break;
                case "place":
                    if (args.length == 4) {
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        int z = Integer.parseInt(args[3]);
                        Storage.self.placeBlock(x, y, z);
                    } else if (args.length == 5) {
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        int z = Integer.parseInt(args[3]);
                        int type = Integer.parseInt(args[4]);
                        InventoryUtil.findAndSelect(Material.fromTypeId(type));
                        Storage.self.placeBlock(x, y, z);
                    }
                    break;
                case "abilities":
                    if (args.length == 2) {
                        Storage.self.setAbilities(Integer.parseInt(args[1]));
                    }
                    break;
                case "action":
                    if (args.length == 2) {
                        Storage.self.performAction(Integer.parseInt(args[1]));
                    }
                    break;
                case "icl":
                    if (args.length == 2) {
                        Storage.self.getInventory().clickSlot(Integer.parseInt(args[1]), 0, 0);
                    }
                    break;
                case "icr":
                    if (args.length == 2) {
                        Storage.self.getInventory().clickSlot(Integer.parseInt(args[1]), 0, 1);
                    }
                    break;
                case "pixel":
                    TaskPixel tp = new TaskPixel(args);
                    if (tp.load()) {
                        int delay = Integer.parseInt(args[4]);
                        int id = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, tp, delay, delay);
                        tp.identify(id);
                        break;
                    }
                /*
                 case "run":
                 if (args.length >= 2) {
                 Task task = null;
                 String[] taskArgs = Arrays.copyOfRange(args, 2, args.length);
                 switch (args[1]) {
                 case "wall":
                 task = new TaskBuild(taskArgs);
                 break;
                 
                 if (task != null) {
                 int taskId = Storage.self.scheduleTask(Storage.plugin, task);
                 respond(respondTo, "Started Task " + taskId);
                 task.start();
                 }
                 }
                 break;
                 */

                case "rm":
                    if (args.length == 3) {
                        switch (args[1]) {
                            case "x":
                                double x = Double.parseDouble(args[2]);
                                Storage.self.moveTo(Storage.self.getLocation().getRelative(x, 0, 0));
                                break;
                            case "y":
                            case "why":
                                double y = Double.parseDouble(args[2]);
                                Storage.self.moveTo(Storage.self.getLocation().getRelative(0, y, 0));
                                break;
                            case "z":
                                double z = Double.parseDouble(args[2]);
                                Storage.self.moveTo(Storage.self.getLocation().getRelative(0, 0, z));
                                break;
                            case "t":
                                double t = Double.parseDouble(args[2]);
                                Storage.self.moveTo(Storage.self.getLocation().getRelative(0, 0, 0, t, 0));
                                break;
                            case "f":
                                double f = Double.parseDouble(args[2]);
                                Storage.self.moveTo(Storage.self.getLocation().getRelative(0, 0, 0, 0, f));
                                break;
                        }
                    }
                    break;
                case "exit":
                    Storage.self.shutdown();
                    break;
            }
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
