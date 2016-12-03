/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

/**
 *
 * @author Dennis
 */
public class TaskSpamBroadcast implements Runnable {

    private final String[] messages = {
        "Give back to the community by donating items for the &2holiday drop party on Dec 26&a! Drop them off at the Christmas tree.",
        "Don't forget to give Santa your &2wish list&a! Put your list in the hopper under the spawn Christmas tree",
        "Wanna participate in the &2winter build contest&a? Leave an ag with your name to get a plot! Contest ends January 1st"
    };
    
    @Override
    public void run() {
        Storage.self.sendChat("/broadcast " + messages[Storage.rnd.nextInt(messages.length)]);
    }

}
