package dev.lone.scratchit.card.actions;

import dev.lone.scratchit.Main;
import dev.lone.scratchit.card.IAction;
import org.bukkit.Bukkit;

abstract public class ActionDelay implements IAction
{
    long delay;

    public ActionDelay(long delay)
    {
        this.delay = delay;
    }

    public void execute(Runnable task)
    {
        if(delay > 0)
            Bukkit.getScheduler().runTaskLater(Main.inst, task, delay);
        else
            task.run();
    }
}
