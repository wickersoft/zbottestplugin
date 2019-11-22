/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine2;

import java.util.ArrayList;
import java.util.List;
import zbottestplugin.InventoryUtil;
import zbottestplugin.Storage;
import zbottestplugin.task.Task;
import zedly.zbot.BlockFace;
import zedly.zbot.Location;
import zedly.zbot.Material;
import zedly.zbot.entity.Entity;
import zedly.zbot.entity.Villager;
import zedly.zbot.event.WindowOpenFinishEvent;
import zedly.zbot.inventory.Trade;
import zedly.zbot.inventory.VillagerInventory;

/**
 *
 * @author Dennis
 */
public class TaskSellBooks extends Task {

    private static final Location BOOK_TRADE_LOC = new Location(167, 143, -8758).centerHorizontally();

    private static final Location EMERALD_WALK_LOC = new Location(175, 143, -8774).centerHorizontally();
    private static final Location EMERALD_TESSERACT_LOC = new Location(175, 144, -8775).centerHorizontally();
    private static final Location EMERALD_CHEST_LOC = new Location(175, 145, -8776).centerHorizontally();

    private static final Location BOOK_WALK_LOC = new Location(174, 143, -8774).centerHorizontally();
    private static final Location BOOK_TESSERACT_LOC = new Location(174, 144, -8775).centerHorizontally();
    private static final Location BOOK_CHEST_LOC = new Location(174, 145, -8776).centerHorizontally();

    public TaskSellBooks() {
        super(100);
    }

    public void run() {
        try {
            ai.moveTo(BOOK_TRADE_LOC);
            while (true) {
                if (InventoryUtil.count(Material.BOOK, true, false) < 32) {
                    restock();
                } else {
                    attemptTrade();
                }
            }
        } catch (InterruptedException ex) {
            unregister();
            return;
        }
    }

    private void restock() throws InterruptedException {
        if (!ai.moveTo(EMERALD_WALK_LOC)) {
            Storage.self.sendChat("aaaaah i'm blind konni brain kill me pls");
            ai.tick(6000);// Move Emeralds to Chest
            return;
        }

        ai.openContainer(EMERALD_CHEST_LOC);
        fillChest(Material.EMERALD);
        ai.closeContainer();

        ai.moveTo(BOOK_WALK_LOC); // Move Iron to Chest
        ai.openContainer(BOOK_CHEST_LOC);
        emptyChest();
        ai.closeContainer();

        ai.moveTo(BOOK_TRADE_LOC);
    }

    private void attemptTrade() throws InterruptedException {
        List<Villager> nearbyEntities = new ArrayList<>();
        Storage.self.getEnvironment().getEntities().forEach((e) -> {
            if (e instanceof Villager && e.getLocation().distanceTo(Storage.self.getLocation()) < 4) {
                nearbyEntities.add((Villager) e);
            }
        });

        for (Villager v : nearbyEntities) {
            Storage.self.interactWithEntity(v, false);
            if (!ai.waitForEvent(WindowOpenFinishEvent.class, 5000)) {
                continue;
            }
            if (!ai.waitForEvent(WindowOpenFinishEvent.class, 5000)) {
                continue;
            }
            if (!(Storage.self.getInventory() instanceof VillagerInventory)) {
                continue;
            }

            VillagerInventory vInv = (VillagerInventory) Storage.self.getInventory();

            for (int i = 0; i < vInv.getNumTrades(); i++) {
                Trade trade = vInv.getTrade(i);
                if (trade.isEnabled() && trade.getInput1().getType() == Material.BOOK && trade.getOutput().getType() == Material.EMERALD) {
                    vInv.selectTrade(i);
                    vInv.click(2, 1, 0); // Shift-click output into inventory
                    break;
                }
            }
            ai.closeContainer();
            ai.tick(10);
        }
    }

    private boolean emptyChest() throws InterruptedException {
        for (int i = 0; i < Storage.self.getInventory().getStaticOffset(); i++) {
            if (InventoryUtil.countFreeStorageSlots(true, false) <= 1) {
                return true;
            }
            if (Storage.self.getInventory().getSlot(i) != null) {
                ai.withdrawSlot(i);
            }
        }
        return false;
    }

    private boolean fillChest(Material mat) throws InterruptedException {
        int staticOffset = Storage.self.getInventory().getStaticOffset();
        for (int i = staticOffset; i < staticOffset + 36; i++) {
            if (InventoryUtil.findFreeStorageSlot(false) == -1) {
                return false;
            }
            if (Storage.self.getInventory().getSlot(i) != null
                    && Storage.self.getInventory().getSlot(i).getType() == mat) {
                ai.depositSlot(i);
            }
        }
        return true;
    }

    private void withdrawFromTesseract(Location tesseractLoc, int stacks) throws InterruptedException {
        for (int i = 0; i < stacks; i++) {
            Storage.self.sneak(false);
            Storage.self.clickBlock(tesseractLoc);
            ai.tick();
        }
    }

    private void depositToTesseract(Location tesseractLoc) throws InterruptedException {
        Storage.self.sneak(true);
        ai.tick();
        Storage.self.placeBlock(tesseractLoc, BlockFace.UP);
        ai.tick();
        Storage.self.sneak(false);
        ai.tick();
    }

}
