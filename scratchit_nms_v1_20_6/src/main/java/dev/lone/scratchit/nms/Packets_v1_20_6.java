package dev.lone.scratchit.nms;

import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

@SuppressWarnings("unused")
public class Packets_v1_20_6 extends Packets
{
    @Override
    public void sendMapPacket(Player player, int mapId, byte[] data)
    {
        var packet = new ClientboundMapItemDataPacket(
                new MapId(mapId),
                (byte) 0,
                false,
                Optional.empty(),
                Optional.of(new MapItemSavedData.MapPatch(0, 0, 128, 128, data))
        );
        var serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(packet);
    }

    @Override
    public void sendFakePotion(Player player, PotionEffectType type, byte amplifier, int duration, boolean ambient, boolean visible)
    {
        var packet = new ClientboundUpdateMobEffectPacket(player.getEntityId(), new MobEffectInstance(
                Holder.direct(CraftPotionEffectType.bukkitToMinecraft(type)), duration, amplifier, ambient, visible
        ), false);
        var serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(packet);
    }

    @Override
    public void removeFakePotion(Player player, PotionEffectType type)
    {
        var packet = new ClientboundRemoveMobEffectPacket(
                player.getEntityId(),
                Holder.direct(CraftPotionEffectType.bukkitToMinecraft(type))
        );
        var serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(packet);
    }

    @Override
    public void sendDestroyEntityPacket(Player player, int entityId)
    {
        var packet = new ClientboundRemoveEntitiesPacket(entityId);
        var serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(packet);
    }

    @Override
    public void sendSpawnInvisibleArmorStand(Player player, int id, Location location)
    {
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        // Purely to get a new safe UUID. Might be worth doing it in another
        // way which doesn't involve instantiating a useless entity.
        Interaction dummy = EntityType.INTERACTION.create(level);
        if(dummy == null)
            throw new RuntimeException("Failed to create an fake entity.");

        var serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(new ClientboundAddEntityPacket(
                id,
                dummy.getUUID(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getPitch(),
                location.getYaw(),
                EntityType.INTERACTION,
                0,
                Vec3.ZERO,
                location.getYaw()
        ));
    }

    @Override
    public void sendTeleport(Player player, int id, Location location)
    {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(id);
        buf.writeDouble(location.getX());
        buf.writeDouble(location.getY());
        buf.writeDouble(location.getZ());
        buf.writeByte(getCompressedAngle(location.getYaw()));
        buf.writeByte(getCompressedAngle(location.getPitch()));
        buf.writeBoolean(false);

        var packet = makeClientboundTeleportEntityPacket(buf);
        var serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(packet);
    }

    @Override
    public int getCurrentWindowId(Player player)
    {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        return nmsPlayer.containerMenu.containerId;
    }

    @Override
    public void sendSetSlotPacket(Player player, int windowId, int slot, ItemStack bukkitItemStack)
    {
        ClientboundContainerSetSlotPacket packet = new ClientboundContainerSetSlotPacket(
                windowId,
                0,
                slot,
                CraftItemStack.asNMSCopy(bukkitItemStack)
        );
        var serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(packet);
    }

    public static byte getCompressedAngle(float value)
    {
        return (byte) (value * 256.0F / 360.0F);
    }

    static MethodHandle constr_ClientboundTeleportEntityPacket;
    static
    {
        constr_ClientboundTeleportEntityPacket = NMS.constructor(ClientboundTeleportEntityPacket.class, FriendlyByteBuf.class);
    }

    public static ClientboundTeleportEntityPacket makeClientboundTeleportEntityPacket(FriendlyByteBuf buff)
    {
        try
        {
            return (ClientboundTeleportEntityPacket) constr_ClientboundTeleportEntityPacket.invokeExact(buff);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
}
