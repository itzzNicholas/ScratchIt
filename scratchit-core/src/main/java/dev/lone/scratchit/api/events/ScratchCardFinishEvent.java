package dev.lone.scratchit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ScratchCardFinishEvent extends AbstractCancellableScratchItEvent
{
    private static final HandlerList HANDLERS = new HandlerList();

    private final Status status;

    public ScratchCardFinishEvent(Player player, ItemStack scratchCard, String id, boolean won)
    {
        super(player, scratchCard, id, true);
        this.status = won ? Status.WIN : Status.LOSE;
    }

    public Status getStatus()
    {
        return status;
    }

    @NotNull
    public HandlerList getHandlers()
    {
        return HANDLERS;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    public enum Status
    {
        WIN,
        LOSE
    }
}
