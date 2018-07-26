/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.oldshit;

import java.util.HashMap;
import java.util.LinkedList;
import zedly.zbot.event.ChatEvent;
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;

/**
 *
 * @author Dennis
 */
public class ChatCommandController implements Listener {
    
//    private final HashMap<String, 
    
    private final LinkedList<String> lineBuffer = new LinkedList<>();
    
    
    @EventHandler
    public void onChat(ChatEvent evt) {
        
    }
    
    public static class ChatCommandResponse {
    }    
    
    public static class WhoisResponse extends ChatCommandResponse {
        private final HashMap<String, String> infoLines;
        
        public WhoisResponse(HashMap<String, String> infoLines) {
            this.infoLines = infoLines;
        }
        
        public String getLine(String name) {
            return infoLines.get(name);
        }
        
    }
    
}
