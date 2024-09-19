package dev.lone.scratchit.config;

import dev.lone.LoneLibs.config.ConfigFile;
import dev.lone.scratchit.Main;
import dev.lone.scratchit.util.Msg;

public class Settings
{
    private static Settings instance;

    public final long COOLDOWN_MS_GLOBAL;
    public final boolean CANCEL_METHOD_F_GLOBAL;
    public final boolean CANCEL_METHOD_DROP_GLOBAL;
    public final boolean CANCEL_METHOD_SHIFT_GLOBAL;

    public final boolean FIX_SEMITRANSPARENT_PIXELS;

    public static Settings inst()
    {
        if (instance == null)
            instance = new Settings();
        return instance;
    }

    public static void reload()
    {
        instance = new Settings();
    }

    public Settings()
    {
        ConfigFile config = Main.config;

        Msg.setPrefix(config.getString("prefix", "[ScratchIt] "));

        COOLDOWN_MS_GLOBAL = config.getInt("cards.usage_cooldown_ticks", 0) * 50L;
        CANCEL_METHOD_F_GLOBAL = config.getBoolean("cards.cancel_scratching_methods.item_swap_F", true);
        CANCEL_METHOD_DROP_GLOBAL = config.getBoolean("cards.cancel_scratching_methods.drop", true);
        CANCEL_METHOD_SHIFT_GLOBAL = config.getBoolean("cards.cancel_scratching_methods.shift", true);

        FIX_SEMITRANSPARENT_PIXELS = config.getBoolean("graphics.fix_semitransparent_pixels", true);

        //TODO: Move all settings to this class.
    }
}
