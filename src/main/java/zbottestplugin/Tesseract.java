package zbottestplugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zedly.zbot.Location;
import zedly.zbot.Material;
import zedly.zbot.block.TileSign;
import zedly.zbot.environment.Block;

final class Tesseract {

    private static final Pattern STACK_NOTATION_PATTERN = Pattern.compile("^(\\d+)x(\\d{1,2})\\+(\\d{1,2})", 0);
    private final Location loc;
    private Material material;
    private long amount;

    /**
     * Creates an empty Tesseract
     */
    public Tesseract(Location loc) {
        this.loc = loc;
    }

    /**
     * Creates a Tesseract with the specified contents
     *
     * @param mat
     * @param amount
     */
    public Tesseract(int x, int y, int z) {
        this.loc = new Location(x, y, z);
    }

    public Material getMaterial() {
        if (!load()) {
            throw new IllegalStateException("This block is not a Tesseract");
        }
        return material;
    }

    public long getAmount() {
        if (!load()) {
            throw new IllegalStateException("This block is not a Tesseract");
        }
        return amount;
    }

    public boolean isEmpty() {
        return amount == 0 || material == Material.AIR;
    }

    /**
     * Check if the given block meets all criteria for a valid Tesseract. - The
     * block must be a sign according to isSign(Block) - The sign must contain
     * the top line [Tesseract] in DARK_BLUE - The sign must match any official
     * encoding scheme (V1-V4)
     *
     * @param block
     * @return
     */
    public static boolean isTesseract(Block block) {
        if (!isSign(block)) {
            return false;
        }
        if (!block.hasTile() || !(block.getTile() instanceof TileSign)) {
            return false;
        }
        TileSign sign = (TileSign) block.getTile();
        return isTesseractV4(sign) || isTesseractV3(sign);
    }

    /**
     * Checks if the given Block is any of the materials representing a type of
     * sign in 1.14.4.
     *
     * @param block
     * @return true if the block is a sign
     */
    public static boolean isSign(Block block) {
        return block != null && isMaterialSign(block.getType());
    }

    public static boolean isMaterialSign(final Material material) {
        switch (material) {
            case ACACIA_SIGN:
            case ACACIA_WALL_SIGN:
            case BIRCH_SIGN:
            case BIRCH_WALL_SIGN:
            case DARK_OAK_SIGN:
            case DARK_OAK_WALL_SIGN:
            case JUNGLE_SIGN:
            case JUNGLE_WALL_SIGN:
            case OAK_SIGN:
            case OAK_WALL_SIGN:
            case SPRUCE_SIGN:
            case SPRUCE_WALL_SIGN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Create a Tesseract based on the contents of a sign. The sign must contain
     * a valid Tesseract encoding according to isTesseract(Sign). Supports all
     * encoding schemes ever published since 2012.
     *
     * @param sign
     * @return
     */
    private boolean load() {
        Block block = Storage.self.getEnvironment().getBlockAt(loc);
        if (!block.hasTile() || !(block.getTile() instanceof TileSign)) {
            return false;
        }
        TileSign sign = (TileSign) block.getTile();
        if (!sign.getLine(0).equals("[Tesseract]")) {
            return false;
        }

        // Attempt to parse
        if (isTesseractV4(sign)) {
            loadV4(sign);
            return true;
        }
        if (isTesseractV3(sign)) {
            loadV3(sign);
            return true;
        }

        // Ignore non-tesseracts entirely.
        // TODO: Add error message if top line matches?
        return false;
    }

    /*
        Tesseract V4 encoding scheme (October 2019):
    
        (1) &1[Tesseract]
        (2) MATERIAL
        (3) AxB+C or A
        (4)
    
        A = Number of stacks if material is stackable, or number of items
        B = Max stack size of material contained
        C = Number of items % stack size
     */
    private static boolean isTesseractV4(TileSign sign) {
        return (sign.getLine(0).equals("[Tesseract]")
                && sign.getLine(3).isEmpty()
                && (sign.getLine(2).matches("\\d+") || sign.getLine(2).matches("^(\\d+)x(\\d{1,2})\\+\\d{1,2}$"))
                && (sign.getLine(1).equals("EMPTY") || Material.valueOf(sign.getLine(1)) != null));
    }

    private boolean loadV4(TileSign sign) {
        material = Material.AIR;
        amount = 0;
        if (sign.getLine(1).equals("EMPTY")) {
            return true;
        }

        material = Material.valueOf(sign.getLine(1));
        amount = parseStackNotation(sign.getLine(2));
        if (material == null || amount == -1) {
            return false;
        }
        return true;
    }

    /*
        Tesseract V3 encoding scheme (Early 2019):
    
        (1) &1[Tesseract]
        (2) -
        (3) MATERIAL
        (4) Number of items (Base 10)
    
     */
    private static boolean isTesseractV3(TileSign sign) {
        return (sign.getLine(0).equals("[Tesseract]")
                && sign.getLine(1).equals("-")
                && sign.getLine(3).matches("^\\d+$")
                && (sign.getLine(1).equals("EMPTY") || Material.valueOf(sign.getLine(2)) != null));
    }

    private boolean loadV3(TileSign sign) {
        material = Material.AIR;
        amount = 0;
        if (sign.getLine(2).equals("EMPTY")) {
            return true;
        }

        material = Material.valueOf(sign.getLine(2));
        amount = Long.parseLong(sign.getLine(3));
        if (material == null || amount == -1) {
            return false;
        }
        return true;
    }

    /**
     * Reconstructs the true number of items from the stacks+items notation.
     *
     * @param amount
     * @return
     */
    private static long parseStackNotation(String amount) {
        Matcher matcher = STACK_NOTATION_PATTERN.matcher(amount);
        if (!matcher.find()) {
            if (amount.matches("\\d+")) {
                return Long.parseLong(amount);
            } else {
                return -1;
            }
        }
        String s_stacks = matcher.group(1);
        String s_stackSize = matcher.group(2);
        String s_items = matcher.group(3);
        return Long.parseLong(s_stacks) * Long.parseLong(s_stackSize) + Long.parseLong(s_items);
    }

    /**
     * Produces a String containing the Tesseract's state for debugging
     * purposes.
     *
     * @return
     */
    @Override
    public String toString() {
        if (material == Material.AIR) {
            return "{Tesseract: EMPTY}";
        }
        return "{Tesseract: " + amount + " " + material + "}";
    }
}
