/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zedly.zbot.block.Material;

/**
 *
 * @author Dennis
 */
public class TaskReplant extends HierarchicalTask {
    
    int tick = 0;
    
    private final int x, y, z;
    
    public TaskReplant(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void tick() {
        if(tick == 0) {
            InventoryUtil.findAndSelect(Material.WHEAT_SEEDS);
            Storage.self.breakBlock(x, y, z);
        } else if(tick == 2) {
            Storage.self.placeBlock(x, y - 1, z);
            finish();
        }
        tick++;
    }
}
