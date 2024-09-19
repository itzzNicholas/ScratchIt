package dev.lone.scratchit.map.card;

import dev.lone.scratchit.map.PlayerMapRenderer;
import dev.lone.scratchit.util.Scheduler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Per player card renderer
 */
public class GenericRenderer extends PlayerMapRenderer
{
    ItemStack itemStack;

    int changedElements = 0;
    private boolean freezePlayer;

    public GenericRenderer(Plugin plugin, Player player)
    {
        super(player);
    }

    public void setItemInfo(String displayName, @Nullable List<String> lore)
    {
        itemStack = new ItemStack(Material.PAPER);
        itemStack.setAmount(1);
        ItemMeta meta = itemStack.getItemMeta();
        if (lore != null)
            meta.setLore(lore);
        meta.setDisplayName(displayName);
        itemStack.setItemMeta(meta);
    }

    public void setFreezePlayer(boolean freezePlayer)
    {
        this.freezePlayer = freezePlayer;
    }

    @Override
    public void start()
    {
        super.start();

        if (freezePlayer)
            freezePlayer();

        refreshFakeItemInHand(0);
    }

    @Override
    public void tickSendMapPacket()
    {
        changedElements = 0;

        //TODO: calculate if changed elements

        //if not changed do not send the packet again, to avoid too much network usage
//        if (changedElements > 0)
        sendMapDataToPlayer();
    }

    @Override
    public void stop(boolean useMainThread)
    {
        super.stop(useMainThread);
        player.updateInventory();

        if (freezePlayer)
        {
            if (useMainThread)
                Scheduler.sync(this::unfreezePlayer);
            else
                unfreezePlayer();
        }
    }

    public void stop()
    {
        stop(false);
    }
}
