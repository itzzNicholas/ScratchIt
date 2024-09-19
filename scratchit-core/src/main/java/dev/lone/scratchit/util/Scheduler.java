package dev.lone.scratchit.util;

import dev.lone.scratchit.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class Scheduler
{
    public static BukkitTask asyncLoop(Runnable task, long startDelay, long delayTicks)
    {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(Main.inst, task, startDelay, delayTicks);
    }

    public static BukkitTask async(Runnable task)
    {
        return Bukkit.getScheduler().runTaskAsynchronously(Main.inst, task);
    }

    public static BukkitTask async(Runnable task, long delayTicks)
    {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(Main.inst, task, delayTicks);
    }

    public static BukkitTask sync(Runnable task)
    {
        return Bukkit.getScheduler().runTask(Main.inst, task);
    }

    public static BukkitTask sync(Runnable task, long delayTicks)
    {
        return Bukkit.getScheduler().runTaskLater(Main.inst, task, delayTicks);
    }

    public static void asyncThread(Runnable task)
    {
        new Thread(task).start();
    }
}
