package dev.lone.scratchit.config;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TemplateCardExtractor extends ResourceFolderExtractor
{
    private final String cardName;

    public TemplateCardExtractor(Plugin plugin, String pluginAbsolutePath, String cardName)
    {
        super(plugin, pluginAbsolutePath);
        this.cardName = cardName;
    }

    @Override
    protected void extract(JarFile jar, JarEntry jarEntry) throws IOException
    {
        if (jarEntry.isDirectory())
            return;
        if (!jarEntry.getName().startsWith("template/card"))
            return;

        try (InputStream entryStream = jar.getInputStream(jarEntry))
        {
            String name = jarEntry.getName();
            if (name.endsWith("settings.yml"))
                name = name.replace("settings.yml", cardName + ".yml");

            name = name.replace("template/card", "cards/" + cardName);
            name.replace("/", File.separator);

            File tmp = new File(plugin.getDataFolder() + File.separator + name);
            if (tmp.exists())
                return;

            if (!tmp.isDirectory())
                tmp.getParentFile().mkdirs();

            try (FileOutputStream output = new FileOutputStream(plugin.getDataFolder() + File.separator + name))
            {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = entryStream.read(buffer)) != -1)
                    output.write(buffer, 0, bytesRead);

            }
        }
    }
}
