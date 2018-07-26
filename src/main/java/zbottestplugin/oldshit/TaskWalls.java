/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.oldshit;

import org.bukkit.Material;
import zbottestplugin.InventoryUtil;
import zbottestplugin.Storage;
import zedly.zbot.environment.Block;
import zedly.zbot.environment.BlockFace;

/**
 *
 * @author Dennis
 */
public class TaskWalls extends Thread {

    private final BlockingAI ai = new BlockingAI();
    private int aiTaskId;
    private final BlockFace[] cardinals = {BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST};

    public TaskWalls() {
    }

    @Override
    public void run() {
        Block block = Storage.self.getEnvironment().getBlockAt(Storage.self.getLocation()).getRelative(0, -1, 0);
        aiTaskId = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, ai, 100, 100);
        try {
            while (block.getY() < 250) {
                int inFloorLevel = block.getY() % 8;

                // Select the right material, derp if run out
                while (!InventoryUtil.findAndSelect((is) -> {
                    return is.getType() == ((inFloorLevel == 5 || inFloorLevel == 6) ? Material.WOOL : Material.STAINED_GLASS)
                            && is.getData() == 15;
                })) {
                    Storage.self.sneak(true);
                    ai.tick(5);
                    Storage.self.sneak(false);
                    ai.tick(5);
                }
                boolean elevate = true;
                for (BlockFace bf : cardinals) {
                    if (block.getRelative(bf).getType() == Material.AIR
                            && block.getRelative(bf).getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                        elevate = false;
                        block = block.getRelative(bf);
                        Storage.self.placeBlock(block.getRelative(BlockFace.DOWN).getLocation(), BlockFace.UP);
                        ai.moveTo(block.getLocation().getRelative(0, 1, 0).centerHorizontally());
                        break;
                    }
                }
                if (elevate) {
                    block = block.getRelative(BlockFace.UP);
                }
                ai.tick(5);
            }
            unregister();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            StackTraceElement ste = ex.getStackTrace()[0];
            unregister();
            Storage.self.sendChat("rip thread :(       " + ste.getClassName() + ":" + ste.getLineNumber());
        }
    }

    private void unregister() {
        Storage.self.cancelTask(aiTaskId);
    }
}
