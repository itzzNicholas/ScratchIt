package dev.lone.scratchit.gui.generic;

import org.bukkit.inventory.ItemStack;

public class GUISettings
{
    public String title;
    public ItemStack getItemIcon;
    public ItemStack backIcon;
    public ItemStack prevIcon;
    public ItemStack nextIcon;
    public ItemStack createItemIcon;
    public ItemStack openOnlineEditorIcon;

    public ItemStack backgroundIcon;

    public GUISettings(String title,
                       ItemStack getItemIcon,
                       ItemStack backIcon,
                       ItemStack prevIcon,
                       ItemStack nextIcon,
                       ItemStack createItemIcon,
                       ItemStack openOnlineEditorIcon,
                       ItemStack backgroundIcon)
    {
        this.title = title;
        this.getItemIcon = getItemIcon;
        this.backIcon = backIcon;
        this.prevIcon = prevIcon;
        this.nextIcon = nextIcon;
        this.createItemIcon = createItemIcon;
        this.openOnlineEditorIcon = openOnlineEditorIcon;

        this.backgroundIcon = backgroundIcon;
    }

    public GUISettings setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public GUISettings setGetItemIcon(ItemStack getItemIcon)
    {
        this.getItemIcon = getItemIcon;
        return this;
    }

    public GUISettings setBackIcon(ItemStack backIcon)
    {
        this.backIcon = backIcon;
        return this;
    }

    public GUISettings setPrevIcon(ItemStack prevIcon)
    {
        this.prevIcon = prevIcon;
        return this;
    }

    public GUISettings setNextIcon(ItemStack nextIcon)
    {
        this.nextIcon = nextIcon;
        return this;
    }

    public GUISettings setBackgroundIcon(ItemStack backgroundIcon)
    {
        this.backgroundIcon = backgroundIcon;
        return this;
    }
}
