/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import edu.kit.informatik.Node;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zedly.zbot.entity.Player;
import zedly.zbot.Location;
import zedly.zbot.event.ChatEvent;
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;
import zedly.zbot.event.PlayerSpawnEvent;
import zedly.zbot.event.SelfTeleportEvent;
import zedly.zbot.event.entity.EntityMetadataEvent;
import zedly.zbot.entity.EntityMeta;

/**
 *
 * @author Dennis
 */
public class Watcher implements Listener {

    private final Pattern p = Pattern.compile("^<(.*?)> (.*)$");
    private final Pattern pmp = Pattern.compile("^\\[(.*?) -> me\\] (.*)$");
    private final Pattern cp = Pattern.compile("^(sm|ms|sayaka\\,|bb|rb) (.+)");
    private String rp = "\\[.+\\] ";

    private final ConcurrentLinkedQueue<Location> trackLocations = new ConcurrentLinkedQueue<>();
    private Location lastLoc;

    public void useNyanRankPattern() {
        rp = "\\(.+\\)";
    }

    @EventHandler
    public void onChat(ChatEvent evt) {
        try {
            Storage.os.write(evt.getMessage().getBytes());
            Storage.os.write(13); // \r
            Storage.os.write(10); // \n
        } catch (IOException ex) {
        }
        
        if (evt.getMessage().startsWith("[LOTTERY]") && (evt.getMessage().contains("Congratulations")
                || evt.getMessage().contains("Draw"))) {
            Storage.self.scheduleSyncDelayedTask(Storage.plugin, (Runnable) () -> {
                Storage.self.sendChat("/lot buy");
            }, 10000);
            return;
        }

        if (evt.getMessage().matches("^Player .+ banned .+ for: .+")) {
            Storage.self.sendChat(Storage.banMessages.get(Storage.rnd.nextInt(Storage.banMessages.size())));
            return;
        }

        Matcher m = p.matcher(evt.getMessage());
        if (m.find()) {
            String user = m.group(1).replaceAll(rp, "");
            String message = m.group(2);

            Matcher cm = cp.matcher(message);
            if (cm.find()) {
                System.out.println(cm.group(1) + " " + cm.group(2));
                try {
                    CommandProcessor.onCommand(user, cm.group(2), false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Storage.self.sendChat("An internal error occurred: " + ex.getClass());
                }
                return;
            }

            if (TranslationService.hasTranslations(user)) {
                Storage.translatorThread.performTranslation(user, message);
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
    public void onPlayerSpawn(PlayerSpawnEvent evt
    ) {
        Location l = evt.getEntity().getLocation();
        if (evt.getEntity() instanceof Player) {
            Player ep = (Player) evt.getEntity();
            System.out.println("Player " + Storage.self.getEnvironment().getPlayerNameByUUID(ep.getUUID()) + " (" + ep.getUUID() + ") has EID " + ep.getEntityId());
        }
    }

    @EventHandler
    public void onSelfTeleport(SelfTeleportEvent evt
    ) {
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
    public void onEntityMeta(EntityMetadataEvent evt
    ) {
        if (Storage.debugEntity == evt.getEntity().getEntityId()) {
            Map<Integer, EntityMeta> metaMap = evt.getMap();
            System.out.println("EntityMeta update for " + Storage.debugEntity + ":");
            for (int i : metaMap.keySet()) {
                System.out.println(i + ": " + metaMap.get(i));
            }
            System.out.println("");
        }
    }
}
