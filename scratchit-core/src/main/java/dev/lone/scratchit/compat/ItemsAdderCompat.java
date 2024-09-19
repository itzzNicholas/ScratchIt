package dev.lone.scratchit.compat;

import dev.lone.LoneLibs.Events;
import dev.lone.scratchit.Main;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.logging.Level;

public class ItemsAdderCompat implements Listener
{
    public void register()
    {
        Events.register(Main.inst, this);
    }

    @EventHandler
    private void onItemsAdderLoadData(ItemsAdderLoadDataEvent e)
    {
        Main.inst.getLogger().log(Level.INFO, ChatColor.GREEN + "ItemsAdder finished loading, now loading scratch cards...");
        Main.inst.load();
    }
}
