/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.UUID;
import zbottestplugin.task.Task;
import zedly.zbot.Location;
import zedly.zbot.entity.Entity;
import zedly.zbot.entity.Player;

/**
 *
 * @author Dennis
 */
public class TaskTravelCompanion extends Task {

    private final UUID leaderUuid;
    private final String leaderName;
    private int noPathCounter = 0;
    private int messageCooldown = 0;

    public TaskTravelCompanion(String leaderName, UUID leaderUuid) {
        super(100);
        this.leaderUuid = leaderUuid;
        this.leaderName = leaderName;
    }

    @Override
    public void work() throws Exception {
        while (true) {
            if (Storage.self.getHealth() <= 17) {
                InventoryUtil.findAndSelect((is) -> {
                    switch (is.getType()) {
                        case COOKED_CHICKEN:
                        case COOKED_PORKCHOP:
                        case COOKED_BEEF:
                        case COOKED_MUTTON:
                        case BREAD:
                            return true;
                        default:
                            return false;
                    }
                });
                ai.eat();
                informLeader(1, "Om nom nom");
            } else {
                ai.tick(10);
            }
            
            Player p = getLeaderEntity();
            if (p == null) {
                informLeader(10, " I lost you! Come get me at " + Storage.self.getLocation().centerHorizontally());
                continue;
            }

            Location myLoc = Storage.self.getLocation();
            Location leaderLoc = p.getLocation();

            if (leaderLoc.distanceTo(myLoc) < 5) {
                continue;
            }

            boolean pathFound = ai.navigateTo2(leaderLoc);
            if (pathFound) {
                noPathCounter = 0;
            } else {
                if (leaderLoc.distanceTo(Storage.self.getLocation()) > 50 || noPathCounter >= 5) {
                    informLeader(4, " I'm losing you! Come get me at " + Storage.self.getLocation().centerHorizontally());
                }
                noPathCounter++;
            }
        }
    }

    private Player getLeaderEntity() {
        for (Entity e : Storage.self.getEnvironment().getEntities()) {
            if (e instanceof Player && ((Player) e).getUUID().equals(leaderUuid)) {
                return (Player) e;
            }
        }
        return null;
    }

    private void informLeader(int interval, String message) {
        if (++messageCooldown % interval == 0) {
            Storage.self.sendChat("/msg " + leaderName + message);
        }
    }
}
