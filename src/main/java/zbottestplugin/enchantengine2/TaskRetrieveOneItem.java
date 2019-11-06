package zbottestplugin.enchantengine2;

import zbottestplugin.enchantengine.*;
import zbottestplugin.InventoryUtil;
import zbottestplugin.Storage;
import zbottestplugin.task.Task;
import zedly.zbot.Location;
import zedly.zbot.inventory.ItemStack;

public class TaskRetrieveOneItem extends Task {

    private final int slotId;

    public TaskRetrieveOneItem(int slot) {
        super(50);
        this.slotId = slot;
    }

    public void run() {
        try {
            LibraryLocation ll = new LibraryLocation(slotId);
            Location gotoLoc = ll.getWalkLocation();

            Storage.self.sendChat("Fetching item " + EnchantEngine.friendlyIndex(slotId));
            ai.moveTo(gotoLoc.centerHorizontally());
            ai.openContainer(ll.getLocation());
            int chestSlot = ll.getSlot();

            ItemStack is = Storage.self.getInventory().getSlot(chestSlot);
            if (is == null) {
                Storage.self.sendChat("Slot " + EnchantEngine.friendlyIndex(ll) + "(" + slotId + ") contains nothing");
                unregister();
                return;
            }
            int destSlot = InventoryUtil.findFreeStorageSlot(true);
            if (ai.transferItem(chestSlot, destSlot) != 0) {
                Storage.self.sendChat("Couldn't get the item from the chest :(");
            }
            destSlot -= Storage.self.getInventory().getStaticOffset();
            ai.closeContainer();
            
            ai.moveTo(zbottestplugin.enchantengine2.EnchantEngine.OUTPUT_LOC.centerHorizontally());
            ai.openContainer(EnchantEngine.OUTPUT_CHEST);          
            ai.depositSlot(destSlot + Storage.self.getInventory().getStaticOffset());            
            ai.closeContainer();
            Storage.self.sendChat("Done!");
            ai.tick();
            ai.moveTo(new Location(-902, 38, 4859).centerHorizontally());
            Storage.self.sendChat("/home spawner");
        } catch (InterruptedException ex) {
        }
        unregister();
    }
}
