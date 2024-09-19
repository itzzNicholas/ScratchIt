package dev.lone.scratchit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public abstract class AbstractScratchItEvent extends Event
{
    private final Player player;
    private final ItemStack scratchCard;
    private final String id;

    AbstractScratchItEvent(Player player, ItemStack scratchCard, String id)
    {
        this(player, scratchCard, id, false);
    }

    AbstractScratchItEvent(Player player, ItemStack scratchCard, String id, boolean async)
    {
        super(async);
        this.player = player;
        this.scratchCard = scratchCard;
        this.id = id;
    }

    public Player getPlayer()
    {
        return player;
    }

    public ItemStack getScratchCard()
    {
        return scratchCard;
    }

    public String getId()
    {
        return id;
    }
}
