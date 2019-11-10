/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine2;

import zbottestplugin.enchantengine.*;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import zbottestplugin.InventoryUtil;
import zbottestplugin.Storage;
import zbottestplugin.task.Task;
import zedly.zbot.BlockFace;
import zedly.zbot.EntityType;
import zedly.zbot.Location;
import zedly.zbot.Material;
import zedly.zbot.entity.Entity;
import zedly.zbot.event.SlotUpdateEvent;
import zedly.zbot.inventory.EnchantingTableInventory;

/**
 *
 * @author Dennis
 */
public class TaskPigCamper extends Task {

    private static final Location PLAYER_FUEL_LOC = new Location(295.5, 137, -8704.5);
    private static final Location HOME_LOC = new Location(295.5, 137, -8701.5);
    private static final Location ENCH_LOC = new Location(295.5, 137, -8698.5);
    private static final Location ENCH_TABLE_LOC = new Location(295, 137, -8698);
    private static final Location BOOK_TESSERACT_LOC = new Location(295, 139, -8699);
    private static final Location LAPIS_TESSERACT_LOC = new Location(295, 140, -8699);
    private static final Location TRASH_CHEST_LOC = new Location(297, 137, -8701);
    private static final Location OUTPUT_CHEST_LOC = new Location(293, 137, -8699);
    private static final HashSet<Material> TRASH_MATERIALS = new HashSet<>();
    private static TaskPigCamper instance;

    private boolean grinding = true;
    private boolean playerPresent = false;

    private TaskPigCamper() {
        super(100);
    }

    public void run() {
        try {
            while (true) {
                ai.tick();
                if (!grinding) {
                    break;
                }
                testPlayerPresent();
                if (playerPresent) {
                    if (Storage.self.getLocation().distanceTo(ENCH_LOC) > 0.1) {
                        ai.moveTo(ENCH_LOC);
                    }
                    continue;
                }
                if (Storage.self.getXPLevels() >= 30) {
                    produceBook();
                }
                tryAttack(HOME_LOC, HOME_LOC);
            }
        } catch (InterruptedException ex) {
        }
        unregister();
        return;
    }

    private boolean produceBook() throws InterruptedException {

        dumpTrash();
        getMaterials();
        doEnchant();
        dumpBook();
        backToGold();

        return true;
    }

    private boolean dumpTrash() throws InterruptedException {
        if (InventoryUtil.findItem((i) -> i != null && TRASH_MATERIALS.contains(i.getType())) == -1) {
            return true;
        }

        if (!ai.openContainer(TRASH_CHEST_LOC)) {
            System.err.println("Can't open disposal");
            ai.tick(50);
            return false;
        }

        int staticOffset = Storage.self.getInventory().getStaticOffset();
        boolean hasTrash;
        do {
            hasTrash = false;
            for (int i = staticOffset; i < staticOffset + 36; i++) {
                if (Storage.self.getInventory().getSlot(i) != null
                        && TRASH_MATERIALS.contains(Storage.self.getInventory().getSlot(i).getType())) {
                    ai.depositSlot(i);
                    hasTrash = true;
                }
            }
        } while (hasTrash);

        ai.closeContainer();

        return true;
    }

    private boolean getMaterials() throws InterruptedException {
        ai.moveTo(ENCH_LOC);
        if (InventoryUtil.count(Material.LAPIS_LAZULI, true, false) < 10) {
            Storage.self.clickBlock(LAPIS_TESSERACT_LOC);
            ai.tick(10);
        }
        if (InventoryUtil.count(Material.BOOK, true, false) < 10) {
            Storage.self.clickBlock(BOOK_TESSERACT_LOC);
            ai.tick(10);
        }
        int bookSlot = InventoryUtil.findItem((i) -> i != null && i.getType() == Material.BOOK);
        int lapisSlot = InventoryUtil.findItem((i) -> i != null && i.getType() == Material.LAPIS_LAZULI);
        ai.clickSlot(lapisSlot, 0, 0); // Collect items onto this stack
        ai.clickSlot(lapisSlot, 6, 0); // Collect items onto this stack
        ai.tick();
        ai.clickSlot(lapisSlot, 0, 0); // Collect items onto this stack
        ai.tick();
        ai.clickSlot(bookSlot, 0, 0); // Collect items onto this stack
        ai.clickSlot(bookSlot, 6, 0); // Collect items onto this stack
        ai.tick();
        ai.clickSlot(bookSlot, 0, 0); // Collect items onto this stack
        return true;
    }

    private boolean doEnchant() throws InterruptedException {
        if (!ai.openContainer(ENCH_TABLE_LOC)) {
            System.err.println("Cannot open ench table");
            ai.tick(50);
            return false;
        }

        if (!(Storage.self.getInventory() instanceof EnchantingTableInventory)) {
            System.err.println("Cannot open ench table");
            ai.tick(50);
            return false;
        }

        int bookSlot = InventoryUtil.findItem((i) -> i != null && i.getType() == Material.BOOK);
        int lapisSlot = InventoryUtil.findItem((i) -> i != null && i.getType() == Material.LAPIS_LAZULI);

        if (bookSlot == -1 || lapisSlot == -1) {
            System.err.println("Cannot find book or lapis");
            ai.tick(50);
            return false;
        }

        // One book into ench table, discard residue
        ai.clickSlot(bookSlot, 0, 0);
        ai.clickSlot(0, 0, 0);
        ai.clickSlot(bookSlot, 0, 0);
        ai.clickSlot(-999, 0, 0);

        // Lapis into ench table, discard residue
        ai.clickSlot(lapisSlot, 0, 0);
        ai.clickSlot(1, 0, 0);

        boolean enchantLoaded = false;
        for (int i = 0; i < 100; i++) {
            if (((EnchantingTableInventory) Storage.self.getInventory()).getEnchantOption(2) != null) {
                enchantLoaded = true;
                break;
            }
            ai.tick();
        }

        if (!enchantLoaded) {
            System.err.println("Enchantment setup not successful");
            ai.tick(50);
            return false;
        }

        ((EnchantingTableInventory) Storage.self.getInventory()).enchant(2);

        boolean enchanted = ai.waitForEvent(SlotUpdateEvent.class, 5000);

        if (!enchanted) {
            System.err.println("Enchantment failed");
            ai.tick(50);
            return false;
        }

        while (ai.waitForEvent(SlotUpdateEvent.class, 1000)) {
        }

        int freeSlot = InventoryUtil.findFreeStorageSlot(true);
        if (freeSlot == -1) {
            System.err.println("No space! FML");
            ai.tick(50);
            return false;
        }

        ai.tick();

        // Book out of ench table
        ai.withdrawSlot(0);
        // Lapis out of ench table
        ai.withdrawSlot(1);

        ai.closeContainer();
        return true;
    }

    private boolean dumpBook() throws InterruptedException {
        if (!ai.openContainer(OUTPUT_CHEST_LOC)) {
            System.err.println("Can't open hopper");
            ai.tick(50);
            return false;
        }

        int staticOffset = Storage.self.getInventory().getStaticOffset();
        for (int i = staticOffset; i < staticOffset + 36; i++) {
            if (Storage.self.getInventory().getSlot(i) != null
                    && Storage.self.getInventory().getSlot(i).getType() == Material.ENCHANTED_BOOK) {
                ai.depositSlot(i);
            }
        }

        ai.closeContainer();
        return true;
    }

    private boolean backToGold() throws InterruptedException {
        ai.moveTo(HOME_LOC);
        return true;
    }

    private boolean tryAttack(Location walkLoc, Location attackLoc) throws InterruptedException {
        Entity skeleton = tryGetEnemy(attackLoc);
        if (skeleton != null) {
            ai.moveTo(walkLoc);
            ai.tick();
            Storage.self.attackEntity(skeleton);
            ai.tick(5);
            return true;
        }
        return false;
    }

    private boolean testPlayerPresent() {
        try {
            for (Entity ent : Storage.self.getEnvironment().getEntities()) {
                if (ent.getType() != EntityType.PLAYER) {
                    continue;
                }
                if (ent.getLocation().distanceTo(PLAYER_FUEL_LOC) < 3) {
                    playerPresent = true;
                    return true;
                }
            }
        } catch (ConcurrentModificationException ex) {
            System.err.println("CME in getEntities() :( :(");
            return false;
        }
        playerPresent = false;
        return true;
    }

    private Entity tryGetEnemy(Location attackLoc) throws InterruptedException {
        try {
            int annoyingEntities = 0;
            Entity nearestAnnoying = null;
            for (Entity ent : Storage.self.getEnvironment().getEntities()) {
                if (ent.getType() != EntityType.CHICKEN
                        && ent.getType() != EntityType.PIG_ZOMBIE) {
                    continue;
                }
                if (ent.getLocation().distanceTo(attackLoc) < 4) {
                    annoyingEntities++;
                    if (nearestAnnoying == null
                            || ent.getLocation().distanceTo(attackLoc)
                            < nearestAnnoying.getLocation().distanceTo(attackLoc)) {
                        nearestAnnoying = ent;
                    }
                }
            }
            if (nearestAnnoying != null) {
                return nearestAnnoying;
            }
        } catch (ConcurrentModificationException ex) {
            System.err.println("CME in getEntities() :( :(");
        }
        return null;
    }

    public static void stopGrinding() {
        instance.grinding = false;
    }

    public static TaskPigCamper instance() {
        if (instance == null || !instance.grinding) {
            instance = new TaskPigCamper();
        }
        return instance;
    }

    static {
        TRASH_MATERIALS.add(Material.ROTTEN_FLESH);
        TRASH_MATERIALS.add(Material.GOLD_NUGGET);
        TRASH_MATERIALS.add(Material.GOLD_INGOT);
        TRASH_MATERIALS.add(Material.GOLDEN_SWORD);
        TRASH_MATERIALS.add(Material.CHICKEN);
        TRASH_MATERIALS.add(Material.COOKED_CHICKEN);
        TRASH_MATERIALS.add(Material.FEATHER);
    }

}
