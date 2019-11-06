package zbottestplugin.enchantengine2;

import zedly.zbot.BlockFace;
import zedly.zbot.Location;
import zedly.zbot.util.CartesianVector;

public class LibraryLocation {

    private static final String[] romanStrings = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private static final int COLUMN_SIZE = EnchantEngine.NUM_ROWS * EnchantEngine.CHEST_SIZE;
    private static final int BLOCK_SIZE = EnchantEngine.NUM_COLUMNS * COLUMN_SIZE;
    private static final int AISLE_SIZE = EnchantEngine.NUM_BLOCKS * BLOCK_SIZE;

    private final int absoluteIndex;

    public LibraryLocation(int aisle, int aBlock, int row, int chest, int column, int rowSlot) {
        absoluteIndex
                = EnchantEngine.NUM_BLOCKS * EnchantEngine.NUM_COLUMNS * EnchantEngine.NUM_ROWS * EnchantEngine.CHEST_SIZE * aisle
                + EnchantEngine.NUM_COLUMNS * EnchantEngine.NUM_ROWS * EnchantEngine.CHEST_SIZE * aBlock
                + EnchantEngine.NUM_ROWS * EnchantEngine.CHEST_SIZE * column
                + EnchantEngine.CHEST_SIZE * chest
                + 9 * row
                + rowSlot;
    }

    public LibraryLocation(int absoluteIndex) {
        this.absoluteIndex = absoluteIndex;
    }

    public Location getLocation() {
        return EnchantEngine.ROOT_CHEST.getRelative(EnchantEngine.INCREMENT_AISLE.getDirection().multiply(getAisle() * EnchantEngine.AISLE_WIDTH))
                .getRelative(EnchantEngine.INCREMENT_AISLE.getDirection().multiply(getBlock() * EnchantEngine.BLOCK_WIDTH))
                .getRelative(EnchantEngine.INCREMENT_COLUMN.getDirection().multiply(getColumn() * EnchantEngine.COLUMN_WIDTH))
                .getRelative(new CartesianVector(0, 1, 0).multiply(getChest()));
    }

    public Location getWalkLocation() {
        return EnchantEngine.ROOT_WALK_LOCATION.getRelative(EnchantEngine.INCREMENT_AISLE.getDirection().multiply(getAisle() * EnchantEngine.AISLE_WIDTH))
                .getRelative(EnchantEngine.INCREMENT_COLUMN.getDirection().multiply(getColumn() * EnchantEngine.COLUMN_WIDTH)).centerHorizontally();
    }
    
    public BlockFace getFaceToClick() {
        return (getBlock() % 2) == 0 ? EnchantEngine.INCREMENT_AISLE : EnchantEngine.INCREMENT_AISLE.getOppositeFace();
    }

    public int getAboluteIndex() {
        return absoluteIndex;
    }

    @Override
    public String toString() {
        return romanStrings[getAisle() * EnchantEngine.NUM_BLOCKS + getBlock()] + "-" + (char) ('A' + getColumn()) + getChest() + ":" + (getRow() + 1) + "," + (getRowSlot() + 1);
    }

    private int getAisle() {
        return (absoluteIndex / AISLE_SIZE) % EnchantEngine.NUM_AISLES;
    }

    private int getBlock() {
        return (absoluteIndex / BLOCK_SIZE) % EnchantEngine.NUM_BLOCKS;
    }

    private int getColumn() {
        return (absoluteIndex / COLUMN_SIZE) % EnchantEngine.NUM_COLUMNS;
    }

    private int getChest() {
        return (absoluteIndex / EnchantEngine.CHEST_SIZE) % EnchantEngine.NUM_ROWS;
    }

    private int getRow() {
        return (absoluteIndex / 9) % (EnchantEngine.CHEST_SIZE / 9);
    }

    private int getRowSlot() {
        return absoluteIndex % 9;
    }

    public int getSlot() {
        return absoluteIndex % 54;
    }
}
