/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Dennis
 */
public class Translation {

    private final HashMap<String, List<String>> playersPerLangpair = new HashMap<>();
    private final HashMap<String, String> langpairsPerPlayer = new HashMap<>();

    public synchronized void addTranslation(String recipient, String langpair) {
        if (langpairsPerPlayer.containsKey(recipient)) {
            playersPerLangpair.get(langpairsPerPlayer.get(recipient)).remove(recipient);
        }
        langpairsPerPlayer.put(recipient, langpair);
        if (playersPerLangpair.containsKey(langpair)) {
            List<String> recipients = playersPerLangpair.get(langpair);
            if (!recipients.contains(recipient)) {
                recipients.add(recipient);
            }
        } else {
            List<String> recipients = new ArrayList<>();
            recipients.add(recipient);
            playersPerLangpair.put(langpair, recipients);
        }
    }

    public synchronized void removeTranslation(String recipient, String langpair) {
        if (playersPerLangpair.containsKey(langpair)) {
            List<String> recipients = playersPerLangpair.get(langpair);
            recipients.remove(recipient);
        }
    }

    public synchronized void removeTranslation(String recipient) {
        List<String> recipients = playersPerLangpair.get(langpairsPerPlayer.get(recipient));
        recipients.remove(recipient);
        langpairsPerPlayer.remove(recipient);
    }

    public synchronized List<String> getRecipientsForLangpair(String langpair) {
        return playersPerLangpair.get(langpair);
    }

    public synchronized Set<String> getLangpairs() {
        return playersPerLangpair.keySet();
    }
    
    public synchronized boolean isEmpty() {
        return langpairsPerPlayer.isEmpty();
    }
}
