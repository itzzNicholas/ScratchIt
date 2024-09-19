package dev.lone.scratchit.loots;

import dev.lone.scratchit.loots.data.CustomBlockLootEntry;
import dev.lone.scratchit.Main;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class IALootsEvents implements Listener
{
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void customBlockBreak(CustomBlockBreakEvent e)
    {
        if(e.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        Block block = e.getBlock();
        if(!Main.inst.cardsStorage.lootsManager.customBlockLootEntries.containsKey(e.getNamespacedID()))
            return;

        for (CustomBlockLootEntry entry : Main.inst.cardsStorage.lootsManager.customBlockLootEntries.get(e.getNamespacedID()))
        {
            if(!entry.canToolGetUnbrokenBlock(e.getPlayer().getItemInHand(), block.getType()))
            {
                entry.dropResultItemsWithChance(e.getPlayer().getItemInHand(), block.getLocation());
                entry.dropResultExpWithChance(block.getLocation());
            }
        }
    }
}
