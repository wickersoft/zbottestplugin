/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zedly.zbot.Location;
import zedly.zbot.BlockFace;

/**
 *
 * @author Dennis
 */
public class TaskBuild extends Thread {

    private int width = 0, height = 0;
    private final String[] args;
    private int jx = 0, jz = 0, ix = 0, iz = 0;
    private int millis = 250, boost = 1;

    public TaskBuild(String[] args) {
        this.args = args;
    }

    public void run() {
        if (args.length < 5) {
            Storage.self.sendChat("build [width] [height] [direction] [interval] [boost]");
            return;
        }
        width = Integer.parseInt(args[0]);
        height = Integer.parseInt(args[1]);

        switch (args[2]) {
            case "+z":
                jz = 1;
                break;
            case "-z":
                jz = -1;
                break;
            case "+x":
                jx = 1;
                break;
            case "-x":
                jx = -1;
                break;
            default:
                Storage.self.sendChat("Unsupported direction");
                return;
        }
        millis = Integer.parseInt(args[3]);
        boost = Integer.parseInt(args[4]);

        Location loc = Storage.self.getLocation();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        int i = 0;
        while (true) {
            if (++i > height) {
                break;
            }
            for (int j = 0; j < width; j++) {
                if (j % boost == 0) {
                    if (j + boost / 2 >= width) {
                        Storage.self.moveTo(x + jx * width - jx + 0.5, y + i + 1, z + jz * width - jz + 0.5);
                    } else {
                        Storage.self.moveTo(x + jx * j + (boost / 2) * jx + 0.5, y + i, z + jz * j + (boost / 2) * jz + 0.5);
                    }
                    //pause(millis);
                }
                Storage.self.placeBlock(x + jx * j, y - 2 + i, z + jz * j, BlockFace.UP);
            }
            if (++i > height) {
                break;
            }
            for (int j = width - 1; j >= 0; j--) {
                if (j % boost == 0) {
                    if (j - boost / 2 < 0) {
                        Storage.self.moveTo(x + 0.5, y + i + 1, z + 0.5);
                    } else {
                        Storage.self.moveTo(x + jx * j - (boost / 2) * jx + 0.5, y + i, z + jz * j - (boost / 2) * jz + 0.5);
                    }
                    //pause(millis);
                }
                Storage.self.placeBlock(x + jx * j, y - 2 + i, z + jz * j, BlockFace.UP);
            }
        }
        Storage.self.sendChat("Done!");
    }
}
