package dev.lone.scratchit.api;

import dev.lone.scratchit.Main;
import dev.lone.scratchit.map.card.GenericRenderer;
import dev.lone.scratchit.map.image.ImageBytes;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

/**
 * This is an "advanced" class if you want to show something custom to your players, for example an animation
 * or an image / logo / menu.
 * Keep in mind that you have to handle all the stuff manually and this is limited compared to Spigot API.
 * The only advantage of using this is that it's not laggy like Spigot API as this is only packet based.
 * <p>
 * Use this class only if you really need to.
 */
@SuppressWarnings("unused")
public class CustomCardRenderer
{
    GenericRenderer internal;

    /**
     * Create a new renderer for the player
     */
    public CustomCardRenderer(Player player, boolean freezePlayer)
    {
        this.internal = new GenericRenderer(Main.inst, player);
        this.internal.setFreezePlayer(freezePlayer);
        Main.inst.mapsRenderingContainer.addPlayer(player, this.internal);

        this.internal.setItemInfo(" ", null);
    }

    /**
     * Set the item in hand info (optional)
     */
    public void setItemInfo(String displayName, @Nullable List<String> lore)
    {
        this.internal.setItemInfo(displayName, lore);
    }

    /**
     * Sets a pixel on the map (doesn't support transparency for now)
     */
    public void setPixel(int x, int y, Color color)
    {
        internal.mapData().set(x, y, internal.paletteType().palette.matchColor(color));
    }

    /**
     * Sets a pixel on the map (doesn't support transparency for now)
     */
    public void setPixel(int x, int y, byte rgb)
    {
        internal.mapData().set(x, y, rgb);
    }

    /**
     * Sets a pixel on the map (doesn't support transparency for now)
     */
    public byte setPixel(int x, int y, int rgb)
    {
        byte b = ImageBytes.intColorToByte(internal.paletteType(), rgb);
        internal.mapData().set(x, y, b);
        return b;
    }

    /**
     * Sets a pixel on the map (doesn't support transparency for now)
     */
    public void setPixels(int[] rgb)
    {
        for (int x = 0; x < 128; x++)
        {
            for (int y = 0; y < 128; y++)
            {
                internal.mapData().internal[x + y * 128] = ImageBytes.intColorToByte(internal.paletteType(), rgb[x + y * 128]);
            }
        }
    }

    /**
     * Start the rendering of this card to the player.
     */
    public void start()
    {
        internal.start();
    }

    /**
     * Stop the rendering of this card and remove listeners.
     */
    public void stop()
    {
        stop(false);
    }

    /**
     * Stop the rendering of this card and remove listeners.
     *
     * @param mainThead if you want to make it run on the main Bukkit thread if you're calling this from another.
     */
    public void stop(boolean mainThead)
    {
        internal.stop(mainThead);
        Main.inst.mapsRenderingContainer.removePlayer(internal.player());
    }
}
