/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine;

import java.util.ConcurrentModificationException;
import zedly.zbot.Material;
import zbottestplugin.InventoryUtil;
import zbottestplugin.Storage;
import zbottestplugin.task.Task;
import zedly.zbot.EntityType;
import zedly.zbot.Location;
import zedly.zbot.entity.Entity;
import zedly.zbot.BlockFace;
import zedly.zbot.event.SlotUpdateEvent;
import zedly.zbot.inventory.EnchantingTableInventory;

/**
 *
 * @author Dennis
 */
public class TaskSkeletonCamper extends Task {

    private static final Location HOME_LOC = new Location(-901.5, 38, 4859.5);
    private static final Location PLAYER_AVOIDANCE_LOC = new Location(-904.5, 35, 4859.85);
    private static final Location LEFT_WALK_LOC = new Location(-903.5, 35, 4859.85);
    private static final Location RIGHT_WALK_LOC = new Location(-905.5, 35, 4859.85);
    private static final Location LEFT_ATTACK_LOC = new Location(-903.5, 36, 4861.5);
    private static final Location RIGHT_ATTACK_LOC = new Location(-905.5, 36, 4861.5);
    private static TaskSkeletonCamper instance;

    private boolean grinding = true;
    private boolean playerPresent = false;

    private TaskSkeletonCamper() {
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
                    if (Storage.self.getLocation().distanceTo(HOME_LOC) > 0.1) {
                        ai.moveTo(HOME_LOC);
                    }
                    continue;
                }

               // if (Storage.self.getXPLevels() >= 30) {
               //     enchant();
               // }

                tryAttack(LEFT_WALK_LOC, LEFT_ATTACK_LOC);
                tryAttack(RIGHT_WALK_LOC, RIGHT_ATTACK_LOC);
            }
        } catch (InterruptedException ex) {
        }
        unregister();
        return;
    }

    private boolean enchant() throws InterruptedException {
        ai.moveTo(RIGHT_WALK_LOC);
        if (!ai.openContainer(-910, 35, 4857)) {
            System.err.println("Cannot open ench table");
            ai.tick(50);
            return false;
        }
        int bookSlot = InventoryUtil.findItem((i) -> i != null && i.getType() == Material.BOOK);
        int lapisSlot = InventoryUtil.findItem((i) -> i != null && i.getType() == Material.LAPIS_ORE);

        if (bookSlot == -1 || lapisSlot == -1) {
            System.err.println("Cannot find book or lapis");
            ai.tick(50);
            return false;
        }

        if (!(Storage.self.getInventory() instanceof EnchantingTableInventory)) {
            System.err.println("Cannot open ench table");
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

        // Book out of ench table, discard residue
        ai.clickSlot(0, 0, 0);
        ai.clickSlot(freeSlot, 0, 0);
        ai.clickSlot(-999, 0, 0);

        // Lapis out of ench table, discard residue
        ai.clickSlot(1, 0, 0);
        ai.clickSlot(lapisSlot, 0, 0);
        ai.clickSlot(-999, 0, 0);

        freeSlot -= Storage.self.getInventory().getStaticOffset();

        ai.closeContainer();

        if (!ai.openContainer(-907, 39, 4859)) {
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

        ai.moveTo(LEFT_WALK_LOC);

        Storage.self.placeBlock(-903, 36, 4859, BlockFace.UP);
        ai.tick();
        Storage.self.placeBlock(-903, 36, 4859, BlockFace.UP);
        ai.tick();
        Storage.self.placeBlock(-903, 37, 4859, BlockFace.UP);
        ai.tick();
        Storage.self.placeBlock(-903, 37, 4859, BlockFace.UP);
        ai.tick();
        Storage.self.placeBlock(-903, 36, 4858, BlockFace.UP);
        ai.tick();
        Storage.self.placeBlock(-903, 36, 4858, BlockFace.UP);
        ai.tick();
        Storage.self.placeBlock(-903, 37, 4858, BlockFace.UP);
        ai.tick();
        Storage.self.placeBlock(-903, 37, 4858, BlockFace.UP);
        ai.tick();
        Storage.self.placeBlock(-903, 37, 4857, BlockFace.UP);
        ai.tick();
        Storage.self.placeBlock(-903, 37, 4857, BlockFace.UP);
        ai.tick();

        if (!ai.openContainer(-903, 36, 4857)) {
            System.err.println("Can't open disposal");
            ai.tick(50);
            return false;
        }

        staticOffset = Storage.self.getInventory().getStaticOffset();
        for (int i = staticOffset; i < staticOffset + 36; i++) {
            if (Storage.self.getInventory().getSlot(i) != null
                    && (Storage.self.getInventory().getSlot(i).getType() == Material.BOW
                    || Storage.self.getInventory().getSlot(i).getType() == Material.GOLDEN_SWORD)) {
                ai.depositSlot(i);
            }
        }

        ai.closeContainer();

        ai.moveTo(RIGHT_WALK_LOC);
        Storage.self.sneak(true);
        Storage.self.clickBlock(-907, 36, 4859);
        ai.tick();
        Storage.self.clickBlock(-907, 35, 4859);
        ai.tick();
        Storage.self.clickBlock(-907, 35, 4859);
        ai.tick();
        Storage.self.clickBlock(-907, 35, 4859);
        ai.tick();
        Storage.self.sneak(false);
        return true;
    }

    private boolean tryAttack(Location walkLoc, Location attackLoc) throws InterruptedException {
        Entity skeleton = tryGetSkeleton(attackLoc);
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
                if (ent.getLocation().distanceTo(PLAYER_AVOIDANCE_LOC) < 5) {
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

    private Entity tryGetSkeleton(Location attackLoc) throws InterruptedException {
        try {
            int skeletons = 0;
            Entity bestSkeleton = null;
            for (Entity ent : Storage.self.getEnvironment().getEntities()) {
                if (ent.getType() != EntityType.SKELETON &&
                        ent.getType() != EntityType.PIG_ZOMBIE) {
                    continue;
                }
                if (ent.getLocation().distanceTo(attackLoc) < 2) {
                    skeletons++;
                    if (bestSkeleton == null
                            || ent.getLocation().distanceTo(attackLoc)
                            < bestSkeleton.getLocation().distanceTo(attackLoc)) {
                        bestSkeleton = ent;
                    }
                }
            }
            if (skeletons > 15 && bestSkeleton != null) {
                return bestSkeleton;
            }
        } catch (ConcurrentModificationException ex) {
            System.err.println("CME in getEntities() :( :(");
        }
        return null;
    }

    public static void stopGrinding() {
        instance.grinding = false;
    }

    public static TaskSkeletonCamper instance() {
        if (instance == null || !instance.grinding) {
            instance = new TaskSkeletonCamper();
        }
        return instance;
    }
}
