/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import zedly.zbot.inventory.Enchantment;
import zedly.zbot.inventory.ItemStack;

/**
 *
 * @author Dennis
 */
public class EnchantEngine {

    private static final String[] ENCHANT_DATABASE = new String[38800];
    private static final HashMap<String, Integer> ENCHANTMENT_PRICES = new HashMap<>();
    private static Integer[] queryResults = new Integer[0];

    private static final String[] zenchantments = {"Anthropomorphism", "Arborist", "Bind", "Blaze's Curse",
        "Blizzard", "Bounce", "Burst", "Combustion", "Conversion", "Decapitation",
        "Extraction", "Fire", "Firestorm", "Fireworks", "Force", "Frozen Step",
        "Fuse", "Germination", "Glide", "Gluttony", "Gold Rush", "Grab", "Green Thumb",
        "Gust", "Harvest", "Haste", "Ice Aspect", "Jump", "Laser", "Level", "Long Cast",
        "Lumber", "Magnetism", "Meador", "Mow", "Mystery Fish", "Nether Step", "Night Vision",
        "Persephone", "Pierce", "Plough", "Potion", "Potion Resistance", "Quick Shot",
        "Rainbow", "Rainbow Slam", "Reaper", "Reveal", "Saturation", "Short Cast",
        "Shred", "Siphon", "Sonic Shock", "Spectral", "Speed", "Spikes", "Spread",
        "Stationary", "Stock", "Stream", "Switch", "Terraformer", "Toxic", "Tracer",
        "Transformation", "Variety", "Vortex", "Weight", "Apocalypse", "Ethereal",
        "Missile", "Singularity", "Unrepairable"};

    public static void rememberItemString(int absoluteIndex, String enchantString) {
        ENCHANT_DATABASE[absoluteIndex] = enchantString;
    }

    public static int queryEnchantment(String filter) {
        ArrayList<Integer> results = new ArrayList<>();
        String[] args = filter.split(" ");

        for (int i = 0; i < ENCHANT_DATABASE.length; i++) {
            boolean found = false;
            for (int k = 0; k < args.length; k++) {
                if (ENCHANT_DATABASE[i] != null && ENCHANT_DATABASE[i].contains(args[k])) {
                    found = true;
                } else {
                    found = false;
                    break;
                }
            }
            if (found) {
                results.add(i);
            }
        }
        queryResults = results.toArray(new Integer[0]);
        return results.size();
    }

    public static int getResultNumber() {
        return queryResults.length;
    }

    public static int getQueryResult(int listItem) {
        if (listItem >= 0 && listItem < queryResults.length) {
            return queryResults[listItem];
        } else {
            return -1;
        }
    }

    public static String getEnchantString(int absoluteIndex) {
        if (absoluteIndex >= 0 && absoluteIndex < ENCHANT_DATABASE.length
                && ENCHANT_DATABASE[absoluteIndex] != null) {
            return ENCHANT_DATABASE[absoluteIndex];
        } else {
            return "<empty>";
        }
    }

    public static int getPriceEstimate(int absoluteIndex) {
        String enchString = getEnchantString(absoluteIndex);
        if (enchString.equals("<empty>")) {
            return 0;
        }

        String[] elements = enchString.split(" ");

        int totalPrice = 0;
        for (String item : elements) {
            totalPrice += ENCHANTMENT_PRICES.getOrDefault(item, 100);
        }
        totalPrice *= Math.pow(0.9, elements.length - 1);
        return totalPrice;
    }

    public static String stringifyItem(ItemStack is) {
        if (is == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(is.getType().toString().toLowerCase()).append(" ");
        if (is.hasEnchantments()) {
            for (Entry<Enchantment, Integer> ench : is.getEnchantments().entrySet()) {
                sb.append(ench.getKey().getName().toLowerCase().replace(" ", "_")).append("-").append(ench.getValue()).append(" ");
            }
        }

        if (is.hasLore()) {
            for (String line : is.getLore()) {
                if (line.matches("^\\u00a77(.+) (I|II|III|IV|V)$")) {
                    String[] loreCandidate = line.split(" ");
                    String name = loreCandidate[0].substring(2);
                    if (isKnownZenchant(name)) {
                        sb.append(name.toLowerCase().replace(" ", "_")).append("-").append(getNumber(loreCandidate[1])).append(" ");
                    }
                }
            }
        }

        return sb.toString();
    }

    public static boolean isKnownZenchant(String name) {
        for (String s : zenchantments) {
            if (s.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // Returns the english number representation of the given roman number string
    public static int getNumber(String numeral) {
        switch (numeral.toUpperCase()) {
            case "-":
                return 0;
            case "I":
                return 1;
            case "II":
                return 2;
            case "III":
                return 3;
            case "IV":
                return 4;
            case "V":
                return 5;
            case "VI":
                return 6;
            case "VII":
                return 7;
            case "VIII":
                return 8;
            case "IX":
                return 9;
            case "X":
                return 10;
            default:
                return 1;
        }
    }

    // Returns the roman number string representation of the given english number
    public static String getRomanString(int number) {
        switch (number) {
            case 0:
                return "-";
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            default:
                return "I";
        }
    }

    public static String friendlyIndex(LibraryLocation loc) {
        return getRomanString(loc.getBlock() + 1) + "-" + (char) ('A' + loc.getColumn()) + (loc.getChest() + 1) + " " + (loc.getRowSlot() + 1) + "," + (loc.getRow() + 1);
    }

    public static String friendlyIndex(int absoluteIndex) {
        return friendlyIndex(new LibraryLocation(absoluteIndex));
    }

    private static final String[] zenchants = {"Anthropomorphism", "Arborist", "Bind", "Blaze's Curse",
        "Blizzard", "Bounce", "Burst", "Combustion", "Conversion", "Decapitation",
        "Extraction", "Fire", "Firestorm", "Fireworks", "Force", "Frozen Step",
        "Fuse", "Germination", "Glide", "Gluttony", "Gold Rush", "Grab", "Green Thumb",
        "Gust", "Harvest", "Haste", "Ice Aspect", "Jump", "Laser", "Level", "Long Cast",
        "Lumber", "Magnetism", "Meador", "Mow", "Mystery Fish", "Nether Step", "Night Vision",
        "Persephone", "Pierce", "Plough", "Potion", "Potion Resistance", "Quick Shot",
        "Rainbow", "Rainbow Slam", "Reaper", "Reveal", "Saturation", "Short Cast",
        "Shred", "Siphon", "Sonic Shock", "Spectral", "Speed", "Spikes", "Spread",
        "Stationary", "Stock", "Stream", "Switch", "Terraformer", "Toxic", "Tracer",
        "Transformation", "Variety", "Vortex", "Weight", "Apocalypse", "Ethereal",
        "Missile", "Singularity", "Unrepairable"};

    static {
        ENCHANTMENT_PRICES.put("anthropomorphism-1", 1);

        ENCHANTMENT_PRICES.put("arborist-1", 20);
        ENCHANTMENT_PRICES.put("arborist-2", 40);
        ENCHANTMENT_PRICES.put("arborist-3", 80);

        ENCHANTMENT_PRICES.put("bind-1", 200);

        ENCHANTMENT_PRICES.put("blaze's_curse-1", 100);

        ENCHANTMENT_PRICES.put("blizzard-1", 1);
        ENCHANTMENT_PRICES.put("blizzard-2", 1);
        ENCHANTMENT_PRICES.put("blizzard-3", 1);

        ENCHANTMENT_PRICES.put("bounce-1", 5);
        ENCHANTMENT_PRICES.put("bounce-2", 10);
        ENCHANTMENT_PRICES.put("bounce-3", 15);
        ENCHANTMENT_PRICES.put("bounce-4", 20);
        ENCHANTMENT_PRICES.put("bounce-5", 25);

        ENCHANTMENT_PRICES.put("burst-1", 15);
        ENCHANTMENT_PRICES.put("burst-2", 25);
        ENCHANTMENT_PRICES.put("burst-3", 50);

        ENCHANTMENT_PRICES.put("combustion-1", 10);
        ENCHANTMENT_PRICES.put("combustion-2", 20);
        ENCHANTMENT_PRICES.put("combustion-3", 30);
        ENCHANTMENT_PRICES.put("combustion-4", 50);

        ENCHANTMENT_PRICES.put("conversion-1", 10);

        ENCHANTMENT_PRICES.put("decapitation-1", 10);
        ENCHANTMENT_PRICES.put("decapitation-2", 15);
        ENCHANTMENT_PRICES.put("decapitation-3", 20);
        ENCHANTMENT_PRICES.put("decapitation-4", 25);

        ENCHANTMENT_PRICES.put("extraction-1", 50);
        ENCHANTMENT_PRICES.put("extraction-2", 100);
        ENCHANTMENT_PRICES.put("extraction-3", 200);

        ENCHANTMENT_PRICES.put("efficiency-1", 1);
        ENCHANTMENT_PRICES.put("efficiency-2", 2);
        ENCHANTMENT_PRICES.put("efficiency-3", 4);
        ENCHANTMENT_PRICES.put("efficiency-4", 8);
        ENCHANTMENT_PRICES.put("efficiency-5", 16);

        ENCHANTMENT_PRICES.put("efficiency-5", 16);

    }

}
