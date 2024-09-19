package dev.lone.scratchit.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import dev.lone.LoneLibs.nbt.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class SignMenuFactory implements Listener
{
    private static final int ACTION_INDEX = 9;
    private static final int SIGN_LINES = 4;

    private static final String NBT_FORMAT = "{\"text\":\"%s\"}";
    private static final String NBT_BLOCK_ID = "minecraft:sign";

    private final Plugin plugin;

    private final Map<Player, Menu> inputReceivers;
    private final Map<Player, BlockPosition> signLocations;

    final PacketAdapter listener;

    public SignMenuFactory(Plugin plugin)
    {
        this.plugin = plugin;
        this.inputReceivers = new HashMap<>();
        this.signLocations = new HashMap<>();

        this.listener = new PacketAdapter(this.plugin, PacketType.Play.Client.UPDATE_SIGN)
        {
            @Override
            public void onPacketReceiving(PacketEvent event)
            {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                String[] input = packet.getStringArrays().read(0);

                Menu menu = inputReceivers.remove(player);
                BlockPosition blockPosition = signLocations.remove(player);

                if (menu == null || blockPosition == null)
                {
                    return;
                }
                event.setCancelled(true);

                boolean success = menu.response.test(player, input);

                if (!success && menu.reopenIfFail)
                {
                    Bukkit.getScheduler().runTaskLater(plugin, menu::open, 2L);
                }
                player.sendBlockChange(blockPosition.toLocation(player.getWorld()), Material.AIR.createBlockData());
            }
        };
        this.listen();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public Menu newMenu(Player player, List<String> text)
    {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(text, "text");
        Menu menu = new Menu(player, text);
        menu.onOpen(blockPosition -> {
            this.signLocations.put(player, blockPosition);
            this.inputReceivers.putIfAbsent(player, menu);
        });
        return menu;
    }

    private void listen()
    {
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
    }

    private void unregister()
    {
        ProtocolLibrary.getProtocolManager().removePacketListener(listener);
    }

    @EventHandler
    void pluginDisable(PluginDisableEvent e)
    {
        if (!e.getPlugin().getName().equals(plugin.getName()))
            return;

        unregister();
    }

    public static class Menu
    {

        private final Player player;

        private final List<String> text;
        private BiPredicate<Player, String[]> response;

        private boolean reopenIfFail;

        private Consumer<BlockPosition> onOpen;

        Menu(Player player, List<String> text)
        {
            this.player = player;
            this.text = text;
        }

        void onOpen(Consumer<BlockPosition> onOpen)
        {
            this.onOpen = onOpen;
        }

        public Menu reopenIfFail()
        {
            this.reopenIfFail = true;
            return this;
        }

        public Menu response(BiPredicate<Player, String[]> response)
        {
            this.response = response;
            return this;
        }

        public void open()
        {
            Location location = this.player.getLocation();
            BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY() - 5, location.getBlockZ());

            player.sendBlockChange(blockPosition.toLocation(location.getWorld()), Material.BIRCH_WALL_SIGN.createBlockData());

            PacketContainer openSign = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
            PacketContainer signData = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TILE_ENTITY_DATA);

            openSign.getBlockPositionModifier().write(0, blockPosition);

            NbtCompound signNBT;
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_18_R1))
                signNBT = NbtFactory.ofCompound("");
            else
                signNBT = (NbtCompound) signData.getNbtModifier().read(0);

            IntStream.range(0, SIGN_LINES).forEach(line -> signNBT.put("Text" + (line + 1), text.size() > line ? String.format(NBT_FORMAT, color(text.get(line))) : "WW"));

            signNBT.put("x", blockPosition.getX());
            signNBT.put("y", blockPosition.getY());
            signNBT.put("z", blockPosition.getZ());
            signNBT.put("id", NBT_BLOCK_ID);

            signData.getBlockPositionModifier().write(0, blockPosition);
            if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_18_R1))
                signData.getIntegers().write(0, ACTION_INDEX);
            signData.getNbtModifier().write(0, signNBT);

            try
            {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, signData);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, openSign);
            }
            catch (Throwable exception)
            {
                exception.printStackTrace();
            }
            this.onOpen.accept(blockPosition);
        }

        private String color(String input)
        {
            return ChatColor.translateAlternateColorCodes('&', input);
        }
    }
}