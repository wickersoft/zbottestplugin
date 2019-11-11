/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin.enchantengine2;

import zbottestplugin.enchantengine.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zedly.zbot.BlockFace;
import zedly.zbot.Location;
import zedly.zbot.inventory.Enchantment;
import zedly.zbot.inventory.ItemStack;

/**
 *
 * @author Dennis
 */
public class EnchantEngine {

    static final Location HOME_LOC = new Location(297, 122, -8704).centerHorizontally();
    static final Location OUTPUT_LOC = HOME_LOC;
    static final Location OUTPUT_CHEST = new Location(296, 122, -8704);

    static final Location ROOT_CHEST = new Location(301, 122, -8717);
    static final Location ROOT_WALK_LOCATION = new Location(301, 122, -8716);
    static final BlockFace INCREMENT_COLUMN = BlockFace.EAST;
    static final BlockFace INCREMENT_AISLE = BlockFace.SOUTH;
    static final int NUM_BLOCKS = 2;
    static final int NUM_COLUMNS = 25;
    static final int AISLE_WIDTH = 6;
    static final int BLOCK_WIDTH = 2;
    static final int NUM_ROWS = 4;
    static final int COLUMN_WIDTH = 1;
    static final int NUM_AISLES = 3;
    static final int CHEST_SIZE = 54;

    private static final Pattern LORE_ENCHANT_PATTERN = Pattern.compile("\\u00a77([A-Za-z\\' ]+) (I|II|III|IV|V)?$");
    private static final String[] ENCHANT_DATABASE = new String[120000];
    private static final HashMap<String, Integer> ENCHANTMENT_PRICES = new HashMap<>();
    private static Integer[] queryResults = new Integer[0];
    private static int lastQueryIndex = 0;

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

    public static final String[] adItems = {"fortune-3", "silk_touch", "shred-5", "pierce", "lumber", "extraction-3", "gluttony", "night_vision", "stock", "aqua_affinity", "depth_strider-3", "infinity"};

    public static void rememberItemString(int absoluteIndex, String enchantString) {
        ENCHANT_DATABASE[absoluteIndex] = enchantString;
    }

    public static int queryEnchantment(String filter) {
        ArrayList<Integer> results = new ArrayList<>();
        String[] args = filter.split(" ");

        for (int i = 0; i < ENCHANT_DATABASE.length; i++) {
            boolean found = false;
            for (int k = 0; k < args.length; k++) {
                if (ENCHANT_DATABASE[i] != null && ENCHANT_DATABASE[i].equals(args[k])) {
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

        Arrays.sort(queryResults, (a, b) -> {
            int aPrice = getPriceEstimate(a);
            int bPrice = getPriceEstimate(b);
            if (aPrice < bPrice) {
                return -1;
            } else if (bPrice < aPrice) {
                return 1;
            } else {
                return 0;
            }
        });

        return results.size();
    }

    public static int getNumResults() {
        return queryResults.length;
    }

    public static int getQueryResult(int listItem) {
        lastQueryIndex = listItem;
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
    
    public static int getLastQueryIndex() {
        return lastQueryIndex;
    }

    public static int getPriceEstimate(int absoluteIndex) {
        String enchString = getEnchantString(absoluteIndex);
        if (enchString.equals("<empty>")) {
            return 0;
        }

        String[] elements = enchString.split(" ");

        int totalPrice = 0;
        for (String item : elements) {
            if (!ENCHANTMENT_PRICES.containsKey(item)) {
                System.err.println("Unknown enchantment: " + item);
            }
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
                Matcher m = LORE_ENCHANT_PATTERN.matcher(line);
                if (m.find()) {
                    String name = m.group(1);
                    String level = m.group(2);
                    if (isKnownZenchant(name)) {
                        sb.append(name.replace(" ", "_").toLowerCase()).append("-").append(getNumber(level)).append(" ");
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
        if (numeral == null) {
            return 1;
        }
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
        return loc.toString();
    }

    public static String friendlyIndex(int absoluteIndex) {
        return friendlyIndex(new LibraryLocation(absoluteIndex));
    }
    
    public static int getNumIndexedItems() {
        int i;
        for (i = 0; i < ENCHANT_DATABASE.length && ENCHANT_DATABASE[i] != null; i++) {
        }
        return i;
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
        ENCHANTMENT_PRICES.put("diamond_pickaxe", 30);
        ENCHANTMENT_PRICES.put("diamond_shovel", 10);
        ENCHANTMENT_PRICES.put("diamond_axe", 30);
        ENCHANTMENT_PRICES.put("diamond_helmet", 50);
        ENCHANTMENT_PRICES.put("diamond_chestplate", 80);
        ENCHANTMENT_PRICES.put("diamond_leggings", 70);
        ENCHANTMENT_PRICES.put("diamond_boots", 40);
        ENCHANTMENT_PRICES.put("enchanted_book", 0);
        ENCHANTMENT_PRICES.put("bow", 0);

        ENCHANTMENT_PRICES.put("anthropomorphism-1", 5);

        ENCHANTMENT_PRICES.put("arborist-1", 15);
        ENCHANTMENT_PRICES.put("arborist-2", 30);
        ENCHANTMENT_PRICES.put("arborist-3", 60);

        ENCHANTMENT_PRICES.put("bind-1", 250);

        ENCHANTMENT_PRICES.put("blaze's_curse-1", 150);

        ENCHANTMENT_PRICES.put("blizzard-1", 5);
        ENCHANTMENT_PRICES.put("blizzard-2", 10);
        ENCHANTMENT_PRICES.put("blizzard-3", 20);

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

        ENCHANTMENT_PRICES.put("conversion-1", 5);
        ENCHANTMENT_PRICES.put("conversion-2", 10);
        ENCHANTMENT_PRICES.put("conversion-3", 20);
        ENCHANTMENT_PRICES.put("conversion-4", 40);

        ENCHANTMENT_PRICES.put("decapitation-1", 15);
        ENCHANTMENT_PRICES.put("decapitation-2", 30);
        ENCHANTMENT_PRICES.put("decapitation-3", 60);
        ENCHANTMENT_PRICES.put("decapitation-4", 120);

        ENCHANTMENT_PRICES.put("extraction-1", 40);
        ENCHANTMENT_PRICES.put("extraction-2", 80);
        ENCHANTMENT_PRICES.put("extraction-3", 160);

        ENCHANTMENT_PRICES.put("fire-1", 50);

        ENCHANTMENT_PRICES.put("firestorm-1", 20);
        ENCHANTMENT_PRICES.put("firestorm-2", 40);
        ENCHANTMENT_PRICES.put("firestorm-3", 80);

        ENCHANTMENT_PRICES.put("fireworks-1", 30);
        ENCHANTMENT_PRICES.put("fireworks-2", 50);
        ENCHANTMENT_PRICES.put("fireworks-3", 75);
        ENCHANTMENT_PRICES.put("fireworks-4", 100);

        ENCHANTMENT_PRICES.put("force-1", 40);
        ENCHANTMENT_PRICES.put("force-2", 80);
        ENCHANTMENT_PRICES.put("force-3", 160);

        ENCHANTMENT_PRICES.put("frozen_step-1", 25);
        ENCHANTMENT_PRICES.put("frozen_step-2", 50);
        ENCHANTMENT_PRICES.put("frozen_step-3", 100);

        ENCHANTMENT_PRICES.put("fuse-1", 50);

        ENCHANTMENT_PRICES.put("germination-1", 20);
        ENCHANTMENT_PRICES.put("germination-2", 40);
        ENCHANTMENT_PRICES.put("germination-3", 80);

        ENCHANTMENT_PRICES.put("glide-1", 40);
        ENCHANTMENT_PRICES.put("glide-2", 80);
        ENCHANTMENT_PRICES.put("glide-3", 160);

        ENCHANTMENT_PRICES.put("gluttony-1", 200);

        ENCHANTMENT_PRICES.put("gold_rush-1", 10);
        ENCHANTMENT_PRICES.put("gold_rush-2", 20);
        ENCHANTMENT_PRICES.put("gold_rush-3", 40);

        ENCHANTMENT_PRICES.put("grab-1", 75);

        ENCHANTMENT_PRICES.put("green_thumb-1", 5);
        ENCHANTMENT_PRICES.put("green_thumb-2", 10);
        ENCHANTMENT_PRICES.put("green_thumb-3", 20);

        ENCHANTMENT_PRICES.put("gust-1", 100);

        ENCHANTMENT_PRICES.put("harvest-1", 20);
        ENCHANTMENT_PRICES.put("harvest-2", 40);
        ENCHANTMENT_PRICES.put("harvest-3", 80);

        ENCHANTMENT_PRICES.put("haste-1", 25);
        ENCHANTMENT_PRICES.put("haste-2", 50);
        ENCHANTMENT_PRICES.put("haste-3", 75);
        ENCHANTMENT_PRICES.put("haste-4", 100);

        ENCHANTMENT_PRICES.put("ice_aspect-1", 20);
        ENCHANTMENT_PRICES.put("ice_aspect-2", 40);

        ENCHANTMENT_PRICES.put("jump-1", 15);
        ENCHANTMENT_PRICES.put("jump-2", 30);
        ENCHANTMENT_PRICES.put("jump-3", 60);
        ENCHANTMENT_PRICES.put("jump-4", 120);

        ENCHANTMENT_PRICES.put("laser-1", 80);
        ENCHANTMENT_PRICES.put("laser-2", 160);
        ENCHANTMENT_PRICES.put("laser-3", 320);

        ENCHANTMENT_PRICES.put("level-1", 30);
        ENCHANTMENT_PRICES.put("level-2", 60);
        ENCHANTMENT_PRICES.put("level-3", 120);

        ENCHANTMENT_PRICES.put("long_cast-1", 5);
        ENCHANTMENT_PRICES.put("long_cast-2", 10);

        ENCHANTMENT_PRICES.put("lumber-1", 300);

        ENCHANTMENT_PRICES.put("magnetism-1", 25);
        ENCHANTMENT_PRICES.put("magnetism-2", 50);
        ENCHANTMENT_PRICES.put("magnetism-3", 100);

        ENCHANTMENT_PRICES.put("meador-1", 400);

        ENCHANTMENT_PRICES.put("mow-1", 10);
        ENCHANTMENT_PRICES.put("mow-2", 20);
        ENCHANTMENT_PRICES.put("mow-3", 40);

        ENCHANTMENT_PRICES.put("mystery_fish-1", 75);
        ENCHANTMENT_PRICES.put("mystery_fish-2", 150);
        ENCHANTMENT_PRICES.put("mystery_fish-3", 300);

        ENCHANTMENT_PRICES.put("nether_step-1", 75);
        ENCHANTMENT_PRICES.put("nether_step-2", 100);
        ENCHANTMENT_PRICES.put("nether_step-3", 150);

        ENCHANTMENT_PRICES.put("night_vision-1", 400);

        ENCHANTMENT_PRICES.put("persephone-1", 20);
        ENCHANTMENT_PRICES.put("persephone-2", 40);
        ENCHANTMENT_PRICES.put("persephone-3", 80);

        ENCHANTMENT_PRICES.put("pierce-1", 250);

        ENCHANTMENT_PRICES.put("plough-1", 20);
        ENCHANTMENT_PRICES.put("plough-2", 40);
        ENCHANTMENT_PRICES.put("plough-3", 80);

        ENCHANTMENT_PRICES.put("potion-1", 10);
        ENCHANTMENT_PRICES.put("potion-2", 20);
        ENCHANTMENT_PRICES.put("potion-3", 40);

        ENCHANTMENT_PRICES.put("potion_resistance-1", 10);
        ENCHANTMENT_PRICES.put("potion_resistance-2", 20);
        ENCHANTMENT_PRICES.put("potion_resistance-3", 40);
        ENCHANTMENT_PRICES.put("potion_resistance-4", 80);

        ENCHANTMENT_PRICES.put("quick_shot-1", 75);

        ENCHANTMENT_PRICES.put("rainbow-1", 100);

        ENCHANTMENT_PRICES.put("rainbow_slam-1", 50);
        ENCHANTMENT_PRICES.put("rainbow_slam-2", 75);
        ENCHANTMENT_PRICES.put("rainbow_slam-3", 100);
        ENCHANTMENT_PRICES.put("rainbow_slam-4", 125);

        ENCHANTMENT_PRICES.put("reaper-1", 30);
        ENCHANTMENT_PRICES.put("reaper-2", 60);
        ENCHANTMENT_PRICES.put("reaper-3", 100);
        ENCHANTMENT_PRICES.put("reaper-4", 150);

        ENCHANTMENT_PRICES.put("saturation-1", 20);
        ENCHANTMENT_PRICES.put("saturation-2", 40);
        ENCHANTMENT_PRICES.put("saturation-3", 80);

        ENCHANTMENT_PRICES.put("short_cast-1", 5);
        ENCHANTMENT_PRICES.put("short_cast-2", 10);

        ENCHANTMENT_PRICES.put("shred-1", 25);
        ENCHANTMENT_PRICES.put("shred-2", 50);
        ENCHANTMENT_PRICES.put("shred-3", 100);
        ENCHANTMENT_PRICES.put("shred-4", 200);
        ENCHANTMENT_PRICES.put("shred-5", 400);

        ENCHANTMENT_PRICES.put("siphon-1", 10);
        ENCHANTMENT_PRICES.put("siphon-2", 20);
        ENCHANTMENT_PRICES.put("siphon-3", 40);
        ENCHANTMENT_PRICES.put("siphon-4", 80);

        ENCHANTMENT_PRICES.put("sonic_shock-1", 10);
        ENCHANTMENT_PRICES.put("sonic_shock-2", 20);
        ENCHANTMENT_PRICES.put("sonic_shock-3", 40);

        ENCHANTMENT_PRICES.put("speed-1", 15);
        ENCHANTMENT_PRICES.put("speed-2", 30);
        ENCHANTMENT_PRICES.put("speed-3", 60);
        ENCHANTMENT_PRICES.put("speed-4", 120);

        ENCHANTMENT_PRICES.put("spikes-1", 20);
        ENCHANTMENT_PRICES.put("spikes-2", 40);
        ENCHANTMENT_PRICES.put("spikes-3", 80);

        ENCHANTMENT_PRICES.put("spread-1", 20);
        ENCHANTMENT_PRICES.put("spread-2", 45);
        ENCHANTMENT_PRICES.put("spread-3", 90);
        ENCHANTMENT_PRICES.put("spread-4", 180);
        ENCHANTMENT_PRICES.put("spread-5", 360);

        ENCHANTMENT_PRICES.put("stationary-1", 10);

        ENCHANTMENT_PRICES.put("stock-1", 250);

        ENCHANTMENT_PRICES.put("stream-1", 5);

        ENCHANTMENT_PRICES.put("switch-1", 50);

        ENCHANTMENT_PRICES.put("terraformer-1", 250);

        ENCHANTMENT_PRICES.put("toxic-1", 25);
        ENCHANTMENT_PRICES.put("toxic-2", 50);
        ENCHANTMENT_PRICES.put("toxic-3", 100);
        ENCHANTMENT_PRICES.put("toxic-4", 200);

        ENCHANTMENT_PRICES.put("tracer-1", 30);
        ENCHANTMENT_PRICES.put("tracer-2", 60);
        ENCHANTMENT_PRICES.put("tracer-3", 120);
        ENCHANTMENT_PRICES.put("tracer-4", 240);

        ENCHANTMENT_PRICES.put("transformation-1", 80);
        ENCHANTMENT_PRICES.put("transformation-2", 160);
        ENCHANTMENT_PRICES.put("transformation-3", 320);

        ENCHANTMENT_PRICES.put("variety-1", 200);

        ENCHANTMENT_PRICES.put("vortex-1", 75);

        ENCHANTMENT_PRICES.put("weight-1", 10);
        ENCHANTMENT_PRICES.put("weight-2", 20);
        ENCHANTMENT_PRICES.put("weight-3", 40);
        ENCHANTMENT_PRICES.put("weight-4", 80);

        
        // VANILLA STARTS HERE
        
        ENCHANTMENT_PRICES.put("aqua_affinity-1", 150);

        ENCHANTMENT_PRICES.put("bane_of_arthropods-1", 5);
        ENCHANTMENT_PRICES.put("bane_of_arthropods-2", 10);
        ENCHANTMENT_PRICES.put("bane_of_arthropods-3", 20);
        ENCHANTMENT_PRICES.put("bane_of_arthropods-4", 40);
        ENCHANTMENT_PRICES.put("bane_of_arthropods-5", 80);

        ENCHANTMENT_PRICES.put("blast_protection-1", 10);
        ENCHANTMENT_PRICES.put("blast_protection-2", 20);
        ENCHANTMENT_PRICES.put("blast_protection-3", 40);
        ENCHANTMENT_PRICES.put("blast_protection-4", 80);

        ENCHANTMENT_PRICES.put("channeling-1", 75);
        
        ENCHANTMENT_PRICES.put("depth_strider-1", 40);
        ENCHANTMENT_PRICES.put("depth_strider-2", 80);
        ENCHANTMENT_PRICES.put("depth_strider-3", 160);

        ENCHANTMENT_PRICES.put("efficiency-1", 5);
        ENCHANTMENT_PRICES.put("efficiency-2", 10);
        ENCHANTMENT_PRICES.put("efficiency-3", 20);
        ENCHANTMENT_PRICES.put("efficiency-4", 40);
        ENCHANTMENT_PRICES.put("efficiency-5", 80);

        ENCHANTMENT_PRICES.put("feather_falling-1", 5);
        ENCHANTMENT_PRICES.put("feather_falling-2", 10);
        ENCHANTMENT_PRICES.put("feather_falling-3", 20);
        ENCHANTMENT_PRICES.put("feather_falling-4", 40);

        ENCHANTMENT_PRICES.put("fire_aspect-1", 20);
        ENCHANTMENT_PRICES.put("fire_aspect-2", 40);

        ENCHANTMENT_PRICES.put("fire_protection-1", 10);
        ENCHANTMENT_PRICES.put("fire_protection-2", 20);
        ENCHANTMENT_PRICES.put("fire_protection-3", 40);
        ENCHANTMENT_PRICES.put("fire_protection-4", 80);

        ENCHANTMENT_PRICES.put("flame-1", 50);

        ENCHANTMENT_PRICES.put("fortune-1", 50);
        ENCHANTMENT_PRICES.put("fortune-2", 100);
        ENCHANTMENT_PRICES.put("fortune-3", 200);

        ENCHANTMENT_PRICES.put("frost_walker-1", 25);
        ENCHANTMENT_PRICES.put("frost_walker-2", 50);
        ENCHANTMENT_PRICES.put("frost_walker-3", 100);

        ENCHANTMENT_PRICES.put("impaling-1", 5);
        ENCHANTMENT_PRICES.put("impaling-2", 10);
        ENCHANTMENT_PRICES.put("impaling-3", 20);
        ENCHANTMENT_PRICES.put("impaling-4", 40);
        ENCHANTMENT_PRICES.put("impaling-5", 80);
        
        ENCHANTMENT_PRICES.put("infinity-1", 150);

        ENCHANTMENT_PRICES.put("knockback-1", 20);
        ENCHANTMENT_PRICES.put("knockback-2", 40);

        ENCHANTMENT_PRICES.put("looting-1", 25);
        ENCHANTMENT_PRICES.put("looting-2", 50);
        ENCHANTMENT_PRICES.put("looting-3", 100);

        ENCHANTMENT_PRICES.put("loyalty-1", 5);
        ENCHANTMENT_PRICES.put("loyalty-2", 10);
        ENCHANTMENT_PRICES.put("loyalty-3", 20);
        
        ENCHANTMENT_PRICES.put("luck_of_the_sea-1", 20);
        ENCHANTMENT_PRICES.put("luck_of_the_sea-2", 40);
        ENCHANTMENT_PRICES.put("luck_of_the_sea-3", 80);

        ENCHANTMENT_PRICES.put("lure-1", 10);
        ENCHANTMENT_PRICES.put("lure-2", 20);
        ENCHANTMENT_PRICES.put("lure-3", 40);

        ENCHANTMENT_PRICES.put("mending-1", 100);

        ENCHANTMENT_PRICES.put("multishot-1", 25);
        
        ENCHANTMENT_PRICES.put("piercing-1", 5);
        ENCHANTMENT_PRICES.put("piercing-2", 10);
        ENCHANTMENT_PRICES.put("piercing-3", 20);
        ENCHANTMENT_PRICES.put("piercing-4", 40);
        
        ENCHANTMENT_PRICES.put("power-1", 10);
        ENCHANTMENT_PRICES.put("power-2", 20);
        ENCHANTMENT_PRICES.put("power-3", 40);
        ENCHANTMENT_PRICES.put("power-4", 80);
        ENCHANTMENT_PRICES.put("power-5", 160);

        ENCHANTMENT_PRICES.put("projectile_protection-1", 10);
        ENCHANTMENT_PRICES.put("projectile_protection-2", 20);
        ENCHANTMENT_PRICES.put("projectile_protection-3", 40);
        ENCHANTMENT_PRICES.put("projectile_protection-4", 80);

        ENCHANTMENT_PRICES.put("protection-1", 25);
        ENCHANTMENT_PRICES.put("protection-2", 50);
        ENCHANTMENT_PRICES.put("protection-3", 100);
        ENCHANTMENT_PRICES.put("protection-4", 200);

        ENCHANTMENT_PRICES.put("punch-1", 25);
        ENCHANTMENT_PRICES.put("punch-2", 50);

        ENCHANTMENT_PRICES.put("quick_charge-1", 5);
        ENCHANTMENT_PRICES.put("quick_charge-2", 10);
        ENCHANTMENT_PRICES.put("quick_charge-3", 20);
        
        ENCHANTMENT_PRICES.put("respiration-1", 75);
        ENCHANTMENT_PRICES.put("respiration-2", 150);
        ENCHANTMENT_PRICES.put("respiration-3", 300);
        
        ENCHANTMENT_PRICES.put("riptide-1", 5);
        ENCHANTMENT_PRICES.put("riptide-2", 10);
        ENCHANTMENT_PRICES.put("riptide-3", 20);
        
        ENCHANTMENT_PRICES.put("sharpness-1", 10);
        ENCHANTMENT_PRICES.put("sharpness-2", 20);
        ENCHANTMENT_PRICES.put("sharpness-3", 40);
        ENCHANTMENT_PRICES.put("sharpness-4", 80);
        ENCHANTMENT_PRICES.put("sharpness-5", 160);

        ENCHANTMENT_PRICES.put("silk_touch-1", 100);

        ENCHANTMENT_PRICES.put("smite-1", 5);
        ENCHANTMENT_PRICES.put("smite-2", 10);
        ENCHANTMENT_PRICES.put("smite-3", 20);
        ENCHANTMENT_PRICES.put("smite-4", 40);
        ENCHANTMENT_PRICES.put("smite-5", 80);

        ENCHANTMENT_PRICES.put("sweeping_edge-1", 20);
        ENCHANTMENT_PRICES.put("sweeping_edge-2", 40);
        ENCHANTMENT_PRICES.put("sweeping_edge-3", 80);

        ENCHANTMENT_PRICES.put("thorns-1", 20);
        ENCHANTMENT_PRICES.put("thorns-2", 40);
        ENCHANTMENT_PRICES.put("thorns-3", 80);
        ENCHANTMENT_PRICES.put("thorns-4", 160);

        ENCHANTMENT_PRICES.put("unbreaking-1", 15);
        ENCHANTMENT_PRICES.put("unbreaking-2", 30);
        ENCHANTMENT_PRICES.put("unbreaking-3", 60);
    }
}
