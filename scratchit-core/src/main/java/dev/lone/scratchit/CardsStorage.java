package dev.lone.scratchit;

import com.google.common.io.BaseEncoding;
import dev.lone.LoneLibs.config.ConfigFile;
import dev.lone.LoneLibs.Mat;
import dev.lone.LoneLibs.nbt.nbtapi.utils.MinecraftVersion;
import dev.lone.scratchit.libs.WeightedRandom;
import dev.lone.scratchit.loots.LootsManager;
import dev.lone.scratchit.api.events.ScratchItCardsLoaded;
import dev.lone.scratchit.card.ActionsGroup;
import dev.lone.scratchit.card.CardData;
import dev.lone.scratchit.card.IAction;
import dev.lone.scratchit.card.SoundDataInterval;
import dev.lone.scratchit.card.actions.*;
import dev.lone.scratchit.config.TemplateCardExtractor;
import dev.lone.scratchit.map.AnimationFrames;
import dev.lone.scratchit.map.image.ImgCache;
import dev.lone.scratchit.util.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class CardsStorage
{
    Plugin plugin;

    public final File CARDS_DIR;
    public List<ConfigFile> cardsFiles = new ArrayList<>();
    public HashMap<String, CardData> cardsById = new HashMap<>();
    public List<CardData> cards = new ArrayList<>();
    public List<ItemStack> cardsItemsCache = new ArrayList<>();

    public LootsManager lootsManager;

    private boolean isLoading;

    private ImgCache cardsImgCache;

    public CardsStorage(Plugin plugin)
    {
        this.plugin = plugin;
        CARDS_DIR = new File(Main.inst.getDataFolder() + File.separator + "cards");
        cardsImgCache = new ImgCache(plugin, "images");
    }

    public void loadCards()
    {
        isLoading = true;
        Msg.sendLocalizedMessage("loading_items", Level.INFO);

        clear();
        Collection<File> files = getCardsFiles();
        for (File config : files)
        {
//           //System.out.println("Found card file " + config.getName());
            loadCard(config, false);
        }

        reloadAllLoots();
        Msg.sendLocalizedMessage("loaded_items", Level.INFO);
        isLoading = false;

        ScratchItCardsLoaded event = new ScratchItCardsLoaded();
        Scheduler.sync(() -> {
            Bukkit.getPluginManager().callEvent(event);
        });
    }

    public void clear()
    {
        cards.clear();
        cardsById.clear();
        cardsFiles.clear();
    }

    public boolean isLoading()
    {
        return isLoading;
    }

    private void reloadAllLoots()
    {
        lootsManager = new LootsManager(plugin);
        lootsManager.loadLootsFromConfigs(cardsFiles);
    }

    public void reloadCard(ConfigFile cardConfig)
    {
        cardConfig.reload();
        loadCard(cardConfig, true);
        reloadAllLoots();
    }


    private void reloadCard(CardData cardData)
    {
        reloadCard(cardData.getConfigFile());
    }

    public void loadCard(File file, boolean isReload)
    {
        ConfigFile cardConfig = Utils.config(file);
        loadCard(cardConfig, isReload);
    }

    public void loadCard(ConfigFile cardConfig, boolean isReload)
    {
        List<Vector> iconsCoords = new ArrayList<>();

        //TODO: check if is scratch_card type (if configuration section exists)
        String baseKey = "scratch_card";

        for (String entryKey : cardConfig.getSectionKeys(baseKey + ".icons"))
        {
            Vector vector = new Vector(
                    cardConfig.getInt(baseKey + ".icons." + entryKey + ".x", 0),
                    cardConfig.getInt(baseKey + ".icons." + entryKey + ".y", 0),
                    0
            );
            iconsCoords.add(vector);
        }

        AnimationFrames winAnimation = loadAnimationFrames(cardConfig, baseKey + ".win_anim", "win_anim", isReload);
        AnimationFrames loseAnimation = loadAnimationFrames(cardConfig, baseKey + ".lose_anim", "lose_anim", isReload);

        CardData cardData = new CardData(
                cardsImgCache,
                cardConfig,
                cardConfig.getFile().getParentFile().getAbsolutePath(),
                cardConfig.getString(baseKey + ".id"),
                cardConfig.getColored(baseKey + ".name"),
                cardConfig.getStrings(baseKey + ".lore", true),
                cardConfig.getMaterial(baseKey + ".material", Material.PAPER),
                cardConfig.getBoolean(baseKey + ".glow", false),
                cardConfig.getInt(baseKey + ".needed_to_win", 3),
                cardConfig.getDouble(baseKey + ".needed_scratch_percentage", 70),
                cardConfig.getDouble(baseKey + ".win_chance", 50),
                cardConfig.getDouble(baseKey + ".cursor.speed", 0.9d),
                null,
                iconsCoords,
                winAnimation,
                loseAnimation,
                isReload
        );

        cardData.setAllowLeftClick(
                cardConfig.getBoolean(baseKey + ".cursor.allow_left_click", false)
        );

        cardData.setCursorPressedSound(
                new SoundDataInterval(
                        cardConfig.getString(baseKey + ".cursor.pressed_sound.name"),
                        (float) cardConfig.getDouble(baseKey + ".cursor.pressed_sound.volume", 1),
                        (float) cardConfig.getDouble(baseKey + ".cursor.pressed_sound.pitch", 1),
                        cardConfig.getInt(baseKey + ".cursor.pressed_sound.interval_ticks", 20)
                )
        );


        cardData.permissions.setShowInListGuiPermission(cardConfig.getString(baseKey + ".permissions.show_in_list_gui", null));
        cardData.permissions.setUsePermission(cardConfig.getString(baseKey + ".permissions.use", null));

        cardData.setWin_backgroundDuration(
                cardConfig.getInt(baseKey + ".win_anim.static_background_duration", 0)
        );

        cardData.setLose_backgroundDuration(
                cardConfig.getInt(baseKey + ".lose_anim.static_background_duration", 0)
        );

        cardData.setEraserParticlesEnabled(
                cardConfig.getBoolean(baseKey + ".eraser.particles", true)
        );

        cardData.setCooldownTicks(
                cardConfig.getInt(baseKey + ".usage_cooldown_ticks", 0)
        );

        //TODO: dynamicize "scratch_card"
        loadActions(cardConfig, cardData.winActions, cardData.winActions_alwaysExecute, baseKey + ".win_actions");
        //TODO: dynamicize "scratch_card"
        loadActions(cardConfig, cardData.loseActions, cardData.loseActions_alwaysExecute, baseKey + ".lose_actions", false);

        //remove if any already registered with the same id
//        Iterator<ScratchCardData> iterator = cards.iterator();
//        while(iterator.hasNext())
//        {
//            if(iterator.next().getId().equals(scratchCardData.getId()))
//            {
//                iterator.remove();
//                break;
//            }
//        }
        CardData oldCardData = cardsById.get(cardData.getId());
        if (oldCardData != null)
        {
            cards.remove(oldCardData);
            cardsItemsCache.remove(oldCardData.getItemStack());
            cardsById.remove(oldCardData.getId());
            cardsFiles.remove(oldCardData.getConfigFile());
        }
        cards.add(cardData);
        cardsItemsCache.add(cardData.getItemStack());
        cardsById.put(cardData.getId(), cardData);
        cardsFiles.add(cardConfig);
    }

    private AnimationFrames loadAnimationFrames(ConfigFile cardConfig, String configEntryKey, String folderName, boolean isReload)
    {
        AnimationFrames animationFrames = new AnimationFrames(
                cardsImgCache,
                cardConfig.getInt(configEntryKey + ".shift.x", 0),
                cardConfig.getInt(configEntryKey + ".shift.y", 0)
        );

        if (cardConfig.hasKey(configEntryKey + ".frames"))
        {
            for (String name : cardConfig.getStrings(configEntryKey + ".frames"))
            {
                if (!name.startsWith(folderName))
                    name = folderName + File.separator + name;
                name = name.replace("\\", File.separator).replace("/", File.separator);

                if (name.contains("|x"))
                {
                    int repeatCount = Utils.parseInt(name.split("\\|x")[1], 1);
                    String fileName = name.split("\\|x")[0];

                    for (int i = 0; i < repeatCount; i++)
                    {
                        File frame = new File(cardConfig.getFile().getParent(), fileName + ".png");
                        if (!frame.exists())
                            continue;
                        animationFrames.addFrame(frame.getAbsolutePath(), isReload);
                    }
                }
                else
                {
                    File frame = new File(cardConfig.getFile().getParent(), name + ".png");
                    if (!frame.exists())
                        continue;
                    animationFrames.addFrame(frame.getAbsolutePath(), isReload);
                }
            }
//           //System.out.println(configEntryKey + " animation frames: " + animationFrames.getFramesCount());
        }
        return animationFrames;
    }

    public Collection<File> getCardsFiles()
    {
        //very bad, may find also other files in the same card folder, is it good?
        return FileUtils.listFiles(CARDS_DIR, new String[]{"yml"}, true);
    }

    public List<String> getCardsFoldersNames()
    {
        List<String> names = new ArrayList<>();
        for (File dir : CARDS_DIR.listFiles(File::isDirectory))
            names.add(dir.getName());
        return names;
    }

    @Nullable
    public CardData getCard(String id)
    {
        return cardsById.get(id);
    }

    private static void loadActions(ConfigFile cardConfig,
                                    WeightedRandom<ActionsGroup> actions,
                                    HashSet<ActionsGroup> actions_always,
                                    String sectionName)
    {
        loadActions(cardConfig, actions, actions_always, sectionName, true);
    }

    private static void loadActions(ConfigFile cardConfig,
                                    WeightedRandom<ActionsGroup> actions,
                                    HashSet<ActionsGroup> actions_always,
                                    String sectionName,
                                    boolean warnNoKey)
    {
        if (cardConfig.hasKey(sectionName))
        {
            for (MemorySection group : Utils.getMemorySections(cardConfig.getSection(sectionName)))
            {
                final ArrayList<IAction> prizes = new ArrayList<>();
                for (String actionKey : group.getKeys(false))
                {
                    ConfigurationSection actionSection = group.getConfigurationSection(actionKey);
                    if (actionSection == null) //not a section, probably a simple entry ("chance" probably)
                        continue;
                    IAction prize;
                    if (actionKey.startsWith("item"))
                    {
                        prize = new ActionGiveItem(
                                Mat.valueOf(actionSection.getString("name")).getItemStack(),
                                actionSection.getInt("amount", 1),
                                actionSection.getInt("damage", 0),
                                actionSection.getLong("delay", 0L)
                                //TODO: itemsadder
                        );
                    }
                    else if (actionKey.startsWith("command"))
                    {
                        prize = new ActionExecuteCommand(
                                actionSection.getString("command"),
                                actionSection.getBoolean("as_console", false),
                                actionSection.getLong("delay", 0L)
                        );
                    }
                    else if (actionKey.startsWith("play_sound") || actionKey.startsWith("playsound"))
                    {
                        prize = new ActionPlaySound(
                                actionSection.getString("name"),
                                (float) actionSection.getDouble("volume", 1),
                                (float) actionSection.getDouble("pitch", 1),
                                actionSection.getLong("delay", 0L)
                        );
                    }
                    else if (actionKey.startsWith("play_loop_sound") || actionKey.startsWith("playrloopsound"))
                    {
                        prize = new ActionPlaySoundLoop(
                                actionSection.getString("name"),
                                (float) actionSection.getDouble("volume", 1),
                                (float) actionSection.getDouble("pitch", 1),
                                actionSection.getLong("delay", 0L),
                                actionSection.getLong("interval", 5L),
                                actionSection.getInt("times", 1)
                        );
                    }
                    else if (actionKey.startsWith("send_message") || actionKey.startsWith("sendmessage"))
                    {
                        prize = new ActionSendMessage(
                                actionSection.getString("message"),
                                actionSection.getString("broadcast", "PLAYER"),
                                actionSection.getLong("delay", 0L)
                        );
                        //TODO: Allow searching message in lang file.
                        //TODO: Add a list of fields to be replaced, example:
                        // replace:
                        //   item_name: "Damaged Diamond Sword x1"
                    }
                    else
                    {
                        Msg.get().error(
                                String.format("&6Error! Unknown action &e%s &6for &e%s &6: &e%s", actionKey, sectionName, cardConfig.getPartialFilePath())
                        );
                        continue;
                    }
                    prizes.add(prize);
                }

                ActionsGroup actionsGroup = new ActionsGroup(prizes, group.getBoolean("execute_instantly", false));
                if (group.getBoolean("always_execute", false))
                {
                    actions_always.add(actionsGroup);
                }
                else
                {
                    double chance = group.getDouble("chance", 50d);
                    actions.set(actionsGroup, chance);
                }
            }
        }
        else
        {
            if (warnNoKey)
                Msg.get().error(
                        String.format("Error! Please set %s for the card &e%s", sectionName, cardConfig.getPartialFilePath())
                );
        }
    }

    public void createNewCard(CommandSender commandSender, String cardName) throws IOException
    {
        File file = new File(Main.inst.getDataFolder(), "cards" + File.separator + cardName + File.separator + cardName + ".yml");
        if (file.exists())
        {
            commandSender.sendMessage(Main.lang.getLocalized("card_already_exists").replace("{name}", cardName));
            return;
        }
        Msg.sendLocalizedMessage(commandSender, "extracting_card_template");
        TemplateCardExtractor extractor = new TemplateCardExtractor(Main.inst, Main.inst.getFile().getAbsolutePath(), cardName.toString());
        extractor.extract();

        ConfigFile config = Utils.config(file);
        config.set("id", cardName);
        config.set("name", "&6" + cardName);
        config.set("permissions.show_in_list_gui", "new_card." + cardName);
        config.set("permissions.use", "new_card." + cardName);
        config.save();
        Main.inst.cardsStorage.loadCard(config, false);

        commandSender.sendMessage(Main.lang.getLocalized("created_card").replace("{name}", cardName));
    }

    public static String uploadFile(File textFile) throws IOException
    {
        URL url = new URL("http://www.matteodev.it/spigot/paste/upload");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("id", BaseEncoding.base16().lowerCase().encode(DigestUtils.sha1(System.currentTimeMillis() + textFile.getAbsolutePath())));
        params.put("content", FileUtils.readFileToString(textFile, StandardCharsets.UTF_8));

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet())
        {
            if (!postData.isEmpty()) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), StandardCharsets.UTF_8));
        }
        byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        for (int c; (c = in.read()) >= 0; )
           System.out.print((char) c);

        return params.get("id");
    }

    public void getFileFromWeb(String fileId, String cardId, CommandSender sender)
    {
        CardData cardData = getCard(cardId);

        try
        {
            URL url = new URL("http://www.matteodev.it/spigot/paste/get?id=" + fileId);

            URLConnection conn = url.openConnection();
            conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (X11; U; Linux x86_64; en-GB; rv:1.8.1.6) Gecko/20070723 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1)"
            );

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            String collect = IOUtils.toString(in);
//            String collect = in.lines().collect(Collectors.joining());

            if(collect == null || collect.isEmpty() || collect.equals(" "))
            {
                Msg.get().send(sender, Main.lang.getLocalized("changes_apply_error"));
                Msg.get().send(sender, ChatColor.RED + "Error getting file from web editor, response is empty!");
            }
            else
            {
                FileUtils.writeStringToFile(
                        cardData.getConfigFile().getFile(),
                        collect,
                        false
                );
                Main.inst.cardsStorage.reloadCard(cardData);

                Msg.get().send(sender, Main.lang.getLocalized("changes_applied"));
            }

        } catch (Exception e)
        {
            Msg.get().send(sender, Main.lang.getLocalized("changes_apply_error"));
            Msg.get().send(sender, ChatColor.RED + "Error getting file from web editor: " + e.getMessage());

            Msg.get().error(ChatColor.RED + "Error getting file from web editor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void createNewWebEditor(String cardId, CommandSender sender)
    {
        CardData card = Main.inst.cardsStorage.getCard(cardId);
        String fileId = null;
        String editorUrl = "";
        try
        {
            editorUrl = "https://ide.devs.beer/scratchit/?file_name=" + card.getConfigFile().getFile().getName();
//            editorUrl = "http://localhost:8080/?file_name=" + card.getConfigFile().getFile().getName();

            fileId = uploadFile(card.getConfigFile().getFile());
        } catch (IOException ioException)
        {
            Msg.get().send(sender, ChatColor.RED + "Error uploading the file to the editor: " + ioException.getMessage());
            Msg.get().log(ChatColor.RED + "Error uploading the file to the editor: " + ioException.getMessage(), Level.SEVERE);

        }

        if (fileId != null)
        {
            editorUrl = editorUrl + "&id=" + fileId;
            editorUrl = editorUrl + "&card_id=" + cardId;
        }

        sender.sendMessage(" ");

        TextComponent message = new TextComponent(Main.lang.getLocalized("click_here_open_editor"));
        message.setColor(ChatColor.AQUA);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, editorUrl));
        if(MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_15_R1)) // Not tested, maybe they added it before!
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Main.lang.getLocalized("click_here_open_editor")).create()));
        sender.spigot().sendMessage(message);
        sender.sendMessage(" ");
    }
}
