/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Dennis
 */
public class ThreadTranslator extends Thread {

    private final ConcurrentLinkedQueue<TranslationInstance> queue = new ConcurrentLinkedQueue<>();

    public ThreadTranslator() {
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                TranslationInstance instance = queue.deq();
                System.out.println("Translating message from " + instance.user);
                final HashMap<String, String> translations = TranslationService.getTranslationsFor(instance.user, instance.message);
                Storage.synch.synchronize((Runnable) () -> {
                    for (String recipient : translations.keySet()) {
                        ArrayList<String> lines = StringUtil.wrap(translations.get(recipient), 80);
                        for (String s : lines) {
                            Storage.self.sendChat("/msg " + recipient + " " + s);
                        }
                    }
                });
            }
        } catch (InterruptedException ex) {
        }
    }

    public synchronized void performTranslation(String user, String message) {
        queue.enq(new TranslationInstance(user, message));
    }

    private class TranslationInstance {

        public final String user, message;

        public TranslationInstance(String user, String message) {
            this.user = user;
            this.message = message;
        }
    }
}
