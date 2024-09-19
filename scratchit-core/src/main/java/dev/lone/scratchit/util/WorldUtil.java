package dev.lone.scratchit.util;

import org.bukkit.block.Biome;

@Deprecated
public class WorldUtil
{
    // TODO: support custom biomes.
    @Deprecated
    public static boolean isVanillaBiome(String str)
    {
        try
        {
            Biome.valueOf(str);
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }
}
