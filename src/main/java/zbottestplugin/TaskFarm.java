/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import edu.kit.informatik.AStar;
import edu.kit.informatik.Node;
import java.util.Iterator;
import java.util.List;
import zedly.zbot.Location;
import zedly.zbot.block.Material;
import zedly.zbot.util.Vector;

/**
 *
 * @author Dennis
 */
public class TaskFarm extends HierarchicalTask {

    private boolean hasArrived = false;
    private int state = 0;
    private int id = 0;

    private int x = -907, y = 87, z = 4832;

    private Runnable[] instructions = {
        () -> {
            state = 1;
        },
        () -> {
            //Storage.self.derp();
        }
    };

    public TaskFarm() {
    }

    public void tick() {
        switch (state) {
            case 0:
                while (x <= -895) {
                    increment();
                    if (x > -895) {
                        state = 2;
                        return;
                    } else if (Storage.self.getEnvironment().getBlockAt(x, y, z).getTypeId() == 59
                            && Storage.self.getEnvironment().getBlockAt(x, y, z).getData() == 7) {
                        state = 1;
                        break;
                    }
                }
                goSub(new BlockingMover(AStar.getPath(new Location(x, y, z).centerHorizontally()).getNodes()));
                break;
            case 1:
                goSub(new TaskReplant(x, y, z));
                state = 0;
                break;
            case 2:
                InventoryUtil.findAndSelect(Material.WHEAT);
                goSub(new BlockingMover(AStar.getPath(new Location(-896, 82, 4833).centerHorizontally()).getNodes()));
                state++;
                break;
            case 3:
            case 4:
                Storage.self.placeBlock(-895, 84, 4833);
                state++;
                break;
            case 5:
            case 6:
                Storage.self.placeBlock(-895, 84, 4834);
                state++;
                break;
            case 7:
                Storage.self.sneak(true);
                state++;
                break;
            case 8:
                Storage.self.clickBlock(-895, 84, 4833);
                Storage.self.clickBlock(-895, 84, 4833);
                Storage.self.clickBlock(-895, 84, 4833);
                Storage.self.clickBlock(-895, 84, 4833);
                Storage.self.clickBlock(-895, 84, 4833);
                state++;
                break;
            case 9:
                Storage.self.sneak(false);
                Storage.self.cancelTask(id);
                Storage.self.sendChat("/msg brainiac94 done farming");
                finish();
        }
    }

    private final void increment() {
        if (x % 2 == 0) {
            if (z == 4832) {
                x++;
            } else {
                z--;
            }
        } else if ((x == -907 && z == 4839)
                || (x == -905 && z == 4838)
                || (z == 4843)) {
            x++;
        } else {
            z++;
        }
    }

    public void identify(int id) {
        this.id = id;
    }

    private enum State {
        WALKING_TO_WHEAT, REPLANTING, WALKING_TO_BENCH, WAITING_FOR_BENCH

    }

}
