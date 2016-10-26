/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import edu.kit.informatik.Graph;
import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import zedly.zbot.self.Self;

/**
 *
 * @author Dennis
 */
public class Storage {
    public static Self self;
    public static BufferedOutputStream os;
    public static ZBotTestPlugin plugin;
    public static MotionRecorder recorder = new MotionRecorder();
    public static ArrayList<String> banMessages = new ArrayList<>();
    public static Random rnd = new Random();
    public static Synchronizer synch = new Synchronizer();
    public static ThreadTranslator translatorThread = new ThreadTranslator();
    public static final HashSet<String> languageCodes = new HashSet<>();
    public static final HashMap<String, String> languageAliases = new HashMap<>();
    public static final TaskRoam roamer = new TaskRoam();
    public static Graph graph;
    public static TaskDefender defender = new TaskDefender();
    public static int debugEntity = 0;
    public static TaskFish fish;
    public static int zombieTaskId = -1;
    
    
    static {
        languageCodes.add("de");
        languageCodes.add("en");
        languageCodes.add("it");
        languageCodes.add("fr");
        languageCodes.add("sv");
        languageCodes.add("nl");
        languageCodes.add("es");
        languageCodes.add("no");
        languageCodes.add("da");
        
        languageAliases.put("german", "de");
        languageAliases.put("english", "en");
        languageAliases.put("italian", "it");
        languageAliases.put("french", "fr");
        languageAliases.put("swedish", "sv");
        languageAliases.put("dutch", "nl");
        languageAliases.put("spanish", "es");
        languageAliases.put("norwegian", "no");
        languageAliases.put("danish", "da");
    }
}
