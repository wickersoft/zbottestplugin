/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;
import zedly.zbot.Location;
import zedly.zbot.block.Material;

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
    private final HashMap<Integer, ItemStack> itemMap = new HashMap<>();

    public TaskPixel(String[] args) {
        this.args = args;
        populateMap();
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
        ItemStack stack = itemMap.get(rgb);
        if(stack == null) {
            System.out.println("Unknown RGB value " + Integer.toHexString(rgb));
            stack = itemMap.get(0xFFFFFF);
        }
        InventoryUtil.findAndSelect(stack.getMaterial(), stack.getDamage());

        
        switch (rgb & 0xFFFFFF) {
            case 0x404040:
            case 0x7D7D7D: // Stone
            case 0x6080C0:
            case 0xA0A0A4:
            case 0x204080:
            case 0x2040C0:
            case 0xdbdbdb: // White Wool
            case 0xEFFBFB:
        }
    }

    private class ItemStack {

        private final Material mat;
        private final short damage;

        public ItemStack(Material mat, short damage) {
            this.mat = mat;
            this.damage = damage;
        }

        public ItemStack(Material mat) {
            this.mat = mat;
            this.damage = 0;
        }

        public Material getMaterial() {
            return mat;
        }

        public short getDamage() {
            return damage;
        }
    }

    private void populateMap() {
        itemMap.put(0xFFFFFF, new ItemStack(Material.GLASS));
        itemMap.put(0x63DBD5, new ItemStack(Material.DIAMOND_BLOCK));
        itemMap.put(0x14121D, new ItemStack(Material.OBSIDIAN));
        itemMap.put(0x1A1717, new ItemStack(Material.WOOL, (short) 15)); // Black
        itemMap.put(0xA32C28, new ItemStack(Material.WOOL, (short) 14)); // Red
        itemMap.put(0xA32C28, new ItemStack(Material.WOOL, (short) 13)); // Orange
        itemMap.put(0x55331B, new ItemStack(Material.WOOL, (short) 12)); // Brown
        itemMap.put(0x263399, new ItemStack(Material.WOOL, (short) 11)); // Blue
        itemMap.put(0x8135C3, new ItemStack(Material.WOOL, (short) 10)); // Purple
        itemMap.put(0x277495, new ItemStack(Material.WOOL, (short) 9)); // Cyan
        itemMap.put(0x9EA5A5, new ItemStack(Material.WOOL, (short) 8)); // LightGray
        itemMap.put(0x424242, new ItemStack(Material.WOOL, (short) 7)); // Gray
        itemMap.put(0xA32C28, new ItemStack(Material.WOOL, (short) 6)); // Red
        itemMap.put(0x3BBC2F, new ItemStack(Material.WOOL, (short) 5)); // Lime
        itemMap.put(0xC2B41C, new ItemStack(Material.WOOL, (short) 4)); // Yellow
        itemMap.put(0x678AD3, new ItemStack(Material.WOOL, (short) 3)); // LightBlue
        itemMap.put(0xBE4BC8, new ItemStack(Material.WOOL, (short) 2)); // Magenta
        itemMap.put(0xEA7F36, new ItemStack(Material.WOOL, (short) 1)); // Orange
        itemMap.put(0xDBDBDB, new ItemStack(Material.WOOL, (short) 0)); // White

    }

}
