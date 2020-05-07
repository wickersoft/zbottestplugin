/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine2;

import zbottestplugin.Storage;
import zbottestplugin.task.Task;
import zedly.zbot.inventory.ItemStack;

/**
 *
 * @author Dennis
 */
public class TaskScanLibrary extends Task {

    public TaskScanLibrary() {
        super(50);
    }

    public void work() {
        int seenBooks = 0;
        EnchantEngine.connect();
        try {
            for (int slotId = 0;; slotId += EnchantEngine.CHEST_SIZE) {
                LibraryLocation ll = new LibraryLocation(slotId);
                ai.moveTo(ll.getWalkLocation());

                System.out.println("opening chest " + EnchantEngine.friendlyIndex(ll));
                if (!ai.openContainer(ll.getLocation())) {
                    System.out.println("Error opening chest " + EnchantEngine.friendlyIndex(ll));
                    continue;
                }

                boolean chestEmpty = true;
                for (int i = 0; i < EnchantEngine.CHEST_SIZE; i++) {
                    ItemStack is = Storage.self.getInventory().getSlot(i);
                    if (is != null) {
                        seenBooks++;
                        chestEmpty = false;
                    }
                    EnchantEngine.cacheItemString(slotId + i, EnchantEngine.stringifyItem(is));
                }

                System.out.println("seenbooks: " + seenBooks + ". closing chest " + EnchantEngine.friendlyIndex(ll));
                Storage.self.closeWindow();
                
                ai.tick();
                if (chestEmpty) {
                    break;
                }
            }
            Storage.self.sendChat("Uploading Cache to SQL server..");
            EnchantEngine.flushCache();
            Storage.self.sendChat("Done! Indexed " + seenBooks + " items");
            ai.moveTo(EnchantEngine.HOME_LOC);
        } catch (InterruptedException ex) {
        }
        unregister();
    }

}
