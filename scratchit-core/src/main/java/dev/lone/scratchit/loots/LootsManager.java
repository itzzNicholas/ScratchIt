package dev.lone.scratchit.loots;

import dev.lone.LoneLibs.Mat;
import dev.lone.scratchit.loots.data.*;
import dev.lone.scratchit.Main;
import dev.lone.scratchit.card.CardData;
import dev.lone.LoneLibs.config.ConfigFile;
import dev.lone.scratchit.util.Msg;
import dev.lone.scratchit.util.WorldUtil;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class LootsManager
{
    final Plugin plugin;

    public HashMap<Material, List<VanillaBlockLootEntry>> blockLootEntries;
    public HashMap<String, List<CustomBlockLootEntry>> customBlockLootEntries;
    public List<FishingLootEntry> fishingLootEntries;
    public HashMap<EntityType, List<MobLootEntry>> mobLootEntries;

    public LootsEvents lootsEvents;

    public LootsManager(Plugin plugin)
    {
        this.plugin = plugin;

        this.lootsEvents = new LootsEvents();
        this.lootsEvents.registerEvents();
    }

    public void loadLootsFromConfigs(Collection<ConfigFile> configFiles)
    {
        this.blockLootEntries = new HashMap<>();
        this.customBlockLootEntries = new HashMap<>();
        this.fishingLootEntries = new ArrayList<>();
        this.mobLootEntries = new HashMap<>();

        for (ConfigFile configFile : configFiles)
            loadLootsFromConfig(configFile);
    }

    // TODO: refactor this shit.
    public void loadLootsFromConfig(ConfigFile configFile)
    {
        if (!configFile.hasKey("loots"))
        {
            if (Main.config.getBoolean("debug.items.log-loots-loading"))
                Msg.get().log(ChatColor.YELLOW + "    No loots found in file " + configFile.getFilePath());
            return;
        }

        for (String lootTypeKey : configFile.getSectionKeys("loots"))//itero i tipi di loot
        {
            String lootTypePath = "loots." + lootTypeKey;
            switch (lootTypeKey)
            {
                case "mobs":
                    for (String lootEntryKey : configFile.getSectionKeys(lootTypePath))
                    {
                        String lootEntryPath = lootTypePath + "." + lootEntryKey;

                        if (!configFile.getBoolean(lootEntryPath + ".enabled", true))
                        {
                            if (Main.config.getBoolean("debug.items.log-loots-loading-disabled"))
                                Msg.get().log("    Ignored disabled loot: " + lootEntryKey);
                            continue;
                        }

                        EntityType entityType = EntityType.valueOf(configFile.getString(lootEntryPath + ".type"));
                        MobLootEntry entry = new MobLootEntry(
                                entityType,
                                configFile.getBoolean(lootEntryPath + ".ignore_spawner", true)
                        );
                        //load rules
                        if (configFile.hasKey(lootEntryPath + ".nbt"))
                        {
                            for (String resultKey : configFile.getSectionKeys(lootEntryPath + ".nbt"))
                            {
                                String nbtRulePath = lootEntryPath + ".nbt." + resultKey;
                                NBTMatchRule nbtMatchRule = new NBTMatchRule(
                                        configFile.getString(nbtRulePath + ".path"),
                                        configFile.getString(nbtRulePath + ".value"),
                                        configFile.getString(nbtRulePath + ".type")
                                );
                                entry.addNBTMatchRule(nbtMatchRule);
                            }
                        }
                        if (configFile.hasKey(lootEntryPath + ".metadata"))
                        {
                            for (String resultKey : configFile.getSectionKeys(lootEntryPath + ".metadata"))
                            {
                                String metadataRulePath = lootEntryPath + ".metadata." + resultKey;
                                MetadataMatchRule metadataMatchRule = new MetadataMatchRule(
                                        configFile.getString(metadataRulePath + ".name"),
                                        configFile.getString(metadataRulePath + ".value"),
                                        configFile.getString(metadataRulePath + ".type", "string")
                                );
                                entry.addMetadataMatchRule(metadataMatchRule);
                            }
                        }
                        addOtherPropertiesToLootEntry(entry, configFile, lootEntryPath);
                        mobLootEntries.computeIfAbsent(entityType, k -> new ArrayList<>()).add(entry);
                    }
                    break;
                case "blocks":
                    for (String lootEntryKey : configFile.getSectionKeys(lootTypePath))
                    {
                        String lootEntryPath = lootTypePath + "." + lootEntryKey;

                        if (!configFile.getBoolean(lootEntryPath + ".enabled", true))
                        {
                            if (Main.config.getBoolean("debug.items.log-loots-loading-disabled"))
                                Msg.get().log("    Ignored disabled loot: " + lootEntryKey);
                            continue;
                        }

                        String blockType = configFile.getString(lootEntryPath + ".type");
                        Mat mat = Mat.valueOf(blockType);
                        if (mat != null) //is vanilla block
                        {
                            VanillaBlockLootEntry entry = new VanillaBlockLootEntry(
                                    Mat.valueOf(blockType).getMaterial()
                            );

                            //load rules
                            if (configFile.hasKey(lootEntryPath + ".nbt"))
                            {
                                Msg.get().log(ChatColor.RED +
                                                               "Ignored NBT rule of loot for block: " + ChatColor.AQUA + lootEntryKey +
                                                               ChatColor.RED + " in file " + ChatColor.AQUA + configFile.getPartialFilePath() +
                                                               ChatColor.RED + " (" + ChatColor.AQUA + entry.getMaterial() + ChatColor.RED + " NBT loots are not currently supported by this plugin!)"
                                );
                            }
                            addOtherPropertiesToLootEntry(entry, configFile, lootEntryPath);
                            blockLootEntries.computeIfAbsent(Mat.valueOf(blockType).getMaterial(), k -> new ArrayList<>()).add(entry);

                            if (Main.config.getBoolean("debug.items.log-loots-loading"))
                            {
                                Msg.get().log(ChatColor.AQUA + "Registered block loot: " + entry.getMaterial());
                            }
                        }
                        else
                        {
                            if(Main.hasItemsAdder)
                            {
                                CustomStack original = CustomStack.getInstance(blockType);
                                if (original == null)
                                {
                                    Msg.get().log(ChatColor.YELLOW + "Ignoring loot: block '" + blockType + "' " +
                                                                   "not found for loot '" + lootEntryKey + "' in file '" + configFile.getFilePath() + "'");
                                    continue;
                                }

                                if (original.isBlock())
                                {
                                    CustomBlockLootEntry entry = new CustomBlockLootEntry(
                                            CustomBlock.getInstance(blockType)
                                    );
                                    addOtherPropertiesToLootEntry(entry, configFile, lootEntryPath);
                                    customBlockLootEntries.computeIfAbsent(original.getNamespacedID(), k -> new ArrayList<>()).add(entry);

                                    if (Main.config.getBoolean("debug.items.log-loots-loading"))
                                        Msg.get().log(ChatColor.AQUA + "Registered block loot: " + entry.getBlockNamespacedId());
                                }
                                else
                                {
                                    Msg.get().error("Error: You have to set a valid BLOCK as the 'type' of block loot '" + lootEntryKey + "'."
                                                                 + " You set " + blockType + "' which is NOT a block."
                                                                 + " File: " + configFile.getFilePath());
                                }
                            }
                            else
                            {
                                Msg.get().error("Error: You have to set a valid BLOCK as the 'type' of block loot '" + lootEntryKey + "'."
                                                             + " You set " + blockType + "' which is NOT a block."
                                                             + " File: " + configFile.getFilePath());
                            }
                        }
                    }
                    break;
                case "fishing":
                    for (String lootEntryKey : configFile.getSectionKeys(lootTypePath))
                    {
                        String lootEntryPath = lootTypePath + "." + lootEntryKey;

                        if (!configFile.getBoolean(lootEntryPath + ".enabled", true))
                        {
                            if (Main.config.getBoolean("debug.items.log-loots-loading-disabled"))
                                Msg.get().log("    Ignored disabled loot: " + lootEntryKey);
                            continue;
                        }

                        FishingLootEntry entry = new FishingLootEntry();
                        addOtherPropertiesToLootEntry(entry, configFile, lootEntryPath);
                        fishingLootEntries.add(entry);
                    }
                    break;
            }
        }
    }

    /**
     * Adds results, exp, biomes and dropOnlyFirst to the {@link LootEntry}
     */
    private void addOtherPropertiesToLootEntry(LootEntry entry, ConfigFile configFile, String path)
    {
        for (String resultKey : configFile.getSectionKeys(path + ".items"))
        {
            String resultPath = path + ".items." + resultKey;

            ItemStack itemStack = null;
            String itemName = configFile.getString(resultPath + ".item", null);
            CardData card = Main.inst.cardsStorage.getCard(itemName);
            if(card != null)
            {
                itemStack = card.getItemStackClone();
            }
            else
            {
                if (Main.hasItemsAdder && CustomStack.getInstance(itemName) != null)
                {
                    itemStack = CustomStack.getInstance(itemName).getItemStack();
                }
                else
                {
                    try
                    {
                        itemStack = Mat.valueOf(itemName).getItemStack();
                    } catch (Exception ex)
                    {
                        Msg.get().log(ChatColor.RED + ex.getMessage());
                    }
                }
                if (itemStack == null)
                {
                    Msg.get().log(ChatColor.YELLOW + "Ignoring loot: item '" + configFile.getString(resultPath + ".item") + "' " +
                                                   "not found for loot '" + path + "' in file '" + configFile.getFilePath() + "'");
                    continue;
                }
            }
            ResultItem resultItem = new ResultItem(
                    itemStack,
                    configFile.getInt(resultPath + ".min_amount", 1),
                    configFile.getInt(resultPath + ".max_amount", 1),
                    configFile.getFloat(resultPath + ".chance", 100),
                    configFile.getBoolean(resultPath + ".ignore_fortune")
            );

            entry.addResultItem(resultItem);
        }

        if (configFile.hasKey(path + ".exp"))
        {
            for (String resultKey : configFile.getSectionKeys(path + ".exp"))
            {
                String resultPath = path + ".exp." + resultKey;

                ResultExpDrop resultExpDrop = new ResultExpDrop(
                        configFile.getInt(resultPath + ".min_amount"),
                        configFile.getInt(resultPath + ".max_amount"),
                        configFile.getFloat(resultPath + ".chance")
                );

                entry.addResultExpDrop(
                        resultExpDrop
                );
            }
        }

        if (configFile.hasKey(path + "." + "biomes"))
        {
            for (String biomeStr : configFile.getStrings(path + "." + "biomes"))
            {
                if (WorldUtil.isVanillaBiome(biomeStr.toUpperCase()))
                    entry.addBiome(Biome.valueOf(biomeStr.toUpperCase()));
            }
        }

        entry.setDropOnlyFirst(configFile.getBoolean(path + "." + "drop_only_first", false));
    }
}
