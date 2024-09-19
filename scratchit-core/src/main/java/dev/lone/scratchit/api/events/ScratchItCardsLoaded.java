package dev.lone.scratchit.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ScratchItCardsLoaded extends Event
{
    private static final HandlerList HANDLERS = new HandlerList();

    public ScratchItCardsLoaded()
    {
        super(false);
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
