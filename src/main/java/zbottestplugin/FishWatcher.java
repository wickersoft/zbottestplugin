/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zbottestplugin.oldshit.TaskFish;
import zedly.zbot.event.EventHandler;
import zedly.zbot.event.Listener;
import zedly.zbot.event.SoundEffectEvent;
import zedly.zbot.event.entity.EntityItemPickupEvent;

/**
 *
 * @author Dennis
 */
public class FishWatcher implements Listener {

    private final TaskFish task;

    public FishWatcher(TaskFish task) {
        this.task = task;
    }

    @EventHandler
    public void soundEffect(SoundEffectEvent evt) {
       if (evt.getSoundId() == 141) {
            synchronized (task) {
                task.notify();
            }
        }
    }

    @EventHandler
    public void pickup(EntityItemPickupEvent evt) {
        System.out.println("Item Pickup");
    }
}
