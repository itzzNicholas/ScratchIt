package dev.lone.scratchit.card.actions;

import dev.lone.scratchit.Main;
import dev.lone.scratchit.card.SoundData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;

public class ActionPlaySoundLoop extends ActionDelay
{
    private SoundData soundData;
    private long interval;
    private int times;

    public ActionPlaySoundLoop(String sound, float volume, float pitch, long delay, long interval, int times)
    {
        super(delay);
        this.interval = interval;
        this.times = times;
        this.soundData = new SoundData(sound, volume, pitch);
    }

    @Override
    public void execute(Player player)
    {
//        AtomicInteger count = new AtomicInteger();
        //Spigot < 1.16 runnables are bugges as shit and this won't work. It will stop at the first iteration
//        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.inst, (task) -> {
//            if(count.get() > times)
//            {
//                task.cancel();
//                return;
//            }
//            soundData.play(player);
//            count.getAndIncrement();
//        }, delay, interval);

        AtomicInteger count = new AtomicInteger();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (count.get() > times)
                {
                    cancel();
                    return;
                }
                soundData.play(player);
                count.getAndIncrement();
            }
        }.runTaskTimerAsynchronously(Main.inst, delay, interval);

    }
}
