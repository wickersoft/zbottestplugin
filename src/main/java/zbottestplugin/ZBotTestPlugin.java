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
import java.util.ArrayList;
import zedly.zbot.self.Self;
import zedly.zbot.plugin.ZBotPlugin;

/**
 *
 * @author Dennis
 */
public class ZBotTestPlugin extends ZBotPlugin {

    private Watcher watcher;
    private static final ArrayList<String> resourceFileNames = new ArrayList<>();

    
    @Override
    public void onEnable(Self self) {
        Storage.self = self;
        Storage.plugin = this;
        setupPluginFolder();
        loadResources();
        Storage.translatorThread.start();
        watcher = new Watcher();
        self.registerEvents(Storage.recorder);
        self.registerEvents(watcher);
        try {
            new File("logs").mkdir();
            Storage.os = new BufferedOutputStream(new FileOutputStream(new File("logs/" + System.currentTimeMillis() + ".txt")));
        } catch (IOException ex) {
        }
    }
    
    @Override
    public void onJoin() {
        System.out.println("Joined! My EID: " + Storage.self.getEntityId());
        Storage.self.scheduleSyncRepeatingTask(this, Storage.synch, 50, 50);
        Storage.self.scheduleSyncRepeatingTask(this, Storage.roamer, 150, 150);
        Storage.self.scheduleSyncRepeatingTask(this, Storage.defender, 500, 500);
    }
    
    @Override
    public void onQuit() {
        System.out.println("Quit!");
    }

    @Override
    public void onDisable() {
        Storage.self.unregisterEvents(watcher);
        Storage.translatorThread.interrupt();
        try {
            Storage.os.flush();
            Storage.os.close();
        } catch (IOException ex) {
        }
    }

    private void loadResources() {
        try {
            File file = new File("plugins/ZBotTestPlugin/goodbye.txt");
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
        File file = new File("plugins/ZBotTestPlugin");
        if (!file.exists()) {
            file.mkdir();
        }
        for (String filename : resourceFileNames) {
            try {
                File resourceFile = new File("plugins/ZBotTestPlugin/" + filename);
                if (!resourceFile.exists()) {
                    InputStream is = this.getClass().getResourceAsStream("resource/" + filename);
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
