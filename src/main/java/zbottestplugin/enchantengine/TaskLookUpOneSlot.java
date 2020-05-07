/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine;

import java.util.function.Predicate;
import zbottestplugin.Storage;
import zbottestplugin.oldshit.BlockingAI;
import zbottestplugin.task.Task;
import zedly.zbot.Location;
import zedly.zbot.BlockFace;
import zedly.zbot.event.WindowOpenFinishEvent;
import zedly.zbot.inventory.ChestInventory;
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

    public void work() {

        try {
            LibraryLocation ll = new LibraryLocation(slotId);
            Location gotoLoc = ll.getWalkLocation();

            Storage.self.sendChat("Walking to " + ll.getX() + " 35 " + ll.getWalkZ());
            ai.moveTo(gotoLoc.centerHorizontally());

            Storage.self.sendChat("Opening " + ll.getX() + " " + ll.getY() + " " + ll.getZ());
            Storage.self.placeBlock(ll.getLocation(), (ll.getX() % 2 != 0) ? BlockFace.SOUTH : BlockFace.NORTH);

            boolean opened = ai.waitForEvent(WindowOpenFinishEvent.class, 5000);
            
            if (!opened) {
                Storage.self.sendChat("Error: Wait for OpenWindowEvent timed out");
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
