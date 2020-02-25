/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import zedly.zbot.YamlConfiguration;
import zedly.zbot.self.Self;
import zedly.zbot.plugin.ZBotPlugin;

/**
 *
 * @author Dennis
 */
public class ZBotTestPlugin extends ZBotPlugin {

    public static final List<String> admins = new ArrayList<>();
    private static final ArrayList<String> resourceFileNames = new ArrayList<>();
    public static YamlConfiguration config;

    @Override
    public void onEnable(Self self) {
        config = getConfig();
        Storage.plugin = this;
        Storage.self = self;
        Storage.spammer = new TaskSpamBroadcast();
        Storage.follower = new TaskHeadFollow(config.getBoolean("follow.spam", false));

        setupPluginFolder();
        saveDefaultConfig(false);
        loadResources();
        Storage.translatorThread.start();
        Storage.watcher = new Watcher();

        if (config.getBoolean("follow.enable", false)) {
            System.out.println("Enabling Follow watcher");
            self.registerEvents(Storage.follower);
        }

        admins.clear();
        for (String item : config.getList("admins", String.class)) {
            admins.add(item);
        }

        self.registerEvents(Storage.recorder);
        self.registerEvents(Storage.watcher);
        self.registerEvents(new ScrambleWatcher());
    }

    @Override
    public void onJoin() {
        System.out.println("Joined! My EID: " + Storage.self.getEntityId());
        Storage.self.scheduleSyncRepeatingTask(this, Storage.synch, 50, 50);
        Storage.self.scheduleSyncRepeatingTask(this, Storage.follower, 15000, 15000);

        if (config.getBoolean("antiafk", false)) {
            Storage.self.scheduleSyncRepeatingTask(this, () -> {
                Storage.self.swingArm(true);
            }, 60000, 60000);
        }

        if (config.getBoolean("roam", false)) {
            System.out.println("Enabling Roamer");
            Storage.self.scheduleSyncRepeatingTask(this, Storage.roamer, 150, 150);
        }

        if (config.getBoolean("broadcast.enable", false)) {
            System.out.println("Enabling Broadcaster");
            Storage.self.scheduleSyncRepeatingTask(this, Storage.spammer, config.getInt("broadcast.delay", 900) * 1000);
        }
        if (config.getBoolean("defend", false)) {
            System.out.println("Enabling Defender");
            Storage.self.scheduleSyncRepeatingTask(this, Storage.defender, config.getInt("defend-delay", 1000), config.getInt("defend-delay", 1000));
        }
    }

    public void onQuit() {
        System.out.println("Quit!");
    }

    @Override
    public void onDisable() {
        Storage.self.unregisterEvents(Storage.watcher);
        Storage.translatorThread.interrupt();
    }

    private void loadResources() {
        try {
            File file = new File(getDataFolder(), "goodbye.txt");
            byte[] bin = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(bin);
            fis.close();
            String[] lines = new String(bin).split("\n");
            for (String s : lines) {
                Storage.banMessages.add(s);
            }
        } catch (IOException ex) {
            System.err.println("Unable to load ban messages!");
        }
    }

    private void setupPluginFolder() {
        for (String filename : resourceFileNames) {
            try {
                File resourceFile = new File(getDataFolder(), filename);
                if (!resourceFile.exists()) {
                    InputStream is = this.getClass().getResourceAsStream("/" + filename);
                    FileOutputStream fos = new FileOutputStream(resourceFile);
                    do {
                        byte[] bytes = new byte[is.available()];
                        is.read(bytes);
                        fos.write(bytes);
                    } while (is.available() != 0);
                    fos.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    static {
        resourceFileNames.add("goodbye.txt");
    }
}
