package dev.lone.scratchit.card;

import dev.lone.LoneLibs.config.ConfigFile;
import dev.lone.LoneLibs.nbt.nbtapi.NBTItem;
import dev.lone.scratchit.MapsRenderingContainer;
import dev.lone.scratchit.map.AnimationFrames;
import dev.lone.scratchit.map.card.CardRenderer;
import dev.lone.scratchit.map.image.ImageBytes;
import dev.lone.scratchit.map.image.ImgCache;
import dev.lone.scratchit.map.image.ImgData;
import dev.lone.scratchit.map.image.PaletteType;
import dev.lone.scratchit.libs.WeightedRandom;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.List;

public class CardData
{
    private final ImgCache imgCache;

    @Getter
    private final String id;
    @Getter
    private final ConfigFile configFile;
    private final String configPath;

    public Permissions permissions;

    private ItemStack cachedItemStack;

    private final String displayName;
    @Nullable
    private final List<String> lore;
    @Nullable
    private final Material material;
    private final boolean glow;
    @Getter
    private final int neededToWin;
    @Getter
    private final double neededScratchPercentage;
    @Getter
    private final double winChance;
    @Getter
    private final double coinSpeed;

    @Setter
    private SoundDataInterval cursorPressedSound;

    @Nullable
    private final String itemsAdderItemId;

    @Getter
    private final List<Vector> iconsCoords;
    @Getter
    private final AnimationFrames winAnimation;
    @Getter
    private final AnimationFrames loseAnimation;

    @Setter
    public boolean eraserParticlesEnabled;

    public WeightedRandom<ActionsGroup> winActions;
    public HashSet<ActionsGroup> winActions_alwaysExecute;
    public WeightedRandom<ActionsGroup> loseActions;
    public HashSet<ActionsGroup> loseActions_alwaysExecute;

    @Setter
    public long win_backgroundDuration = 0;
    @Setter
    public long lose_backgroundDuration = 0;
    @Setter
    public boolean allowLeftClick;

    public long cooldownMs;

    //TODO:
    // win: particle
    // lose: particle

    public CardData(ImgCache imgCache,
                    ConfigFile configFile,
                    String configPath,
                    String id,
                    String displayName,
                    @Nullable List<String> lore,
                    @Nullable Material material,
                    boolean glow,
                    int neededToWin,
                    double neededScratchPercentage,
                    double winChance,
                    double coinSpeed,
                    @Nullable String itemsAdderItemId,
                    List<Vector> iconsCoords,
                    AnimationFrames winAnimation,
                    AnimationFrames loseAnimation,
                    boolean isReload)
    {
        this.imgCache = imgCache;
        this.configFile = configFile;
        this.configPath = configPath;
        this.id = id;
        this.displayName = displayName;
        this.lore = lore;
        this.material = material;
        this.glow = glow;
        this.neededToWin = neededToWin;
        this.neededScratchPercentage = neededScratchPercentage;
        this.winChance = winChance;
        this.coinSpeed = coinSpeed;
        this.itemsAdderItemId = itemsAdderItemId;
        this.iconsCoords = iconsCoords;

        this.winAnimation = winAnimation;
        this.loseAnimation = loseAnimation;

        this.winActions = new WeightedRandom<>();
        this.winActions_alwaysExecute = new HashSet<>();
        this.loseActions = new WeightedRandom<>();
        this.loseActions_alwaysExecute = new HashSet<>();

        this.permissions = new Permissions();

        //shit for caching
        getOverlay(isReload);
        getCoin(isReload);
        getPressedCoin(isReload);
        getRight(isReload);
        getWrong(isReload);
        getEraser(isReload);
        getImgData("background", isReload);
        getImgData("background_win", isReload);
        getImgData("background_lose", isReload);
    }

    public static String getId(ItemStack item)
    {
        return new NBTItem(item).getCompound("scratchit").getString("id");
    }

    @Nullable
    public SoundDataInterval getCursorPressedSound()
    {
        return cursorPressedSound;
    }

    public boolean hasCursorPressedSound()
    {
        return cursorPressedSound != null;
    }

    public void setCooldownTicks(int cooldownTicks)
    {
        this.cooldownMs = cooldownTicks * 50L;
    }

    public ItemStack getItemStackClone()
    {
        return getItemStack().clone();
    }

    public ItemStack getItemStack()
    {
        if (cachedItemStack != null)
            return cachedItemStack;

        if (material != null)
            cachedItemStack = new ItemStack(material);

        //TODO: itemsadder

        ItemMeta meta = cachedItemStack.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);

        if(glow)
        {
            //TODO: use modern API to add glow effect.
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.values()[0], 1, true);
        }

        cachedItemStack.setItemMeta(meta);

        NBTItem nbt = new NBTItem(cachedItemStack);
        nbt.addCompound("scratchit").setString("id", id);
        cachedItemStack = nbt.getItem();

        return cachedItemStack;
    }

    public static boolean isScratchCard(ItemStack item)
    {
        if (item == null || item.getType() == Material.AIR)
            return false;
        if(!item.hasItemMeta())
            return false;
        return new NBTItem(item).hasKey("scratchit");
    }

    public boolean hasWon()
    {
        return (Math.random() * 100 < winChance);
    }

    public CardRenderer getNewRenderer(Player player, MapsRenderingContainer mapsRenderingContainer)
    {
        return new CardRenderer(player, this, mapsRenderingContainer);
    }

    @Nullable
    public ImgData getImgData(String name, boolean isReload)
    {
        String pngPath = configPath + File.separator + name + ".png";

        if(isReload)
            imgCache.checkForFileChanges(pngPath);

        if (imgCache.has(pngPath))
            return imgCache.get(pngPath);


        ImgData imgData = new ImgData(
                pngPath,
                ImageBytes.imageToByteMatrix(PaletteType.MC1_8, pngPath),
                ImageBytes.imageToByteMatrix(PaletteType.MC1_12, pngPath),
                ImageBytes.imageToByteMatrix(PaletteType.MC1_16, pngPath)
        );
        imgCache.save(pngPath, imgData);
        return imgData;
    }

    public ImgData getOverlay(boolean isReload)
    {
        return getImgData("overlay", isReload);
    }

    public ImgData getCoin(boolean isReload)
    {
        return getImgData("cursor", isReload);
    }

    public ImgData getPressedCoin(boolean isReload)
    {
        return getImgData("pressed_cursor", isReload);
    }

    @Nullable
    public ImgData getRight(boolean isReload)
    {
        return getImgData("right", isReload);
    }

    @Nullable
    public ImgData getWrong(boolean isReload)
    {
        return getImgData("wrong", isReload);
    }

    public ImgData getEraser(boolean isReload)
    {
        return getImgData("eraser", isReload);
    }
}


