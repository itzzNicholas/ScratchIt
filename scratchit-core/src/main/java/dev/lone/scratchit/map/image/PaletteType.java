package dev.lone.scratchit.map.image;

import dev.lone.LoneLibs.nbt.nbtapi.utils.MinecraftVersion;

public enum PaletteType
{
    MC1_8(new Palette_1_8_1()),
    MC1_12(new Palette_1_12()),
    MC1_16(new Palette_1_16());

    public final Palette palette;
    PaletteType(Palette palette)
    {
        this.palette = palette;
    }

    public static PaletteType getSameAsServer()
    {
        if(MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_16_R1.getVersionId())
            return MC1_16;
        if(MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_12_R1.getVersionId())
            return MC1_12;
        return MC1_8;
    }
}
