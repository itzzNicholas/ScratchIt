package dev.lone.scratchit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractCancellableScratchItEvent extends AbstractScratchItEvent implements Cancellable
{
    private boolean cancelled;

    AbstractCancellableScratchItEvent(Player player, ItemStack scratchCard, String id)
    {
        super(player, scratchCard, id);
    }

    AbstractCancellableScratchItEvent(Player player, ItemStack scratchCard, String id, boolean async)
    {
        super(player, scratchCard, id, async);
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }
}
