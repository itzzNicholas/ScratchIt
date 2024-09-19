package dev.lone.scratchit.card.actions;

import dev.lone.scratchit.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionExecuteCommand extends ActionDelay
{
    String command;
    boolean asConsole;

    public ActionExecuteCommand(String command, boolean asConsole, long delay)
    {
        super(delay);
        this.command = command;
        this.asConsole = asConsole;
    }

    @Override
    public void execute(Player player)
    {
        execute(() -> doExecute(player));
    }

    private void doExecute(Player player)
    {
        String cmd = command
                .replace("{player}", player.getName())
                .replace("{x}", String.valueOf(player.getLocation().getX()))
                .replace("{y}", String.valueOf(player.getLocation().getY()))
                .replace("{z}", String.valueOf(player.getLocation().getZ()));

        if(Main.hasPlaceholderAPI)
            cmd = PlaceholderAPI.setPlaceholders(player, cmd);

        if(asConsole)
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
        else
        {
            Bukkit.dispatchCommand(player, cmd);
        }
    }
}
