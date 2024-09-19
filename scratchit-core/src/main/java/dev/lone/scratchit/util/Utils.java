package dev.lone.scratchit.util;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import dev.lone.LoneLibs.config.ConfigFile;
import dev.lone.scratchit.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Utils
{
    public static float randomNumber(float f, float g)
    {
        Random random = new Random();
        return random.nextFloat() * (g - f) + f;
    }

    public static int getRandomInt(int min, int max)
    {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public static String convertColor(String name)
    {
        return name.replace("&", "\u00A7");
    }

    public static int ceilDivision(float a, float b)
    {
        return (int) Math.ceil(a / b);
    }

    public static int parseInt(String number, int defaultValue)
    {
        try
        {
            return Integer.parseInt(number);
        }
        catch (Exception ignored) {}
        return defaultValue;
    }

    public static void sendPacket(Player player, PacketContainer packetContainer, boolean printError)
    {
        try
        {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        }
        catch (Exception e)
        {
            if (printError)
                Msg.get().error("Error while sending packet to player " + player.getName(), e);
        }
    }

    public static String formatReadableTime(long millis)
    {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

        if (minutes > 0)
        {
            return String.format(
                    "%d min, %d sec",
                    minutes,
                    TimeUnit.MILLISECONDS.toSeconds(millis) - seconds
            );
        }
        return String.format(
                "%d sec",
                TimeUnit.MILLISECONDS.toSeconds(millis) - seconds
        );
    }

    public static List<MemorySection> getMemorySections(ConfigFile configFile, String path)
    {
        ConfigurationSection baseSection = configFile.getConfig().getConfigurationSection(path);
        if (baseSection == null)
            return null;
        return getMemorySections(baseSection);
    }

    public static List<MemorySection> getMemorySections(ConfigurationSection baseSection)
    {
        List<MemorySection> sections = new ArrayList<>();
        for (Object entry : baseSection.getValues(false).values())
        {
            if (entry instanceof MemorySection)
                sections.add((MemorySection) entry);
        }

        return sections;
    }

    public static ConfigFile config(File file)
    {
        return new ConfigFile(Main.inst, Msg.get(), false, file.getAbsolutePath(), false, false, false);
    }
}
