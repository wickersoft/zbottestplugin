/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zedly.zbot.Location;
import zedly.zbot.self.Self;

/**
 *
 * @author Dennis
 */
public class TaskPlay extends Thread {

    private final String filename;
    private final int delay;
    private final boolean useWings;

    public TaskPlay(String filename, int delay, boolean useWings) {
        this.filename = filename;
        this.delay = delay;
        this.useWings = useWings;
    }

    public void run() {
        String[] lines = FileInputHelper.read(filename);
        String[] firstLoc = lines[0].split(" ");
        if (firstLoc.length != 5) {
            Storage.self.sendChat("Invalid recording! (1)");
            return;
        }

        Location[] locations = new Location[lines.length];
        double[][] coordinates = new double[lines.length][5];
        try {
            Location loc = new Location(Double.parseDouble(firstLoc[0]), Double.parseDouble(firstLoc[1]), Double.parseDouble(firstLoc[2]),
                    Double.parseDouble(firstLoc[3]), Double.parseDouble(firstLoc[4]));
            if (Storage.self.getLocation().distanceTo(loc) > 5) {
                Storage.self.sendChat("I am not at this recording's starting position. Move me to " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "!");
            }
            for (int i = 0; i < lines.length - 1; i++) {
                String[] line = lines[i].split(" ");
                if (firstLoc.length != 5) {
                    Storage.self.sendChat("Invalid recording! (2)");
                    return;
                }
                locations[i] = new Location(Double.parseDouble(line[0]), Double.parseDouble(line[1]), Double.parseDouble(line[2]), Double.parseDouble(line[3]), Double.parseDouble(line[4]));
            }
        } catch (NumberFormatException ex) {
            Storage.self.sendChat("Invalid recording! (3)");
            return;
        }

        try {
            for (int i = 0; i < lines.length - 1; i++) {
                Storage.self.moveTo(locations[i]);
                sleep(delay);
            }
        } catch (InterruptedException ex) {
        }
    }
}
