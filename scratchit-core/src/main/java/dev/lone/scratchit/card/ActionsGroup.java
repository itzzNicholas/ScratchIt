package dev.lone.scratchit.card;

import com.google.common.collect.ImmutableList;
import dev.lone.scratchit.Main;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ActionsGroup
{
    private final ImmutableList<IAction> prizes;
    public final boolean executeInstantly;

    public ActionsGroup(List<IAction> prizes, boolean executeInstantly)
    {
        this.prizes = ImmutableList.copyOf(prizes);
        this.executeInstantly = executeInstantly;
    }

    public void giveAllPrizes(Player player, @Nullable Runnable finishAllCallback)
    {
        for (IAction prize : prizes)
        {
            prize.execute(player);
        }
        if (finishAllCallback != null)
            finishAllCallback.run();
    }

    public void executeAllActionsSyncThread(Player player, @Nullable Runnable finishAllCallback)
    {
        Bukkit.getScheduler().runTask(Main.inst, () -> {
            giveAllPrizes(player, finishAllCallback);
        });
    }

    public void executeAllActionsSyncThread(Player player)
    {
        Bukkit.getScheduler().runTask(Main.inst, () -> {
            giveAllPrizes(player, null);
        });
    }
}
