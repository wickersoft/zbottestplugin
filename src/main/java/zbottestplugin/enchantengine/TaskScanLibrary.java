/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine;

import zbottestplugin.Storage;
import zbottestplugin.task.Task;
import zedly.zbot.inventory.ItemStack;

/**
 *
 * @author Dennis
 */
public class TaskScanLibrary extends Task {

    private static final int MAX_INDEX = 1000;
    private static final int CHEST_SIZE = 54;

    public TaskScanLibrary() {
        super(50);
    }

    public void run() {
        int seenBooks = 0;
        try {
            for (int slotId = 0; slotId < MAX_INDEX; slotId += CHEST_SIZE) {
                LibraryLocation ll = new LibraryLocation(slotId);
                ai.moveTo(ll.getWalkLocation());

                System.out.println("opening chest " + EnchantEngine.friendlyIndex(ll));
                if(!ai.openContainer(ll.getX(), ll.getY(), ll.getZ())) {
                    System.out.println("Error opening chest " + EnchantEngine.friendlyIndex(ll));
                    continue;
                }
                
                for (int i = 0; i < CHEST_SIZE; i++) {
                    ItemStack is = Storage.self.getInventory().getSlot(i);
                    if(is != null) {
                        seenBooks++;
                    }
                    EnchantEngine.rememberItemString(slotId + i, EnchantEngine.stringifyItem(is));
                }
                
                System.out.println("seenbooks: " + seenBooks + ". closing chest " + EnchantEngine.friendlyIndex(ll));
                Storage.self.closeWindow();
                ai.tick();
            }
            Storage.self.sendChat("Done! Indexed " + seenBooks + " books");
            ai.tick();
            Storage.self.sendChat("/home spawner");
            unregister();
        } catch (InterruptedException ex) {
        }
    }

}
