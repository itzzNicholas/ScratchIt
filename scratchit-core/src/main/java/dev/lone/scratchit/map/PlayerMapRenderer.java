package dev.lone.scratchit.map;

import dev.lone.scratchit.map.image.ImgData;
import dev.lone.scratchit.map.image.PaletteType;
import dev.lone.scratchit.util.ByteMatrix2x2;
import dev.lone.scratchit.util.Scheduler;
import dev.lone.scratchit.nms.Packets;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Per player map renderer.
 */
@Accessors(fluent = true, chain = true)
public abstract class PlayerMapRenderer implements IMapRenderer
{
    static final int INV_HOTBAR_OFFSET_MAGIC_NUMBER = 36;
    static final int mapID = -1337;

    @Getter
    protected final Player player;
    @Getter
    private final PaletteType paletteType;
    protected Plugin plugin;
    @Getter
    private final ByteMatrix2x2 mapData = new ByteMatrix2x2(128, 128);

    protected long renderFrequencyTicks = 1L;
    protected long renderFrequencyMs = renderFrequencyTicks * 1000 / 20;

    public float startYav = 0;
    public float startPitch = 36;

    Timer timer;
    private BukkitTask loop;

    public ItemStack fakeItemStack;

    public PlayerMapRenderer(Player player)
    {
        this.player = player;

        this.paletteType = ImgData.calculateSupportedPaletteType(player);
    }

    @Override
    public abstract void tickSendMapPacket();

    public void start()
    {
        Location tmp = player.getLocation();
        tmp.setYaw(startYav);
        tmp.setPitch(startPitch);
        player.teleport(tmp);

        fakeItemStack = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) fakeItemStack.getItemMeta();
        meta.setDisplayName(" ");
        meta.setMapId(mapID);

        fakeItemStack.setItemMeta(meta);
        int amount = player.getItemInHand().getAmount();
        fakeItemStack.setAmount(amount == 0 ? 1 : amount);

        refreshFakeItemInHand(5);

        timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                tickSendMapPacket();
            }
        };
        timer.scheduleAtFixedRate(task, 0, renderFrequencyMs);
    }

    protected void sendMapDataToPlayer()
    {
        Packets.get().sendMapPacket(player, mapID, mapData.internal);
    }

    public void refreshFakeItemInHand(long delay)
    {
        if (delay > 0)
            Scheduler.sync(this::refreshFakeItemInHand, delay);
        else
            refreshFakeItemInHand();
    }

    public void refreshFakeItemInHand()
    {
        Packets.get().sendSetSlotPacket(player, 0, INV_HOTBAR_OFFSET_MAGIC_NUMBER + player.getInventory().getHeldItemSlot(), fakeItemStack);
    }

    public void stop(boolean useMainThread)
    {
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }

        if (loop != null)
        {
            loop.cancel();
            loop = null;
        }
    }

    public void stop()
    {
        stop(false);
    }

    protected void freezePlayer()
    {
        Scheduler.sync(() -> {
            player.setWalkSpeed(0);
            player.setFlySpeed(0);
        });
        sendFakeJumpBoost(player);
    }

    protected void unfreezePlayer()
    {
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        removeFakeJumpBoost(player);
    }

    private static void sendFakeJumpBoost(Player player)
    {
        Packets.get().sendFakePotion(player, PotionEffectType.JUMP_BOOST, (byte) 128, 5 * 60 * 20, false, false);
    }

    private static void removeFakeJumpBoost(Player player)
    {
        Packets.get().removeFakePotion(player, PotionEffectType.JUMP_BOOST);
    }
}
