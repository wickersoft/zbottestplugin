/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zedly.zbot.block.Material;
import zedly.zbot.inventory.ItemStack;

/**
 *
 * @author Dennis
 */
public class InventoryUtil {

    private static int nextSlot = 0;

    public static boolean findAndSelect(Material mat) {
        if (Storage.self.getInventory().getItemInHand().getType() == mat) {
            return true;
        }
        for (int i = 36; i <= 44; i++) {
            if (Storage.self.getInventory().getSlot(i).getType() == mat) {
                Storage.self.selectSlot(i - 36);
                return true;
            }
        }
        for (int i = 9; i <= 35; i++) {
            if (Storage.self.getInventory().getSlot(i).getType() == mat) {
                Storage.self.getInventory().clickSlot(i, 0, 0);
                Storage.self.getInventory().clickSlot(nextSlot + 36, 0, 0);
                Storage.self.getInventory().clickSlot(i, 0, 0);
                Storage.self.selectSlot(nextSlot);
                nextSlot = (nextSlot + 1) % 9;
                return true;
            }
        }
        return false;
    }

    public static boolean findAndSelect(Material mat, short damage) {
        if (Storage.self.getInventory().getItemInHand().getType() == mat
                && Storage.self.getInventory().getItemInHand().getDamageValue() == damage) {
            return true;
        }
        for (int i = 36; i <= 44; i++) {
            if (Storage.self.getInventory().getSlot(i).getType() == mat
                    && Storage.self.getInventory().getSlot(i).getDamageValue() == damage) {
                Storage.self.selectSlot(i - 36);
                return true;
            }
        }
        for (int i = 9; i <= 35; i++) {
            if (Storage.self.getInventory().getSlot(i).getType() == mat
                    && Storage.self.getInventory().getSlot(i).getDamageValue() == damage) {
                Storage.self.getInventory().clickSlot(i, 0, 0);
                Storage.self.getInventory().clickSlot(nextSlot + 36, 0, 0);
                Storage.self.getInventory().clickSlot(i, 0, 0);
                Storage.self.selectSlot(nextSlot);
                nextSlot = (nextSlot + 1) % 9;
                return true;
            }
        }
        return false;
    }

    public static int count(Material mat) {
        int count = 0;
        for (int i = 9; i <= 44; i++) {
            ItemStack is = Storage.self.getInventory().getSlot(i);
            if (is.getType() == mat) {
                count += is.getAmount();
            }
        }
        return count;
    }

    public static int count(Material mat, short damage) {
        int count = 0;
        for (int i = 9; i < 44; i++) {
            ItemStack is = Storage.self.getInventory().getSlot(i);
            if (is.getType() == mat && is.getDamageValue() == damage) {
                count += is.getAmount();
            }
        }
        return count;
    }
}
