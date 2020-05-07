/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.Collection;
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
    private final Location COAL_TESSERACT = new Location(265, 137, -8836);
    private final Location SAPLING_TESSERACT = new Location(265, 137, -8837);
    private final Location STICK_TESSERACT = new Location(265, 137, -8838);

    public TaskTreeFarm(int interval) {
        super(interval);
    }

    @Override
    public void work() throws InterruptedException {
        if (checkAxeDamage() >= 1350) {
            ai.navigateTo2(0, 0, 0);
            InventoryUtil.findAndSelect((is) -> {
                return is != null && is.getType() == Material.DIAMOND_AXE;
            });
            while (checkAxeDamage() > 0) {
                ai.tick(20);
            }
        }
        for (int treeZ = 0; treeZ < 4; treeZ++) {
            boolean rowGrown = true;
            for (int treeX = 0; treeX < 5; treeX++) {
                if (!isTreeGrown(treeX, treeZ)) {
                    rowGrown = false;
                }
            }
            if (rowGrown) {
                clearTreeRow(treeZ);
            }
        }
    }

    private void clearTreeRow(int treeZ) throws InterruptedException {
        // Get in position
        int z = getTreeZ(treeZ);
        ai.navigateTo(WORK_X_MIN, WORK_Y, z);

        // Break the blocks
        InventoryUtil.findAndSelect((is) -> {
            return is != null && is.getType() == Material.DIAMOND_AXE;
        });
        for (int x = WORK_X_MIN; x < WORK_X_MAX; x++) {
            for (int y = WORK_Y; y <= WORK_Y + 1; y++) {
                Material typeAtY = Storage.self.getEnvironment().getBlockAt(x, WORK_Y, z).getType();
                int breakMillis = typeAtY == Material.SPRUCE_LOG ? 300 : 750;
                if (typeAtY.isSolid()) {
                    Storage.self.sneak(true);
                    ai.breakBlock(x, y, z, breakMillis);
                    Storage.self.sneak(false);
                    ai.tick(20);
                }
            }
            ai.moveTo(x, WORK_Y, z);
        }

        ai.tick(20);
        // Collect all coal items in row
        while (true) {
            Collection<Entity> items = Storage.self.getEnvironment().getEntities();
            items.removeIf((e) -> {
                return !(e instanceof Item) || ((Item) e).getItemStack().getType() != Material.CHARCOAL;
            });
            if (items.isEmpty()) {
                break;
            }
            for (Entity e : items) {
                ai.navigateTo(e.getLocation().getBlockX(), WORK_Y, e.getLocation().getBlockZ());
                ai.tick(5);
            }
        }

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
        ai.navigateTo2(COAL_TESSERACT);
        Storage.self.placeBlock(COAL_TESSERACT, BlockFace.UP);
        Storage.self.placeBlock(COAL_TESSERACT, BlockFace.UP);
        ai.tick(10);

        ai.navigateTo2(STICK_TESSERACT);
        Storage.self.placeBlock(STICK_TESSERACT, BlockFace.UP);
        Storage.self.placeBlock(STICK_TESSERACT, BlockFace.UP);
        ai.tick(10);

        ai.navigateTo2(SAPLING_TESSERACT);
        Storage.self.placeBlock(SAPLING_TESSERACT, BlockFace.UP);
        Storage.self.placeBlock(SAPLING_TESSERACT, BlockFace.UP);
        ai.tick(10);

        Storage.self.clickBlock(SAPLING_TESSERACT);
        ai.tick(10);
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

}
