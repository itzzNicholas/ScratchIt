package dev.lone.scratchit.commands;

import dev.lone.LoneLibs.config.ConfigFile;
import dev.lone.scratchit.Main;
import dev.lone.scratchit.api.events.ScratchCardObtainedEvent;
import dev.lone.scratchit.card.CardData;
import dev.lone.scratchit.gui.CardsListViewer;
import dev.lone.scratchit.util.*;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter
{
    public void register(JavaPlugin plugin)
    {
        plugin.getCommand("scratchit").setExecutor(this);
        plugin.getCommand("scratchit").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (Main.inst.cardsStorage == null || Main.inst.cardsStorage.isLoading())
        {
            sender.sendMessage(Main.lang.getLocalized("items_still_loading_on_commands"));
            return true;
        }

        boolean isPlayer = sender instanceof Player;

        if (args.length >= 1)
        {
            switch (args[0])
            {
                case "get":
                {
                    if (!checkPermission(sender, "scratchit.admin.get"))
                        return true;
                    if (!isPlayer)
                        return true;

                    Player player = (Player) sender;
                    CardData cardData = Main.inst.cardsStorage.getCard(args[1]);
                    if (cardData == null)
                    {
                        sender.sendMessage("Item not found");
                        return true;
                    }

                    ItemStack cardItem = cardData.getItemStackClone();
                    if(EventsUtil.call(new ScratchCardObtainedEvent(player, cardItem, cardData.getId())))
                        InvUtil.giveItem(player, cardItem);
                    return true;
                }
                case "give":
                {
                    if (!checkPermission(sender, "scratchit.admin.give"))
                        return true;

                    String playerToGiveName = getParam(1, args, null);
                    if (playerToGiveName == null)
                    {
                        sender.sendMessage("Specify a player to give the item");
                        return true;
                    }

                    Player pToGive = Bukkit.getPlayer(playerToGiveName);
                    if (pToGive == null)
                    {
                        sender.sendMessage("Player is offline");
                        return true;
                    }

                    CardData cardData = Main.inst.cardsStorage.getCard(args[2]);
                    if (cardData == null)
                    {
                        sender.sendMessage("Item not found");
                        return true;
                    }
                    ItemStack cardItem = cardData.getItemStackClone();
                    if(EventsUtil.call(new ScratchCardObtainedEvent(pToGive, cardItem, cardData.getId())))
                        InvUtil.giveItem(pToGive, cardItem);
                    return true;
                }
                case "list":
                    if (!checkPermission(sender, "scratchit.user.list"))
                        return true;
                    if (isPlayer)
                    {
                        Player player = (Player) sender;
                        new CardsListViewer(player, Main.inst.cardsStorage).show();
                    }
                    else
                    {
                        sender.sendMessage("This command can be run only by players!");
                    }
                    break;
                case "config":
                    if (getParam(1, args) != null)
                    {
                        switch (getParam(1, args))
                        {
                            case "AutoListAnimationFiles":
                            {
                                if (!checkPermission(sender, "scratchit.admin.config.AutoListAnimationFiles"))
                                    return true;
                                String cardName = getParam(2, args);
                                if (cardName == null)
                                {
                                    sender.sendMessage(Main.lang.getLocalized("specify_valid_card_name"));
                                    return true;
                                }

                                String animType = getParam(3, args);
                                if (animType == null || (!animType.equals("win_anim") && !animType.equals("lose_anim")))
                                {
                                    sender.sendMessage(Main.lang.getLocalized("specify_anim_type"));
                                    return true;
                                }

                                List<String> imagesPath = new ArrayList<>();

                                ConfigFile cardConfig = Main.inst.cardsStorage.getCard(cardName).getConfigFile();
                                File animFolder = new File(cardConfig.getFile().getParentFile(), animType);
                                if (!animFolder.exists())
                                {
                                    sender.sendMessage(Main.lang.getLocalized("anim_folder_doesnt_exists").replace("{name}", animType));
                                    return true;
                                }

                                List<File> files = new ArrayList<>(FileUtils.listFiles(animFolder, new String[]{"png"}, true));
                                files.sort((f1, f2) -> {
                                    try
                                    {
                                        int i1 = Integer.parseInt(cleanupFileName(f1.getName()));
                                        int i2 = Integer.parseInt(cleanupFileName(f2.getName()));
                                        return i1 - i2;
                                    }
                                    catch (NumberFormatException e)
                                    {
                                        //throw new AssertionError(e);
                                        return 0;
                                    }
                                });

                                files.forEach(file -> {
                                    imagesPath.add(file.getName().replace(".png", ""));
                                });

                                //TODO: dynamicize "scratch_card"
                                cardConfig.set("scratch_card." + animType + ".frames", imagesPath);
                                cardConfig.save();
                                Main.inst.cardsStorage.loadCard(cardConfig, true);

                                sender.sendMessage("Done! Found " + imagesPath.size() + " animation frames.");
                                break;
                            }
                            case "reload":
                            {
                                if (!checkPermission(sender, "scratchit.admin.config.reload"))
                                    return true;

                                Main.inst.mapsRenderingContainer.stopAllPlayer();
                                String cardName = getParam(2, args);
                                if (cardName == null || cardName.equals("all"))
                                {
                                    Msg.sendLocalizedMessage(sender, "reloading");
                                    Main.inst.initialize();
                                    Msg.sendLocalizedMessage(sender, "reloaded");
                                }
                                else
                                {
                                    Msg.sendLocalizedMessage(sender, "reloading");
                                    ConfigFile cardConfig = Main.inst.cardsStorage.getCard(cardName).getConfigFile();
                                    Main.inst.cardsStorage.reloadCard(cardConfig);
                                    Msg.sendLocalizedMessage(sender, "reloaded");
                                }
                                break;
                            }
                            case "create":
                            {
                                if (!checkPermission(sender, "scratchit.admin.config.create"))
                                    return true;
                                String cardName = getParam(2, args);
                                if (cardName == null || cardName.equals("<cardname>"))
                                {
                                    sender.sendMessage(Main.lang.getLocalized("wrong_command_usage"));
                                    break;
                                }
                                try
                                {
                                    Main.inst.cardsStorage.createNewCard(sender, cardName);
                                }
                                catch (IOException e)
                                {
                                    sender.sendMessage("Error while creating the card. Check console for more info. " + e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case "editconfig":
                            {
                                if (!checkPermission(sender, "scratchit.admin.config.create"))
                                    return true;
                                String cardName = getParam(2, args);
                                CardData card = Main.inst.cardsStorage.getCard(cardName);
                                if (card == null)
                                {
                                    sender.sendMessage(Main.lang.getLocalized("wrong_command_usage"));
                                    break;
                                }
                                Main.inst.cardsStorage.createNewWebEditor(card.getId(), sender);
                                break;
                            }
                            case "applyconfig":
                            {
                                if (!checkPermission(sender, "scratchit.admin.config.create"))
                                    return true;
                                String fileId = getParam(2, args);
                                String cardId = getParam(3, args);

                                if (fileId == null || cardId == null)
                                {
                                    sender.sendMessage(Main.lang.getLocalized("wrong_command_usage"));
                                    break;
                                }

                                Main.inst.cardsStorage.getFileFromWeb(fileId, cardId, sender);
                                break;
                            }
                        }
                    }
                    else
                    {
                        sender.sendMessage(Main.lang.getLocalized("wrong_command_usage"));
                    }
                    break;
            }
        }
        else
        {
            sender.sendMessage(Main.lang.getLocalized("wrong_command_usage"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args)
    {
        if (args.length == 1)
        {
            return Arrays.asList("config", "list", "get", "give");
        }

        if(Main.inst.cardsStorage == null)
            return Collections.singletonList("");

        if (args.length == 2)
        {
            switch (args[0])
            {
                case "config":
                    return Arrays.asList("reload", "create", "editconfig", "applyconfig", "AutoListAnimationFiles");
                case "get":
                {
                    return new ArrayList<>(Main.inst.cardsStorage.cardsById.keySet());
                }
                case "give":
                {
                    List<String> names = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers())
                        names.add(p.getName());
                    return names;
                }
            }
        }

        if (args.length >= 2)
        {
            switch (getParam(1, args, ""))
            {
                case "AutoListAnimationFiles":
                    if (args.length == 3)
                    {
                        return Main.inst.cardsStorage.getCardsFoldersNames();
                    }
                    else if (args.length == 4)
                    {
                        return Arrays.asList("<animation type>", "win_anim", "lose_anim");
                    }
                    break;
                case "reload":
                    ArrayList<String> tmp = new ArrayList<>(Main.inst.cardsStorage.cardsById.keySet());
                    tmp.add("all");
                    return tmp;
                case "create":
                    return Collections.singletonList("<cardname>");
                case "editconfig":
                    return new ArrayList<>(Main.inst.cardsStorage.cardsById.keySet());
                case "applyconfig":
                    return Collections.singletonList("");
            }

            if (getParam(0, args, "").equals("give"))
            {
                return new ArrayList<>(Main.inst.cardsStorage.cardsById.keySet());
            }
        }

        return Collections.singletonList("");
    }

    private String getParam(int index, String[] args, String def)
    {
        String res = getParam(index, args);
        if (res == null)
            return def;
        return res;
    }

    @Nullable
    private String getParam(int index, String[] args)
    {
        if (index > args.length - 1)
            return null;
        return args[index];
    }

    private String cleanupFileName(String name)
    {
        return name
                .replace(".png", "")
                .replace("anim", "")
                .replace("_", "")
                .replace(" ", "")
                .replace("(", "")
                .replace(")", "")
                ;
    }

    public static boolean checkPermission(CommandSender commandSender, String permission)
    {
        if (!commandSender.hasPermission(permission))
        {
            Msg.get().send(commandSender, Main.lang.getLocalized("no_permission").replace("{permission}", permission));
            return false;
        }
        return true;
    }
}
