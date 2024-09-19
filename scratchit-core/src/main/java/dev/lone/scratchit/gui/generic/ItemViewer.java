package dev.lone.scratchit.gui.generic;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ItemViewer extends AbstractGUI
{
    protected Player owner;
    protected GUISettings settings;
    protected final ItemStack itemStack;

    protected final FastInv fastInv;

    public ItemViewer(Player owner, GUISettings settings, ItemStack itemStack)
    {
        this.owner = owner;
        this.settings = settings;
        this.itemStack = itemStack;

        this.fastInv = new FastInv(6 * 9, settings.title);
    }

    public void show()
    {
        fastInv.open(owner);
    }

    protected abstract void drawGui(FastInv fastInv);
}
