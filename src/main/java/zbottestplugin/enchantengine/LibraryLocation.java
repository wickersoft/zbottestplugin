package zbottestplugin.enchantengine;

import zedly.zbot.Location;

public class LibraryLocation {

    private final int absoluteIndex;

    public LibraryLocation(int aisle, int aBlock, int row, int chest, int column, int rowSlot) {
        absoluteIndex
                = 7776 * aisle
                + 3888 * aBlock
                + 216 * column
                + 54 * chest
                + 9 * row
                + rowSlot;
    }

    public LibraryLocation(int absoluteIndex) {
        this.absoluteIndex = absoluteIndex;
    }

    public Location getLocation() {
        return new Location(getX(), getY(), getZ());
    }
    
    public Location getWalkLocation() {
        return new Location(getX(), 35, getWalkZ()).centerHorizontally();
    }

    public int getAisle() {
        return (absoluteIndex / 9 / 6 / 4 / 18 / 2) % 4;
    }

    public int getABlock() {
        return (absoluteIndex / 9 / 6 / 4 / 18) % 2;
    }

    public int getBlock() {
        return (absoluteIndex / 9 / 6 / 4 / 18) % 8;
    }

    public int getColumn() {
        return (absoluteIndex / 9 / 6 / 4) % 18;
    }

    public int getChest() {
        return (absoluteIndex / 9 / 6) % 4;
    }

    public int getRow() {
        return (absoluteIndex / 9) % 6;
    }

    public int getRowSlot() {
        return absoluteIndex % 9;
    }

    public int getSlot() {
        return absoluteIndex % 54;
    }

    public int getX() {
        return 2 * getColumn() - 884;
    }

    public int getY() {
        return getChest() + 35;
    }

    public int getZ() {
        return 4837 + 6 * getAisle() + 4 * getABlock();
    }

    public int getWalkZ() {
        return 4839 + 6 * getAisle();
    }

    public int getAboluteIndex() {
        return absoluteIndex;
    }
}
