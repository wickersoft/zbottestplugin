/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import zedly.zbot.Location;

/**
 *
 * @author Dennis
 */
public class TaskPixel extends Thread {

    int originX, originY, originZ, width, height, boost, iteration = 0, id;
    boolean move = true;
    int jx = 0, jz = 0;
    String filename;
    String[] args;
    BufferedImage img;

    public TaskPixel(String[] args) {
        this.args = args;
    }

    public void run() {
        int x, y;
        if (move) {
            x = getTransformedHorizontalOffset(iteration + boost / 2);
            y = getTransformedVerticalOffset(iteration + boost / 2);
            Storage.self.moveTo(originX + jx * x + 0.5, originY + height - y + 2, originZ + jz * x + 0.5);
            move = false;
        } else {
            for (int i = 0; i < boost && iteration < width * height; i++) {
                x = getTransformedHorizontalOffset(iteration);
                y = getTransformedVerticalOffset(iteration);
                int rgb = img.getRGB(x, y);
                selectPixelBlock(rgb);
                Storage.self.placeBlock(originX + jx * x, originY - 2 + height - y, originZ + jz * x);
                iteration++;
            }
            if (iteration == width * height) {
                Storage.self.sendChat("boop!");
                Storage.self.cancelTask(id);
            }
            move = true;
        }
    }

    public boolean load() {
        if (args.length < 3) {
            Storage.self.sendChat("pixel [filename] [direction]");
            return false;
        }
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
                return false;
        }
        filename = args[1];
        boost = Integer.parseInt(args[3]);
        try {
            img = ImageIO.read(new File(filename));
        } catch (IOException ex) {
            Storage.self.sendChat("IO error..");
            return false;
        }
        width = img.getWidth();
        height = img.getHeight();

        Location loc = Storage.self.getLocation();
        originX = loc.getBlockX();
        originY = loc.getBlockY();
        originZ = loc.getBlockZ();
        Storage.self.setAbilities(7);
        return true;
    }

    public void identify(int id) {
        this.id = id;
    }

    public int getTransformedHorizontalOffset(int iteration) {
        int direction = (iteration / width) % 2;
        if (direction == 0) {
            return iteration % width;
        } else {
            return width - (iteration % width) - 1;
        }
    }

    public int getTransformedVerticalOffset(int iteration) {
        return height - (iteration / width) - 1;
    }

    private void selectPixelBlock(int rgb) {
        switch (rgb & 0xFFFFFF) {
            case 0xFFFFFF: // Glass
                Storage.self.selectSlot(0);
                break;
            case 0x1A1717: // Black Wool
            case 0xA32C28: // Red Wool
                Storage.self.selectSlot(1);
                break;
            case 0x263399: // Blue Wool
            case 0xEA7F36: // Orange Wool
                Storage.self.selectSlot(2);
                break;
            case 0x404040:
            case 0x277495: // Cyan Wool
            case 0x55331B: // Brown Wool
            case 0x7D7D7D: // Stone
            case 0x14121D: // Obsidian
                Storage.self.selectSlot(3);
                break;
            case 0x6080C0:
            case 0x678AD3: // Light Blue Wool
            case 0x8135C3: // Purple Wool
            case 0xC2B41C: // Yellow Wool
            case 0xBE4BC8: // Magenta Wool
                Storage.self.selectSlot(4);
                break;
            case 0xA0A0A4:
            case 0x9EA5A5: //Light Gray Wool
                Storage.self.selectSlot(5);
                break;
            case 0x204080:
            case 0x424242: // Gray Wool
            case 0x3BBC2F: // Lime Wool
                Storage.self.selectSlot(6);
                break;
            case 0x2040C0:
            case 0xdbdbdb: // White Wool
            case 0x000000: // Shit
                Storage.self.selectSlot(7);
                break;
            case 0xEFFBFB:
            case 0x63DBD5: // Diamond Block
                Storage.self.selectSlot(8);
                break;
            default:
                System.out.println("Unknown RGB value " + Integer.toHexString(rgb));
        }
    }

}
