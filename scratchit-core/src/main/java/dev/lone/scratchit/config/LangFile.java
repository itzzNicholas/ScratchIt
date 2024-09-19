package dev.lone.scratchit.config;

import dev.lone.LoneLibs.config.ConfigFile;
import dev.lone.scratchit.Main;
import dev.lone.scratchit.util.Msg;

public class LangFile extends ConfigFile
{
    ConfigFile fallbackLang;

    public LangFile(String lang)
    {
        super(Main.inst, Msg.get(), true, "lang/" + lang + ".yml", true, true);
        initFallback(lang);
    }

    private void initFallback(String lang)
    {
        if (!lang.equals("en"))
            fallbackLang = new ConfigFile(Main.inst, Msg.get(), true, "lang/en.yml", true, true, false);
    }

    /**
     * Get localized and colored text.
     * Returns english version if path doesn't exist in current file
     */
    public String getLocalized(String path)
    {
        if (hasKey(path))
            return getColored(path);

        if (fallbackLang != null)
        {
            if (fallbackLang.hasKey(path))
                return fallbackLang.getColored(path);
        }

        notifyMissingProperty(path);
        return null;
    }
}