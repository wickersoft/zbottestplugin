/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine2;

import zbottestplugin.Storage;
import zbottestplugin.task.Task;
import zedly.zbot.Location;
import zedly.zbot.event.WindowOpenFinishEvent;
import zedly.zbot.inventory.ItemStack;

/**
 *
 * @author Dennis
 */
public class TaskLookUpOneSlot extends Task {

    private final int slotId;
    
    public TaskLookUpOneSlot(int slot) {
        super(100);
        this.slotId = slot;
    }

    public void run() {

        try {
            LibraryLocation ll = new LibraryLocation(slotId);
            Location gotoLoc = ll.getWalkLocation();

            Storage.self.sendChat("Walking to " + gotoLoc.toString());
            ai.moveTo(gotoLoc.centerHorizontally());

            Storage.self.sendChat("Opening " + ll.getLocation().toString());
            Storage.self.placeBlock(ll.getLocation(), ll.getFaceToClick());

            boolean opened = ai.waitForEvent(WindowOpenFinishEvent.class, 5000);
            
            if (!opened) {
                Storage.self.sendChat("Error: Wait for OpenWindowEvent timed out");
                unregister();
                return;
            }

            int chestSlot = ll.getSlot();
            ItemStack is = Storage.self.getInventory().getSlot(chestSlot);
            Storage.self.sendChat("Slot " + EnchantEngine.friendlyIndex(ll) + "(" + slotId + ") contains " + ((is != null) ? EnchantEngine.stringifyItem(is) : "nothing"));

            Storage.self.closeWindow();

        } catch (InterruptedException ex) {
        }

        unregister();
    }
}
