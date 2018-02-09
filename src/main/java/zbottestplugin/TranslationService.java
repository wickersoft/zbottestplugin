/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import zbottestplugin.HTTP.HTTPResponse;

/**
 *
 * @author Dennis
 */
public class TranslationService {

    private static final HashMap<String, Translation> translations = new HashMap<>();

    public static synchronized boolean hasTranslations(String player) {
        return translations.containsKey(player);
    }
    
    public static synchronized HashMap<String, String> getTranslationsFor(String player, String message) {
        HashMap<String, String> map = new HashMap<>();
        Translation trans = translations.get(player);
        for (String langpair : trans.getLangpairs()) {
            String translated = translate(message, langpair);
            for (String recipient : trans.getRecipientsForLangpair(langpair)) {
                map.put(recipient, translated);
            }
        }
        return map;
    }
    
    public static synchronized void addTranslationFor(String player, String recipient, String langpair) {
        if(translations.containsKey(player)) {
            Translation trans = translations.get(player);
            trans.addTranslation(recipient, langpair);
        } else {
            Translation trans = new Translation();
            trans.addTranslation(recipient, langpair);
            translations.put(player, trans);
        }
    }
    
    public static synchronized void removeTranslationFor(String player, String recipient) {
        if(translations.containsKey(player)) {
            Translation trans = translations.get(player);
            trans.removeTranslation(recipient);
            if(trans.isEmpty()) {
                translations.remove(player);
            }
        }
    }
    
    public static synchronized void disableAllTranslationsFor(String recipient) {
        for(Translation trans : translations.values()) {
            trans.removeTranslation(recipient);
        }
    }

    public static synchronized String translate(String message, String langpair) {
        try {
            String trans = new String(HTTP.httpFast("http://mymemory.translated.net/api/get?langpair=" + langpair + "&de=dennis.wickersheim%40gmail.com&q=" + URLEncoder.encode(message, "UTF-8"), 3000).getContent());
            String translated = SimpleJson.parse(trans);
            return translated;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "Unable to translate!";
    }
    
}
