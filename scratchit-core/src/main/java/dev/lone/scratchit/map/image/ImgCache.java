package dev.lone.scratchit.map.image;

import com.google.common.io.BaseEncoding;
import dev.lone.LoneLibs.nbt.nbtapi.utils.MinecraftVersion;
import dev.lone.scratchit.Main;
import dev.lone.scratchit.util.ByteMatrix3x3;
import dev.lone.LoneLibs.config.ConfigFile;
import dev.lone.scratchit.util.Msg;
import dev.lone.scratchit.util.Utils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

public class ImgCache
{
    private final Plugin plugin;

    File cacheFolder;
    private HashMap<String, ImgData> cachedImages = new HashMap<>();

    public ImgCache(Plugin plugin, String folderName)
    {
        this.plugin = plugin;

        cacheFolder = new File(Main.inst.getDataFolder(), "cache" + File.separator + folderName);
        cacheFolder.mkdirs();

        if(!isBuggedSpigot())
            loadCache();
    }

    public static String hashFileName(String name)
    {
        return BaseEncoding.base16().lowerCase().encode(DigestUtils.sha1(name));
    }

    public boolean has(String name)
    {
        return cachedImages.containsKey(name);
    }

    @Nullable
    public ImgData get(String name)
    {
        return cachedImages.get(name);
    }

    public void save(String name, ImgData imgData)
    {
        if (imgData.isNull())
            return;
        cachedImages.put(name, imgData);

        if(!isBuggedSpigot())
        {
            String cacheFileName = hashFileName(name);

            ConfigFile cacheFile = Utils.config(new File(cacheFolder.getAbsolutePath(), cacheFileName + ".yml"));
            cacheFile.set("file_path", imgData.filePath);
            cacheFile.set("last_edit", new File(imgData.filePath).lastModified());
            cacheFile.set("width", imgData.getWidth());
            cacheFile.set("height", imgData.getHeight());
            cacheFile.set("1_8", imgData.texture18.internal);
            cacheFile.set("1_12", imgData.texture112.internal);
            cacheFile.set("1_16", imgData.texture116.internal);
            cacheFile.save();
            //System.out.println("Added image to cache: " + new File(name).getName() + " -> " + cacheFile.getFile().getName());
        }
    }

    private void checkForFileChanges(ConfigFile cacheFile)
    {
        String pngPath = cacheFile.getString("file_path");
        if (pngPath == null) //file doesn't exists
            return;

        //check if png exists, if not delete the cache file completely.
        File pngFile = new File(pngPath);
        if (!pngFile.exists())
        {
            File tmp = cacheFile.getFile();
            tmp.delete();
            return;
        }

        long pngLastEdit = cacheFile.getConfig().getLong("last_edit");
        if (pngFile.lastModified() != pngLastEdit) // file got modified
        {
//            System.out.println(ChatColor.RED + "detected changes for " + pngPath);
            cachedImages.remove(pngPath);
        }
        else // no changes
        {
            int width = cacheFile.getInt("width");
            int height = cacheFile.getInt("height");


            ImgData imgData = new ImgData(
                    pngPath,
                    new ByteMatrix3x3((byte[]) cacheFile.getConfig().get("1_8"), width, height, 2),
                    new ByteMatrix3x3((byte[]) cacheFile.getConfig().get("1_12"), width, height, 2),
                    new ByteMatrix3x3((byte[]) cacheFile.getConfig().get("1_16"), width, height, 2)
            );
//            System.out.println(ChatColor.GREEN + "no changes for " + pngPath);
            cachedImages.put(pngPath, imgData);
        }
    }

    public void checkForFileChanges(String pngFileName)
    {
        if(isBuggedSpigot())
            return;

        String cacheFileName = hashFileName(pngFileName);

        ConfigFile cacheFile = Utils.config(new File(cacheFolder.getAbsolutePath(), cacheFileName + ".yml"));
        checkForFileChanges(cacheFile);
    }

    public void loadCache()
    {
        FileUtils.listFiles(cacheFolder, new String[]{"yml"}, false).forEach(file -> {
            if (file.isDirectory())
                return;

            ConfigFile cacheFile = Utils.config(file);
            checkForFileChanges(cacheFile);
        });
    }

    private static boolean isBuggedSpigot()
    {
        // 1.12.2 snakeyaml has a bug which makes YML loading extremely slow... I cannot use cache then.
        return (MinecraftVersion.getVersion() == MinecraftVersion.MC1_12_R1);
    }
}
