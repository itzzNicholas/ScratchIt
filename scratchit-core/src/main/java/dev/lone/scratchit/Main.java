package dev.lone.scratchit;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.lone.LoneLibs.nbt.nbtapi.utils.Metrics;
import dev.lone.LoneLibs.nbt.nbtapi.utils.MinecraftVersion;
import dev.lone.scratchit.compat.ItemsAdderCompat;
import dev.lone.scratchit.compat.protection.ProtectionPlugins;
import dev.lone.scratchit.commands.MainCommand;
import dev.lone.scratchit.config.Settings;
import dev.lone.scratchit.config.ResourceFolderExtractor;
import dev.lone.scratchit.gui.GUIsGlobalStuff;
import dev.lone.LoneLibs.config.ConfigFile;
import dev.lone.scratchit.config.LangFile;
import dev.lone.scratchit.listener.CardEventsListener;
import dev.lone.scratchit.util.Msg;
import dev.lone.scratchit.util.Scheduler;
import dev.lone.itemsadder.api.ItemsAdder;
import dev.lone.scratchit.nms.Packets;
import fr.mrmicky.fastinv.FastInvManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public final class Main extends JavaPlugin implements Listener
{
    //DO NOT SET AS "final" OR SPIGOT.MC won't replace it.
    public static String USER_ID = "%%__USER__%%";

    public static LangFile lang;
    public static Main inst;

    public static boolean hasItemsAdder;
    public static boolean hasPlaceholderAPI;
    public static boolean hasViaVersion;
    public static boolean isPapermc;
    public static boolean is_v17_or_greather;
    public static boolean is_v18_2_or_greather;

    private CardEventsListener cardEventsListener;

    public MapsRenderingContainer mapsRenderingContainer;
    @Nullable
    public CardsStorage cardsStorage = null;

    public static ConfigFile config;

    //TODO: shift all the card data into a category names scratch_card:, this allows the yml editor to autocomplete correctly based on the card type.
    // Ideas:
    // - minigame where the player has to click multiple times on a card and the texture changes on each click.
    // - minigame where you have to shoot at moving objects (balloons, etc).
    // - retro console emulator?
    // - web browser based games?? So that people can render web pages in the card and create detailed games.

    //TODO: command to check who is scratching and how many players are scratching

    //TODO: clean up all the legacy version-related tricks and use an actual NMS implementation instead.
    // TODO: get rid of all pre-1.19.4 stuff as now I want to support only these versions.

    @Override
    public void onLoad()
    {
        inst = this;
        ProtectionPlugins.register();
    }

    @Override
    public void onEnable()
    {
        if (MinecraftVersion.getVersion().getVersionId() < MinecraftVersion.MC1_9_R2.getVersionId())
        {
            getLogger().log(Level.SEVERE, "This plugin is not compatible with your Minecraft version: " + MinecraftVersion.getVersion());
            return;
        }

        Scheduler.async(() -> new Metrics(this, 11));

        is_v17_or_greather = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_17_R1);
        is_v18_2_or_greather = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_18_R2);

        Packets.init(this);

        MainCommand mainCommand = new MainCommand();
        mainCommand.register(this);

        licenseCheck();

        try
        {
            isPapermc = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
        }
        catch (ClassNotFoundException ignored) {}

        hasItemsAdder = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
        hasPlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        hasViaVersion = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
        if(hasViaVersion)
        {
            try
            {
                Class.forName("com.viaversion.viaversion.api.Via");
                hasViaVersion = true;
            }
            catch (ClassNotFoundException ignored)
            {
                hasViaVersion = false;
                getLogger().severe("Error: Please update ALL your ViaVersion plugins to the latest dev build: https://scratchit.devs.beer/first-install");
            }
        }

        if (hasItemsAdder)
            Msg.get().log( ChatColor.YELLOW + "Detected ItemsAdder");
        if (hasPlaceholderAPI)
            Msg.get().log(ChatColor.YELLOW + "Detected PlaceholderAPI");
        if (hasViaVersion)
            Msg.get().log(ChatColor.YELLOW + "Detected ViaVersion");

        initialize();
    }

    public void initialize()
    {
        config = new ConfigFile(this, Msg.get(), true, "config.yml", true, true, false);

        ResourceFolderExtractor extractor = new ResourceFolderExtractor(this, getFile().getAbsolutePath());
        extractor.extract();

        lang = new LangFile(config.getString("lang", "en"));

        if (hasItemsAdder)
        {
            if (ItemsAdder.areItemsLoaded())
            {
                getLogger().log(Level.INFO, ChatColor.GREEN + "ItemsAdder already finished loading, now loading the plugin...");
                load();
            }
            else
            {
                getLogger().log(Level.INFO, ChatColor.YELLOW + "Waiting ItemsAdder to register custom items...");
                new ItemsAdderCompat().register();
            }
        }
        else
        {
            load();
        }
    }

    @Override
    public void onDisable()
    {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);

        mapsRenderingContainer.players.values().forEach(entry -> entry.stop(false));
        mapsRenderingContainer.removeAll();
        cardEventsListener.unregister();

        FastInvManager.closeAll();
    }

    public void load()
    {
        GUIsGlobalStuff.init();

        Settings.reload();

        Scheduler.async(() -> {
            cardsStorage = new CardsStorage(this);
            mapsRenderingContainer = new MapsRenderingContainer();

            cardEventsListener = new CardEventsListener();
            cardEventsListener.register(this);

            cardsStorage.loadCards();

            Scheduler.sync(() -> {
                Bukkit.getPluginManager().registerEvents(this, this);
            });
        });
    }

    @NotNull
    @Override
    public File getFile()
    {
        return super.getFile();
    }

    @SuppressWarnings({"deprecation", "StringConcatenationInsideStringBufferAppend"})
    @Nullable
    public JsonObject downloadJson(String url, int timeout) throws IOException
    {
        URL u = new URL(url);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        c.setRequestProperty("Content-length", "0");
        c.setUseCaches(false);
        c.setAllowUserInteraction(false);
        c.setConnectTimeout(timeout);
        c.setReadTimeout(timeout);
        c.connect();
        int status = c.getResponseCode();

        switch (status)
        {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                br.close();
                c.disconnect();
                return new Gson().fromJson(sb.toString(), JsonObject.class);
        }

        c.disconnect();
        throw new IOException("Error connecting: " + status);
    }

    private void licenseCheck()
    {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            String licensedUsername = null;
            try
            {
                JsonObject json = downloadJson("https://api.spigotmc.org/simple/0.2/index.php?action=getAuthor&id=" + USER_ID, 15000);
                if (json == null)
                    throw new IOException("Error connecting");
                licensedUsername = json.get("username").getAsString();

            }
            catch (Exception e)
            {
                Msg.get().log(ChatColor.YELLOW + "You can ignore this message: Can't connect to SpigotMC.org, probably internet connection is blocked.\n" + ChatColor.GRAY + "( " + e.getMessage() + " ) - " + USER_ID);
            }

            if (licensedUsername != null)
                Msg.get().log(ChatColor.GREEN + "[License] Product licensed to: " + ChatColor.AQUA + licensedUsername + " (" + USER_ID + ")");
            else
                Msg.get().log(ChatColor.RED + "[License] Unknown user ID: " + ChatColor.AQUA + USER_ID);
        });
    }
}
