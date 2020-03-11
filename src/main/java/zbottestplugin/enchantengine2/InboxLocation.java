package zbottestplugin.enchantengine2;

import zedly.zbot.BlockFace;
import zedly.zbot.Location;
import zedly.zbot.util.CartesianVector;

public class InboxLocation {

    private static final String[] romanStrings = {"i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x"};

    private static final int CHEST_SIZE = 54;
    private static final int NUM_ROWS = 4;
    private static final int NUM_COLUMNS = 13;
    private static final int NUM_BLOCKS = 2;
    private static final int NUM_AISLES = 1;
    static final int AISLE_WIDTH = 6;
    static final int BLOCK_WIDTH = 2;
    static final int COLUMN_WIDTH = 1;
    

    static final Location ROOT_CHEST = new Location(291, 131, -8696);
    static final Location ROOT_WALK_LOCATION = new Location(291, 131, -8695);
    static final BlockFace INCREMENT_COLUMN = BlockFace.EAST;
    static final BlockFace INCREMENT_AISLE = BlockFace.SOUTH;

    private static final int COLUMN_SIZE = NUM_ROWS * CHEST_SIZE;
    private static final int BLOCK_SIZE = NUM_COLUMNS * COLUMN_SIZE;
    private static final int AISLE_SIZE = NUM_BLOCKS * BLOCK_SIZE;

    private final int absoluteIndex;

    public InboxLocation(int aisle, int aBlock, int row, int chest, int column, int rowSlot) {
        absoluteIndex
                = NUM_BLOCKS * NUM_COLUMNS * NUM_ROWS * CHEST_SIZE * aisle
                + NUM_COLUMNS * NUM_ROWS * CHEST_SIZE * aBlock
                + NUM_ROWS * CHEST_SIZE * column
                + CHEST_SIZE * chest
                + 9 * row
                + rowSlot;
    }

    public InboxLocation(int absoluteIndex) {
        this.absoluteIndex = absoluteIndex;
    }

    public Location getLocation() {
        return ROOT_CHEST.getRelative(INCREMENT_AISLE.getDirection().multiply(getAisle() * AISLE_WIDTH))
                .getRelative(INCREMENT_AISLE.getDirection().multiply(getBlock() * BLOCK_WIDTH))
                .getRelative(INCREMENT_COLUMN.getDirection().multiply(getColumn() * COLUMN_WIDTH))
                .getRelative(new CartesianVector(0, 1, 0).multiply(getChest()));
    }

    public Location getWalkLocation() {
        return ROOT_WALK_LOCATION.getRelative(INCREMENT_AISLE.getDirection().multiply(getAisle() * AISLE_WIDTH))
                .getRelative(INCREMENT_COLUMN.getDirection().multiply(getColumn() * COLUMN_WIDTH)).centerHorizontally();
    }

    public BlockFace getFaceToClick() {
        return (getBlock() % 2) == 0 ? INCREMENT_AISLE : INCREMENT_AISLE.getOppositeFace();
    }

    public int getAboluteIndex() {
        return absoluteIndex;
    }

    @Override
    public String toString() {
        return romanStrings[getAisle() * NUM_BLOCKS + getBlock()] + "-" + (char) ('A' + getColumn()) + getChest() + ":" + (getRow() + 1) + "," + (getRowSlot() + 1);
    }

    private int getAisle() {
        return (absoluteIndex / AISLE_SIZE) % NUM_AISLES;
    }

    private int getBlock() {
        return (absoluteIndex / BLOCK_SIZE) % NUM_BLOCKS;
    }

    private int getColumn() {
        if (getBlock() % 2 != 0) {
            return NUM_COLUMNS - 1 - ((absoluteIndex / COLUMN_SIZE) % NUM_COLUMNS);
        } else {
            return (absoluteIndex / COLUMN_SIZE) % NUM_COLUMNS;
        }
    }

    private int getChest() {
        return (absoluteIndex / CHEST_SIZE) % NUM_ROWS;
    }

    private int getRow() {
        return (absoluteIndex / 9) % (CHEST_SIZE / 9);
    }

    private int getRowSlot() {
        return absoluteIndex % 9;
    }

    public int getSlot() {
        return absoluteIndex % 54;
    }
}
