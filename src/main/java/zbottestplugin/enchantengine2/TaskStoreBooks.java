/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine2;

import zbottestplugin.InventoryUtil;
import zbottestplugin.Storage;
import zbottestplugin.enchantengine.*;
import zbottestplugin.task.Task;
import zedly.zbot.Location;
import zedly.zbot.Material;
import zedly.zbot.inventory.ItemStack;

/**
 *
 * @author Dennis
 */
public class TaskStoreBooks extends Task {

    private int inboxIndex = 0;

    private int libraryIndex = 0;
    private int stored_books = 0;

    public TaskStoreBooks() {
        super(50);
    }

    public void run() {
        try {
            while (true) {
                boolean allBooksGone = loadUp();
                loadOff();
                if (allBooksGone) {
                    ai.moveTo(EnchantEngine.HOME_LOC);
                    Storage.self.sendChat("Done! Indexed " + EnchantEngine.getNumIndexedItems() + " items (added " + stored_books + ")");
                    unregister();
                    return;
                }
            }
        } catch (InterruptedException ex) {
            unregister();
            return;
        }
    }

    private boolean loadUp() throws InterruptedException {
        while (tryGrabMoreItems()) {
            if ((inboxIndex += 54) >= 5616) {
                return true;
            }
        }
        return false;
    }

    private boolean tryGrabMoreItems() throws InterruptedException {
        InboxLocation grabLoc = new InboxLocation(inboxIndex);
        ai.moveTo(grabLoc.getWalkLocation());
        System.out.println("opening chest " + grabLoc.toString());
        ai.openContainer(grabLoc.getLocation());
        for (int i = 0; i < 54; i++) {
            if (InventoryUtil.findFreeStorageSlot(true) == -1) {
                System.out.println("closing chest " + grabLoc.toString());
                ai.closeContainer();
                return false;
            }
            if (Storage.self.getInventory().getSlot(i) != null) {
                ai.withdrawSlot(i);
            }
        }
        System.out.println("closing chest " + grabLoc.toString());
        ai.closeContainer();
        return true;
    }

    private boolean loadOff() throws InterruptedException {
        while (tryDumpMoreItems()) {
            libraryIndex += EnchantEngine.CHEST_SIZE;
        }
        return false;
    }

    private boolean tryDumpMoreItems() throws InterruptedException {
        LibraryLocation dumpLoc = new LibraryLocation(libraryIndex);
        ai.moveTo(dumpLoc.getWalkLocation());
        System.out.println("closing chest " + EnchantEngine.friendlyIndex(dumpLoc));
        ai.openContainer(dumpLoc.getLocation());
        int staticOffset = Storage.self.getInventory().getStaticOffset();

        for (int i = staticOffset; i < staticOffset + 36; i++) {
            if (InventoryUtil.findFreeStorageSlot(false) == -1) {
                scanContainer();
                ai.closeContainer();
                return true;
            }
            if (Storage.self.getInventory().getSlot(i) != null
                    && Storage.self.getInventory().getSlot(i).getType() == Material.ENCHANTED_BOOK) {
                ai.depositSlot(i);
                stored_books++;
            }
        }

        scanContainer();
        System.out.println("closing chest " + EnchantEngine.friendlyIndex(dumpLoc));
        ai.closeContainer();
        return false;
    }

    private void scanContainer() {
        for (int i = 0; i < EnchantEngine.CHEST_SIZE; i++) {
            ItemStack is = Storage.self.getInventory().getSlot(i);
            EnchantEngine.rememberItemString(libraryIndex + i, EnchantEngine.stringifyItem(is));
        }
    }
}
