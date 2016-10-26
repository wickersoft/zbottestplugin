/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zedly.zbot.Location;
import zedly.zbot.block.Material;
import zedly.zbot.self.Self;

/**
 *
 * @author Dennis
 */
public class ThreadWheatFast extends Thread {

    private int aiTaskId = 0;
    private final Self self = Storage.self;
    private final BlockingAI ai = new BlockingAI();
    private final Location wheatLocation = new Location(-915, 63, 4832);
    private final Location soilLocation = new Location(-915, 62, 4832);

    public void run() {
        try {
            aiTaskId = self.scheduleSyncRepeatingTask(Storage.plugin, ai, 100, 100);
            for (int i = 0; i < 1000; i++) {
                ai.breakBlock(wheatLocation, 0);
                ai.tick();
                InventoryUtil.findAndSelect(Material.WHEAT_SEEDS);
                ai.tick();
                self.placeBlock(soilLocation);
                ai.tick();
                do {
                    while (!InventoryUtil.findAndSelect(Material.INK_SACK, (short) 15)) {
                        ai.tick(10);
                    }
                    self.placeBlock(wheatLocation);
                    ai.tick(5);
                } while (self.getEnvironment().getBlockAt(wheatLocation).getData() != 7);
            }
        } catch (InterruptedException ex) {
            unregister();
            ex.printStackTrace();
            StackTraceElement ste = ex.getStackTrace()[0];
            self.sendChat("rip thread :(       " + ste.getClassName() + ":" + ste.getLineNumber());
        }
    }

    private void dumpInv() {
        for (int i = 9; i <= 44; i++) {
            System.out.println("Slot " + i + ": " + self.getInventory().getSlot(i));
        }
    }

    private void unregister() {
        self.cancelTask(aiTaskId);
    }

}
