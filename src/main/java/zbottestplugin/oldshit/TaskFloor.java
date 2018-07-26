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
public class TaskFloor extends Thread {

    private final BlockingAI ai = new BlockingAI();
    private int aiTaskId;
    private final BlockFace[] cardinals = {BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST};

    public TaskFloor() {
    }

    @Override
    public void run() {
        Block block = Storage.self.getEnvironment().getBlockAt(Storage.self.getLocation()).getRelative(BlockFace.DOWN);
        aiTaskId = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, ai, 50, 50);
        boolean finished = false;
        try {
            while (!finished) {
                while (!InventoryUtil.findAndSelect((is) -> {
                    return is.getType() == Material.STONE && is.getData() == 0;
                })) {
                    Storage.self.sneak(true);
                    ai.tick(5);
                    Storage.self.sneak(false);
                    ai.tick(5);
                }
                finished = true;
                for (BlockFace bf : cardinals) {
                    if (block.getRelative(bf).getType() == Material.AIR) {
                        finished = false;
                        Storage.self.placeBlock(block.getLocation(), bf);
                        block = block.getRelative(bf);
                        ai.moveTo(block.getLocation().getRelative(0, 1, 0).centerHorizontally());
                        break;
                    }
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
