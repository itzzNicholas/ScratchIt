package dev.lone.scratchit.config;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceFolderExtractor
{
    protected Plugin plugin;
    private final String pluginAbsolutePath;

    public ResourceFolderExtractor(Plugin plugin, String pluginAbsolutePath)
    {
        this.plugin = plugin;
        this.pluginAbsolutePath = pluginAbsolutePath;
    }

    public void extract()
    {
        plugin.getDataFolder().mkdirs();

        JarFile jarFile = null;
        try
        {
            jarFile = new JarFile(pluginAbsolutePath);
            Enumeration<JarEntry> en = jarFile.entries();
            while (en.hasMoreElements())
            {
                JarEntry ent = en.nextElement();
                //System.out.println(ent.getName());
                extract(jarFile, ent);
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                jarFile.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected void extract(JarFile jar, JarEntry jarEntry) throws IOException
    {
        if (jarEntry.isDirectory())
            return;
        if (jarEntry.getName().endsWith(".class") || jarEntry.getName().startsWith("template"))
            return;

        InputStream entryStream = jar.getInputStream(jarEntry);
        try
        {
            File tmp = new File(plugin.getDataFolder() + "/" + jarEntry.getName());
//            if(!jarEntry.getName().contains("resources"))
//                return;

            if (tmp.exists())
                return;

            if (!tmp.isDirectory())
            {
                tmp.getParentFile().mkdirs();
            }

            FileOutputStream output = new FileOutputStream(plugin.getDataFolder() + "/" + jarEntry.getName());
            try
            {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = entryStream.read(buffer)) != -1)
                    output.write(buffer, 0, bytesRead);

            }
            finally
            {
                output.close();
            }
        }
        finally
        {
            entryStream.close();
        }
    }
}
