package dev.lone.scratchit.map.image;

import com.viaversion.viaversion.api.Via;
import dev.lone.scratchit.Main;
import dev.lone.scratchit.annotations.HeavyMethod;
import dev.lone.scratchit.util.ByteMatrix3x3;
import org.bukkit.entity.Player;

public class ImgData
{
    public final String filePath;
    /** x, y, [0=color, 1=alpha from -128 to 128] */
    public final ByteMatrix3x3 texture18;
    public final ByteMatrix3x3 texture112;
    public final ByteMatrix3x3 texture116;

    private int imageHashCode = -1;

    public ImgData(String filePath, ByteMatrix3x3 texture18, ByteMatrix3x3 texture112, ByteMatrix3x3 texture116)
    {
        this.filePath = filePath;
        this.texture18 = texture18;
        this.texture112 = texture112;
        this.texture116 = texture116;
        getImageHashCode();
    }

    public int getHeight()
    {
        return texture18.height;
    }

    public int getWidth()
    {
        return texture18.width;
    }

    public boolean isTransparent(int x, int y)
    {
        return (texture18.get(x, y, 1) == -128);
    }

    public boolean isSemiTransparent(int x, int y)
    {
        return (texture18.get(x, y, 1) < 127);
    }

    public int getImageHashCode()
    {
        if (imageHashCode != -1)
            return imageHashCode;

        imageHashCode = 31 + (texture18 == null ? 0 : texture18.hashCode());
        return imageHashCode;
    }

    @HeavyMethod
    public static PaletteType calculateSupportedPaletteType(Player player)
    {
        int clientVersion;
        PaletteType paletteType;
        if (Main.hasViaVersion)
        {
            clientVersion = Via.getAPI().getPlayerVersion(player.getUniqueId());
            if (clientVersion >= 721)
                paletteType = PaletteType.MC1_16;
            else if (clientVersion >= 328)
                paletteType = PaletteType.MC1_12;
            else
                paletteType = PaletteType.MC1_8;
        }
        else
        {
            paletteType = PaletteType.getSameAsServer();
        }
        return paletteType;
    }

    @Deprecated
    @HeavyMethod
    public ByteMatrix3x3 byPalette(Player player)
    {
        return byPalette(calculateSupportedPaletteType(player));
    }

    public ByteMatrix3x3 byPalette(PaletteType paletteType)
    {
        switch (paletteType)
        {
            case MC1_12:
                return texture112;
            case MC1_16:
                return texture116;
        }
        return texture18; // 1.8 or fallback if unknown.
    }

    public boolean isNull()
    {
        return texture18 == null;
    }
}
