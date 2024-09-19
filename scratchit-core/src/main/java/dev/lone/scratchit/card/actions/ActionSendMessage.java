package dev.lone.scratchit.card.actions;

import dev.lone.scratchit.Main;
import dev.lone.scratchit.util.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionSendMessage extends ActionDelay
{
    String message;
    BroadcastType broadcastType;

    public ActionSendMessage(String message, String broadcastTypeStr, long delay)
    {
        super(delay);
        this.message = Utils.convertColor(message);
        this.broadcastType = BroadcastType.valueOf(broadcastTypeStr);
    }

    @Override
    public void execute(Player player)
    {
        execute(() -> doExecute(player));
    }

    private void doExecute(Player player)
    {
        String msg = message
                .replace("{player}", player.getName());

        if (Main.hasPlaceholderAPI)
            msg = PlaceholderAPI.setPlaceholders(player, msg);

        if (broadcastType == BroadcastType.PLAYER)
        {
            player.sendMessage(msg);
        }
        else if (broadcastType == BroadcastType.WORLD)
        {
            for (Player p : player.getWorld().getPlayers())
            {
                p.sendMessage(msg);
            }
        }
        else if (broadcastType == BroadcastType.SERVER)
        {
            for (Player p : Bukkit.getServer().getOnlinePlayers())
            {
                p.sendMessage(msg);
            }
        }
    }

    enum BroadcastType
    {
        WORLD,
        SERVER,
        PLAYER;
    }
}
