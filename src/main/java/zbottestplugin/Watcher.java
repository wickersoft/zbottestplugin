/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zbottestplugin.oldshit.DijkstraCommands;
import zbottestplugin.oldshit.TranslationService;
import edu.kit.informatik.Node;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zbottestplugin.HTTP.HTTPResponse;
import zedly.zbot.entity.Player;
import zedly.zbot.Location;
import zedly.zbot.event.ChatEvent;
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;
import zedly.zbot.event.PlayerSpawnEvent;
import zedly.zbot.event.SelfTeleportEvent;
import zedly.zbot.event.entity.EntityMetadataEvent;
import zedly.zbot.entity.EntityMeta;
import zedly.zbot.event.RecipeResponseEvent;
import zedly.zbot.event.WindowOpenFinishEvent;
import zedly.zbot.inventory.FurnaceInventory;
import zedly.zbot.inventory.Inventory;

/**
 *
 * @author Dennis
 */
public class Watcher implements Listener {

    //private final Pattern p = Pattern.compile("^\\[[VMT]\\].*<(.*?)> (.*)$");
    //private final Pattern pmp = Pattern.compile("^\\[\\[[VMT]\\].*<(.*?)> -> me\\] (.*)$");
    private final Pattern p = Pattern.compile("^.*<(.*?)> (.*)$");
    private final Pattern pmp = Pattern.compile("^\\[.*<(.*?)> -> Me\\] (.*)$");
    private final Pattern urlp = Pattern.compile("(https?:\\/\\/\\S+)");
    private final Pattern cp;
    private final Pattern welcomePattern = Pattern.compile("^Everybody welcome (.+) to the server!$");
    private final String rp;

    private final ConcurrentLinkedQueue<Location> trackLocations = new ConcurrentLinkedQueue<>();
    private Location lastLoc;
    private boolean resolveOwnLink = false;
    private boolean resolveLinks = false;

    public Watcher() {
        resolveLinks = Storage.plugin.getConfig().getBoolean("resolveLinks", false);
        cp = Pattern.compile("^" + ZBotTestPlugin.config.getString("prefix", "zb") + " (.+)");
        if (Storage.self.getServerConnection().getIp().equals("85.131.153.100")
                || Storage.self.getServerConnection().getIp().equals("127.0.0.1")
                || Storage.self.getServerConnection().getIp().equals("155.254.35.239")) {
            rp = "";
        } else {
            rp = "\\[.+\\] ";
        }
    }

    @EventHandler
    public void onChat(ChatEvent evt) {
        if (evt.getMessage().startsWith("[LOTTERY]") && (evt.getMessage().contains("Congratulations")
                || evt.getMessage().contains("Draw"))) {
            Storage.self.scheduleSyncDelayedTask(Storage.plugin, (Runnable) () -> {
                //Storage.self.sendChat("/lot buy");
            }, 10000);
            return;
        }

        if (evt.getMessage().matches("^Player .+ banned .+ for: .+") && Storage.plugin.getConfig().getBoolean("banReaction", false)) {
            Storage.self.sendChat(Storage.banMessages.get(Storage.rnd.nextInt(Storage.banMessages.size())));
            return;
        }

        Matcher m = welcomePattern.matcher(evt.getMessage());
        if (m.find()) {
            String user = m.group(1);
            Storage.self.scheduleSyncDelayedTask(Storage.plugin, () -> {
                Storage.self.sendChat("/msg " + user + " Welcome, " + user + "!");
            }, 12000);
            return;
        }

        m = p.matcher(evt.getMessage());
        if (m.find()) {
            String user = m.group(1).replaceAll(rp, "");
            String message = m.group(2);

            Matcher cm = cp.matcher(message);
            if (cm.find()) {
                try {
                    CommandProcessor.onCommand(user, cm.group(1), false);
                    DijkstraCommands.onCommand(user, cm.group(1), false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Storage.self.sendChat("An internal error occurred: " + ex.getClass());
                }
                return;
            }

            if (TranslationService.hasTranslations(user)) {
                Storage.translatorThread.performTranslation(user, message);
            }

            if (resolveLinks && (!user.equals(Storage.self.getServerConnection().getUsername()) || resolveOwnLink)) {
                m = urlp.matcher(evt.getMessage());
                if (m.find()) {
                    String url = m.group(1);
                    new Thread(() -> {
                        String title = getTitleForSite(url, 0);
                        if (title != null) {
                            Storage.self.sendChat(title);
                        }
                    }).start();
                }
                resolveOwnLink = false;
            }
        }
        m = pmp.matcher(evt.getMessage());
        if (m.find()) {
            String user = m.group(1).replaceAll(rp, "");
            String message = m.group(2);
            System.out.println("PM: " + message);
            try {
                CommandProcessor.onCommand(user, message, true);
            } catch (Exception ex) {
                ex.printStackTrace();
                Storage.self.sendChat("/r An internal error occurred: " + ex.getClass());
            }
            return;
        }
    }

    @EventHandler
    public void onPlayerSpawn(PlayerSpawnEvent evt) {
        Location l = evt.getEntity().getLocation();
        if (evt.getEntity() instanceof Player) {
            Player ep = (Player) evt.getEntity();
            System.out.println("Player " + Storage.self.getEnvironment().getPlayerNameByUUID(ep.getUUID()) + " (" + ep.getUUID() + ") has EID " + ep.getEntityId());
        }
    }
    
    @EventHandler
    public void recipeResponse(RecipeResponseEvent evt) {
        System.out.println(evt.getRecipeId());        
    }

    @EventHandler
    public void onSelfTeleport(SelfTeleportEvent evt) {
        System.out.println("Teleported to " + evt.getNewLocation());
        if (Storage.graph != null) {
            Node n = Storage.graph.getClosestNodeTo(Storage.self.getLocation());
            if (n.getLocation().distanceTo(evt.getNewLocation()) < 5) {
                Storage.roamer.setCurrentNode(n);
            } else {
                Storage.roamer.setRoaming(false);
            }
        }
    }

    @EventHandler
    public void onEntityMeta(EntityMetadataEvent evt) {
        if (Storage.debugEntity == evt.getEntity().getEntityId()) {
            Map<Integer, EntityMeta> metaMap = evt.getMap();
            System.out.println("EntityMeta update for " + Storage.debugEntity + ":");
            for (int i : metaMap.keySet()) {
                System.out.println(i + ": " + metaMap.get(i));
            }
            System.out.println("");
        }
    }

    public void previewNextLink() {
        resolveOwnLink = true;
    }

    private String getTitleForSite(String urlString, int depth) {
        if (depth > 5) {
            return null;
        }
        try {
            URL url = new URL(urlString);
            HTTPResponse http = HTTP.get(url);
            if (http.getHeaders().containsKey("Location")) {
                return getTitleForSite(http.getHeaders().get("Location").get(0), depth + 1);
            }
            String html = new String(http.getContent(), "UTF-8");
            if (html.equals("")) {
                return null;
            }
            String title = StringUtil.extract(html, "<title", "/title>");
            if (title == null) {
                return null;
            }
            title = StringUtil.extract(title, ">", "<");
            title = title.replaceAll("&", "&&");
            title = title.replaceAll("/", "&f/");
            return title;
        } catch (IOException ex) {
            return null;
        }
    }

    @EventHandler
    public void onWindowOpenFinish(WindowOpenFinishEvent evt) {
        Inventory inv = evt.getInventory();
        if (inv instanceof FurnaceInventory) {
            FurnaceInventory fi = (FurnaceInventory) inv;
            Storage.self.sendChat("Opened furnace. Flame " + fi.getRemainingBurnTime() + "/" + fi.getMaxBurnTime() + " Arrow " + fi.getProgress() + "/" + fi.getMaxProgress());
        }
    }
}
