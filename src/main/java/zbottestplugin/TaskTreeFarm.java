/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.Collection;
import java.util.HashSet;
import net.minecraft.server.NBTTagCompound;
import zbottestplugin.task.Task;
import zedly.zbot.BlockFace;
import zedly.zbot.Location;
import zedly.zbot.Material;
import zedly.zbot.entity.Entity;
import zedly.zbot.entity.Item;
import zedly.zbot.inventory.ItemStack;

/**
 *
 * @author Dennis
 */
public class TaskTreeFarm extends Task {

    private final int WORK_X_MIN = 266;
    private final int WORK_X_MAX = 304;
    private final int WORK_Y = 137;
    private final int TREE_SIZE = 7;
    private final int TREE_ROOT_X = 272;
    private final int TREE_ROOT_Z = -8839;
    private final Location COAL_TESSERACT = new Location(265, 137, -8836).centerHorizontally();
    private final Location SAPLING_TESSERACT = new Location(265, 137, -8837).centerHorizontally();
    private final Location STICK_TESSERACT = new Location(265, 137, -8838).centerHorizontally();
    private final Location TREE_FARM_HOME = new Location(266, 137, -8837).centerHorizontally();
    private final Location GOLD_FARM = new Location(295.5, 137, -8704.8);
    private static final Location TRASH_CHEST_LOC = new Location(295, 136, -8705);
    private static final HashSet<Material> TRASH_MATERIALS = new HashSet<>();

    public TaskTreeFarm(int interval) {
        super(interval);
    }

    @Override
    public void work() throws InterruptedException {
        while (true) {
            for (int treeZ = 0; treeZ < 4; treeZ++) {
                if (checkAxeDamage() >= 1350) {
                    goRepairAxe();
                }

                boolean rowGrown = true;
                for (int treeX = 0; treeX < 5; treeX++) {
                    if (!isTreeGrown(treeX, treeZ)) {
                        rowGrown = false;
                    }
                }
                if (rowGrown) {
                    IWantToBreakTree(treeZ);
                } else {
                    goCollectCoalItems();
                    ai.tick(20);
                }
            }
        }
    }

    private void IWantToBreakTree(int treeZ) throws InterruptedException {
        // Get in position
        int z = getTreeZ(treeZ);
        ai.navigateTo2(WORK_X_MIN, WORK_Y, z);

        // Break the blocks
        InventoryUtil.findAndSelect(Material.DIAMOND_AXE, 1);
        for (int x = WORK_X_MIN; x < WORK_X_MAX; x++) {
            for (int y = WORK_Y; y <= WORK_Y + 1; y++) {
                Material typeAtY = Storage.self.getEnvironment().getBlockAt(x, y, z).getType();
                int breakMillis = typeAtY == Material.SPRUCE_LOG ? 300 : 750;
                if (typeAtY.isSolid()) {
                    Storage.self.sneak(true);
                    ai.breakBlock(x, y, z, breakMillis);
                    Storage.self.sneak(false);
                    ai.tick(3);
                }
            }
            ai.moveTo(x, WORK_Y, z);
            ai.tick(5);
        }

        ai.tick(10);

        // Replant
        for (int i = 4; i >= 0; i--) {
            int x = getTreeX(i);
            ai.navigateTo2(x, WORK_Y, z);
            InventoryUtil.findAndSelect((is) -> {
                return is != null && is.getType() == Material.SPRUCE_SAPLING;
            });

            Storage.self.placeBlock(x, WORK_Y, z, BlockFace.UP);
            ai.tick();
            Storage.self.placeBlock(x + 1, WORK_Y, z, BlockFace.UP);
            ai.tick();
            Storage.self.placeBlock(x, WORK_Y, z + 1, BlockFace.UP);
            ai.tick();
            Storage.self.placeBlock(x + 1, WORK_Y, z + 1, BlockFace.UP);
            ai.tick();
        }

        // Restock
        ai.navigateTo2(COAL_TESSERACT.getRelative(1, 0, 0));
        Storage.self.placeBlock(COAL_TESSERACT, BlockFace.UP);
        Storage.self.placeBlock(COAL_TESSERACT, BlockFace.UP);
        ai.tick(10);

        ai.navigateTo2(STICK_TESSERACT.getRelative(1, 0, 0));
        Storage.self.placeBlock(STICK_TESSERACT, BlockFace.UP);
        Storage.self.placeBlock(STICK_TESSERACT, BlockFace.UP);
        ai.tick(10);

        ai.navigateTo2(SAPLING_TESSERACT.getRelative(1, 0, 0));
        Storage.self.placeBlock(SAPLING_TESSERACT, BlockFace.UP);
        Storage.self.placeBlock(SAPLING_TESSERACT, BlockFace.UP);
        ai.tick(10);

        Storage.self.clickBlock(SAPLING_TESSERACT);
        ai.tick(10);
    }

    private void goRepairAxe() throws InterruptedException {
        InventoryUtil.findAndSelect(Material.DIAMOND_AXE, 1);
        ai.navigateTo2(GOLD_FARM);
        while (checkAxeDamage() > 0) {
            InventoryUtil.findAndSelect(Material.DIAMOND_AXE, 1);
            ai.tick(20);
        }
        dumpTrash();
        ai.navigateTo2(TREE_FARM_HOME);
    }

    private void goCollectCoalItems() throws InterruptedException {
        Collection<Entity> items = Storage.self.getEnvironment().getEntities();
        items.removeIf((e) -> {
            return !(e instanceof Item) || ((Item) e).getItemStack() == null || 
                    ((Item) e).getItemStack().getType() != Material.CHARCOAL
                    || e.getLocation().getY() > WORK_Y + 3
                    || e.getLocation().getY() < WORK_Y;
        });
        if (!items.isEmpty()) {
            for (Entity e : items) {
                ai.navigateTo2(e.getLocation().getBlockX(), WORK_Y, e.getLocation().getBlockZ());
                ai.tick(5);
            }
        } else {
            ai.navigateTo2(TREE_FARM_HOME);
        }
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

    private int getTreeZ(int treeZ) {
        return TREE_ROOT_Z + TREE_SIZE * treeZ;
    }

    private int getTreeX(int treeX) {
        return TREE_ROOT_X + TREE_SIZE * treeX;
    }

    private boolean isTreeGrown(int treeX, int treeZ) {
        return Storage.self.getEnvironment().getBlockAt(TREE_ROOT_X + TREE_SIZE * treeX, WORK_Y, TREE_ROOT_Z + TREE_SIZE * treeZ).getType().isSolid();
    }

    private int checkAxeDamage() {
        ItemStack axe = Storage.self.getInventory().getSlot(InventoryUtil.findItem((is) -> {
            return is != null && is.getType() == Material.DIAMOND_AXE;
        }));
        if (axe.getNbt() instanceof NBTTagCompound) {
            NBTTagCompound nbt = (NBTTagCompound) axe.getNbt();
            int damage = nbt.getInteger("Damage");
            return damage;
        }
        return -1;
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
