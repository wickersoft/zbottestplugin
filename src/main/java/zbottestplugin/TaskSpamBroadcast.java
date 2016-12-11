/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.List;

/**
 *
 * @author Dennis
 */
public class TaskSpamBroadcast implements Runnable {

    private final List<String> messages = Storage.plugin.getConfig().getList("broadcast.msgs", String.class);
    
    @Override
    public void run() {
        Storage.self.sendChat("/broadcast " + messages.get(Storage.rnd.nextInt(messages.size())));
    }

}
