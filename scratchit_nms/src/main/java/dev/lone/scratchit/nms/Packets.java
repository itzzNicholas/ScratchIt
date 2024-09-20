package dev.lone.scratchit.nms;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

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

        instance = NMS.get(Packets.class, plugin.getLogger());
        return instance;
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
