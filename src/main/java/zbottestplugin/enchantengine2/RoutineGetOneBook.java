/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine2;

import zbottestplugin.InventoryUtil;
import zbottestplugin.Storage;
import zbottestplugin.oldshit.BlockingAI;
import zbottestplugin.task.Routine;
import zedly.zbot.Location;
import zedly.zbot.event.WindowOpenFinishEvent;
import zedly.zbot.inventory.ItemStack;

/**
 *
 * @author Dennis
 */
public class RoutineGetOneBook extends Routine {

    private boolean success = false;
    private int slotId;

    public RoutineGetOneBook(int slotId, BlockingAI ai) {
        super(ai);
        this.slotId = slotId;
    }

    @Override
    public void work() throws InterruptedException {
        LibraryLocation ll = new LibraryLocation(slotId);
        Location gotoLoc = ll.getWalkLocation();

        System.out.println("Fetching item " + EnchantEngine.friendlyIndex(slotId));
        Storage.self.sendChat("/home amazon");

        if (!ai.openContainer(EnchantEngine.AMAZON_OUTPUT_CHEST, 10000)) {
            Storage.self.sendChat("Sorry, I'm lagging too much");
            return;
        }

        ai.moveTo(gotoLoc.centerHorizontally());
        ai.openContainer(ll.getLocation());
        int chestSlot = ll.getSlot();

        ItemStack is = Storage.self.getInventory().getSlot(chestSlot);
        if (is == null) {
            Storage.self.sendChat("Slot " + EnchantEngine.friendlyIndex(ll) + "(" + slotId + ") contains nothing");
            Storage.self.sendChat("/home mall");
            return;
        }
        int destSlot = InventoryUtil.findFreeStorageSlot(true);
        if (ai.transferItem(chestSlot, destSlot) != 0) {
            Storage.self.sendChat("Couldn't get the item from the chest :(");
        }
        destSlot -= Storage.self.getInventory().getStaticOffset();
        ai.closeContainer();

        ai.moveTo(zbottestplugin.enchantengine2.EnchantEngine.OUTPUT_LOC.centerHorizontally());
        ai.openContainer(EnchantEngine.AMAZON_OUTPUT_CHEST);
        ai.depositSlot(destSlot + Storage.self.getInventory().getStaticOffset());
        ai.closeContainer();
        EnchantEngine.rememberItemString(slotId, null);
        Storage.self.sendChat("Done!");
        ai.tick();
        ai.moveTo(new Location(-902, 38, 4859).centerHorizontally());
        Storage.self.sendChat("/home spawner");
    }

    public boolean successful() {
        return success;
    }
}
