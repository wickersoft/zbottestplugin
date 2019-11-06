/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.function.Predicate;
import zedly.zbot.Material;
import zedly.zbot.inventory.ItemStack;

/**
 *
 * @author Dennis
 */
public class InventoryUtil {

    private static int nextSlot = 0;

    public static int findItem(Predicate<ItemStack> itemFilter) {
        int staticOffset = Storage.self.getInventory().getStaticOffset();
        for (int i = staticOffset; i < staticOffset + 36; i++) {
            if (itemFilter.test(Storage.self.getInventory().getSlot(i))) {
                return i;
            }
        }
        return -1;
    }

    public static boolean findAndSelect(Predicate<ItemStack> itemFilter) {
        if (itemFilter.test(Storage.self.getInventory().getItemInHand())) {
            return true;
        }
        for (int i = 36; i <= 44; i++) {
            if (itemFilter.test(Storage.self.getInventory().getSlot(i))) {
                Storage.self.selectSlot(i - 36);
                return true;
            }
        }
        for (int i = 9; i <= 35; i++) {
            if (itemFilter.test(Storage.self.getInventory().getSlot(i))) {
                Storage.self.getInventory().click(i, 0, 0);
                Storage.self.getInventory().click(nextSlot + 36, 0, 0);
                Storage.self.getInventory().click(i, 0, 0);
                Storage.self.selectSlot(nextSlot);
                nextSlot = (nextSlot + 1) % 9;
                return true;
            }
        }
        return false;
    }

    public static boolean findAndSelect(Material mat, int amount) {
        return findAndSelect((i) -> i.getType() == mat && i.getAmount() == amount);
    }

    public static int count(Material mat) {
        int count = 0;
        for (int i = 9; i <= 44; i++) {
            ItemStack is = Storage.self.getInventory().getSlot(i);
            if (is != null && is.getType() == mat) {
                count += is.getAmount();
            }
        }
        return count;
    }

    public static int findFreeStorageSlot(boolean staticInv) {
        int staticStart = Storage.self.getInventory().getStaticOffset();
        if (staticInv) {
            for (int i = staticStart; i < staticStart + 36; i++) {
                if (Storage.self.getInventory().getSlot(i) == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < staticStart; i++) {
                if (Storage.self.getInventory().getSlot(i) == null) {
                    return i;
                }
            }
        }
        return -1;
    }
}
