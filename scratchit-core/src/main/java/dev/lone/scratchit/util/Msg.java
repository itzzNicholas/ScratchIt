package dev.lone.scratchit.util;

import dev.lone.scratchit.Main;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class Msg
{
    private static dev.lone.LoneLibs.chat.Msg msg = new dev.lone.LoneLibs.chat.Msg("[ScratchIt] ");

    public static dev.lone.LoneLibs.chat.Msg get()
    {
        return msg;
    }

    public static void setPrefix(String prefix)
    {
        msg.setPrefix(prefix);
    }

    public static void sendLocalizedMessage(CommandSender commandSender, String path)
    {
        msg.send(commandSender, Main.lang.getLocalized(path));
    }

    public static void sendLocalizedMessage(String path, Level level)
    {
        msg.log(Main.lang.getLocalized(path), level);
    }

}
