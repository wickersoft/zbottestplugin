/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zedly.zbot.event.ChatEvent;
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;

/**
 *
 * @author Dennis
 */
public class ScrambleWatcher implements Listener {

    private static final File scrambleDictionaryFile = new File(Storage.plugin.getDataFolder(), "scramble.txt");
    private static final ArrayList<String> scrambleWords = new ArrayList<>();
    private String unknownScramble = null;
    private final Pattern scramblePattern = Pattern.compile("\"hoverEvent\":\\{\"action\":\"show_text\",\"value\":\\[\\{\"color\":\"white\",\"text\":\"(.+?)\"\\}\\]\\}");
    private final Pattern scrambleSolvedPattern = Pattern.compile("\\{\"color\":\"white\",\"text\":\"(.+?) \"\\}");

    public ScrambleWatcher() {
        loadScrambleDictionary();
    }

    @EventHandler
    public void onChat(ChatEvent evt) {
        //System.out.println(evt.getRawMessage());
        if (evt.getRawMessage().contains("{\"color\":\"aqua\",\"text\":\"Hover for the word to unscramble!\"}")) {
            boolean answerScramble = Storage.rnd.nextDouble() < ZBotTestPlugin.config.getDouble("scrambleChance", 0);
            Matcher m = scramblePattern.matcher(evt.getRawMessage());
            if (m.find()) {
                String anagram = m.group(1);
                for (String s : scrambleWords) {
                    if (StringUtil.isAnagram(anagram, s)) {
                        if (answerScramble) {
                            Storage.self.sendChat(s);
                        }
                        System.out.println("Scramble Challenge: " + anagram + "  Solution: " + s);
                        return;
                    }
                }
                System.out.println("Scramble: Unknown word " + anagram);
                return;
            }

        }
        String solution = null;
        if (evt.getRawMessage().contains("{\"color\":\"green\",\"text\":\"unscrambled the word \"}")) {
            Matcher m = scrambleSolvedPattern.matcher(evt.getRawMessage());
            if (m.find() && m.find()) {
                solution = m.group(1);
            }
        } else if (evt.getRawMessage().contains("{\"color\":\"red\",\"text\":\"Nobody got the word \"}")) {
            Matcher m = scrambleSolvedPattern.matcher(evt.getRawMessage());
            if (m.find()) {
                solution = m.group(1);
            }
        }
        if (solution != null) {
            if (!scrambleWords.contains(solution)) {
                scrambleWords.add(solution);
                saveScrambleDictionary();
                System.out.println("Scramble: Added the word \"" + solution + "\" to the dictionary");
            }
        }
    }

    private void loadScrambleDictionary() {
        try (BufferedReader br = new BufferedReader(new FileReader(scrambleDictionaryFile))) {
            String t;
            while ((t = br.readLine()) != null) {
                scrambleWords.add(t);
            }
            br.close();
        } catch (IOException ex) {
        }
    }

    private void saveScrambleDictionary() {
        try {
            FileOutputStream fos = new FileOutputStream(scrambleDictionaryFile);
            for (String s : scrambleWords) {
                fos.write(s.getBytes());
                fos.write("\r\n".getBytes());
            }
            fos.close();
        } catch (IOException ex) {
        }
    }
}
