package dev.lone.scratchit.nms;

import lonelibs.dev.lone.fastnbt.nms.Version;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

public abstract class Packets
{
    private static Plugin plugin;
    private static Packets instance;

    public static void init(Plugin plugin0)
    {
        plugin = plugin0;
    }

    @NotNull
    public static Packets get()
    {
        if (instance != null)
            return instance;

        // This is the only way to avoid cyclic dependency in Maven.
        try
        {
            Class<?> clazz = findClass("dev.lone.scratchit.nms.Packets_" + Version.get().name());
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            instance = (Packets) constructor.newInstance();
        }
        catch (Exception e)
        {
            plugin.getLogger().log(Level.SEVERE, "Please check if the version of the plugin is compatible with the server version.");
            throw new RuntimeException(e);
        }

        return instance;
    }

    private static Class<?> findClass(String name) throws ClassNotFoundException
    {
        return Class.forName(name);
    }

    public abstract void sendMapPacket(Player player, int mapId, byte[] data);

    public abstract void sendFakePotion(Player player, PotionEffectType type, byte amplifier, int duration, boolean ambient, boolean visible);

    public abstract void removeFakePotion(Player player, PotionEffectType type);

    public abstract void sendDestroyEntityPacket(Player player, int entityId);

    public abstract void sendSpawnInvisibleArmorStand(Player player, int id, Location location);

    public abstract void sendTeleport(Player player, int id, Location location);

    public abstract int getCurrentWindowId(Player player);

    public abstract void sendSetSlotPacket(Player player, int windowId, int slot, ItemStack bukkitItemStack);
}
